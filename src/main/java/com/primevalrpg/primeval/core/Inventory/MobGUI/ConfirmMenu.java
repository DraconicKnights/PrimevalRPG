package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ConfirmMenu extends BaseMenu {

    private final Runnable onConfirm, onCancel;

    /**
     * @param title       the inventory title
     * @param confirmItem the green-wool (or whatever) item at slot 11
     * @param cancelItem  the red-wool (or whatever) item at slot 15
     * @param onConfirm   code to run if player clicks slot 11
     * @param onCancel    code to run if player clicks slot 15
     */
    public ConfirmMenu(String title,
                       ItemStack confirmItem,
                       ItemStack cancelItem,
                       Runnable onConfirm,
                       Runnable onCancel) {
        super(3 * 9, title);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        inventory.setItem(11, confirmItem);
        inventory.setItem(15, cancelItem);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player p = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 11) {
            onConfirm.run();
        } else if (slot == 15) {
            onCancel.run();
        }

        p.updateInventory();
    }

}
