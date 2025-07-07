package com.primevalrpg.primeval.core.Inventory.MobGUI.Armour;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class PieceSelectMenu extends BaseMenu {
    private static final int ROWS        = 6;
    private static final int PER_PAGE    = 4 * 7;

    private static final int PREV_SLOT   = 5*9 + 1; // slot 46
    private static final int REMOVE_SLOT = 5*9 + 3; // slot 48
    private static final int CANCEL_SLOT = 5*9 + 4; // slot 49
    private static final int NEXT_SLOT   = 5*9 + 7; // slot 52

    private final CreationSession session;
    private final List<Material>   workingList;
    private final int              pieceIndex;
    private final List<Material>   choices;
    private final int              page;

    public PieceSelectMenu(Player p,
                           List<Material> workingList,
                           int pieceIndex,
                           List<Material> choices) {
        this(p, workingList, pieceIndex, choices, 0);
    }

    public PieceSelectMenu(Player p,
                           List<Material> workingList,
                           int pieceIndex,
                           List<Material> choices,
                           int page) {
        super(ROWS * 9, ColourCode.colour("&6Pick " + pieceLabel(pieceIndex)));
        this.session     = CreationManager.get(p.getUniqueId());
        this.workingList = workingList;
        this.pieceIndex  = pieceIndex;
        this.choices     = choices;
        this.page        = page;
        render();
    }

    @Override
    public void open(Player player) {
        render();
        super.open(player);
    }

    private static String pieceLabel(int idx) {
        return switch (idx) {
            case 0 -> "Helmet";
            case 1 -> "Chestplate";
            case 2 -> "Leggings";
            default -> "Boots";
        };
    }

    private void render() {
        inventory.clear();
        drawBorder();

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= choices.size()) break;
            int row = 1 + i / 7, col = 1 + i % 7, slot = row * 9 + col;
            Material m = choices.get(idx);
            inventory.setItem(slot,
                    ItemBuilder.CreateMultiLoreItem(
                            m, false,
                            ColourCode.colour("&e" + m.name()),
                            String.valueOf(List.of(ColourCode.colour("&7Click to select")))
                    )
            );
        }

        // prev/next page arrows
        if (page > 0) {
            inventory.setItem(PREV_SLOT,
                    ItemBuilder.CreateMultiLoreItem(
                            Material.ARROW, false,
                            ColourCode.colour("&a← Prev"),
                            String.valueOf(List.of(ColourCode.colour("&7Page " + page)))
                    )
            );
        }
        if ((page + 1) * PER_PAGE < choices.size()) {
            inventory.setItem(NEXT_SLOT,
                    ItemBuilder.CreateMultiLoreItem(
                            Material.ARROW, false,
                            ColourCode.colour("&aNext →"),
                            String.valueOf(List.of(ColourCode.colour("&7Page " + (page + 2))))
                    )
            );
        }

        //back
        inventory.setItem(
                CANCEL_SLOT,
                ItemBuilder.CreateCustomItem(
                        Material.BARRIER,
                        false,
                        ColourCode.colour("&cBack"),
                        ColourCode.colour("&7Return to main menu")
                )
        );
    }

    private void drawBorder() {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                Material pane = ((row + col) % 2 == 0
                        ? Material.ORANGE_STAINED_GLASS_PANE
                        : Material.RED_STAINED_GLASS_PANE);
                inventory.setItem(slot,
                        ItemBuilder.CreateCustomItem(pane, true, " ", null)
                );
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        int slot = e.getRawSlot();

        int maxPage = (choices.size() - 1) / PER_PAGE;

        if (slot == CANCEL_SLOT) {
            MenuHistory.goBack(p);
            return;
        }

        if (slot == PREV_SLOT && page > 0) {
            new PieceSelectMenu(p, workingList, pieceIndex, choices, page - 1)
                    .open(p);
            return;
        }

        if (slot == NEXT_SLOT && page < maxPage) {
            new PieceSelectMenu(p, workingList, pieceIndex, choices, page + 1)
                    .open(p);
            return;
        }

        int row = slot / 9, col = slot % 9;
        if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
            int idx = page * PER_PAGE + (row - 1) * 7 + (col - 1);
            if (idx < choices.size()) {
                workingList.set(pieceIndex, choices.get(idx));
                session.setArmour(workingList.stream().filter(Objects::nonNull).toList());
                MenuHistory.goBack(p);
            }
        }
    }
}