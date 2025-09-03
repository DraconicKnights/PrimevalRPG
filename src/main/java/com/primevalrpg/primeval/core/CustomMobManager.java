package com.primevalrpg.primeval.core;

import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Data.CoreDataHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom Mob Manager that deals with mob management and spawning
 */

public class CustomMobManager {
    private static CustomMobManager Instance;
    private static final java.util.Random random = new java.util.Random();

    public CustomMobManager() {
        Instance = this;
    }

    public void setMobLevelAndSpawn(Player player, CustomMob customMob, Location spawnLocation) {
        CustomMob mob = new CustomMob(customMob);

        ElementType[] values = ElementType.values();
        mob.setElementType(values[ThreadLocalRandom.current().nextInt(values.length)]);

        int spawnLevel = 1;

        if (CoreDataHandler.levelingEnable) {
            PlayerData pd = PlayerDataManager.getInstance()
                    .getPlayerData(player.getUniqueId());
            int playerLvl = pd.getOverallLevel();

            spawnLevel = Math.max(1, playerLvl - 5 + random.nextInt(11));

        }
        mob.spawnEntity(spawnLocation, spawnLevel);
    }

    public static CustomMobManager getInstance() {
        return Instance;
    }
}
