package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.utils.Data.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The ReloadConfigCommand class is responsible for reloading various configuration files and components
 * related to the Primeval RPG plugin. This command can only be executed by players with the required
 * permission and enforces a cooldown system.
 *
 * It performs the following actions:
 * - Reloads mob data configurations.
 * - Reloads core data configurations.
 * - Reloads custom item configurations.
 * - Reloads regional data configurations.
 *
 * Upon execution, the player is notified about the reloading process.
 */
@Commands
public class PrimevalReloadCommand extends CommandCore {

    public PrimevalReloadCommand() {
        super("primeval reload", "PrimevalRPG Reload Command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {

        MobDataHandler.getInstance().ReloadMobsConfig();
        CoreDataHandler.getInstance().reloadConfig();
        CustomItemHandler.reload();
        RegionDataHandler.getInstance().reload();

        player.sendMessage(ChatColor.RED + "Reloading all Primeval data...");
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {

        return null;
    }

}
