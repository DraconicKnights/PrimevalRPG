package com.primevalrpg.primeval.core.Inventory.MobGUI.Armour;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.Desing.MobHeads;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

// Armor menu planned to be re-worked for enchantment and additional data support

/**
 * Mob GUI Armour Menu
 * This is used for assigning armour sets and values to the mob
 */
public class ArmourMenu extends BaseMenu {
    private static final int ROWS     = 3;
    private static final String TITLE = ColourCode.colour("&3&lConfigure Armour");

    // pick-slot indices (row 1)
    private static final int SLOT_HELM  = 1*9 + 2;  // 11
    private static final int SLOT_CHEST = 1*9 + 4;  // 13
    private static final int SLOT_LEGS  = 1*9 + 6;  // 15
    private static final int SLOT_BOOTS = 1*9 + 8;  // 17

    // remove-slot indices (row 2) for better placement
    private static final int REMOVE_HELM  = 2*9 + 2; // 20
    private static final int REMOVE_CHEST = 2*9 + 4; // 22
    private static final int REMOVE_LEGS  = 2*9 + 6; // 24
    private static final int REMOVE_BOOTS = 2*9 + 8; // 26

    // back-button in bottom row. might re-work all buttons to be a priority of bottom left - would need to re-design some menus for working logic.
    private static final int BACK_SLOT = 2*9 + 1;      // 19

    private static final List<Material> ALL_PIECES;

    static {
        List<Material> temp = new ArrayList<>(Arrays.asList(
                Material.LEATHER_HELMET,    Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS,  Material.LEATHER_BOOTS,
                Material.GOLDEN_HELMET,     Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS,   Material.GOLDEN_BOOTS,
                Material.CHAINMAIL_HELMET,  Material.CHAINMAIL_CHESTPLATE,
                Material.CHAINMAIL_LEGGINGS,Material.CHAINMAIL_BOOTS,
                Material.IRON_HELMET,       Material.IRON_CHESTPLATE,
                Material.IRON_LEGGINGS,     Material.IRON_BOOTS,
                Material.DIAMOND_HELMET,    Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,  Material.DIAMOND_BOOTS,
                Material.NETHERITE_HELMET,  Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,Material.NETHERITE_BOOTS,
                Material.TURTLE_HELMET,
                Material.CARVED_PUMPKIN,    Material.JACK_O_LANTERN
        ));

        temp.addAll(MobHeads.getFullBlocks());
        temp.addAll(MobHeads.getMobHeads());

        ALL_PIECES = Collections.unmodifiableList(temp);
    }



    private static final List<Material> HELMETS;
    static {
        Set<Material> blockHeadMaterials = new HashSet<>(MobHeads.getFullBlocks());
        Set<Material> headMaterials = new HashSet<>(MobHeads.getMobHeads());

        HELMETS = ALL_PIECES.stream()
                .filter(m ->
                        m.name().endsWith("_HELMET")
                                || blockHeadMaterials.contains(m)
                                || headMaterials.contains(m)
                                || m == Material.CARVED_PUMPKIN
                                || m == Material.JACK_O_LANTERN
                )
                .toList();
    }

    private static final List<Material> CHESTPLATES = ALL_PIECES.stream()
            .filter(m -> m.name().endsWith("_CHESTPLATE"))
            .toList();
    private static final List<Material> LEGGINGS = ALL_PIECES.stream()
            .filter(m -> m.name().endsWith("_LEGGINGS"))
            .toList();
    private static final List<Material> BOOTS = ALL_PIECES.stream()
            .filter(m -> m.name().endsWith("_BOOTS"))
            .toList();

    private final CreationSession session;
    private final List<Material>   workingList;

    public ArmourMenu(Player p) {
        super(ROWS * 9, TITLE);
        this.session     = CreationManager.get(p.getUniqueId());
        this.workingList = initWorkingList(session.getArmourPieces());
        render();
    }

    @Override
    public void open(Player player) {
        render();
        super.open(player);
    }

    private List<Material> initWorkingList(List<Material> saved) {
        List<Material> list = new ArrayList<>(Arrays.asList(null, null, null, null));
        if (saved != null) {
            for (int i = 0; i < Math.min(4, saved.size()); i++) {
                list.set(i, saved.get(i));
            }
        }
        return list;
    }

    private void render() {
        inventory.clear();
        drawBorder();

        // top row of picks
        drawSlot(SLOT_HELM,  0, "Helmet");
        drawSlot(SLOT_CHEST, 1, "Chestplate");
        drawSlot(SLOT_LEGS,  2, "Leggings");
        drawSlot(SLOT_BOOTS, 3, "Boots");

        // second row of removes
        drawRemove(REMOVE_HELM,  0, "Helmet");
        drawRemove(REMOVE_CHEST, 1, "Chestplate");
        drawRemove(REMOVE_LEGS,  2, "Leggings");
        drawRemove(REMOVE_BOOTS, 3, "Boots");
        
        // 4) Back
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

    private void drawSlot(int slot, int idx, String label) {
        Material mat = workingList.get(idx);
        boolean empty = (mat == null);
        inventory.setItem(slot,
                ItemBuilder.CreateMultiLoreItem(
                        empty ? Material.GRAY_STAINED_GLASS_PANE : mat,
                        false,
                        ColourCode.colour(empty ? "&7" + label : "&e" + label),
                        String.valueOf(List.of(ColourCode.colour(
                                empty ? "&7Click to choose" : "&7Click to change"
                        )))
                )
        );
    }

    private void drawRemove(int slot, int idx, String label) {
        boolean has = Objects.nonNull(workingList.get(idx));
        inventory.setItem(slot,
                ItemBuilder.CreateMultiLoreItem(
                        has ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE,
                        false,
                        ColourCode.colour(has ? "&cRemove " + label : "&7No " + label),
                        String.valueOf(List.of(ColourCode.colour(
                                has ? "&7Click to remove" : "&7Nothing to remove"
                        )))
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

        if (slot == BACK_SLOT) {
            MenuHistory.goBack(p);
            return;
        }
        if (slot == SLOT_HELM) {
            MenuHistory.open(p, new PieceSelectMenu(p, workingList, 0, HELMETS));
            return;
        }
        if (slot == SLOT_CHEST) {
            MenuHistory.open(p, new PieceSelectMenu(p, workingList, 1, CHESTPLATES));
            return;
        }
        if (slot == SLOT_LEGS) {
            MenuHistory.open(p, new PieceSelectMenu(p, workingList, 2, LEGGINGS));
            return;
        }
        if (slot == SLOT_BOOTS) {
            MenuHistory.open(p, new PieceSelectMenu(p, workingList, 3, BOOTS));
            return;
        }
        if (slot == REMOVE_HELM || slot == REMOVE_CHEST
                || slot == REMOVE_LEGS || slot == REMOVE_BOOTS) {
            int idx = switch(slot) {
                case REMOVE_HELM  -> 0;
                case REMOVE_CHEST -> 1;
                case REMOVE_LEGS  -> 2;
                default            -> 3;
            };
            workingList.set(idx, null);
            session.setArmour(workingList.stream().filter(Objects::nonNull).toList());
            render();
        }
    }
}