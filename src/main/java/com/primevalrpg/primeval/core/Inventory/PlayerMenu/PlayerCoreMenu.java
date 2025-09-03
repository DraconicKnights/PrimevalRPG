package com.primevalrpg.primeval.core.Inventory.PlayerMenu;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerCoreMenu extends BaseMenu {
    private static final int ROWS = 3, COLS = 9, SIZE = ROWS * COLS;
    private static final String TITLE = ColourCode.colour("&5&lElemental Menu");

    public PlayerCoreMenu() {
        super(SIZE, TITLE);
        render();
    }

    private void render() {
        inventory.clear();

        for (int i = 0; i < SIZE; i++) {
            int row = i / COLS, col = i % COLS;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                setItem(i, ItemBuilder.CreateCustomItem(
                        Material.BLACK_STAINED_GLASS_PANE,
                        false, " ", " "
                ));
            }
        }

        setItem(4, ItemBuilder.CreateCustomItem(
                Material.END_CRYSTAL,
                false,
                TITLE,
                " "
        ));

        setItem(10, ItemBuilder.CreateCustomItem(
                Material.FIRE_CHARGE, false,
                ColourCode.colour("&cFire Stats"),
                ColourCode.colour("&7View your Fire progress")
        ));
        setItem(12, ItemBuilder.CreateCustomItem(
                Material.WATER_BUCKET, false,
                ColourCode.colour("&9Water Stats"),
                ColourCode.colour("&7View your Water progress")
        ));
        setItem(14, ItemBuilder.CreateCustomItem(
                Material.DIRT, false,
                ColourCode.colour("&2Earth Stats"),
                ColourCode.colour("&7View your Earth progress")
        ));
        setItem(16, ItemBuilder.CreateCustomItem(
                Material.PHANTOM_MEMBRANE, false,
                ColourCode.colour("&eWind Stats"),
                ColourCode.colour("&7View your Wind progress")
        ));
        setItem(22, ItemBuilder.CreateCustomItem(
                Material.COMPASS, false,
                ColourCode.colour("&bSelect Active Element"),
                ColourCode.colour("&7Choose which element is active")
        ));
        setItem(24, ItemBuilder.CreateCustomItem(
                Material.BOOK, false,
                ColourCode.colour("&aSelect Ability"),
                ColourCode.colour("&7Pick an ability to bind")
        ));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player p = (Player) event.getWhoClicked();
        switch (event.getSlot()) {
            case 10 -> new ElementMenu(ElementType.FIRE).open(p);
            case 12 -> new ElementMenu(ElementType.WATER).open(p);
            case 14 -> new ElementMenu(ElementType.EARTH).open(p);
            case 16 -> new ElementMenu(ElementType.WIND).open(p);
            case 22 -> new ElementSelectMenu().open(p);
            case 24 -> new AbilitySelectMenu(p, PlayerDataManager.getInstance().getPlayerData(p.getUniqueId()).getActiveElement()).open(p);
            default -> p.sendMessage(ColourCode.colour("&cThis slot is not clickable."));
        }
    }
}