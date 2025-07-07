package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Inventory.PlayerMenu.PlayerCoreMenu;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

@Commands
public class PlayerGUICommand extends CommandCore {
    public PlayerGUICommand() {
        super("primeval playergui", "Player GUI menu command","primeval.default", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        new PlayerCoreMenu().open(player);
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
