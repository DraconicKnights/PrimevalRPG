package com.primevalrpg.primeval.utils.Handlers;

import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Data.SerializableLocation;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Region implements Serializable {
    private String regionName;
    private SerializableLocation firstCorner;
    private SerializableLocation secondCorner;
    private List<CustomMob> spawnableMobs;
    private int currentMobs;
    private int minLevel;
    private int maxLevel;

    public Region(String regionName, Location firstCorner, Location secondCorner, List<CustomMob> spawnableMobs, int minLevel, int maxlevel) {
        this.regionName = regionName;
        this.firstCorner = (firstCorner != null) ? new SerializableLocation(firstCorner) : null;
        this.secondCorner = (secondCorner != null) ? new SerializableLocation(secondCorner) : null;
        this.spawnableMobs = spawnableMobs;
        this.minLevel = minLevel;
        this.maxLevel = maxlevel;
    }

    public Region() {
        this(null, null, null, new ArrayList<>(), 0, 0);
    }

    private Location toLocation(SerializableLocation serializableLocation) {
        return serializableLocation == null ? null : serializableLocation.toLocation();
    }

    public boolean isLocationInRegion(Location location) {
        assert location != null;

        Location firstCornerLocation = this.getFirstCorner();
        Location secondCornerLocation = this.getSecondCorner();

        // Check if corners are defined
        if(firstCornerLocation == null || secondCornerLocation == null) {
            return false;
        }

        // Check boundaries
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double x1 = Math.min(secondCornerLocation.getX(), firstCornerLocation.getX());
        double x2 = Math.max(secondCornerLocation.getX(), firstCornerLocation.getX());
        double y1 = Math.min(secondCornerLocation.getY(), firstCornerLocation.getY());
        double y2 = Math.max(secondCornerLocation.getY(), firstCornerLocation.getY());
        double z1 = Math.min(secondCornerLocation.getZ(), firstCornerLocation.getZ());
        double z2 = Math.max(secondCornerLocation.getZ(), firstCornerLocation.getZ());

        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public Location getRandomLocation() {
        if (!isDefined())
            return null;

        Random rand = new Random();

        Location firstCornerLocation = toLocation(firstCorner);
        Location secondCornerLocation = toLocation(secondCorner);

        double minX = Math.min(firstCornerLocation.getX(), secondCornerLocation.getX());
        double maxX = Math.max(firstCornerLocation.getX(), secondCornerLocation.getX());

        double minY = Math.min(firstCornerLocation.getY(), secondCornerLocation.getY());
        double maxY = Math.max(firstCornerLocation.getY(), secondCornerLocation.getY());

        double minZ = Math.min(firstCornerLocation.getZ(), secondCornerLocation.getZ());
        double maxZ = Math.max(firstCornerLocation.getZ(), secondCornerLocation.getZ());

        double x = minX + (maxX - minX) * rand.nextDouble();
        double y = minY + (maxY - minY) * rand.nextDouble();
        double z = minZ + (maxZ - minZ) * rand.nextDouble();

        return new SerializableLocation(firstCornerLocation.getWorld().getName(), x, y, z).toLocation();
    }

    public boolean isDefined() {
        return firstCorner != null && secondCorner != null;
    }

    public String getRegionName() {
        return regionName;
    }

    public List<CustomMob> getSpawnableMobs() {
        return spawnableMobs;
    }

    public int getCurrentMobs() {
        return currentMobs;
    }

    public int getRandomLevel() {
        return minLevel + (int) (Math.random() * ((maxLevel - minLevel) + 1));
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Location getFirstCorner() {
        return toLocation(firstCorner);
    }

    public Location getSecondCorner() {
        return toLocation(secondCorner);
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setFirstCorner(Location location) {
        this.firstCorner = new SerializableLocation(location);
    }

    public void setSecondCorner(Location location) {
        this.secondCorner = new SerializableLocation(location);
    }

    public void setSpawnableMobs(List<CustomMob> spawnableMobs) {
        this.spawnableMobs.addAll(spawnableMobs);
    }

    public void setCurrentMobs(int currentMobs) {
        this.currentMobs = currentMobs;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxlevel) {
        this.maxLevel = maxlevel;
    }
}


