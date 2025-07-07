package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.PlayerDataHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Player Level listener
 * Adjusts the players custom XP via event actions and scales based on each time and a multiplier within the Player YML
 */
@Events
public class PlayerLevelListener implements Listener {

    // 1) Ensure data exists when they join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        Player p = ev.getPlayer();
        PlayerDataManager mgr = PlayerDataManager.getInstance();
        if (mgr.getPlayerData(p.getUniqueId()) == null) {
            PlayerData data = mgr.loadPlayerData(p.getUniqueId());
            mgr.addPlayerData(p.getUniqueId(), data);
        }
    }

    // 2) When they kill a mob
    @EventHandler
    public void onEntityKill(EntityDeathEvent ev) {
        if (!(ev.getEntity().getKiller() instanceof Player)) return;
        Player p = ev.getEntity().getKiller();
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null) return;
        int xpGain = PlayerDataHandler.MOB_KILL_XP * getMobExperienceMultiplier(ev.getEntity());
        data.onMobKill(p, xpGain);
        PlayerDataManager.getInstance().savePlayerData(data);
    }

    // 3) When they break a block
    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev) {
        Player p = ev.getPlayer();
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null) return;
        int xpGain = PlayerDataHandler.BLOCK_BREAK_XP * getBlockExperienceMultiplier(ev.getBlock());
        data.onBlockMine(p, xpGain);
        PlayerDataManager.getInstance().savePlayerData(data);
    }

    private int getMobExperienceMultiplier(LivingEntity entity) {
        if (entity instanceof EnderDragon || entity instanceof Wither) {
            return 10;
        } else if (CustomEntityArrayHandler.getCustomEntities().containsKey(entity)) {
            CustomMob customMob = CustomEntityArrayHandler.getCustomEntities().get(entity);
            // Custom mobs grant XP equal to their level
            return customMob.getLevel();
        }
        return 1;
    }

    private int getBlockExperienceMultiplier(Block block) {
        Material type = block.getType();
        if(type == Material.DIAMOND_ORE || type == Material.EMERALD_ORE) {
            return 5;
        } else if(type == Material.COAL_ORE || type == Material.IRON_ORE) {
            return 2;
        }
        return 1;
    }
}
