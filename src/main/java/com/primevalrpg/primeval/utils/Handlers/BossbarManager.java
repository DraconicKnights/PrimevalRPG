package com.primevalrpg.primeval.utils.Handlers;

import java.util.*;
import org.bukkit.boss.BossBar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Tracks bossbars by entity UUID and bar-id.
 */
public class BossbarManager {
    // barId → BossBar instance
    private static final Map<String, BossBar> bars = new ConcurrentHashMap<>();
    // entityUUID → list of barIds
    private static final Map<UUID, List<String>> entityBars = new ConcurrentHashMap<>();

    public static void registerBar(UUID entityId, String barId, BossBar bar) {
        bars.put(barId, bar);
        entityBars.computeIfAbsent(entityId, k -> new ArrayList<>()).add(barId);
    }

    public static void removeAllForEntity(UUID entityId) {
        List<String> ids = entityBars.remove(entityId);
        if (ids == null) return;
        for (String id : ids) {
            BossBar bar = bars.remove(id);
            if (bar != null) {
                bar.removeAll();
                bar.setVisible(false);
            }
        }
    }
}