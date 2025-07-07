package com.primevalrpg.primeval.core.NPCCore;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NPCManager {
    private static NPCManager instance;

    private File npcFile;
    private FileConfiguration npcConfig;

    private final Map<String, NPC> npcs = new HashMap<>();

    public NPCManager() {
        setupConfig();
        loadAll();
        spawnAll();
    }

    private void setupConfig() {
        npcFile = new File(PrimevalRPG.getInstance().getDataFolder(), "npcdata.yml");
        if (!npcFile.exists()) {
            PrimevalRPG.getInstance().saveResource("npcdata.yml", false);
        }
        npcConfig = YamlConfiguration.loadConfiguration(npcFile);
    }

    public void loadAll() {
        if (!npcConfig.isConfigurationSection("npcs")) return;

        npcConfig.getConfigurationSection("npcs").getKeys(false).forEach(id -> {
            String path = "npcs." + id;
            String name = npcConfig.getString(path + ".name");
            List<String> dialogue = npcConfig.getStringList(path + ".dialogue");

            // loading location as a section
            Location loc = new Location(
                    PrimevalRPG.getInstance().getServer().getWorld(npcConfig.getString(path + ".location.world")),
                    npcConfig.getDouble(path + ".location.x"),
                    npcConfig.getDouble(path + ".location.y"),
                    npcConfig.getDouble(path + ".location.z"),
                    (float) npcConfig.getDouble(path + ".location.yaw"),
                    (float) npcConfig.getDouble(path + ".location.pitch")
            );

            String flag = npcConfig.getString(path + ".questFlag");
            NPC npc = new NPC(id, name, loc, dialogue, flag);
            npcs.put(id, npc);
            spawn(npc);
        });
    }

    public void spawnAll() {
        npcs.values().forEach(this::spawn);
    }

    public void spawn(NPC npc) {
        Villager v = (Villager) npc.getLocation().getWorld()
                .spawnEntity(npc.getLocation(), EntityType.VILLAGER);

        v.setCustomName(npc.getName());
        v.setCustomNameVisible(true);
        v.setAI(false);
        v.setInvulnerable(true);
        v.setPersistent(true);
        v.setMetadata("primeval_npc_id",
                new FixedMetadataValue(PrimevalRPG.getInstance(), npc.getId()));
        npc.setEntity(v);
        npc.spawn();
    }

    public void saveAll() {
        npcConfig.set("npcs", null);
        for (NPC npc : npcs.values()) {
            String path = "npcs." + npc.getId();
            npcConfig.set(path + ".name", npc.getName());

            Location loc = npc.getLocation();
            npcConfig.set(path + ".location.world", loc.getWorld().getName());
            npcConfig.set(path + ".location.x", loc.getX());
            npcConfig.set(path + ".location.y", loc.getY());
            npcConfig.set(path + ".location.z", loc.getZ());
            npcConfig.set(path + ".location.yaw", loc.getYaw());
            npcConfig.set(path + ".location.pitch", loc.getPitch());

            npcConfig.set(path + ".dialogue", npc.getDialogue());
            npcConfig.set(path + ".questFlag", npc.getQuestFlagToSet());
        }
        try {
            npcConfig.save(npcFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNPC(NPC npc) {
        npcs.put(npc.getId(), npc);
        spawn(npc);
        saveAll();
    }

    public Optional<NPC> getById(String id) {
        return Optional.ofNullable(npcs.get(id));
    }

    public Collection<NPC> getAll() {
        return npcs.values();
    }

    public void removeNPC(String id) {
        npcs.remove(id);
        npcs.get(id).getEntity().remove();
        saveAll();
    }

    public void removeALLNPC() {
        getAll().forEach(npc -> {
            npc.getEntity().remove();
        });
        npcs.clear();
    }

    public static NPCManager getInstance() {
        if (instance == null) {
            instance = new NPCManager();
        }
        return instance;
    }
}
