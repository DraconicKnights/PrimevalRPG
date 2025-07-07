package com.primevalrpg.primeval.core.RPGData;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Loads and reloads items.yml from your plugin folder.
 */
public class ItemDataHandler {
    private static ItemDataHandler instance;
    private FileConfiguration config;

    private ItemDataHandler() {
        reloadConfig();
    }

    public static void init() {
        if (instance == null) {
            instance = new ItemDataHandler();
        }
    }

    public static ItemDataHandler getInstance() {
        return instance;
    }

    /**
     * (Re)loads items.yml. If not present, copies the default from your jar.
     */
    public void reloadConfig() {
        File file = new File(PrimevalRPG.getInstance().getDataFolder(), "items.yml");
        if (!file.exists()) {
            PrimevalRPG.getInstance().saveResource("items.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(new File(PrimevalRPG.getInstance().getDataFolder(), "items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}