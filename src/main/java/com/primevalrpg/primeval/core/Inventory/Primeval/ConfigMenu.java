package com.primevalrpg.primeval.core.Inventory.Primeval;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.utils.Data.CoreDataHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConfigMenu extends BaseMenu {
    private static final int ROWS         = 3;
    private static final String TITLE     = ColourCode.colour("&6&lPrimeval Config Options");
    private static final int ENABLE_SLOT  = 10;
    private static final int ENABLE_LEVEL_SLOT     = 12;
    private static final int ENABLE_DEBUG_SLOT     = 14;
    private static final int BACK_SLOT    = 22;

    public ConfigMenu() {
        super(ROWS * 9, TITLE);
        render();
    }

    private void render() {
        inventory.clear();
        int size = getInventory().getSize();

        for (int slot = 0; slot < size; slot++) {
            int row = slot / 8, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                Material glass = ((row + col) % 2 == 0
                        ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                        : Material.GRAY_STAINED_GLASS_PANE);
                inventory.setItem(slot,
                        ItemBuilder.CreateCustomItem(glass, true, " ", List.of().toString()));
            }
        }

        boolean enabled = CustomEntityData.getInstance().isCustomMobsEnabled();
        ItemStack toggleItem = ItemBuilder.CreateCustomItem(
                enabled ? Material.LIME_WOOL : Material.RED_WOOL,
                enabled,
                ColourCode.colour(enabled ? "&aCustom Mobs: ON" : "&cCustom Mobs: OFF"),
                ColourCode.colour("&7Click to toggle")
        );
        inventory.setItem(ENABLE_SLOT, toggleItem);

        boolean levEnabled = CoreDataHandler.levelingEnable;
        ItemStack levToggleItem = ItemBuilder.CreateCustomItem(
                levEnabled ? Material.LIME_WOOL : Material.RED_WOOL,
                levEnabled,
                ColourCode.colour(levEnabled ? "&aLeveling: ON" : "&cLeveling: OFF"),
                ColourCode.colour("&7Click to toggle")
        );
        inventory.setItem(ENABLE_LEVEL_SLOT, levToggleItem);

        boolean debugEnabled = CoreDataHandler.debugMode;
        ItemStack debugToggleItem = ItemBuilder.CreateCustomItem(
                debugEnabled ? Material.LIME_WOOL : Material.RED_WOOL,
                debugEnabled,
                ColourCode.colour(debugEnabled ? "&aDebug Mode ON" : "&cDebug Mode: OFF"),
                ColourCode.colour("&7Click to toggle")
        );
        inventory.setItem(ENABLE_DEBUG_SLOT, debugToggleItem);

        inventory.setItem(
                BACK_SLOT,
                ItemBuilder.CreateCustomItem(
                        Material.BARRIER,
                        false,
                        ColourCode.colour("&cBack"),
                        ColourCode.colour("&7Return to main menu")
                )
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == ENABLE_SLOT) {
            boolean newVal = !CoreDataHandler.isEnabled;
            CoreDataHandler.isEnabled = newVal;
            CoreDataHandler.saveSettings();
            render();
            return;
        }

        if (slot == ENABLE_LEVEL_SLOT) {
            boolean newVal = !CoreDataHandler.levelingEnable;
            CoreDataHandler.levelingEnable = newVal;
            CoreDataHandler.saveSettings();
            render();
            return;
        }

        if (slot == ENABLE_DEBUG_SLOT) {
            boolean newVal = !CoreDataHandler.debugMode;
            CoreDataHandler.debugMode = newVal;
            CoreDataHandler.saveSettings();
            render();
            return;
        }

        // 4) Back to main menu
        if (slot == BACK_SLOT) {
            p.closeInventory();
            p.updateInventory();
        }
    }
}