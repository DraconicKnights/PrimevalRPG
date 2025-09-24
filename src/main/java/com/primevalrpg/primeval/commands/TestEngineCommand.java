package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import com.primevalrpg.primeval.utils.ScriptingEngine.EngineHandler;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

@Commands
public class TestEngineCommand extends CommandCore {

    public TestEngineCommand() {
        super("primeval engine","Primeval engine test for running javascript logic","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {

        // Try catch block to determine errors and help correct and pinpoint issues.

        try {
            EngineHandler.getInstance().getEngine().eval("print('Hello World');");
        } catch (Exception e) {
            RPGLogger.get().error("Failed to execute command: " + e.getMessage());
        }
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return List.of();
    }
}
