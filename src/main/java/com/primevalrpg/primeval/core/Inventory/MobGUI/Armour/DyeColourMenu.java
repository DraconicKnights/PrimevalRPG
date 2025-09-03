package com.primevalrpg.primeval.core.Inventory.MobGUI.Armour;

import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DyeColourMenu extends BaseMenu {
    private static final int ROWS      = 3;                    // 27 slots
    private static final int BACK_SLOT = (ROWS - 1) * 9 + 4;   // slot 22

    private static final Map<Material, Color> DYE_MAP = new LinkedHashMap<>();
    static {
        DYE_MAP.put(Material.WHITE_DYE,   Color.WHITE);
        DYE_MAP.put(Material.ORANGE_DYE,  Color.ORANGE);
        DYE_MAP.put(Material.MAGENTA_DYE, Color.MAROON);
        DYE_MAP.put(Material.LIGHT_BLUE_DYE, Color.AQUA);
        DYE_MAP.put(Material.YELLOW_DYE,  Color.YELLOW);
        DYE_MAP.put(Material.LIME_DYE,    Color.GREEN);
        DYE_MAP.put(Material.PINK_DYE,    Color.FUCHSIA);
        DYE_MAP.put(Material.GRAY_DYE,    Color.GRAY);
        DYE_MAP.put(Material.LIGHT_GRAY_DYE, Color.SILVER);
        DYE_MAP.put(Material.CYAN_DYE,    Color.TEAL);
        DYE_MAP.put(Material.PURPLE_DYE,  Color.PURPLE);
        DYE_MAP.put(Material.BLUE_DYE,    Color.BLUE);
        DYE_MAP.put(Material.BROWN_DYE,   Color.fromRGB(150, 75, 0));
        DYE_MAP.put(Material.GREEN_DYE,   Color.fromRGB(0, 100, 0));
        DYE_MAP.put(Material.RED_DYE,     Color.RED);
        DYE_MAP.put(Material.BLACK_DYE,   Color.BLACK);
    }

    private final CreationSession session;
    private final Material        piece;

    public DyeColourMenu(CreationSession session, Material piece) {
        super(ROWS*9, ColourCode.colour("&6 Choose Colour"));
        this.session = session;
        this.piece = piece;
        render();
    }

    private void render() {
        inventory.clear();
        int idx = 0;
        for (Map.Entry<Material,Color> e : DYE_MAP.entrySet()) {
            var mat = e.getKey();
            var col = e.getValue();
            inventory.setItem(idx++, ItemBuilder.CreateMultiLoreItem(
                    mat,false,
                    ColourCode.colour("&a"+mat.name()),
                    String.valueOf(List.of("§7Click to apply"))
            ));
        }
        inventory.setItem(BACK_SLOT, ItemBuilder.CreateCustomItem(
                Material.ARROW,false,
                ColourCode.colour("&cBack"),
                null
        ));
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
        if (slot >= 0 && slot < DYE_MAP.size()) {
            Material dye = (Material) DYE_MAP.keySet().toArray()[slot];
            Color   col = DYE_MAP.get(dye);
            session.getArmourColours().put(piece, col);
            MenuHistory.goBack(p);
        }
    }
}