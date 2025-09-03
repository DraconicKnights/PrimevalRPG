package com.primevalrpg.primeval.utils.API;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Scripts.ScriptUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.function.Function;

/**
 * Hook a list of script-lines to fire on any Bukkit event.
 */
public final class ScriptEventAPI {
    private ScriptEventAPI() {}

    /**
     * Generic event-hook.  Caller must supply a function that extracts
     * the "self" LivingEntity out of the event (or returns null to skip).
     */
    public static <E extends Event> void onEvent(
            Plugin plugin,
            Class<E> eventClass,
            Function<E, LivingEntity> selfMapper,
            List<String> scriptLines
    ) {
        PluginManager pm = plugin.getServer().getPluginManager();

        if (plugin instanceof PrimevalRPG) {
            ((PrimevalRPG) plugin).CustomMobLogger(
                    "Plugin '" + plugin.getName() +
                            "' is registering listener for " + eventClass.getSimpleName(),
                    LoggerLevel.INFO
            );
        }
        // marker listener (we don't rely on @EventHandler for better use cases)
        Listener listener = new Listener() {};

        // this executor will only fire for the given eventClass
        EventExecutor executor = (lstnr, ev) -> {
            if (!eventClass.isInstance(ev)) return;
            E e = eventClass.cast(ev);
            LivingEntity self = selfMapper.apply(e);
            if (self == null) return;
            ScriptUtils.execScript(scriptLines, self, List.of(), e);
        };

        pm.registerEvent(
                eventClass,
                listener,
                EventPriority.NORMAL,
                executor,
                plugin
        );
    }

    /**
     * Convenience overload for all EntityEvent subclasses.
     * Will skip any event whose entity isn’t a LivingEntity.
     */
    public static <E extends EntityEvent> void onEntityEvent(
            Plugin plugin,
            Class<E> eventClass,
            List<String> scriptLines
    ) {
        onEvent(
                plugin,
                eventClass,
                e -> {
                    Entity ent = e.getEntity();
                    return (ent instanceof LivingEntity)
                            ? (LivingEntity) ent
                            : null;
                },
                scriptLines
        );
    }

    /**
     * Unregister all listeners registered under this plugin.
     */
    public static void clearAll(Plugin plugin) {
        HandlerList.unregisterAll(plugin);
    }
}