package com.primevalrpg.primeval.utils.Handlers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FlagManager {
    private final Plugin plugin;
    private final File file;
    private FileConfiguration cfg;

    public FlagManager(Plugin plugin, String filename) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), filename);
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create flags file: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save flags file: " + e.getMessage());
        }
    }

    private String path(UUID id) {
        return "players." + id.toString();
    }

    public boolean hasFlag(UUID id, String flag) {
        List<String> list = cfg.getStringList(path(id));
        return list.contains(flag);
    }

    public void setFlag(UUID id, String flag) {
        String key = path(id);
        List<String> list = cfg.getStringList(key);
        if (!list.contains(flag)) {
            list.add(flag);
            cfg.set(key, list);
            save();
            plugin.getLogger().info("Set flag '" + flag + "' for " + id);
        }
    }

    public void clearFlag(UUID id, String flag) {
        String key = path(id);
        List<String> list = cfg.getStringList(key);
        if (list.remove(flag)) {
            cfg.set(key, list);
            save();
            plugin.getLogger().info("Cleared flag '" + flag + "' for " + id);
        }
    }
}