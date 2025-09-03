package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
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
        instance = this;
        load();
        loadSettings();
    }

    // Enabled once
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

    private void loadSettings() {
        isEnabled      = DataConfig.getBoolean("customMobsEnabled", true);
        levelingEnable = DataConfig.getBoolean("levelingEnable", true);
        debugMode      = DataConfig.getBoolean("debugMode", false);
    }

    public static void saveSettings() {
        DataConfig.set("customMobsEnabled", isEnabled);
        DataConfig.set("levelingEnable", levelingEnable);
        DataConfig.set("debugMode", debugMode);
        try {
            DataConfig.save(configDataFile);
        } catch (IOException e) {
            RPGLogger.get().log(LoggerLevel.ERROR,">>> Could not save config.yml: " + e.getMessage() + " <<<");
        }
    }

    // Reloads values
    public void reloadConfig() {
        try {
            DataConfig.load(configDataFile);
            loadSettings();
        } catch (Exception e) {
                RPGLogger.get().log(LoggerLevel.ERROR,">>> Could not reload config.yml: " + e.getMessage() + " <<<");
            e.printStackTrace();
        }
    }

    public static CoreDataHandler getInstance() {
        return instance;
    }
}