package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.RPGMobs.BossMob;
import com.primevalrpg.primeval.core.CustomEvents.CustomEntityEvent;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.Handlers.Region;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * EntitySpawn Event that checks and deals with events related to the Custom Mob Manager
 */
@Events
public class CustomEntitySpawnEvent implements Listener {

    @EventHandler
    public void onCreatureDestroyed(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (CustomEntityArrayHandler.getCustomEntities().containsKey(entity)) {
            CustomMob customMob = CustomEntityArrayHandler.getCustomEntities().get(entity);
            CustomEntityArrayHandler.getCustomEntities().remove(entity);

            if (customMob != null) {
                // Decrease the count of current mobs in the region
                Region region = customMob.getRegion();
                if(region != null){
                    region.setCurrentMobs(region.getCurrentMobs() - 1);
                }

                List<ItemStack> lootToDrop = customMob.getLootTable().rollLoot();
                // Log the loot to be dropped
                PrimevalRPG.getInstance().CustomMobLogger("Loot to be dropped: " + lootToDrop.toString(), LoggerLevel.DEBUG);

                List<ItemStack> drops = event.getDrops();
                drops.clear();
                drops.addAll(lootToDrop);

            } else {
                PrimevalRPG.getInstance().CustomMobLogger("CustomMob is null", LoggerLevel.ERROR);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!CustomEntityArrayHandler.getCustomEntities().containsKey(event.getEntity())) return;

        CustomMob customMob = CustomEntityArrayHandler.getCustomEntities().get(event.getEntity());
        LivingEntity entity = (LivingEntity) event.getEntity();

        String elemColor;
        String icon;
        switch (customMob.getElementType()) {
            case FIRE:  elemColor = "&c"; icon = "🔥"; break;
            case WATER: elemColor = "&b"; icon = "💧"; break;
            case EARTH: elemColor = "&2"; icon = "⛰"; break;
            case WIND:  elemColor = "&7"; icon = "💨"; break;
            default:    elemColor = "&f"; icon = "";   break;
        }

        double damage        = event.getFinalDamage();
        double currentHealth = Math.max(entity.getHealth() + entity.getAbsorptionAmount() - damage, 0);
        double maxHealth     = customMob.getMaxHealth();

        int barLength = 7;
        int filled    = (int) Math.ceil((currentHealth / maxHealth) * barLength);
        filled = Math.max(0, Math.min(filled, barLength));
        int empty     = barLength - filled;

        String greenPart = "|".repeat(filled);
        String redPart   = "|".repeat(empty);
        String healthBar = String.format("&5[&a%s&c%s&5]&r", greenPart, redPart);

        // determine tier color and display text
        int lvl = customMob.getLevel();
        String tierColor;
        String levelDisplay;
        if (lvl <= 10) {
            tierColor = "&f"; // white
            levelDisplay = String.valueOf(lvl);
        } else if (lvl <= 20) {
            tierColor = "&a"; // green
            levelDisplay = String.valueOf(lvl);
        } else if (lvl <= 30) {
            tierColor = "&6"; // orange
            levelDisplay = String.valueOf(lvl);
        } else if (lvl <= 40) {
            tierColor = "&5"; // purple
            levelDisplay = String.valueOf(lvl);
        } else if (lvl <= 50) {
            tierColor = "&b"; // light blue
            levelDisplay = String.valueOf(lvl);
        } else {
            // skull icon
            if (customMob.getChampion()) {
                tierColor ="&4";
            } else {
                tierColor = "&c"; // dark red
            }
            levelDisplay = "☠"; // skull icon
        }
        String levelPart = String.format("%s[%s]&r", tierColor, levelDisplay);

        // rebuild the name with tier colors
        String customName = String.format(
                "%s%s %s %s %s",
                elemColor, icon, levelPart, customMob.getName(), healthBar
        );

        entity.setCustomName(ColourCode.colour(customName));
        entity.setCustomNameVisible(true);
    }

    @EventHandler
    public void onZombieDrown(EntityTransformEvent event) {
        Entity entity = event.getEntity();

        // only cancel event for zombies within the custom entity group transforming to drowned

        if (!CustomEntityArrayHandler.getCustomEntities().containsKey(entity)) return;

        if (entity instanceof Zombie && event.getTransformReason() == EntityTransformEvent.TransformReason.DROWNED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPiglinZombie(EntityTransformEvent event) {
        Entity entity = event.getEntity();

        if (!CustomEntityArrayHandler.getCustomEntities().containsKey(entity)) return;

        if (entity instanceof Piglin && event.getTransformReason() == EntityTransformEvent.TransformReason.PIGLIN_ZOMBIFIED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCustomEntity(CustomEntityEvent customEntityEvent) {
        CustomMob customMob = customEntityEvent.getCustomMob();

        PrimevalRPG.getInstance().CustomMobLogger("Entity: " + customMob.getEntityType() + " Has spawned near player: " + customEntityEvent.getPlayer().getName() + " Type: " + customMob.getName(), LoggerLevel.DEBUG);
    }

    private int calculateMinSpawnDistance(Player player) {
        if (player.getWorld().getTime() > 13000 && player.getWorld().getTime() < 23000) {
            return MobDataHandler.minDistance / 2;
        }
        return MobDataHandler.minDistance;
    }

    private int calculateMaxSpawnDistance(Player player) {
        int numPlayers = Bukkit.getOnlinePlayers().size();
        return MobDataHandler.maxDistance + numPlayers * 10;
    }

    private int calculateSpawnChance(int baseChance, double distance) {
        float distanceEffect = 0.8f;

        //distance affects spawn chance now
        int distanceAdjustedChance = (int) (baseChance * Math.pow(distanceEffect, distance));

        //Make sure we return a positive number.
        if (distanceAdjustedChance <= 0) {
            return 1;
        }
        else {
            return distanceAdjustedChance;
        }
    }

}
