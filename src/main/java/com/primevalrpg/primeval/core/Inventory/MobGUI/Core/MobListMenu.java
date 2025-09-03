package com.primevalrpg.primeval.core.Inventory.MobGUI.Core;

import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.IntStream;

public class MobListMenu extends BaseMenu {

    private static final int ROWS = 6;               // 6 rows × 9 cols = 54 slots
    private static final String TITLE = ColourCode.colour("&5 &lRegistered Mobs");
    private static final Material BORDER_MAT = Material.GRAY_STAINED_GLASS_PANE;
    private static final ItemStack PREV_ARROW = ItemBuilder.CreateCustomItem(
            Material.ARROW, false,
            ColourCode.colour("&aPrevious"),
            ColourCode.colour("&7Go to previous page")
    );
    private static final ItemStack NEXT_ARROW = ItemBuilder.CreateCustomItem(
            Material.ARROW, false,
            ColourCode.colour("&aNext"),
            ColourCode.colour("&7Go to next page")
    );

    private static final List<Integer> CONTENT_SLOTS = new ArrayList<>();
    static {
        for (int r = 1; r <= ROWS - 2; r++) {
            for (int c = 1; c <= 7; c++) {
                CONTENT_SLOTS.add(r * 9 + c);
            }
        }
    }

    private static final int BACK_SLOT = (ROWS - 1) * 9 + 4;

    private final int page;
    private final List<CustomMob> mobs;
    private final int totalPages;

    public MobListMenu() {
        this(0);
    }

    public MobListMenu(int page) {
        super(ROWS * 9, TITLE);
        this.page = page;

        // fetch all registered mobs, sort by ID/name
        Set<CustomMob> sorted = new TreeSet<>(Comparator.comparing(CustomMob::getMobNameID));
        sorted.addAll(CustomEntityArrayHandler.getRegisteredCustomMobs().values());
        this.mobs = new ArrayList<>(sorted);

        int perPage = CONTENT_SLOTS.size();
        this.totalPages = (int) Math.ceil(mobs.size() / (double) perPage);

        drawBorder();
        drawPageItems();
        drawNavigation();
    }

    private void drawBorder() {
        IntStream.range(0, inventory.getSize()).forEach(slot -> {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                inventory.setItem(slot,
                        ItemBuilder.CreateCustomItem(BORDER_MAT, true, " ", Collections.emptyList().toString())
                );
            }
        });
    }

    private void drawPageItems() {
        int start = page * CONTENT_SLOTS.size();
        for (int i = 0; i < CONTENT_SLOTS.size(); i++) {
            int idx = start + i;
            if (idx >= mobs.size()) break;

            CustomMob mob = mobs.get(idx);
            Material egg;
            try {
                egg = Material.valueOf(mob.getEntityType().name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException ex) {
                egg = Material.BARRIER;
            }

            List<String> lore = List.of(
                    ColourCode.colour("&7ID: " + mob.getMobID()),
                    ColourCode.colour("&7Left Click to spawn"),
                    ColourCode.colour("&cRight Click to Edit")
            );

            ItemStack item = ItemBuilder.CreateMultiLoreItem(
                    egg, false,
                    ColourCode.colour("&5" + mob.getMobNameID()),
                    String.valueOf(lore)
            );
            inventory.setItem(CONTENT_SLOTS.get(i), item);
        }

        if (mobs.isEmpty()) {
            ItemStack none = ItemBuilder.CreateCustomItem(
                    Material.BARRIER, false,
                    ColourCode.colour("&cNo custom mobs"),
                    ColourCode.colour("&7Use the creation menu to add some")
            );
            inventory.setItem((ROWS * 9) / 2 - 1, none);
        }

        ItemStack back = ItemBuilder.CreateCustomItem(
                Material.BARRIER, false,
                ColourCode.colour("&cBack"),
                ColourCode.colour("&7Return to main menu")
        );
        inventory.setItem(BACK_SLOT, back);
    }

    private void drawNavigation() {
        // prev arrow (bottom-left)
        if (page > 0) {
            inventory.setItem((ROWS - 1) * 9 + 0, PREV_ARROW);
        }
        // next arrow (bottom-right)
        if (page < totalPages - 1) {
            inventory.setItem((ROWS - 1) * 9 + 8, NEXT_ARROW);
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        int slot = e.getSlot();

        // ←/→ navigation
        if (clicked != null && clicked.isSimilar(PREV_ARROW) && page > 0) {
            new MobListMenu(page - 1).open(player);
            return;
        }
        if (clicked != null && clicked.isSimilar(NEXT_ARROW) && page < totalPages - 1) {
            new MobListMenu(page + 1).open(player);
            return;
        }

        if (slot == BACK_SLOT) {
            new Menu().open(player);
            return;
        }

        int idxInPage = CONTENT_SLOTS.indexOf(slot);
        if (idxInPage == -1) return;

        int globalIndex = page * CONTENT_SLOTS.size() + idxInPage;
        if (globalIndex < 0 || globalIndex >= mobs.size()) return;

        CustomMob mob = mobs.get(globalIndex);

        switch (e.getClick()) {
            case LEFT:
                CustomMobManager.getInstance().setMobLevelAndSpawn(player, mob, player.getLocation());
                //mob.spawnEntity(player.getLocation(), mob.getLevel());
                player.sendMessage(ColourCode.colour("&aSpawned “" + mob.getName() + "”!"));
                break;

            case RIGHT:
                CreationManager.startEditSession(player, mob);
                break;

            default:
                break;
        }
    }
}