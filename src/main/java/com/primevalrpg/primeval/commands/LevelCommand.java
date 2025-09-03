package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Level command used for modifying the players level
 */
@Commands
public class LevelCommand extends CommandCore{
    public LevelCommand() {
        super("primeval level", "Player level command", "primeval.admin",0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.sendMessage(ChatColor.RED + "Invalid usage. Usage: /level [player] <level>");
            return;
        }

        Player targetPlayer;
        int newLevel;

        if (args.length == 2) {
            targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Cannot find player " + args[0]);
                return;
            }
            newLevel = Integer.parseInt(args[1]);
        } else {
            targetPlayer = player;
            newLevel = Integer.parseInt(args[0]);
        }

        PlayerData targetPlayerData = PlayerDataManager.getInstance().getPlayerData(targetPlayer.getUniqueId());
        if (targetPlayerData != null) {
            targetPlayerData.setOverallLevel(newLevel, player);
            player.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "'s level set to " + newLevel);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to fetch PlayerData for " + targetPlayer.getName());
        }
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
