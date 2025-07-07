package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.Menu;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Mob menu command used for opening and instantiating a new instance of Mob Menu
 */
@Commands
public class MobGUICommand extends CommandCore{
    public MobGUICommand() {
        super("primeval mobgui","Mob GUI menu command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        Menu menu = new Menu();

        menu.open(player);
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
