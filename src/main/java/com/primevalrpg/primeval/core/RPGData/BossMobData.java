package com.primevalrpg.primeval.core.RPGData;

import com.primevalrpg.primeval.core.RPGMobs.BossMob;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;

import java.util.Map;

/**
 * Boss Mob Data
 * Instantiated upon plugin initialization and grabs the data from the yml and passes it to the Mob builder
 */
public class BossMobData {
    private static BossMobData instance;

    public BossMobData() {
        instance = this;
        getData();
    }

    public void getData() {
        try {
            // Iterates over each boss mob configuration.
            for (Map<?, ?> bossMobMap : MobDataHandler.GetConfig().getMapList("customBossMobs")) {
                BossMob bossMob = BossMobCreation.fromMap(bossMobMap);
                CustomEntityArrayHandler.getRegisteredBossMobs().put(bossMob.getMobID(), bossMob);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BossMob getRandomBossMob() {
        if (CustomEntityArrayHandler.getRegisteredBossMobs().isEmpty()) return null;
        int randomIndex = (int) (Math.random() * CustomEntityArrayHandler.getRegisteredBossMobs().size());
        return CustomEntityArrayHandler.getRegisteredBossMobs().get(randomIndex);
    }

    public static BossMobData getInstance() {
        return instance;
    }
}
