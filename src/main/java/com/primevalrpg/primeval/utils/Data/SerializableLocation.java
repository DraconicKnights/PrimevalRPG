package com.primevalrpg.primeval.utils.Data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

public class SerializableLocation implements Serializable {
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    public SerializableLocation(Location location) {
        if (location != null) {
            this.worldName = location.getWorld().getName();
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
        } else {
            this.worldName = null;
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
    }

    public SerializableLocation(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z);
    }
}
