package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CoreDataHandler {

    private static CoreDataHandler instance;

    // Config values
    public static boolean isEnabled;
    public static boolean levelingEnable;
    public static boolean debugMode;

    private static File configDataFile;
    private static YamlConfiguration DataConfig;

    private CoreDataHandler() {
        // private constructor to enforce singleton
        instance = this;
        load();
        loadSettings();
    }

    /** Call once in onEnable() */
    public static void initialize(PrimevalRPG plugin) {
        configDataFile = new File(plugin.getDataFolder(), "config.yml");
        DataConfig = YamlConfiguration.loadConfiguration(configDataFile);
        new CoreDataHandler();
    }

    private void load() {
        if (!configDataFile.exists()) {
            // save the default if it doesn't exist
            PrimevalRPG.getInstance().saveResource("config.yml", false);
        }
        DataConfig = YamlConfiguration.loadConfiguration(configDataFile);
    }

    /** Load values from disk into the static fields */
    private void loadSettings() {
        isEnabled      = DataConfig.getBoolean("customMobsEnabled", true);
        levelingEnable = DataConfig.getBoolean("levelingEnable", true);
        debugMode      = DataConfig.getBoolean("debugMode", false);
    }

    /** Write the current static fields back to disk */
    public static void saveSettings() {
        DataConfig.set("customMobsEnabled", isEnabled);
        DataConfig.set("levelingEnable", levelingEnable);
        DataConfig.set("debugMode", debugMode);
        try {
            DataConfig.save(configDataFile);
        } catch (IOException e) {
            PrimevalRPG.getInstance()
                    .getLogger()
                    .severe("Could not save config.yml: " + e.getMessage());
        }
    }

    /** Reload from disk (useful if you want to detect external edits) */
    public void reloadConfig() {
        try {
            DataConfig.load(configDataFile);
            loadSettings();
        } catch (Exception e) {
            PrimevalRPG.getInstance()
                    .getLogger()
                    .severe("Could not reload config.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CoreDataHandler getInstance() {
        return instance;
    }
}