package com.primevalrpg.primeval.core.RPGData;


import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.CoreDataHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Entity Data
 * Instantiated upon plugin initialization and passed the data from the yml towards the mob builder
 */
public class CustomEntityData {

    private static CustomEntityData Instance;

    public CustomEntityData() {
        Instance = this;
        loadAllMobs();
    }

    @SuppressWarnings("unchecked")
    public void loadAllMobs() {
        FileConfiguration cfg = MobDataHandler.GetConfig();
        ConfigurationSection root = cfg.getConfigurationSection("customMobs");
        if (root == null) return;

        for (String mobKey : root.getKeys(false)) {
            ConfigurationSection mobSect = root.getConfigurationSection(mobKey);
            if (mobSect == null) continue;

            // Keeping this logic for use to avoid issues.
            Map<String,Object> mobMap = sectionToMap(mobSect);

            CustomMob mob = CustomMobCreation.fromMap(mobMap);
            CustomEntityArrayHandler.getRegisteredCustomMobs().put(mob.getMobID(), mob);
        }
    }

    /**
     * Recursively converts a Bukkit ConfigurationSection into
     * a nested Map<String,Object>, so no MemorySection survives.
     * Should resolve any parsing issues. Worked without use but changes resulted in Dimensions overwriting other mobs and leading to data issues
     */
    private Map<String,Object> sectionToMap(ConfigurationSection section) {
        Map<String,Object> result = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object val = section.get(key);
            if (val instanceof ConfigurationSection) {
                result.put(key, sectionToMap((ConfigurationSection) val));
            } else {
                result.put(key, val);
            }
        }
        return result;
    }

    public static CustomMob getRandomMob() {
        if (CustomEntityArrayHandler.getRegisteredCustomMobs().isEmpty()) return null;
        int randomIndex = (int) (Math.random() * CustomEntityArrayHandler.getRegisteredCustomMobs().size());

        return CustomEntityArrayHandler.getRegisteredCustomMobs().get(randomIndex);
    }

    public static CustomMob getCustomMobByName(String mobNameID) {
        return CustomEntityArrayHandler.getRegisteredCustomMobs().values().stream()
                .filter(mob -> mob.getMobNameID().equals(mobNameID))
                .findFirst()
                .orElse(null);
    }

    public void saveCustomMob(CustomMob mob) {
        try {
            MobDataHandler.saveCustomMobData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Completely deletes a custom mob from memory and from mobs.yml, then reloads.
     * Fully removes the custom mob from the Entity Array Handler and will reload the plugin to ensure it's cleaned up.
     */
    public void removeCustomMob(String mobID, Player player) {
        CustomEntityArrayHandler.getRegisteredCustomMobs().remove(mobID);

        YamlConfiguration cfg = MobDataHandler.GetConfig();
        cfg.set("customMobs." + mobID, null);
        try {
            cfg.save(MobDataHandler.getDataFile());
        } catch (IOException ex) {
            player.sendMessage("§cFailed to save mobs.yml!");
            ex.printStackTrace();
            return;
        }

        MobDataHandler.getInstance().ReloadMobsConfig();

        player.sendMessage("§aRemoved custom mob \"" + mobID + "\".");
    }

    /// Quick boolean and Values grab for use elsewhere

    public boolean isCustomMobsEnabled() {
        return CoreDataHandler.isEnabled;
    }
    public int CustomMobMaxSpawnDistance() {
        return MobDataHandler.maxDistance;
    }
    public int CustomMobMinSpawnDistance() {
        return MobDataHandler.minDistance;
    }

    public static CustomEntityData getInstance() {
        return Instance;
    }

}
