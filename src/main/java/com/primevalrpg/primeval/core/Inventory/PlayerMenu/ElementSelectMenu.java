package com.primevalrpg.primeval.core.Inventory.PlayerMenu;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ElementSelectMenu extends BaseMenu {
    private static final int ROWS = 3, COLS = 3, SIZE = ROWS * COLS;
    private static final String TITLE = ColourCode.colour("&bSelect Active Element");
    private Player viewer;

    public ElementSelectMenu() {
        super(SIZE, TITLE);
    }

    @Override
    public void open(Player p) {
        this.viewer = p;
        render();
        super.open(p);
    }

    private void render() {
        inventory.clear();
        PlayerData data = PlayerDataManager.getInstance()
                .getPlayerData(viewer.getUniqueId());
        ElementType active = data.getActiveElement();

        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, ItemBuilder.CreateCustomItem(
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    false, " ", " "
            ));
        }

        ElementType[] types = ElementType.values();
        int[] slots = {3, 4, 5, 6};
        for (int i = 0; i < types.length; i++) {
            ElementType type = types[i];
            Material mat = switch (type) {
                case FIRE  -> Material.FIRE_CHARGE;
                case WATER -> Material.WATER_BUCKET;
                case EARTH -> Material.DIRT;
                case WIND  -> Material.PHANTOM_MEMBRANE;
            };
            boolean isActive = type == active;
            inventory.setItem(slots[i], ItemBuilder.CreateCustomItem(
                    mat,
                    isActive,
                    ColourCode.colour("&6" + type.name()),
                    ColourCode.colour(isActive
                            ? "&aCurrent Active"
                            : "&7Click to activate")
            ));
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        ElementType[] types = ElementType.values();
        int[] slots = {3, 4, 5, 6};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                PlayerDataManager.getInstance()
                        .getPlayerData(p.getUniqueId())
                        .setActiveElement(types[i]);
                p.sendMessage(ColourCode.colour(
                        "&aActive element set to &6" + types[i].name()
                ));
                p.closeInventory();
                new PlayerCoreMenu().open(p);
            }
        }
    }
}