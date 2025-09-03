package com.primevalrpg.primeval.core.RPGMobs.listeners;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.utils.Handlers.BossbarManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@Events
public class BossBarCleanupListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Clean up any bossbars tied to this entity
        BossbarManager.removeAllForEntity(event.getEntity().getUniqueId());
    }

}
