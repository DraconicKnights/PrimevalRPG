package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.MobListMenu;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


@Events
public class PlayerInventoryEvents implements Listener {

    @EventHandler
    public void onInventoryClickMenu(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseMenu)) return;
        event.setCancelled(true);
        BaseMenu menu = (BaseMenu) event.getInventory().getHolder();
        menu.onClick(event);
    }
}
