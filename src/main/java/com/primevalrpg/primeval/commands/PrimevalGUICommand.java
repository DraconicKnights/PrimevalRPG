package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Inventory.Primeval.ConfigMenu;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

@Commands
public class PrimevalGUICommand extends CommandCore {
    public PrimevalGUICommand() {
        super("primeval coregui", "PrimevalRPG GUI Menu command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        new ConfigMenu().open(player);
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
