package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.CustomEvents.CustomMobCombatEvent;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class CombatIntegrationListener implements Listener {

    private static final double LEVEL_SCALING_FACTOR = 0.05;

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        CustomMob customMob = CustomEntityArrayHandler.getCustomEntities().get(damager);

        if (customMob != null && event.getEntity() instanceof LivingEntity target) {
            double baseDamage = customMob.calculateDamage();

            double scaledDamage = baseDamage * (1 + customMob.getLevel() * LEVEL_SCALING_FACTOR);

            CustomMobCombatEvent combatEvent = new CustomMobCombatEvent(customMob, target, scaledDamage);
            Bukkit.getPluginManager().callEvent(combatEvent);

            if (combatEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            event.setDamage(combatEvent.getModifiedDamage());
        }
    }

    @EventHandler
    public void onEntityDeath(PlayerPortalEvent event) {

    }
}
