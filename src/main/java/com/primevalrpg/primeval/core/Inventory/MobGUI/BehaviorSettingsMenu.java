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

public class BehaviorSettingsMenu extends BaseMenu {
    private static final int ROWS = 3;
    private static final String TITLE = ColourCode.colour("&6&lBehavior Stats");
    private final CreationSession session;

    public BehaviorSettingsMenu(Player p) {
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

        setSlot(11, Material.SUGAR,    "&fSpeed",      session.getBehaviorSpeed());
        setSlot(13, Material.ROTTEN_FLESH, "&cAggression",session.getBehaviorAggression());
        setSlot(15, Material.PAPER,    "&ePattern",    session.getBehaviorPattern());

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
            case 11 -> promptField(p, "speed",      txt -> session.setBehavior(
                    Double.parseDouble(txt), session.getBehaviorAggression(),
                    session.getBehaviorPattern()));
            case 13 -> promptField(p, "aggression", txt -> session.setBehavior(
                    session.getBehaviorSpeed(), Integer.parseInt(txt),
                    session.getBehaviorPattern()));
            case 15 -> promptField(p, "pattern",    txt -> session.setBehavior(
                    session.getBehaviorSpeed(), session.getBehaviorAggression(),
                    txt));

            case 18 -> { MenuHistory.goBack(p); }
            case 26 -> { MenuHistory.goBack(p); }
            default -> { }
        }
    }

    private void promptField(Player p, String label, java.util.function.Consumer<String> apply) {
        p.closeInventory();
        ChatPrompt.prompt(p,
                ChatColor.YELLOW + "Enter " + label + " or 'cancel':",
                text -> {
                    if (!text.equalsIgnoreCase("cancel")) {
                        try {
                            apply.accept(text);
                        } catch (Exception ex) {
                            p.sendMessage(ChatColor.RED + "Invalid input!");
                        }
                    }
                    render();
                    open(p);
                });
    }
}