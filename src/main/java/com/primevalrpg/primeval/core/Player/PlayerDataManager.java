package com.primevalrpg.primeval.core.Player;

import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final Gson gson = new Gson();
    private final Path saveFolder;
    private final Map<UUID,PlayerData> levels = new HashMap<>();

    public PlayerDataManager(Path pluginDataFolder) {
        instance = this;
        saveFolder = pluginDataFolder.resolve("playerdata");
        try {
            Files.createDirectories(saveFolder);
            // preload all existing files
            Files.list(saveFolder)
                    .filter(p->p.toString().endsWith(".json"))
                    .forEach(p->{
                        UUID uuid = UUID.fromString(p.getFileName().toString().replace(".json",""));
                        levels.put(uuid, loadPlayerData(uuid));
                    });
        } catch(IOException e) {
            RPGLogger.get().log(LoggerLevel.ERROR,
                    "Could not create/read playerdata folder");
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        // load+cache if missing
        return levels.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public PlayerData loadPlayerData(UUID uuid) {
        Path file = saveFolder.resolve(uuid + ".json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                return gson.fromJson(r, PlayerData.class);
            } catch (IOException e) {
                RPGLogger.get().log(LoggerLevel.ERROR,
                        "Failed loading data for " + uuid);
            }
        }
        // no file or error → new data
        return new PlayerData(uuid);
    }

    public void savePlayerData(PlayerData data) {
        Path file = saveFolder.resolve(data.getPlayerUUID() + ".json");
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(data, w);
        } catch (IOException e) {
            RPGLogger.get().log(LoggerLevel.ERROR,
                    "Failed saving data for " + data.getPlayerUUID());
        }
    }

    /** only needed if you want to manage the cache yourself */
    public void addPlayerData(UUID uuid, PlayerData data) {
        levels.put(uuid, data);
    }

    /** call on plugin disable */
    public void saveAll() {
        levels.values().forEach(this::savePlayerData);
    }

    public static PlayerDataManager getInstance() {
        return instance;
    }
}