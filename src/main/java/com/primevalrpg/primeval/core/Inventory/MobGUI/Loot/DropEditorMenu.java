package com.primevalrpg.primeval.core.Inventory.MobGUI.Loot;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
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
import java.util.List;

public class DropEditorMenu extends BaseMenu {

    private static final String TITLE = ColourCode.colour("&3Edit Drop: ");
    private static final int SIZE = 27; // 3 rows

    private final CreationSession session;
    private final Material material;
    private ItemDrop drop;

    public DropEditorMenu(Player p, ItemStack clicked) {
        super(SIZE,
                TITLE + ((clicked.getType() != Material.AIR
                        ? clicked.getType()
                        : Material.STONE
                ).name())
        );
        this.session  = CreationManager.get(p.getUniqueId());
        this.material = clicked.getType() != Material.AIR
                ? clicked.getType()
                : Material.STONE;
        this.drop     = findOrCreate(clicked);
        render();
    }

    private void render() {
        fillBackground(inventory);

        String name = ColourCode.colour("&b" + material.name());
        inventory.setItem(13,
                ItemBuilder.CreateMultiLoreItem(material, false, name,
                        ColourCode.colour("&eAmount: " + drop.getItem().getAmount()),
                        ColourCode.colour("&eChance: " + String.format("%.1f%%", drop.getDropChance() * 100)))
        );

        // amount controls
        inventory.setItem(11, controlBtn(Material.RED_STAINED_GLASS_PANE, "&c−1", "&7Decrease by 1"));
        inventory.setItem(12, controlBtn(Material.RED_STAINED_GLASS_PANE, "&c−5", "&7Decrease by 5"));
        inventory.setItem(14, controlBtn(Material.LIME_STAINED_GLASS_PANE, "&a+1", "&7Increase by 1"));
        inventory.setItem(15, controlBtn(Material.LIME_STAINED_GLASS_PANE, "&a+5", "&7Increase by 5"));

        // chance controls
        inventory.setItem(19, controlBtn(Material.RED_STAINED_GLASS_PANE, "&c−5%", "&7Drop chance −5%"));
        inventory.setItem(20, controlBtn(Material.RED_STAINED_GLASS_PANE, "&c−1%", "&7Drop chance −1%"));
        inventory.setItem(22, controlBtn(Material.LIME_STAINED_GLASS_PANE, "&a+1%", "&7Drop chance +1%"));
        inventory.setItem(23, controlBtn(Material.LIME_STAINED_GLASS_PANE, "&a+5%", "&7Drop chance +5%"));

        // cancel & done
        inventory.setItem(21,
                ItemBuilder.CreateCustomItem(Material.BARRIER, false,
                        ColourCode.colour("&cCancel"),
                        ColourCode.colour("&7Discard changes"))
        );
        inventory.setItem(25,
                ItemBuilder.CreateCustomItem(Material.EMERALD, false,
                        ColourCode.colour("&aDone"),
                        ColourCode.colour("&7Save changes"))
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        Player p = (Player) e.getWhoClicked();

        switch (slot) {
            // amount
            case 11 -> adjustAmount(-1);
            case 12 -> adjustAmount(-5);
            case 14 -> adjustAmount(+1);
            case 15 -> adjustAmount(+5);
            // chance
            case 19 -> adjustChance(-0.05);
            case 20 -> adjustChance(-0.01);
            case 22 -> adjustChance(+0.01);
            case 23 -> adjustChance(+0.05);
            // cancel
            case 21 -> MenuHistory.goBack(p);
            // done
            case 25 -> {
                List<ItemDrop> drops = new ArrayList<>(session.getLootDrops());
                drops.removeIf(d -> d.getItem().getType() == material);
                drops.add(drop);                       // add our rebuilt drop
                session.setLootDrops(drops);
                MenuHistory.goBack(p);
                //new LootMenu(p).open(p);
            }
        }
    }

    /**
     * Finds an existing ItemDrop by customId or material;
     * if none exists, adds a new one with default chance=1.0.
     */
    private ItemDrop findOrCreate(ItemStack clicked) {
        NamespacedKey key = new NamespacedKey(PrimevalRPG.getInstance(), "customId");
        ItemMeta cm  = clicked.getItemMeta();
        String    cid = (cm != null && cm.getPersistentDataContainer().has(key, PersistentDataType.STRING))
                ? cm.getPersistentDataContainer().get(key, PersistentDataType.STRING)
                : null;

        for (ItemDrop d : session.getLootDrops()) {
            ItemMeta dm = d.getItem().getItemMeta();
            if (cid != null && dm != null && dm.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String did = dm.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if (cid.equals(did)) return d;
            }
            if (d.getItem().getType() == clicked.getType()) return d;
        }

        ItemDrop newDrop = new ItemDrop(clicked.clone(), 1.0);
        session.getLootDrops().add(newDrop);
        return newDrop;
    }


    private void adjustAmount(int delta) {
        int curr = drop.getItem().getAmount();
        int next = Math.max(1, curr + delta);
        ItemStack stack = drop.getItem();
        stack.setAmount(next);
        drop = new ItemDrop(stack, drop.getDropChance());
        render();
    }

    private void adjustChance(double delta) {
        double curr = drop.getDropChance();
        double next = Math.max(0.0, Math.min(1.0, curr + delta));
        drop = new ItemDrop(drop.getItem(), next);
        render();
    }

    private ItemStack controlBtn(Material mat, String title, String lore) {
        return ItemBuilder.CreateCustomItem(
                mat, false,
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
}