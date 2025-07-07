// DimensionsMenu.java
package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DimensionsMenu extends BaseMenu {
    private static final int ROWS = 3;
    private static final String TITLE = ColourCode.colour("&6&lSelect Dimensions");
    private final CreationSession session;

    public DimensionsMenu(Player player) {
        super(ROWS * 9, TITLE);
        this.session = CreationManager.get(player.getUniqueId());
        render();
    }

    private void render() {
        inventory.clear();
        int size = getInventory().getSize();

        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                Material glass = ((row + col) % 2 == 0
                        ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                        : Material.GRAY_STAINED_GLASS_PANE);
                inventory.setItem(slot,
                        ItemBuilder.CreateCustomItem(glass, true, " ", List.of().toString()));
            }
        }

        List<Integer> interior = new ArrayList<>();
        for (int col = 1; col <= 7; col++) {
            interior.add(1 * 9 + col);
        }

        List<Environment> all = Arrays.asList(Environment.values());
        for (int i = 0; i < all.size() && i < interior.size(); i++) {
            Environment env = all.get(i);
            boolean sel = session.getSpawnDimensions().contains(env);

            Material mat = sel ? Material.GREEN_WOOL : Material.RED_WOOL;
            String name = ColourCode.colour((sel ? "&a" : "&c") + env.name());
            List<String> lore = List.of(sel ? "§7Click to remove" : "§7Click to add");

            ItemStack button = ItemBuilder.CreateMultiLoreItem(mat, false, name, String.valueOf(lore));
            inventory.setItem(interior.get(i), button);
        }

        int doneSlot = size - 1;
        ItemStack done = ItemBuilder.CreateMultiLoreItem(
                Material.EMERALD_BLOCK,
                false,
                ColourCode.colour("&aDone"),
                String.valueOf(List.of("§7Back to Creator"))
        );
        inventory.setItem(doneSlot, done);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        Player p = (Player) e.getWhoClicked();
        int size = getInventory().getSize();

        if (slot == size - 1) {
            MenuHistory.goBack(p);
            return;
        }

        List<Environment> all = Arrays.asList(Environment.values());
        int interiorIndex = -1;
        if (slot >= 9 && slot < 9*2) {
            int col = slot % 9;
            if (col >= 1 && col <= 7) interiorIndex = col - 1;
        }
        if (interiorIndex >= 0 && interiorIndex < all.size()) {
            session.toggleDimension(all.get(interiorIndex));
            render();
        }
    }
}