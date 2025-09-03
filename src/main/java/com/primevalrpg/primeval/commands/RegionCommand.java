package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.utils.Data.RegionDataHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands
public class RegionCommand extends CommandCore {
    // holds first corner per region name
    private final Map<String, Location> firstCorners = new HashMap<>();

    public RegionCommand() {
        super("primeval region", "Region creation command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (args.length != 2 ||
                !(args[0].equalsIgnoreCase("pos1") || args[0].equalsIgnoreCase("pos2"))) {
            player.sendMessage("Usage: /region pos1 <name>  or  /region pos2 <name>");
            return;
        }

        String mode = args[0].toLowerCase();
        String name = args[1];

        if (mode.equals("pos1")) {
            firstCorners.put(name, player.getLocation());
            player.sendMessage("Region '" + name + "' first corner set.");
            return;
        }

        // pos2
        Location corner1 = firstCorners.get(name);
        if (corner1 == null) {
            player.sendMessage("First corner not set! Use /region pos1 " + name);
            return;
        }
        Location corner2 = player.getLocation();

        // register region
        RegionDataHandler.getInstance().addRegion(
                name,
                new RegionDataHandler.Cuboid(
                        corner1.getWorld(),
                        corner1.getX(), corner1.getY(), corner1.getZ(),
                        corner2.getX(), corner2.getY(), corner2.getZ()
                )
        );
        firstCorners.remove(name);
        player.sendMessage("Region '" + name + "' defined.");

        // visualize boundary with particles
        showBoundary(player, corner1, corner2);
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] args) {
        return null;
    }

    private void showBoundary(Player player, Location l1, Location l2) {
        double xMin = Math.min(l1.getX(), l2.getX());
        double yMin = Math.min(l1.getY(), l2.getY());
        double zMin = Math.min(l1.getZ(), l2.getZ());
        double xMax = Math.max(l1.getX(), l2.getX());
        double yMax = Math.max(l1.getY(), l2.getY());
        double zMax = Math.max(l1.getZ(), l2.getZ());

        // outline bottom perimeter
        for (double x = xMin; x <= xMax; x += 1.0) {
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), x, yMin, zMin), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), x, yMin, zMax), 1, 0, 0, 0, 0);
        }
        for (double z = zMin; z <= zMax; z += 1.0) {
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), xMin, yMin, z), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), xMax, yMin, z), 1, 0, 0, 0, 0);
        }

        // outline top perimeter
        for (double x = xMin; x <= xMax; x += 1.0) {
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), x, yMax, zMin), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), x, yMax, zMax), 1, 0, 0, 0, 0);
        }
        for (double z = zMin; z <= zMax; z += 1.0) {
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), xMin, yMax, z), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.END_ROD,
                    new Location(l1.getWorld(), xMax, yMax, z), 1, 0, 0, 0, 0);
        }

        // draw vertical edges
        for (double y = yMin; y <= yMax; y += 1.0) {
            spawnEdgeParticle(player, xMin, y, zMin, l1.getWorld());
            spawnEdgeParticle(player, xMin, y, zMax, l1.getWorld());
            spawnEdgeParticle(player, xMax, y, zMin, l1.getWorld());
            spawnEdgeParticle(player, xMax, y, zMax, l1.getWorld());
        }
    }

    private void spawnEdgeParticle(Player p, double x, double y, double z, org.bukkit.World w) {
        p.spawnParticle(Particle.END_ROD, new Location(w, x, y, z), 1, 0, 0, 0, 0);
    }
}