package com.primevalrpg.primeval.utils.Scripts;

import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.List;

/**
 * Holds one running script and where we are in it.
 */
public class ScriptContext {
    private final List<ScriptCommand> commands;
    private int currentIndex = 0;
    public final LivingEntity self;
    public final Collection<LivingEntity> nearby;
    public final Event event;
    public final DefaultExecutors executors;
    public final Plugin plugin;

    public ScriptContext(List<ScriptCommand> commands,
                         LivingEntity self,
                         Collection<LivingEntity> nearby,
                         Event event,
                         DefaultExecutors executors,
                         Plugin plugin) {
        this.commands  = commands;
        this.self      = self;
        this.nearby    = nearby;
        this.event     = event;
        this.executors = executors;
        this.plugin    = plugin;
    }

    /** Kick off execution from the current index onward. */
    public void run() {
        while (currentIndex < commands.size()) {
            ScriptCommand cmd = commands.get(currentIndex++);
            DefaultExecutors.CommandExecutor executor = executors.get(cmd.getName());
            if (executor == null) {
                RPGLogger.get().error("No executor registered for '" + cmd.getName() + "' – skipping.");
                continue;
            }
            executors.get(cmd.getName()).execute(cmd, this);
        }
    }

    /** Used by execDelay to grab whatever is left after a bare delay. */
    public List<ScriptCommand> takeRemaining() {
        List<ScriptCommand> tail = commands.subList(currentIndex, commands.size());
        currentIndex = commands.size();
        return tail;
    }
}