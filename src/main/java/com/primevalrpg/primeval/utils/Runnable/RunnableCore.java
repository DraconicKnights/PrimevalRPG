package com.primevalrpg.primeval.utils.Runnable;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable Core used for all custom Runnable actions within this plugin
 */
public abstract class RunnableCore extends BukkitRunnable {
    private final PrimevalRPG plugin;
    private final long delayTicks;
    private final long periodTicks;

    public RunnableCore(PrimevalRPG plugin, long delaySeconds, long periodSeconds) {
        this.plugin = plugin;
        this.delayTicks = delaySeconds * 20L;
        this.periodTicks = periodSeconds * 20L;
    }

    protected abstract void event();

    @Override
    public void run() {
        event();
    }

    public void startTimedTask() {
        this.runTaskTimer(plugin, delayTicks, periodTicks);
    }

    public void startTimedAsyncTask() {
        this.runTaskTimerAsynchronously(plugin, delayTicks, periodTicks);
    }

    protected void startTaskLater() {
        this.runTaskLater(plugin, delayTicks);
    }

    protected void startTask() {
        this.runTask(plugin);
    }

    public void stop() {
        this.cancel();
    }
}
