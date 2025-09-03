package com.primevalrpg.primeval.core.Inventory.MobGUI.Core;

import com.primevalrpg.primeval.core.Inventory.MobGUI.ConfirmMenu;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobRemovalMenu extends BaseMenu {

    private static final int ROWS = 6;
    private static final String TITLE = ColourCode.colour("&c&lRemove Custom Mob");
    private static final Material BORDER_MAT = Material.GRAY_STAINED_GLASS_PANE;
    private static final ItemStack PREV_ARROW =
            ItemBuilder.CreateCustomItem(Material.ARROW, false, "&aPrevious", Collections.emptyList().toString());
    private static final ItemStack NEXT_ARROW =
            ItemBuilder.CreateCustomItem(Material.ARROW, false, "&aNext", Collections.emptyList().toString());

    private static final List<Integer> CONTENT_SLOTS = new ArrayList<>();
    static {
        for (int row = 1; row <= ROWS - 2; row++) {
            for (int col = 1; col <= 7; col++) {
                CONTENT_SLOTS.add(row * 9 + col);
            }
        }
    }

    private static final int BACK_SLOT = (ROWS - 1) * 9 + 4;


    private final int page;
    private final List<CustomMob> mobs;
    private final int totalPages;

    public MobRemovalMenu() {
        this(0);
    }

    public MobRemovalMenu(int page) {
        super(ROWS * 9, TITLE);
        this.page = page;
        this.mobs = new ArrayList<>(CustomEntityArrayHandler.getRegisteredCustomMobs().values());
        this.totalPages = (int) Math.ceil(mobs.size() / (double) CONTENT_SLOTS.size());

        drawBorder();
        drawPageItems();
        drawNavigation();
    }

    private void drawBorder() {
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                inventory.setItem(slot, ItemBuilder.CreateCustomItem(
                        BORDER_MAT, true, " ", List.of().toString()));
            }
        }
    }

    private void drawPageItems() {
        int start = page * CONTENT_SLOTS.size();
        for (int i = 0; i < CONTENT_SLOTS.size(); i++) {
            int idx = start + i;
            int slot = CONTENT_SLOTS.get(i);
            if (idx >= mobs.size()) break;
            CustomMob mob = mobs.get(idx);
            ItemStack item = ItemBuilder.CreateCustomItem(
                    Material.PAPER,
                    false,
                    ColourCode.colour("&c&l" + mob.getMobNameID()),
                    ColourCode.colour("&7ID: " + mob.getMobID())
            );
            inventory.setItem(slot, item);
        }

        ItemStack back = ItemBuilder.CreateCustomItem(
                Material.BARRIER, false,
                ColourCode.colour("&cBack"),
                ColourCode.colour("&7Return to main menu")
        );
        inventory.setItem(BACK_SLOT, back);
    }

    private void drawNavigation() {
        // previous
        if (page > 0) {
            inventory.setItem((ROWS - 1) * 9 + 0, PREV_ARROW);
        }
        // next
        if (page < totalPages - 1) {
            inventory.setItem((ROWS - 1) * 9 + 8, NEXT_ARROW);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player p = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // navigation
        if (slot == (ROWS - 1) * 9 + 0 && page > 0) {
            new MobRemovalMenu(page - 1).open(p);
            return;
        }
        if (slot == (ROWS - 1) * 9 + 8 && page < totalPages - 1) {
            new MobRemovalMenu(page + 1).open(p);
            return;
        }

        if (slot == BACK_SLOT) {
            new Menu().open(p);
            return;
        }


        // content
        int idxInPage = CONTENT_SLOTS.indexOf(slot);
        if (idxInPage == -1) return;
        int mobIdx = page * CONTENT_SLOTS.size() + idxInPage;
        if (mobIdx >= mobs.size()) return;

        String mobID = mobs.get(mobIdx).getMobNameID();

        ItemStack confirm = ItemBuilder.CreateCustomItem(
                Material.GREEN_WOOL, false,
                ColourCode.colour( "&aConfirm Removal"),
                ColourCode.colour("&7Delete mob &f" + mobID + "&7 permanently")
        );
        ItemStack cancel = ItemBuilder.CreateCustomItem(
                Material.RED_WOOL, false,
                ColourCode.colour( "&cCancel"),
                ColourCode.colour("&7&lBack")
        );

        new ConfirmMenu(
                ColourCode.colour("&4Confirm Deletion"),
                confirm,
                cancel,
                () -> {
                    CustomEntityData.getInstance().removeCustomMob(mobID, p);
                    new MobRemovalMenu(page).open(p);
                },
                () -> new MobRemovalMenu(page)
        ).open(p);
    }
}