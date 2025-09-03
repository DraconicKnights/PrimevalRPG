package com.primevalrpg.primeval.utils.Handlers;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * FLags Manager
 * Responsible for setting and removing flags with the purpose for use with the DSL (Dynamic Scripting Language)
 */
public class FlagManager {
    private final File file;
    private FileConfiguration cfg;

    // Special UUID for server-wide flags
    private static final UUID SERVER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public FlagManager(String filename) {
        this.file   = new File(PrimevalRPG.getInstance().getDataFolder(), filename);
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            PrimevalRPG.getInstance().getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                PrimevalRPG.getInstance().getLogger().severe("Could not create flags file: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            PrimevalRPG.getInstance().getLogger().severe("Could not save flags file: " + e.getMessage());
        }
    }

    private String path(UUID id) {
        if (id.equals(SERVER_UUID)) {
            return "server";
        }
        return "players." + id.toString();
    }

    // ========== SERVER FLAG METHODS ==========

    public boolean hasServerFlag(String flag) {
        List<String> list = cfg.getStringList("server");
        return list.contains(flag);
    }

    public void setServerFlag(String flag) {
        String key = "server";
        List<String> list = cfg.getStringList(key);
        if (!list.contains(flag)) {
            list.add(flag);
            cfg.set(key, list);
            save();
            RPGLogger.get().log(LoggerLevel.INFO, "Set server flag '" + flag + "'");
        }
    }

    public void clearServerFlag(String flag) {
        String key = "server";
        List<String> list = cfg.getStringList(key);
        if (list.remove(flag)) {
            cfg.set(key, list);
            save();
            RPGLogger.get().log(LoggerLevel.INFO, "Cleared server flag '" + flag + "'");
        }
    }

    public String getServerFlagValue(String flag) {
        return cfg.getString("server_values." + flag);
    }

    public void setServerFlagValue(String flag, String value) {
        cfg.set("server_values." + flag, value);
        save();
        RPGLogger.get().log(LoggerLevel.INFO, "Set server flag '" + flag + "' = '" + value + "'");
    }

    public void clearServerFlagValue(String flag) {
        cfg.set("server_values." + flag, null);
        save();
        RPGLogger.get().log(LoggerLevel.INFO, "Cleared server flag '" + flag + "'");
    }

    // ========== EXISTING PLAYER FLAG METHODS ==========

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
            RPGLogger.get().log(LoggerLevel.INFO, "Set flag '" + flag + "' for " + id);
        }
    }

    public void clearFlag(UUID id, String flag) {
        String key = path(id);
        List<String> list = cfg.getStringList(key);
        if (list.remove(flag)) {
            cfg.set(key, list);
            save();
            RPGLogger.get().log(LoggerLevel.INFO, "Cleared flag '" + flag + "' for " + id);
        }
    }

    // ========== FLAG VALUE METHODS ==========

    public String getFlagValue(UUID id, String flag) {
        return cfg.getString(path(id) + "_values." + flag);
    }

    public void setFlagValue(UUID id, String flag, String value) {
        cfg.set(path(id) + "_values." + flag, value);
        save();
        RPGLogger.get().log(LoggerLevel.INFO, "Set flag '" + flag + "' = '" + value + "' for " + id);
    }

    public void clearFlagValue(UUID id, String flag) {
        cfg.set(path(id) + "_values." + flag, null);
        save();
        RPGLogger.get().log(LoggerLevel.INFO, "Cleared flag '" + flag + "' for " + id);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Get flag value as integer, with default if not set or invalid
     */
    public int getFlagValueAsInt(UUID id, String flag, int defaultValue) {
        String value = getFlagValue(id, flag);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get server flag value as integer, with default if not set or invalid
     */
    public int getServerFlagValueAsInt(String flag, int defaultValue) {
        String value = getServerFlagValue(flag);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Increment a numeric flag value
     */
    public void incrementFlagValue(UUID id, String flag, int increment) {
        int current = getFlagValueAsInt(id, flag, 0);
        setFlagValue(id, flag, String.valueOf(current + increment));
    }

    /**
     * Increment a numeric server flag value
     */
    public void incrementServerFlagValue(String flag, int increment) {
        int current = getServerFlagValueAsInt(flag, 0);
        setServerFlagValue(flag, String.valueOf(current + increment));
    }
}