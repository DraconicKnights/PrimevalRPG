package com.primevalrpg.primeval.core.Inventory.MobGUI.Enchant;

import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantmentListMenu extends BaseMenu {
    private final Player player;
    private final CreationSession session;
    private final Material targetPiece;
    private final List<Enchantment> enchants;

    private boolean showOnlyApplied = false;
    private int currentPage = 0;
    private static final List<Integer> ENCHANT_SLOTS = List.of(
            10, 11, 12, 13, 14, 15, 16,   // row 1 cols 1–7
            19, 20, 21, 22, 23, 24, 25    // row 2 cols 1–7
    );


    public EnchantmentListMenu(Player player,
                               CreationSession session,
                               Material targetPiece) {
        super(45, ColourCode.colour("&3Select an Enchantment"));
        this.player      = player;
        this.session     = session;
        this.targetPiece = targetPiece;

        ItemStack preview = (targetPiece == null)
                ? session.getWeapon()
                : new ItemStack(targetPiece);
        List<Enchantment> ok  = new ArrayList<>();
        List<Enchantment> nok = new ArrayList<>();
        for (Enchantment e : Enchantment.values()) {
            (e.canEnchantItem(preview) ? ok : nok).add(e);
        }
        Comparator<Enchantment> byName = Comparator.comparing(
                e -> e.getKey().getKey(),
                String.CASE_INSENSITIVE_ORDER
        );
        ok.sort(byName);
        nok.sort(byName);
        this.enchants = new ArrayList<>();
        this.enchants.addAll(ok);
        this.enchants.addAll(nok);

        render();
    }

    protected void fillBackground() {
        Material border = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        for (int i = 0; i < inventory.getSize(); i++) {
            int r = i / 9, c = i % 9;
            if (r == 0 || r == 4 || c == 0 || c == 8) {
                inventory.setItem(i,
                        ItemBuilder.CreateCustomItem(border, true, " ", null));
            } else {
                inventory.setItem(i, null);
            }
        }
    }

    private void render() {
        fillBackground();
        List<Enchantment> view = showOnlyApplied
                ? getAppliedList()
                : enchants;
        int perPage  = ENCHANT_SLOTS.size();
        int pages    = (view.size() + perPage - 1) / perPage;
        currentPage  = Math.max(0, Math.min(currentPage, pages - 1));

        for (int i = 0; i < perPage; i++) {
            int slot = ENCHANT_SLOTS.get(i);
            inventory.setItem(slot, null);
            int idx = currentPage * perPage + i;
            if (idx < view.size()) {
                Enchantment e = view.get(idx);
                String name = e.getKey().getKey()
                        .replace('_',' ').toUpperCase();
                List<String> lore = List.of(
                        ColourCode.colour(showOnlyApplied
                                ? "&7Level: &f" + getAppliedMap().get(e)
                                : "&7Max: &f" + e.getMaxLevel())
                );
                inventory.setItem(slot,
                        ItemBuilder.CreateMultiLoreItem(
                                Material.ENCHANTED_BOOK,
                                false,
                                ColourCode.colour("&a" + name),
                                String.valueOf(lore)
                        ));
            }
        }

        int base       = 4 * 9;
        int prevSlot   = base + 2;
        int pageSlot   = base + 3;
        int backSlot   = base + 4;
        int toggleSlot = base + 5;
        int nextSlot   = base + 6;


        // prev
        inventory.setItem(prevSlot,
                ItemBuilder.CreateMultiLoreItem(
                        Material.ARROW, false,
                        ColourCode.colour("&e« Prev"),
                        String.valueOf(List.of(ColourCode.colour("&7Page " + (currentPage+1) + "/" + Math.max(pages,1))))
                ));

        // page indicator
        inventory.setItem(pageSlot,
                ItemBuilder.CreateCustomItem(
                        Material.PAPER, false,
                        ColourCode.colour("&6" + (currentPage+1) + "/" + Math.max(pages,1)),
                        null
                ));

        // back
        inventory.setItem(backSlot,
                ItemBuilder.CreateMultiLoreItem(
                        Material.BARRIER, false,
                        ColourCode.colour("&cBack"),
                        String.valueOf(List.of(ColourCode.colour("&7Return")))
                ));

        // toggle
        Material dye = showOnlyApplied ? Material.LIME_DYE : Material.RED_DYE;
        String txt   = showOnlyApplied
                ? "&aApplied Only"
                : "&cShow Applied";
        inventory.setItem(toggleSlot,
                ItemBuilder.CreateMultiLoreItem(
                        dye, false,
                        ColourCode.colour(txt),
                        String.valueOf(List.of(ColourCode.colour("&7Click to toggle")))
                ));

        // next
        inventory.setItem(nextSlot,
                ItemBuilder.CreateMultiLoreItem(
                        Material.ARROW, false,
                        ColourCode.colour("&eNext »"),
                        String.valueOf(List.of(ColourCode.colour("&7Page " + (currentPage+1) + "/" + Math.max(pages,1))))
                ));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        int base = 4 * 9;
        int prev = base + 2, page = base + 3, back = base + 4, tog = base + 5, nxt = base + 6;

        // navigate
        if (slot == prev && currentPage > 0) {
            currentPage--;
            render();
            return;
        }
        if (slot == nxt && getViewSize() > (currentPage + 1) * ENCHANT_SLOTS.size()) {
            currentPage++;
            render();
            return;
        }
        if (slot == tog) {
            showOnlyApplied = !showOnlyApplied;
            currentPage = 0;
            render();
            return;
        }
        if (slot == back) {
            player.closeInventory();
            MenuHistory.goBack(player);
            return;
        }

        int idx = ENCHANT_SLOTS.indexOf(slot);
        if (idx >= 0) {
            int real = currentPage * ENCHANT_SLOTS.size() + idx;
            List<Enchantment> view = showOnlyApplied ? getAppliedList() : enchants;
            if (real < view.size()) {
                Enchantment eSel = view.get(real);
                if (showOnlyApplied) {
                    getAppliedMap().remove(eSel);
                    render();
                } else {
                    player.closeInventory();
                    MenuHistory.open(player,
                            new EnchantmentLevelMenu(player, session, eSel, targetPiece));
                }
            }
        }
    }

    private int getViewSize() {
        return (showOnlyApplied
                ? getAppliedList().size()
                : enchants.size());
    }

    private Map<Enchantment,Integer> getAppliedMap() {
        if (targetPiece == null) {
            return session.getWeaponEnchants();
        }
        return session.getArmourEnchants()
                .getOrDefault(targetPiece, Collections.emptyMap());
    }

    private List<Enchantment> getAppliedList() {
        List<Enchantment> ls = new ArrayList<>(getAppliedMap().keySet());
        ls.sort(Comparator.comparing(
                e->e.getKey().getKey(),
                String.CASE_INSENSITIVE_ORDER));
        return ls;
    }
}