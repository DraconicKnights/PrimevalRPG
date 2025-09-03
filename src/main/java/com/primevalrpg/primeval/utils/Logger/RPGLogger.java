package com.primevalrpg.primeval.utils.Logger;

import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Central logger for PrimevalRPG.
 * Supports INFO/ERROR/STARTUP/SHUTDOWN and a DEBUG toggle.
 */
public final class RPGLogger {
    private final Plugin plugin;
    private boolean debugEnabled = false;
    private static RPGLogger instance;

    private RPGLogger(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Initialize once in your onEnable() */
    public static void init(Plugin plugin, boolean debugEnabled) {
        instance = new RPGLogger(plugin);
        instance.debugEnabled = debugEnabled;
        instance.log(LoggerLevel.STARTUP, "Logger initialized (debug=" + debugEnabled + ")");
    }

    /** Get the singleton */
    public static RPGLogger get() {
        if (instance == null) {
            throw new IllegalStateException("CustomLogger not initialized!");
        }
        return instance;
    }

    /** Generic log call */
    public void log(LoggerLevel level, String message) {
        if (level == LoggerLevel.DEBUG && !debugEnabled) return;

        String prefix = switch (level) {
            case INFO    -> "&5&l[Primeval] [INFO]: &r";
            case ERROR   -> "&4&l[Primeval] [ERROR]: &r";
            case STARTUP -> "&6&l[Primeval] [STARTUP]: &r";
            case SHUTDOWN-> "&c&l[Primeval] [SHUTDOWN]: &r";
            case DEBUG   -> "&3&l[Primeval] [DEBUG]: &r";
        };
        Bukkit.getConsoleSender().sendMessage(ColourCode.colour(prefix + message));
    }

    /** Convenience methods */
    public void info(String msg)    { log(LoggerLevel.INFO, msg); }
    public void error(String msg)   { log(LoggerLevel.ERROR, msg); }
    public void startup(String msg) { log(LoggerLevel.STARTUP, msg); }
    public void shutdown(String msg){ log(LoggerLevel.SHUTDOWN, msg); }
    public void debug(String msg)   { log(LoggerLevel.DEBUG, msg); }
}