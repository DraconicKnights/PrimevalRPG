package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.events.ChatPrompt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MobConfigMenu extends BaseMenu {

    private static final int ROWS            = 3;
    private static final String TITLE        = ColourCode.colour("&6&lMob Config Options");
    private static final int MIN_DIST_SLOT   = 10;
    private static final int MAX_DIST_SLOT   = 12;
    private static final int MOB_SPAWN_SLOT  = 14;
    private static final int CAVE_SPAWN_SLOT = 16;
    private static final int MAX_PER_SLOT    = 20;
    private static final int BACK_SLOT       = 22;

    private final Inventory inv = Bukkit.createInventory(this, ROWS * 9, TITLE);

    public MobConfigMenu() {
        super(ROWS * 9, TITLE);
        render();
    }

    private void render() {
        inv.clear();

        ItemStack minItem = ItemBuilder.CreateCustomItem(
                Material.ENDER_PEARL,
                false,
                ColourCode.colour("&eMin Spawn Dist: &f" + MobDataHandler.minDistance),
                ColourCode.colour("&7Click to enter new value")
        );
        inv.setItem(MIN_DIST_SLOT, minItem);

        ItemStack maxItem = ItemBuilder.CreateCustomItem(
                Material.ENDER_PEARL,
                false,
                ColourCode.colour("&eMax Spawn Dist: &f" + MobDataHandler.maxDistance),
                ColourCode.colour("&7Click to enter new value")
        );
        inv.setItem(MAX_DIST_SLOT, maxItem);

        ItemStack mobChance = ItemBuilder.CreateCustomItem(
                Material.EGG,
                false,
                ColourCode.colour("&eMob Spawn Chance: &f" + MobDataHandler.mobSpawnChance),
                ColourCode.colour("&7Click to enter new value")
        );
        inv.setItem(MOB_SPAWN_SLOT, mobChance);

        ItemStack caveChance = ItemBuilder.CreateCustomItem(
                Material.EGG,
                false,
                ColourCode.colour("&eCave Spawn Chance: &f" + MobDataHandler.caveSpawnChance),
                ColourCode.colour("&7Click to enter new value")
        );
        inv.setItem(CAVE_SPAWN_SLOT, caveChance);

        ItemStack maxPer = ItemBuilder.CreateCustomItem(
                Material.IRON_BOOTS,
                false,
                ColourCode.colour("&eMax Per Player: &f" + MobDataHandler.maxPerPlayer),
                ColourCode.colour("&7Click to enter new value")
        );
        inv.setItem(MAX_PER_SLOT, maxPer);

        ItemStack back = ItemBuilder.CreateCustomItem(
                Material.BARRIER,
                false,
                ColourCode.colour("&cBack"),
                ColourCode.colour("&7Return to main menu")
        );
        inv.setItem(BACK_SLOT, back);
    }

    @Override
    public void open(Player player) {
        player.openInventory(inv);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == MIN_DIST_SLOT) {
            ChatPrompt.prompt(p,
                    ColourCode.colour("&eEnter new min spawn distance:"),
                    input -> {
                        try {
                            MobDataHandler.minDistance = Integer.parseInt(input);
                            MobDataHandler.saveSettings();
                            p.sendMessage(ColourCode.colour("&aMin distance set to &f" + input));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid number."));
                        }
                        new MobConfigMenu().open(p);
                    });

        } else if (slot == MAX_DIST_SLOT) {
            ChatPrompt.prompt(p,
                    ColourCode.colour("&eEnter new max spawn distance:"),
                    input -> {
                        try {
                            MobDataHandler.maxDistance = Integer.parseInt(input);
                            MobDataHandler.saveSettings();
                            p.sendMessage(ColourCode.colour("&aMax distance set to &f" + input));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid number."));
                        }
                        new MobConfigMenu().open(p);
                    });

        } else if (slot == MOB_SPAWN_SLOT) {
            ChatPrompt.prompt(p,
                    ColourCode.colour("&eEnter new mob spawn chance (decimal):"),
                    input -> {
                        try {
                            MobDataHandler.mobSpawnChance = Double.parseDouble(input);
                            MobDataHandler.saveSettings();
                            p.sendMessage(ColourCode.colour("&aMob spawn chance set to &f" + input));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid decimal."));
                        }
                        new MobConfigMenu().open(p);
                    });

        } else if (slot == CAVE_SPAWN_SLOT) {
            ChatPrompt.prompt(p,
                    ColourCode.colour("&eEnter new cave spawn chance (decimal):"),
                    input -> {
                        try {
                            MobDataHandler.caveSpawnChance = Double.parseDouble(input);
                            MobDataHandler.saveSettings();
                            p.sendMessage(ColourCode.colour("&aCave spawn chance set to &f" + input));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid decimal."));
                        }
                        new MobConfigMenu().open(p);
                    });

        } else if (slot == MAX_PER_SLOT) {
            ChatPrompt.prompt(p,
                    ColourCode.colour("&eEnter new max per player:"),
                    input -> {
                        try {
                            MobDataHandler.maxPerPlayer = Integer.parseInt(input);
                            MobDataHandler.saveSettings();
                            p.sendMessage(ColourCode.colour("&aMax per player set to &f" + input));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid number."));
                        }
                        new MobConfigMenu().open(p);
                    });

        } else if (slot == BACK_SLOT) {
            MenuHistory.goBack(p);
        }
    }
}