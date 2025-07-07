package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.core.RPGData.CustomMobCreation;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MobDataHandler {

    private static MobDataHandler Instance;
    public static int minDistance;
    public static int maxDistance;

    public static double mobSpawnChance;
    public static double caveSpawnChance;
    public static int maxPerPlayer;

    private static File mobDataFile = new File(PrimevalRPG.getInstance().getDataFolder(), "mobs.yml");
    private static YamlConfiguration mobDataConfig = YamlConfiguration.loadConfiguration(mobDataFile);

    private static File abilitiesFile = new File(PrimevalRPG.getInstance().getDataFolder(), "abilities.yml");
    private static YamlConfiguration abilitiesConfig = YamlConfiguration.loadConfiguration(abilitiesFile);


    public MobDataHandler() {
        Instance = this;
        load();
        loadSettings();
        loadAbilities();
        setDistanceValues();
    }

    public void loadSettings() {
        mobSpawnChance = mobDataConfig.getDouble("mobSpawnChance");
        caveSpawnChance = mobDataConfig.getDouble("caveSpawnChance");
        maxPerPlayer = mobDataConfig.getInt("maxMobsPerPlayer");
    }

    private void load() {
        if (!mobDataFile.exists()) PrimevalRPG.getInstance().saveResource("mobs.yml", false);
        mobDataConfig = YamlConfiguration.loadConfiguration(mobDataFile);
    }

    // NEW METHOD: ensure abilities.yml exists & load it
    private void loadAbilities() {
        if (!abilitiesFile.exists())
            PrimevalRPG.getInstance().saveResource("abilities.yml", false);
        abilitiesConfig = YamlConfiguration.loadConfiguration(abilitiesFile);
    }

    public static YamlConfiguration getAbilitiesConfig() {
        return abilitiesConfig;
    }

    /** Call this to re-read abilities.yml from disk after a reload command */
    public static void reloadAbilitiesConfig() {
        abilitiesConfig = YamlConfiguration.loadConfiguration(abilitiesFile);
    }

    public static YamlConfiguration GetConfig() {
        return mobDataConfig;
    }

    private void setDistanceValues() {
        minDistance = mobDataConfig.getInt("spawningDistanceMin");
        maxDistance = mobDataConfig.getInt("spawningDistanceMax");
    }


    public void ReloadMobsConfig() {
        RemoveAllCustomMobs();
        CustomEntityArrayHandler.getRegisteredCustomMobs().clear();
        CustomEntityArrayHandler.getCustomEntities().clear();
/*        CustomEntityArrayHandler.getRegisteredBossMobs().clear();
        CustomEntityArrayHandler.getBossEntities().clear();*/

        load();

        CustomEntityData.getInstance().loadAllMobs();
        reloadAbilitiesConfig();
/*
        BossMobData.getInstance().getData();
*/
    }

    public void RemoveAllMobs() {
        RemoveAllCustomMobs();
        CustomEntityArrayHandler.getRegisteredCustomMobs().clear();
/*
        CustomEntityArrayHandler.getRegisteredBossMobs().clear();
*/
    }

    private void RemoveAllCustomMobs() {
        try {
            PrimevalRPG.getInstance().CustomMobLogger("Starting removal of all custom mobs", LoggerLevel.INFO);
            for (Entity entity : CustomEntityArrayHandler.getCustomEntities().keySet()) {
                entity.remove();
            }
/*            for (Entity entity : CustomEntityArrayHandler.getBossEntities().keySet()) {
                entity.remove();
            }*/
            PrimevalRPG.getInstance().CustomMobLogger("All mobs have successfully been removed", LoggerLevel.INFO);
            PrimevalRPG.getInstance().CustomMobLogger("Mobs active: " + CustomEntityArrayHandler.getRegisteredCustomMobs().keySet().size(), LoggerLevel.INFO);
        } catch (Exception e) {
            PrimevalRPG.getInstance().CustomMobLogger("An error occurred while attempting to remove all custom mobs", LoggerLevel.INFO);
            e.printStackTrace();
        }
    }

    /**
     * Rename an existing mob key under customMobs.oldID → customMobs.newID
     */
    public void renameMobData(String oldID, String newID) {
        String root = "customMobs";
        if (!mobDataConfig.contains(root + "." + oldID)) return;

        ConfigurationSection oldSec =
                mobDataConfig.getConfigurationSection(root + "." + oldID);
        @SuppressWarnings("unchecked")
        Map<String, Object> values = oldSec.getValues(true);

        // delete old and write new
        mobDataConfig.set(root + "." + oldID, null);
        mobDataConfig.set(root + "." + newID, values);

        try {
            mobDataConfig.save(mobDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // reload in‐memory registry
        CustomEntityData.getInstance().loadAllMobs();
    }

    /**
     * Create or update a mob under customMobs.<mobID>.  If we passed an
     * originalKey that differs, delete the old entry first.
     */
    public void saveOrUpdate(CustomMob mob, String originalKey) {
        String root = "customMobs";

        // 1) if renaming, delete the old record
        if (originalKey != null && !originalKey.equals(mob.getMobNameID())) {
            mobDataConfig.set(root + "." + originalKey, null);
        }

        // 2) write the new map under its ID
        mobDataConfig.set(root + "." + mob.getMobNameID(),
                CustomMobCreation.toMap(mob));

        // 3) persist to disk
        try {
            mobDataConfig.save(mobDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4) reload in‐memory cache so the old key truly disappears
        CustomEntityData.getInstance().loadAllMobs();
    }


    /**
     * Persist general settings (enabled flag & spawn distances) into mobs.yml.
     */
    public static void saveSettings() {
        // write into memory
        mobDataConfig.set("spawningDistanceMin",  minDistance);
        mobDataConfig.set("spawningDistanceMax",  maxDistance);

        // save to file
        try {
            mobDataConfig.save(mobDataFile);
        } catch (IOException e) {
            PrimevalRPG.getInstance().CustomMobLogger("Could not save mobs.yml settings: " + e.getMessage(), LoggerLevel.ERROR);
        }
    }

    public static void saveCustomMobData() throws IOException {
        // load the file fresh (or you could use the in-memory mobDataConfig)
        YamlConfiguration disk = YamlConfiguration.loadConfiguration(mobDataFile);

        // clear out anything under customMobs
        disk.set("customMobs", null);

        // rewrite every registered mob
        for (CustomMob mob : CustomEntityArrayHandler.getRegisteredCustomMobs().values()) {
            Map<String, Object> props = CustomMobCreation.toMap(mob);
            String key = ((String) props.get("mobID"))
                    .replaceAll("&[0-9A-FK-ORa-fk-or]", "");
            disk.set("customMobs." + key, props);
        }

        // save back to disk
        disk.save(mobDataFile);

        // refresh the in-memory config if you need it immediately
        mobDataConfig = YamlConfiguration.loadConfiguration(mobDataFile);
    }

    public static void registerCustomMob(CustomMob mob) {
        CustomEntityArrayHandler.getRegisteredCustomMobs().put(mob.getMobID(), mob);
    }

    public static File getDataFile() {
        return mobDataFile;
    }

    public static MobDataHandler getInstance() {
        return Instance;
    }

}
