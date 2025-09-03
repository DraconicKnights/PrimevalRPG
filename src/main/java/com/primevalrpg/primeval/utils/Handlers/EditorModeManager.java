package com.primevalrpg.primeval.utils.Handlers;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.EditorAction;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EditorModeManager {
    private static Set<UUID> editorPlayers = new HashSet<>();
    private static final Map<UUID, Region> playerRegions = new HashMap<>();
    private static Map<UUID, EditorAction> playerActions = new HashMap<>();
    private static Map<UUID, Integer> particleTasks = new HashMap<>();

    public static void enterEditorMode(Player player) {
        editorPlayers.add(player.getUniqueId());
        player.sendMessage(ChatColor.AQUA + "You have entered Editor Mode.");

        // Give player tools for selection and mob spawning
        ItemStack selectionWand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta wandMeta = selectionWand.getItemMeta();
        wandMeta.setDisplayName(ChatColor.GREEN + "Region Selector");
        selectionWand.setItemMeta(wandMeta);

        ItemStack regionNameTool = new ItemStack(Material.ANVIL);
        ItemMeta regionNameToolItemMeta = regionNameTool.getItemMeta();
        regionNameToolItemMeta.setDisplayName(ChatColor.GREEN + "Region Name Tool");
        regionNameTool.setItemMeta(regionNameToolItemMeta);

        ItemStack mobSpawnerTool = new ItemStack(Material.CHEST);
        ItemMeta mobToolMeta = mobSpawnerTool.getItemMeta();
        mobToolMeta.setDisplayName(ChatColor.YELLOW + "Mob Spawn Tool");
        mobSpawnerTool.setItemMeta(mobToolMeta);

        ItemStack levelSetTool = new ItemStack(Material.DIAMOND);
        ItemMeta levelSetToolMeta = levelSetTool.getItemMeta();
        levelSetToolMeta.setDisplayName(ChatColor.GREEN + "Level Tool");
        levelSetTool.setItemMeta(levelSetToolMeta);

        ItemStack saveTool = new ItemStack(Material.NETHER_STAR);
        ItemMeta saveToolMeta = saveTool.getItemMeta();
        saveToolMeta.setDisplayName(ChatColor.GOLD + "Save Region");
        saveTool.setItemMeta(saveToolMeta);

        player.getInventory().clear();
        player.getInventory().setItem(0, selectionWand);
        player.getInventory().setItem(2, regionNameTool);
        player.getInventory().setItem(4, mobSpawnerTool);
        player.getInventory().setItem(6, levelSetTool);
        player.getInventory().setItem(8, saveTool);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Region region : RegionManager.getInstance().getRegions()) {
                    displayRegionOutline(player, region);
                }
            }
        };
        runnable.runTaskTimer(PrimevalRPG.getInstance(), 0, 20);

        particleTasks.put(player.getUniqueId(), runnable.getTaskId());
    }

    public static Region getPlayerRegion(Player player) {
        return playerRegions.computeIfAbsent(player.getUniqueId(), p -> new Region());
    }

    public static void setRegionCorner(Player player, Location location, boolean isFirstCorner) {
        Region region = getPlayerRegion(player);
        if (isFirstCorner) {
            region.setFirstCorner(location);
            player.sendMessage(ChatColor.GREEN + "First corner set at " + formatLocation(location));
        } else {
            region.setSecondCorner(location);
            player.sendMessage(ChatColor.GREEN + "Second corner set at " + formatLocation(location));
        }
        RegionManager.getInstance().addRegion(region);
    }

    public static void setRegionName(Player player, String name) {
        Region region = getPlayerRegion(player);
        region.setRegionName(name);
        player.sendMessage(ChatColor.GREEN + "Region name has been set");
    }

    public static void setRegionMobs(Player player) {
        Region region = getPlayerRegion(player);
        List<CustomMob> mobList = new ArrayList<>(CustomEntityArrayHandler.getRegisteredCustomMobs().values());
        region.setSpawnableMobs(mobList);
        player.sendMessage(ChatColor.GREEN + "Region mobs set");
    }

    public static void setRegionLevels(Player player, int minLevel, int maxLevel) {
        Region region = getPlayerRegion(player);
        region.setMinLevel(minLevel);
        region.setMaxLevel(maxLevel);
        player.sendMessage(ChatColor.GREEN + "Region level set");
    }

    public static void saveRegion(Player player) {
        Region region = getPlayerRegion(player);
        RegionManager.getInstance().saveRegion(region);
        player.sendMessage(ChatColor.GOLD + "Region data has been saved successfully");
    }

    public static void setPlayerAction(Player player, EditorAction action) {
        playerActions.put(player.getUniqueId(), action);
    }

    public static EditorAction getPlayerAction(Player player) {
        return playerActions.getOrDefault(player.getUniqueId(), EditorAction.NONE);
    }

    public static void exitEditorMode(Player player) {
        editorPlayers.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "You have exited Editor Mode.");
        player.getInventory().clear();

        if (particleTasks.containsKey(player.getUniqueId())) {
            int taskId = particleTasks.get(player.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskId);
            particleTasks.remove(player.getUniqueId());
        }
    }

    public static boolean isInEditorMode(Player player) {
        return editorPlayers.contains(player.getUniqueId());
    }

    private static String formatLocation(Location location) {
        return String.format("(%d, %d, %d)", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static void displayRegionOutline(Player player, Region region) {
        Location firstCorner = region.getFirstCorner();
        Location secondCorner = region.getSecondCorner();
        if (firstCorner == null || secondCorner == null) {
            return;
        }

        // Calculate min and max coordinates
        int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
        int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
        int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
        int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

        // Spawn particles at the edges of the region, forming an outline
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    Location loc = new Location(player.getWorld(), x, firstCorner.getY(), z);
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0, 0, 0, 0.0);
                }
            }
        }
    }
}
