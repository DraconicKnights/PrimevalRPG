package com.primevalrpg.primeval.core.Inventory.MobGUI.Armour;

import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Enchant.EnchantmentListMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

public class ArmourPieceMenu extends BaseMenu {

    private static final int ROWS      = 4;                    // 36 slots
    private static final int PREVIEW   = 13;
    private static final int COLOR_BTN = 11;
    private static final int ENCH_BTN  = 15;
    private static final int BACK_SLOT = (ROWS - 1) * 9 + 4;   // slot 31

    private final CreationSession session;
    private final Material piece;

    public ArmourPieceMenu(CreationSession session, Material piece) {
        super(ROWS*9, ColourCode.colour("&6 Configure " + piece.name()));
        this.session = session;
        this.piece = piece;
        render();
    }

    private void render() {
        inventory.clear();
        ItemBuilder.CreateCustomItem(piece, false,
                ColourCode.colour("&e"+piece.name()),
                null);
        inventory.setItem(PREVIEW,
                session.getArmourColours().containsKey(piece)
                        ? dyedPreview()
                        : ItemBuilder.CreateCustomItem(piece,false,
                        ColourCode.colour("&e"+piece.name()),null)
        );

        if (piece.name().startsWith("LEATHER_")) {
            inventory.setItem(COLOR_BTN, ItemBuilder.CreateMultiLoreItem(
                    Material.LIME_DYE, false,
                    ColourCode.colour("&bSet Colour"),
                    String.valueOf(List.of("§7Current: "+
                            session.getArmourColours().getOrDefault(piece, null)))
            ));
        }
        inventory.setItem(ENCH_BTN, ItemBuilder.CreateMultiLoreItem(
                Material.ENCHANTED_BOOK,false,
                ColourCode.colour("&bEnchants"),
                String.valueOf(List.of("§7Click to add/remove"))
        ));
        inventory.setItem(BACK_SLOT, ItemBuilder.CreateCustomItem(
                Material.ARROW,false,
                ColourCode.colour("&cBack"),
                null
        ));
    }

    private org.bukkit.inventory.ItemStack dyedPreview() {
        var is = new org.bukkit.inventory.ItemStack(piece);
        LeatherArmorMeta meta = (LeatherArmorMeta)is.getItemMeta();
        meta.setColor(session.getArmourColours().get(piece));
        is.setItemMeta(meta);
        return is;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            MenuHistory.goBack(p);
        }
        else if (slot == COLOR_BTN && piece.name().startsWith("LEATHER_")) {
            MenuHistory.open(p, new DyeColourMenu(session, piece));
        }
        else if (slot == ENCH_BTN) {
            MenuHistory.open(p,
                    new EnchantmentListMenu(p, session, piece)
            );
        }
    }
}