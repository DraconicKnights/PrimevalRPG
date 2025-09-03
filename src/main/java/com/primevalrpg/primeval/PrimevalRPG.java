package com.primevalrpg.primeval;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.Annotations.Runnable;
import com.primevalrpg.primeval.core.CustomEvents.CustomEntityEvent;
import com.primevalrpg.primeval.core.Player.PlayerAbilityManager;
import com.primevalrpg.primeval.core.RPGData.CustomEntityData;
import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.Abilities.AbilityCommands;
import com.primevalrpg.primeval.utils.Data.*;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Handlers.EventManager;
import com.primevalrpg.primeval.utils.Handlers.FlagManager;
import com.primevalrpg.primeval.utils.Handlers.RegionManager;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.Set;

/**
 * Core class and entry point for the Plugin.
 * Handles and deals with object instantiation, managing and logging
 */
public final class PrimevalRPG extends JavaPlugin {

    private static PrimevalRPG Instance;
    private FlagManager flagManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        setInstance();

        getDataFolder().mkdir();

        CoreDataHandler.initialize(this);

        RPGLogger.init(this, CoreDataHandler.debugMode);
        RPGLogger.get().startup("Plugin is initializing");

        flagManager = new FlagManager("flags.yml");
        saveResource("globalEvents.yml", false);
        saveResource("playerAbilities.yml", false);

        registerPluginCore();
        registerObjects();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        CustomMobLogger("Plugin is shutting down", LoggerLevel.SHUTDOWN);

        flagManager.save();
        PlayerDataManager.getInstance().saveAll();
        MobDataHandler.getInstance().RemoveAllMobs();
        CoreDataHandler.saveSettings();
    }

    private void registerPluginCore() {
        new AbilityCommands();
        CustomItemHandler.initialize(this);
        new MobDataHandler();
        new PlayerDataHandler();
        new PlayerDataManager(getDataFolder().toPath());
        new RegionDataHandler();
        new CustomEntityData();
        new CustomMobManager();
        new RegionManager();
        new EventManager(this);
        new PlayerAbilityManager(this);
    }

    private void registerObjects() {
        registerPluginCommands();
        registerEvents();
        registerRunnables();
    }

    private void registerPluginCommands() {
        Reflections reflections = new Reflections("com.primevalrpg.primeval.commands", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customCommandClasses = reflections.getTypesAnnotatedWith(Commands.class);

        for (Class<?> commandClass : customCommandClasses) {
            try {
                commandClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CustomMobLogger("Commands Registered Successfully", LoggerLevel.INFO);
    }

    private void registerEvents() {
        Reflections reflections = new Reflections("com.primevalrpg.primeval.events", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customEventClasses = reflections.getTypesAnnotatedWith(Events.class);

        for (Class<?> eventClass : customEventClasses) {
            try {
                Listener listener = (Listener) eventClass.getDeclaredConstructor().newInstance();
                Bukkit.getServer().getPluginManager().registerEvents( listener, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Reflections reflectionsMobs = new Reflections("com.primevalrpg.primeval.core.RPGMobs.listeners", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customEventMobClasses = reflectionsMobs.getTypesAnnotatedWith(Events.class);

        for (Class<?> eventClass : customEventMobClasses) {
            try {
                Listener listener = (Listener) eventClass.getDeclaredConstructor().newInstance();
                Bukkit.getServer().getPluginManager().registerEvents( listener, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CustomMobLogger("Events Registered Successfully", LoggerLevel.INFO);
    }

    private void registerRunnables() {
        Reflections reflections = new Reflections("com.primevalrpg.primeval.runnables", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customRunnableClasses = reflections.getTypesAnnotatedWith(Runnable.class);

        for (Class<?> runableClass : customRunnableClasses) {
            try {
                runableClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CustomMobLogger("Runnable Events Registered Successfully", LoggerLevel.INFO);
    }

    public void TriggerCustomEvent(Player player, CustomMob customMob) {
        Bukkit.getServer().getPluginManager().callEvent(new CustomEntityEvent(player, customMob));
    }


    public void CustomMobLogger(String log, LoggerLevel loggerLevel) {
        RPGLogger.get().log(loggerLevel, log);
    }

    private void setInstance() {
        Instance = this;
    }

    public FlagManager getFlagManager() {
        return flagManager;
    }

    public static PrimevalRPG getInstance() {
        return Instance;
    }
}
