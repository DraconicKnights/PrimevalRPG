package com.primevalrpg.primeval.core.Inventory.MobGUI.Enchant;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class EnchantmentLevelMenu extends BaseMenu {
    private final Player player;
    private final CreationSession session;
    private final Enchantment enchant;
    private final Material targetPiece;

    public EnchantmentLevelMenu(Player player,
                                CreationSession session,
                                Enchantment enchant,
                                Material targetPiece) {
        super(9, ColourCode.colour("&3Level: "+enchant.getKey().getKey()));
        this.player      = player;
        this.session     = session;
        this.enchant     = enchant;
        this.targetPiece = targetPiece;
        render();
    }

    private void render() {

        // centre fill
        ItemStack fillPane = ItemBuilder.CreateCustomItem(
                Material.LIGHT_GRAY_STAINED_GLASS_PANE, false, " ", null
        );
        // border materials
        Material cornerMat = Material.CYAN_STAINED_GLASS_PANE;
        Material edgeMat1  = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        Material edgeMat2  = Material.BLUE_STAINED_GLASS_PANE;

        int size = inventory.getSize();
        int rows = size / 9;
        for (int idx = 0; idx < size; idx++) {
            int row = idx / 9, col = idx % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                Material mat;
                boolean isCorner = (row == 0 || row == rows - 1) && (col == 0 || col == 8);
                if (isCorner) {
                    mat = cornerMat;
                } else {
                    mat = ((row + col) % 2 == 0 ? edgeMat1 : edgeMat2);
                }
                inventory.setItem(idx,
                        ItemBuilder.CreateCustomItem(mat, false, " ", null));
            } else {
                inventory.setItem(idx, fillPane);
            }
        }


        int max = enchant.getMaxLevel();
        for (int i = 1; i <= max && i <= 7; i++) {
            ItemStack it = ItemBuilder.CreateCustomItem(
                    Material.PAPER,
                    false,
                    ColourCode.colour("&aLevel " + i),
                    null
            );
            inventory.setItem(i - 1, it);
        }

        inventory.setItem(8, ItemBuilder.CreateCustomItem(
                Material.BARRIER,false,
                ColourCode.colour("&cBack"),null
        ));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot >= 0 && slot < enchant.getMaxLevel()) {
            int level = slot + 1;

            ItemStack clicked = inventory.getItem(slot);
            if (clicked != null) {
                ItemMeta m = clicked.getItemMeta();
                m.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                m.setDisplayName(ColourCode.colour("&eLevel " + level));
                clicked.setItemMeta(m);
                inventory.setItem(slot, clicked);
            }

            if (targetPiece == null) {
                session.getWeaponEnchants().put(enchant, level);
            } else {
                session.getArmourEnchants()
                        .computeIfAbsent(targetPiece, k -> new HashMap<>())
                        .put(enchant, level);
            }

            Bukkit.getScheduler().runTaskLater(
                    PrimevalRPG.getInstance(),
                    () -> MenuHistory.goBack(player),
                    1L
            );
            return;
        }

        // back button
        if (slot == 8) {
            MenuHistory.goBack(player);
        }
    }

}