package com.primevalrpg.primeval.runnables;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Runnable;
import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import com.primevalrpg.primeval.utils.Runnable.RunnableCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Runnable
public class SpawnAroundPlayerRunable extends RunnableCore {

    private static final int   MAX_PER_PLAYER    = 8;
    private static final int    MAX_LOCATION_ATTEMPTS   = 10;

    private static final int MAX_SPAWN_LIGHT_LEVEL = 12;

    private static final int    CAVE_SURFACE_BUFFER  = 5;
    private static final int    CAVE_DEPTH_MARGIN    = 5;

    private final Random rng = new Random();
    private final int minDistance;
    private final int maxDistance;

    public SpawnAroundPlayerRunable() {
        super(PrimevalRPG.getInstance(), 60, 5);
        this.minDistance = MobDataHandler.minDistance;
        this.maxDistance = MobDataHandler.maxDistance;
        this.startTimedTask();
    }

    @Override
    protected void event() {
        if (!CustomEntityData.getInstance().isCustomMobsEnabled()) {
            return;
        }

        Logger log = Bukkit.getLogger();
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            World.Environment here = world.getEnvironment();

            //RPGLogger.get().debug("mobSpawnChance = " + MobDataHandler.mobSpawnChance);
            if (rng.nextDouble() * 100 > MobDataHandler.mobSpawnChance) {
                //RPGLogger.get().debug("Skipping spawn for " + player.getName() + " due to spawn chance");
                continue;
            }

            // 2) Build allowed‐mob list
            List<CustomMob> allowed = CustomEntityArrayHandler.getRegisteredCustomMobs()
                    .values()
                    .stream()
                    .filter(m -> !m.getChampion())
                    .filter(m -> {
                        List<World.Environment> dims = m.getSpawnDimensions();
                        return dims.isEmpty() || dims.contains(here);
                    })
                    .toList();
            if (allowed.isEmpty()) {
                RPGLogger.get().debug("Skipping spawn for " + player.getName() + " due to spawn dimensions");
                continue;
            }

            // 3) Pick one mob at random
            CustomMob template = allowed.get(rng.nextInt(allowed.size()));
            RPGLogger.get().debug("Picked " + template.getName() + " for " + player.getName());

            // 4) Try a few candidate locations
            boolean spawned = false;
            for (int attempt = 1; attempt <= MAX_LOCATION_ATTEMPTS; attempt++) {

                // 4a) Random polar around player
                double angle = rng.nextDouble() * 2 * Math.PI;
                double dist  = minDistance + rng.nextDouble() * (maxDistance - minDistance);
                int x = player.getLocation().getBlockX() + (int)(Math.cos(angle) * dist);
                int z = player.getLocation().getBlockZ() + (int)(Math.sin(angle) * dist);
                RPGLogger.get().debug("Attempting " + template.getName() + " at " + x + "," + z);

                // 4b) Compute surface height
                int surfaceY = world.getHighestBlockYAt(x, z);

                // 4c) Decide cave vs. surface/Nether
                boolean tryCave = here == World.Environment.NORMAL
                        && rng.nextDouble() * 100 < MobDataHandler.caveSpawnChance
                        && (surfaceY - (world.getMinHeight() + CAVE_DEPTH_MARGIN)) > CAVE_SURFACE_BUFFER;

                RPGLogger.get().debug("SurfaceY=" + surfaceY + ", tryCave=" + tryCave);

                int y;
                if (tryCave) {
                    int minY = world.getMinHeight() + CAVE_DEPTH_MARGIN;
                    int maxY = surfaceY - CAVE_SURFACE_BUFFER;
                    y = minY + rng.nextInt(maxY - minY + 1);
                } else if (here == World.Environment.NETHER) {
                    int minY = world.getMinHeight() + CAVE_DEPTH_MARGIN;
                    int maxY = surfaceY - CAVE_SURFACE_BUFFER;
                    maxY = Math.max(minY, maxY);
                    y = minY + rng.nextInt(maxY - minY + 1);
                } else {
                    y = surfaceY + 1;
                }

                RPGLogger.get().debug("Y=" + y);

                Location spawnLoc = new Location(world, x + 0.5, y, z + 0.5);
                int finalAttempt = attempt;
                log.fine(() -> String.format("Attempt %d for %s at %s cave=%b",
                        finalAttempt, template.getName(), spawnLoc, tryCave));

                // 4d) Light check (only enforce on non-cave)
                if (!tryCave) {
                    int threshold = (here == World.Environment.NETHER) ? 15 : MAX_SPAWN_LIGHT_LEVEL;
                    if (spawnLoc.getBlock().getLightLevel() > threshold) {
                        RPGLogger.get().debug("Skipping spawn due to light level");
                        continue;
                    }
                }

                RPGLogger.get().debug("Final spawnLoc=" + spawnLoc);

                // 4e) Block‐and‐passability checks
                Block floor = spawnLoc.getBlock().getRelative(BlockFace.DOWN);
                Block mid   = spawnLoc.getBlock();
                Block head  = mid.getRelative(BlockFace.UP);
                Block head2 = head.getRelative(BlockFace.UP);

                if (floor.getType() == Material.WATER
                        || mid.getType()   == Material.WATER
                        || head.getType()  == Material.WATER
                        || !floor.getType().isSolid()
                        || !mid.isPassable()
                        || !head.isPassable()
                        || !head2.isPassable()) {
                    RPGLogger.get().debug("Skipping spawn due to block/passability");
                    continue;
                }
                if (mid.getType() == Material.AIR) {
                    Block d1 = mid.getRelative(BlockFace.DOWN);
                    Block d2 = d1.getRelative(BlockFace.DOWN);
                    if (!(d1.getType().isSolid() || d2.getType().isSolid())) {
                        RPGLogger.get().debug("Skipping spawn due to block/passability");
                        continue;
                    }
                }

                // 4f) Distance‐based champ
                double actualDist = spawnLoc.distance(player.getLocation());
                int spawnChance = calculateSpawnChance(template.getSpawnChance(), actualDist);
                if (rng.nextDouble() * 100 > spawnChance) {
                    RPGLogger.get().debug("Skipping spawn due to distance");
                    continue;
                }

                // 4g) Nearby‐limit
                long nearby = CustomEntityArrayHandler.getCustomEntities().keySet().stream()
                        .map(Entity::getLocation)
                        .filter(loc -> loc.getWorld().equals(world))
                        .filter(loc -> loc.distance(player.getLocation()) <= maxDistance)
                        .count();
                if (nearby >= MAX_PER_PLAYER) {
                    RPGLogger.get().debug("Skipping spawn due to nearby limit");
                    continue;
                }

                // 5) Spawn and fire event
                CustomMobManager.getInstance().setMobLevelAndSpawn(player, template, spawnLoc);
                PrimevalRPG.getInstance().TriggerCustomEvent(player, template);
                RPGLogger.get().debug("Spawned " + template.getName() + " at " + spawnLoc);
                spawned = true;
                break;
            }

            // 6) Only one per tick per player
            if (spawned) {
                break;
            }
        }
    }

    private int countCustomMobsNear(Player player) {
        double r = maxDistance;
        return (int) player.getNearbyEntities(r, r, r).stream()
                .filter(this::isCustomMobEntity)
                .count();
    }

    private boolean isCustomMobEntity(Entity e) {
        // check in the active‐entities map
        return CustomEntityArrayHandler.getCustomEntities().containsKey(e);
    }

    /** Adjust your fall‐off formula here. */
    private int calculateSpawnChance(int baseChance, double distance) {
        double factor = 1.0 - (distance - minDistance) / (maxDistance - minDistance);
        int boosted = (int)(baseChance * (0.5 + 0.5 * factor));
        return Math.min(100, boosted);
    }



}
