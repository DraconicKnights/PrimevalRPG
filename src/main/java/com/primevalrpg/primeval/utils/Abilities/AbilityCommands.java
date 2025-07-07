package com.primevalrpg.primeval.utils.Abilities;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.utils.API.ScriptCommandAPI;
import com.primevalrpg.primeval.utils.API.ScriptEventAPI;
import com.primevalrpg.primeval.utils.Scripts.DefaultExecutors;
import com.primevalrpg.primeval.utils.Scripts.ScriptCommand;
import com.primevalrpg.primeval.utils.Scripts.ScriptContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class AbilityCommands {

    public AbilityCommands() {
        registerCommands();
    }

    private void registerCommands() {
        buildCommandExecutionLogic();
    }

    private void buildCommandExecutionLogic() {
        ScriptCommandAPI.builder()
                .command("orbit")
                .alias("orbit", "orbital")
                .execute(this::execOrbit)
                .command("launchUp")
                .alias("launchUp", "soar")
                .execute(this::execLaunchUp)
                .command("shockwave")
                .execute(this::execShockwave)
                .register();

        //ScriptEventAPI.onEntityEvent(PrimevalRPG.getInstance(), CreatureSpawnEvent.class, List.of("actionbar text='§5The End Portal glows with cosmic energy…' @self"));
    }

    private void execLaunchUp(ScriptCommand cmd, ScriptContext ctx) {
        double power = Double.parseDouble(
                cmd.args.getOrDefault("power", "1.0")
        );
        ctx.self.setVelocity(new Vector(0, power, 0));
        ctx.self.sendMessage("↑ Launched with power=" + power);
    }

    private void execOrbit(ScriptCommand cmd, ScriptContext ctx) {
        String typesArg = cmd.getArg("type", null);
        List<Material> materials = null;
        if (typesArg != null) {
            materials = Arrays.stream(typesArg.split("\\s*,\\s*"))
                    .map(s -> {
                        try { return Material.valueOf(s.toUpperCase()); }
                        catch (IllegalArgumentException ex) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (materials.isEmpty()) materials = null;
        }

        List<String> centerSel = Collections.singletonList(cmd.getArg("center", "@self"));
        Collection<LivingEntity> centres = DefaultExecutors.resolveEntities(
                centerSel, ctx.self, ctx.nearby, ctx.event);
        List<LivingEntity> orbiters = DefaultExecutors.resolveEntities(
                        cmd.targets, ctx.self, ctx.nearby, ctx.event)
                .stream().collect(Collectors.toList());

        double radius  = cmd.getDouble("radius", 3.0);
        double speed   = cmd.getDouble("speed", 0.1);
        long   interval= (long) cmd.getDouble("interval", 1);

        Plugin plugin = PrimevalRPG.getInstance();
        Random rnd = new Random();

        for (LivingEntity centre : centres) {
            World world = centre.getWorld();
            Location base = centre.getLocation().getBlock().getLocation();

            int totalRings  = (int)Math.ceil(radius);
            long totalTicks = totalRings * interval + 20; // +20 grace
            centre.addPotionEffect(new PotionEffect(
                    PotionEffectType.LEVITATION, (int)totalTicks, 1, false, false));

            Map<Location, BlockData> original = new HashMap<>();
            List<List<Location>> rings = new ArrayList<>();
            for (int d = 1; d <= totalRings; d++) {
                List<Location> ring = new ArrayList<>();
                int d2 = d*d, d2m = (d-1)*(d-1);
                for (int dx = -d; dx <= d; dx++) {
                    for (int dz = -d; dz <= d; dz++) {
                        int dist2 = dx*dx + dz*dz;
                        if (dist2 > d2 || dist2 < d2m) continue;
                        Location loc = base.clone().add(dx, 0, dz);
                        original.putIfAbsent(loc, loc.getBlock().getBlockData());
                        ring.add(loc);
                    }
                }
                rings.add(ring);
            }

            for (int i = 0; i < rings.size(); i++) {
                List<Location> ring = rings.get(i);
                List<Material> finalMaterials = materials;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Location loc : ring) {
                            BlockData bd = (finalMaterials != null)
                                    ? Bukkit.createBlockData(
                                    finalMaterials.get(rnd.nextInt(finalMaterials.size())))
                                    : original.get(loc);
                            FallingBlock fb = world.spawnFallingBlock(loc, bd);
                            fb.setDropItem(false);
                            fb.setMetadata("cmc_shockwave",
                                    new FixedMetadataValue(plugin, true));
                        }
                    }
                }.runTaskLater(plugin, i * interval);
            }

            long launchDelay = rings.size() * interval + 5;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Collection<LivingEntity> targets;
                    if (cmd.targets.contains("@nearest")) {
                        targets = orbiters.stream()
                                .min(Comparator.comparingDouble(e->e.getLocation()
                                        .distanceSquared(centre.getLocation())))
                                .map(Collections::singletonList)
                                .orElse(Collections.emptyList());
                    } else {
                        targets = orbiters;
                    }
                    if (targets.isEmpty()) {
                        centre.removePotionEffect(PotionEffectType.LEVITATION);
                        return;
                    }

                    for (Map.Entry<Location,BlockData> e : original.entrySet()) {
                        FallingBlock fb = world.spawnFallingBlock(
                                e.getKey().add(0, 1, 0),
                                e.getValue());
                        fb.setDropItem(false);
                        fb.setMetadata("cmc_shockwave",
                                new FixedMetadataValue(plugin, true));

                        // choose random target
                        LivingEntity tgt = targets.stream()
                                .skip(rnd.nextInt(targets.size()))
                                .findFirst().get();

                        Vector dir = tgt.getLocation().toVector()
                                .subtract(fb.getLocation().toVector())
                                .normalize()
                                .multiply(speed)
                                .setY(0.5);
                        fb.setVelocity(dir);
                    }

                    centre.removePotionEffect(PotionEffectType.LEVITATION);
                }
            }.runTaskLater(plugin, launchDelay);
        }
    }

    private void execShockwave(ScriptCommand cmd, ScriptContext ctx) {
        String typesArg = cmd.getArg("type", null);
        List<Material> materials = null;
        if (typesArg != null) {
            materials = Arrays.stream(typesArg.split("\\s*,\\s*"))
                    .map(s -> {
                        try { return Material.valueOf(s.toUpperCase()); }
                        catch (IllegalArgumentException ex) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (materials.isEmpty()) materials = null;
        }

        double rawInner = cmd.getDouble("innerRadius", 0.0);
        int outerRadius = (int) cmd.getDouble("radius", 8);
        int innerRadius;
        if (rawInner > 0 && rawInner < 1) {
            innerRadius = (int) Math.max(0, Math.round(rawInner * outerRadius));
        } else {
            innerRadius = (int) Math.round(rawInner);
        }
        innerRadius = Math.min(innerRadius, outerRadius);

        double speed  = cmd.getDouble("speed", 1.0);
        double lift   = cmd.getDouble("height", 1.5);
        long delay    = (long) cmd.getDouble("delay", 2);

        Location center = ctx.self.getLocation().getBlock().getLocation();
        World world     = center.getWorld();

        Map<Location, BlockData> original = new HashMap<>();
        List<List<Location>> rings = new ArrayList<>();
        for (int d = innerRadius; d <= outerRadius; d++) {
            List<Location> ring = new ArrayList<>();
            int d2  = d * d;
            int d2m = (d - 1) * (d - 1);
            for (int dx = -d; dx <= d; dx++) {
                for (int dz = -d; dz <= d; dz++) {
                    int dist2 = dx*dx + dz*dz;
                    if (dist2 > d2 || dist2 < d2m) continue;
                    Location loc = center.clone().add(dx, 0, dz);
                    original.put(loc, loc.getBlock().getBlockData());
                    ring.add(loc);
                }
            }
            rings.add(ring);
        }

        for (int i = 0; i < rings.size(); i++) {
            List<Location> ring = rings.get(i);
            List<Material> finalMaterials = materials;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Location loc : ring) {
                        BlockData origBd = original.get(loc);
                        // pick a material or use original
                        BlockData bd = finalMaterials != null
                                ? Bukkit.createBlockData(
                                finalMaterials.get(new Random().nextInt(finalMaterials.size()))
                        )
                                : origBd;
                        FallingBlock fb = world.spawnFallingBlock(loc, bd);
                        fb.setDropItem(false);

                        fb.setMetadata("cmc_shockwave", new FixedMetadataValue(PrimevalRPG.getInstance(), true));

                        Vector dir = loc.toVector().subtract(center.toVector());
                        if (dir.lengthSquared() == 0) continue;
                        dir.normalize().multiply(speed).setY(lift);
                        fb.setVelocity(dir);

                        loc.getBlock().setType(Material.AIR, false);
                    }
                }
            }.runTaskLater(PrimevalRPG.getInstance(), i * delay);
        }

        long restoreTick = rings.size() * delay + 5;
        new BukkitRunnable() {
            @Override
            public void run() {
                original.forEach((loc, bd) -> {
                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setBlockData(bd, false);
                    }
                });
            }
        }.runTaskLater(PrimevalRPG.getInstance(), restoreTick);
    }
}
