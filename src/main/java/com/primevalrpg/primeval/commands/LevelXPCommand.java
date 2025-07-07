package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.enums.ElementType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Level XP Command used for setting the custom plugin XP value
 */
@Commands
public class LevelXPCommand extends CommandCore{
    public LevelXPCommand() {
        super("primeval levelxp", "Player levelxp command","primeval.admin",0);
    }

    @Override
    protected void execute(Player sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /levelxp [player] <element> <amount>");
            return;
        }

        // determine target, element arg, xp arg
        Player target;
        String elementArg;
        String amountArg;
        if (args.length == 3) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return;
            }
            elementArg = args[1];
            amountArg  = args[2];
        } else {
            target     = sender;
            elementArg = args[0];
            amountArg  = args[1];
        }

        // parse element type
        ElementType type;
        try {
            type = ElementType.valueOf(elementArg.toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid element: " + elementArg);
            return;
        }

        // parse xp amount
        int xp;
        try {
            xp = Integer.parseInt(amountArg);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return;
        }

        // fetch target data
        PlayerData data = PlayerDataManager.getInstance()
                .getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(ChatColor.RED + "Player data not found for: " + target.getName());
            return;
        }

        // apply XP
        data.addElementXp(type, xp, target);
        PlayerDataManager.getInstance().savePlayerData(data);

        // feedback
        sender.sendMessage(ChatColor.GREEN + "Gave " + xp +
                " XP to " + target.getName() + "'s " + type.name() + " element.");
        if (!target.equals(sender)) {
            target.sendMessage(ChatColor.GREEN + "You received " + xp +
                    " XP to your " + type.name() + " element.");
        }
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
