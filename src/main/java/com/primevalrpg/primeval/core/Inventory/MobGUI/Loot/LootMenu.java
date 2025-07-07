package com.primevalrpg.primeval.core.Inventory.MobGUI.Loot;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Data.CustomItemHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LootMenu extends BaseMenu {

    private static final String TITLE = ColourCode.colour("&3&lSelect Loot Drops");
    private static final int SIZE = 54;  // 6 rows
    private static final int PER_PAGE = 45;  // slots 0–44

    private final CreationSession session;
    private int page = 0;
    private LootFilter filter = LootFilter.ALL;
    private boolean viewSelectedOnly = false;
    private boolean viewCustom = false;

    private final List<Material> allLoot = List.of(Material.values()).stream()
            .filter(m -> m.isItem() && m != Material.AIR)
            .sorted(Comparator.comparing(Enum::name))
            .collect(Collectors.toList());


    private List<ItemStack> displayStacks = new ArrayList<>();

    public LootMenu(Player p) {
        super(SIZE, TITLE);
        this.session = CreationManager.get(p.getUniqueId());
        render();
    }

    private void render() {
        fillBackground(inventory);

        List<ItemStack> allDisplayed;
        if (viewCustom) {
            allDisplayed = new ArrayList<>(CustomItemHandler.getAllItems().values());
        } else if (viewSelectedOnly) {
            allDisplayed = session.getLootDrops().stream()
                    .map(ItemDrop::getItem)
                    .collect(Collectors.toList());
        } else {
            allDisplayed = allLoot.stream()
                    .filter(filter.getPredicate())
                    .map(mat -> new ItemStack(mat, 1))
                    .collect(Collectors.toList());
        }

        int start = page * PER_PAGE;
        int end   = Math.min(start + PER_PAGE, allDisplayed.size());

        displayStacks.clear();
        displayStacks.addAll(allDisplayed.subList(start, end));

        for (int slot = 0; slot < PER_PAGE; slot++) {
            if (slot < displayStacks.size()) {
                ItemStack stack = displayStacks.get(slot);
                Material m = stack.getType();
                boolean selected = session.getLootDrops().stream()
                        .anyMatch(d -> d.getItem().getType() == m);

                ItemStack icon = ItemBuilder.CreateCustomItem(
                        m,
                        selected,
                        "§a" + m.name(),
                        selected ? "§7(current)" : "§7Click to toggle"
                );
                inventory.setItem(slot, icon);
            } else {
                inventory.setItem(slot, null);
            }
        }

        if (page > 0) {
            inventory.setItem(45, arrow("&a« Prev", "&7Page " + page));
        }
        if (end < allDisplayed.size()) {
            inventory.setItem(53, arrow("&aNext »", "&7Page " + (page + 2)));
        }

        if (!viewSelectedOnly && !viewCustom) {
            inventory.setItem(46,
                    ItemBuilder.CreateCustomItem(
                            Material.PAPER, false,
                            ColourCode.colour("&eFilter: " + filter.name()),
                            ColourCode.colour("&7Click to cycle")
                    )
            );
        }

        inventory.setItem(47,
                ItemBuilder.CreateCustomItem(
                        Material.CHEST, false,
                        ColourCode.colour("&eView: " + (viewSelectedOnly ? "Selected" : "All")),
                        ColourCode.colour("&7Click to toggle")
                )
        );

        inventory.setItem(48,
                ItemBuilder.CreateCustomItem(
                        Material.BOOK, false,
                        ColourCode.colour("&eCustom: " + (viewCustom ? "On" : "Off")),
                        ColourCode.colour("&7Click to toggle")
                )
        );

        inventory.setItem(49,
                ItemBuilder.CreateCustomItem(
                        Material.EMERALD, false,
                        ColourCode.colour("&b&lDone"),
                        ColourCode.colour("&7Return to creation menu")
                )
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        Player p = (Player) e.getWhoClicked();

        List<ItemStack> allDisplayed;
        if (viewCustom) {
            allDisplayed = new ArrayList<>(CustomItemHandler.getAllItems().values());
        } else if (viewSelectedOnly) {
            allDisplayed = session.getLootDrops().stream()
                    .map(ItemDrop::getItem)
                    .collect(Collectors.toList());
        } else {
            allDisplayed = allLoot.stream()
                    .filter(filter.getPredicate())
                    .map(mat -> new ItemStack(mat, 1))
                    .collect(Collectors.toList());
        }
        int maxPage = (allDisplayed.size() - 1) / PER_PAGE;

        if (slot >= 0 && slot < displayStacks.size()) {
            ItemStack clicked = displayStacks.get(slot);

            NamespacedKey key = new NamespacedKey(PrimevalRPG.getInstance(), "customId");
            Optional<ItemDrop> existing = session.getLootDrops().stream()
                    .filter(d -> {
                        ItemMeta dm = d.getItem().getItemMeta();
                        // custom‐item match?
                        if (dm != null && dm.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                            String id1 = dm.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                            ItemMeta cm = clicked.getItemMeta();
                            if (cm != null && cm.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                                String id2 = cm.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                                return id1.equals(id2);
                            }
                        }
                        return d.getItem().getType() == clicked.getType();
                    })
                    .findFirst();

            if (e.getClick().isRightClick()) {
                existing.ifPresent(d -> {
                    session.getLootDrops().remove(d);
                    p.sendMessage(ColourCode.colour("&cRemoved drop: " + clicked.getType()));
                    render();
                });
            } else {
                new DropEditorMenu(p, clicked).open(p);
            }
            return;
        }

        if (slot == 45 && page > 0) {
            page--; render(); return;
        }
        if (slot == 53 && page < maxPage) {
            page++; render(); return;
        }
        if (slot == 46 && !viewSelectedOnly && !viewCustom) {
            filter = filter.next(); page = 0; render(); return;
        }
        if (slot == 47) {
            viewSelectedOnly = !viewSelectedOnly;
            if (viewSelectedOnly) viewCustom = false;
            page = 0; render(); return;
        }
        if (slot == 48) {
            viewCustom = !viewCustom;
            if (viewCustom) viewSelectedOnly = false;
            page = 0; render(); return;
        }
        if (slot == 49) {
            MenuHistory.goBack(p);
        }
    }

    private ItemStack arrow(String title, String lore) {
        return ItemBuilder.CreateCustomItem(
                Material.ARROW, false,
                ColourCode.colour(title),
                ColourCode.colour(lore)
        );
    }

    private void fillBackground(Inventory inv) {
        ItemStack gray = ItemBuilder.CreateCustomItem(
                Material.GRAY_STAINED_GLASS_PANE, false, " ", null
        );
        for (int i = 0; i < SIZE; i++) inv.setItem(i, gray);
    }

    private enum LootFilter {
        ALL(m -> true),
        BLOCKS(Material::isBlock),
        ITEMS(m -> m.isItem() && !m.isBlock()),
        WEAPONS(m -> {
            String n = m.name();
            return n.endsWith("_SWORD") || n.endsWith("_AXE") || n.endsWith("_HOE") || n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL");
        }),
        ARMOUR(m -> {
            String n = m.name();
            return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
        });

        private final Predicate<Material> pred;
        LootFilter(Predicate<Material> p) { this.pred = p; }
        Predicate<Material> getPredicate() { return pred; }
        LootFilter next() {
            LootFilter[] vals = values();
            return vals[(ordinal()+1) % vals.length];
        }
    }
}