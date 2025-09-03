package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for submitting a spawn request for a custom mob and grabbing the corresponding mob via the registered custom mob map
 */
@Commands
public class SpawnCustomEntity extends CommandCore {

    public SpawnCustomEntity() {
        super("primeval spawnCustom","Spawn Custom Entity Command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {

        if (args.length == 4) {

            String mobName = args[0];
            CustomMob[] customMobs = CustomEntityArrayHandler.getRegisteredCustomMobs().values().toArray(new CustomMob[0]);

            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);

            Location location = new Location(player.getWorld(), x, y, z);

            for (CustomMob customMob : customMobs) {
                if (customMob.getMobNameID().equals(mobName)) {
                    CustomMobManager.getInstance().setMobLevelAndSpawn(player, customMob, location);
                    player.sendMessage(ColourCode.colour("&5[CustomMobControl]: Spawned " + customMob.getName()));
                }
            }
        }

        if (args.length == 2) {
            String mobName = args[0];
            CustomMob[] customMobs = CustomEntityArrayHandler.getRegisteredCustomMobs().values().toArray(new CustomMob[0]);

            for (CustomMob customMob : customMobs) {
                if (customMob.getMobNameID().equals(mobName)) {
                    for (int i = 0; i < Integer.parseInt(args[1]); i++) {
                        CustomMobManager.getInstance().setMobLevelAndSpawn(player, customMob, player.getLocation());
                    }
                    player.sendMessage(ColourCode.colour("&5[CustomMobControl]: Spawned " + customMob.getName()));
                }
            }
        }

        if (args.length == 1) {
            String mobName = args[0];
            CustomMob[] customMobs = CustomEntityArrayHandler.getRegisteredCustomMobs().values().toArray(new CustomMob[0]);

            for (CustomMob customMob : customMobs) {
                if (customMob.getMobNameID().equals(mobName)) {
                    CustomMobManager.getInstance().setMobLevelAndSpawn(player, customMob, player.getLocation());
                    player.sendMessage(ColourCode.colour("&5[CustomMobControl]: Spawned " + customMob.getName()));
                }
            }
        }

    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {

        List<String> completion = new ArrayList<>();

        CustomMob[] customMobs = CustomEntityArrayHandler.getRegisteredCustomMobs().values().toArray(new CustomMob[0]);

        if (command.getName().equalsIgnoreCase(subName)) {
            if (strings.length == 1) {
                for (CustomMob customMob : customMobs) {
                    completion.add(customMob.getMobNameID());
                }
            }

            if (strings.length == 2) {
                completion.add("x");
            }

            if (strings.length == 3) {
                completion.add("y");
            }

            if (strings.length == 4) {
                completion.add("Z");
            }

        }

        return completion;
    }

}
