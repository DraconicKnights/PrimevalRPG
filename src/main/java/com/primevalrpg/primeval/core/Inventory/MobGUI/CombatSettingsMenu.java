package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.events.ChatPrompt;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CombatSettingsMenu extends BaseMenu {
    private static final int ROWS = 3;                      // 3 rows × 9 = 27 slots
    private static final String TITLE = ColourCode.colour("&6&lCombat Stats");
    private final CreationSession session;

    public CombatSettingsMenu(Player p) {
        super(ROWS * 9, TITLE);
        this.session = CreationManager.get(p.getUniqueId());
        render();
    }

    private void render() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            int r = i / 9, c = i % 9;
            if (r == 0 || r == ROWS - 1 || c == 0 || c == 8) {
                Material glass = ((r + c) % 2 == 0
                        ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                        : Material.GRAY_STAINED_GLASS_PANE);
                getInventory().setItem(i,
                        ItemBuilder.CreateCustomItem(glass, true, " ", List.of().toString()));
            } else {
                getInventory().setItem(i, null);
            }
        }

        setSlot(10, Material.IRON_SWORD, "&cDamage", session.getCombatDamage());
        setSlot(12, Material.ARROW,        "&aRange",  session.getCombatRange());
        setSlot(14, Material.CROSSBOW,     "&bCrit %", session.getCombatCrit() * 100 + "%");
        setSlot(16, Material.SHIELD,       "&eDefense",session.getCombatDef());

        setSlot(18, Material.BARRIER, "&cBack", "");
        setSlot(26, Material.GREEN_CONCRETE, "&aDone", "");
    }

    private void setSlot(int slot, Material mat, String title, Object val) {
        String display = ColourCode.colour("&a" + title + ": &f" + val);
        ItemStack item = ItemBuilder.CreateCustomItem(mat, false, display, null);
        inventory.setItem(slot, item);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        switch (e.getRawSlot()) {
            case 10 -> promptStat(p, "damage",  (txt)-> session.setCombat(
                    Double.parseDouble(txt), session.getCombatRange(),
                    session.getCombatCrit(), session.getCombatDef()));
            case 12 -> promptStat(p, "range",   (txt)-> session.setCombat(
                    session.getCombatDamage(), Double.parseDouble(txt),
                    session.getCombatCrit(), session.getCombatDef()));
            case 14 -> promptStat(p, "crit %",  (txt)-> session.setCombat(
                    session.getCombatDamage(), session.getCombatRange(),
                    Double.parseDouble(txt), session.getCombatDef()));
            case 16 -> promptStat(p, "defense", (txt)-> session.setCombat(
                    session.getCombatDamage(), session.getCombatRange(),
                    session.getCombatCrit(), Double.parseDouble(txt)));

            case 18 -> {
                MenuHistory.goBack(p);
            }
            case 26 -> {
                MenuHistory.goBack(p);
            }
            default -> { }
        }
    }

    private void promptStat(Player p, String label, java.util.function.Consumer<String> apply) {
        p.closeInventory();
        ChatPrompt.prompt(p,
                ChatColor.YELLOW + "Enter " + label + " or 'cancel':",
                text -> {
                    if (!text.equalsIgnoreCase("cancel")) {
                        try {
                            apply.accept(text);
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ChatColor.RED + "Invalid number!");
                        }
                    }
                    render();
                    open(p);
                });
    }
}