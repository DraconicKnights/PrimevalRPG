package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegionDataHandler {
    private static RegionDataHandler Instance;

    private final File file;
    private YamlConfiguration cfg;
    private final Map<String, Cuboid> regions = new HashMap<>();

    public RegionDataHandler() {
        Instance = this;
        this.file = new File(PrimevalRPG.getInstance().getDataFolder(), "regions.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        if (cfg.contains("regions")) {
            for (String key : cfg.getConfigurationSection("regions").getKeys(false)) {
                World w = Bukkit.getWorld(cfg.getString("regions." + key + ".world"));
                double x1 = cfg.getDouble("regions." + key + ".x1");
                double y1 = cfg.getDouble("regions." + key + ".y1");
                double z1 = cfg.getDouble("regions." + key + ".z1");
                double x2 = cfg.getDouble("regions." + key + ".x2");
                double y2 = cfg.getDouble("regions." + key + ".y2");
                double z2 = cfg.getDouble("regions." + key + ".z2");
                regions.put(key, new Cuboid(w, x1,y1,z1, x2,y2,z2));
            }
        }
    }

    public void reload() {
        try {
            cfg = YamlConfiguration.loadConfiguration(file);
            load();
        } catch (Exception e) {
            RPGLogger.get().log(LoggerLevel.ERROR,">>> Could not reload regions.yml: " + e.getMessage() + " <<<");
        }
    }

    private void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            RPGLogger.get().log(LoggerLevel.ERROR,"Could not save regions.yml: " + e.getMessage());
        }
    }

    public void addRegion(String name, Cuboid cuboid) {
        regions.put(name, cuboid);
        cfg.set("regions." + name + ".world", cuboid.world.getName());
        cfg.set("regions." + name + ".x1", cuboid.x1);
        cfg.set("regions." + name + ".y1", cuboid.y1);
        cfg.set("regions." + name + ".z1", cuboid.z1);
        cfg.set("regions." + name + ".x2", cuboid.x2);
        cfg.set("regions." + name + ".y2", cuboid.y2);
        cfg.set("regions." + name + ".z2", cuboid.z2);
        save();
    }

    public boolean isInside(org.bukkit.entity.Player p, String name) {
        Cuboid c = regions.get(name);
        return (c != null) && c.contains(p.getLocation());
    }

    public static class Cuboid {
        public final World world;
        public final double x1,y1,z1, x2,y2,z2;
        public Cuboid(World world,
                      double x1, double y1, double z1,
                      double x2, double y2, double z2) {
            this.world = world;
            this.x1 = Math.min(x1,x2);
            this.y1 = Math.min(y1,y2);
            this.z1 = Math.min(z1,z2);
            this.x2 = Math.max(x1,x2);
            this.y2 = Math.max(y1,y2);
            this.z2 = Math.max(z1,z2);
        }
        public boolean contains(Location loc) {
            if (!loc.getWorld().equals(world)) return false;
            double x=loc.getX(), y=loc.getY(), z=loc.getZ();
            return x>=x1 && x<=x2
                    && y>=y1 && y<=y2
                    && z>=z1 && z<=z2;
        }
    }

    public static RegionDataHandler getInstance() {
        return Instance;
    }
}