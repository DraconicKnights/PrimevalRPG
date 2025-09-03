package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PlayerDataHandler {

    private static PlayerDataHandler Instance;
    public static int MOB_KILL_XP;
    public static int BLOCK_BREAK_XP;

    public PlayerDataHandler() {
        Instance = this;
        load();
        setPlayerDataValues();
    }

    private void load() {
        File playerConfig = new File(PrimevalRPG.getInstance().getDataFolder(), "player.yml");
        if (!playerConfig.exists()) PrimevalRPG.getInstance().saveResource("player.yml", false);
    }

    private static YamlConfiguration getConfig() {
        File configFile = new File(PrimevalRPG.getInstance().getDataFolder(), "player.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void setPlayerDataValues() {
        MOB_KILL_XP   = getConfig().getInt("MOB_KILL_XP", 0);
        BLOCK_BREAK_XP = getConfig().getInt("BLOCK_BREAK_XP", 0);
    }

    public static PlayerDataHandler getInstance() {
        return Instance;
    }

}