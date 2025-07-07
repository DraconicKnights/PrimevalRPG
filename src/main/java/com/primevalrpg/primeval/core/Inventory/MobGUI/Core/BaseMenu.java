package com.primevalrpg.primeval.core.Inventory.MobGUI.Core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class BaseMenu implements InventoryHolder {

    protected final Inventory inventory;

    /**
     * Creates a new BaseMenu with the specified size and title.
     *
     * @param size  inventory size (must be a multiple of 9)
     * @param title inventory title
     */
    public BaseMenu(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Opens this menu for the specified player.
     *
     * @param player the player to open the menu for
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Helper for placing an item in this menu.
     *
     * @param slot  the slot index
     * @param item  the ItemStack to set
     */
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Abstract method to handle click events on this menu.
     *
     * @param event the InventoryClickEvent
     */
    public abstract void onClick(InventoryClickEvent event);

}
