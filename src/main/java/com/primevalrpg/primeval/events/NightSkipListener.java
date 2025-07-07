package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NightSkipListener implements Listener {

    // fraction of online players that must be sleeping to skip night (e.g. 0.5 = 50%)
    private static final double SLEEP_THRESHOLD = 0.5;

    // track sleeping players per world
    private final Map<World, Set<UUID>> sleeping = new HashMap<>();

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent evt) {
        if (evt.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        Player p = evt.getPlayer();
        World w = p.getWorld();

        sleeping.computeIfAbsent(w, __ -> new HashSet<>()).add(p.getUniqueId());
        announceProgress(w, p, true);
        trySkipNight(w);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent evt) {
        Player p = evt.getPlayer();
        World w = p.getWorld();

        Set<UUID> set = sleeping.get(w);
        if (set != null && set.remove(p.getUniqueId())) {
            announceProgress(w, p, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        Player p = evt.getPlayer();
        World w = p.getWorld();

        Set<UUID> set = sleeping.get(w);
        if (set != null && set.remove(p.getUniqueId())) {
            // someone disconnected while in bed
            announceProgress(w, p, false);
        }
    }

    private void announceProgress(World w, Player changed, boolean wentToBed) {
        int sleepingCount = sleeping.getOrDefault(w, Set.of()).size();
        int totalPlayers   = w.getPlayers().size();
        int stillNeeded    = (int)Math.ceil(SLEEP_THRESHOLD * totalPlayers) - sleepingCount;

        // if this is a "left the bed" event and we're already at or past the threshold,
        // don't spam a leave‐message—just return:
        if (!wentToBed && stillNeeded <= 0) {
            return;
        }

        String action = wentToBed ? "has gone to bed" : "left the bed";
        String progressMsg = String.format(
                "§7[%s] §e%s §7%s (§f%d§7/§f%d§7). %s",
                w.getName(),
                changed.getName(),
                action,
                sleepingCount,
                totalPlayers,
                stillNeeded > 0
                        ? ("§a" + stillNeeded + " more needed§7 to skip night.")
                        : "§aThreshold reached! Skipping night…"
        );

        Bukkit.getScheduler().runTaskLater(
                PrimevalRPG.getInstance(),
                () -> w.getPlayers().forEach(pl -> pl.sendMessage(progressMsg)),
                1L
        );
    }

    private void trySkipNight(World w) {
        int total = w.getPlayers().size();
        if (total == 0) return;

        int asleep = sleeping.getOrDefault(w, Set.of()).size();
        if ((double) asleep / total >= SLEEP_THRESHOLD) {
            // perform skip next tick (to allow bed events to settle)
            new BukkitRunnable() {
                @Override
                public void run() {
                    // clear weather
                    w.setStorm(false);
                    w.setThundering(false);
                    // set to morning (1000 ticks is dawn)
                    w.setTime(1000);

                    // wake up everyone
                    w.getPlayers().forEach(pl -> {
                        if (pl.isSleeping()) pl.wakeup(true);
                    });
                    // clear our tracker
                    sleeping.remove(w);
                }
            }.runTaskLater(PrimevalRPG.getInstance(), 2L);
        }
    }
}