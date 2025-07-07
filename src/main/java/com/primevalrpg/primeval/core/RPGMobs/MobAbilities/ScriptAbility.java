package com.primevalrpg.primeval.core.RPGMobs.MobAbilities;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.utils.API.Interface.MobAbility;
import com.primevalrpg.primeval.utils.Scripts.DefaultExecutors;
import com.primevalrpg.primeval.utils.Scripts.ScriptCommand;
import com.primevalrpg.primeval.utils.Scripts.ScriptContext;
import com.primevalrpg.primeval.utils.Scripts.ScriptParser;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptAbility implements MobAbility, Listener {

    private final LivingEntity mob;
    private final DefaultExecutors executors = DefaultExecutors.getShared();

    // commands that run every tick
    private final List<ScriptCommand> tickCommands = new ArrayList<>();

    // commands keyed by the event name they listen to
    private final Map<String, List<ScriptCommand>> eventCommands = new HashMap<>();


    public ScriptAbility(LivingEntity mob, List<String> rawLines) {
        this.mob    = mob;

        // parse and split into tick vs. event-driven
        for (ScriptCommand cmd : ScriptParser.parse(rawLines)) {
            if (cmd.triggerEvent == null) {
                tickCommands.add(cmd);
            } else {
                eventCommands
                        .computeIfAbsent(cmd.triggerEvent, k -> new ArrayList<>())
                        .add(cmd);
            }
        }

        // register one listener per distinct event type
        eventCommands.keySet().forEach(this::registerEventListener);

    }

    @Override
    public void apply(LivingEntity ignored, JavaPlugin plugin, int mobLevel) {
        // find nearby players once per tick
        Collection<LivingEntity> nearbyAll =
                mob.getWorld()
                        .getNearbyEntities(mob.getLocation(), 20,20,20).stream()
                        .filter(ent -> ent instanceof LivingEntity)
                        .map(ent -> (LivingEntity)ent)
                        .collect(Collectors.toList());

        ScriptContext ctx = new ScriptContext(
                tickCommands,
                mob,
                nearbyAll,
                null,
                executors,
                PrimevalRPG.getInstance()
        );

        ctx.run();
    }

    private void registerEventListener(String eventName) {
        Class<? extends Event> evClass = null;
        // try entity package
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Event> cls =
                    (Class<? extends Event>) Class.forName("org.bukkit.event.entity." + eventName);
            evClass = cls;
        } catch (ClassNotFoundException e1) {
            // fallback to player package
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Event> cls =
                        (Class<? extends Event>) Class.forName("org.bukkit.event.player." + eventName);
                evClass = cls;
            } catch (ClassNotFoundException e2) {
                PrimevalRPG.getInstance().getLogger()
                        .warning("ScriptAbility: unknown event “" + eventName + "”");
                return;
            }
        }

        Class<? extends Event> finalEvClass = evClass;
        PrimevalRPG.getInstance()
                .getServer()
                .getPluginManager()
                .registerEvent(
                        evClass,
                        this,
                        EventPriority.NORMAL,
                        (listener, rawEvent) -> {
                            if (!finalEvClass.isInstance(rawEvent)) return;
                            if (rawEvent instanceof EntityEvent ee
                                    && !ee.getEntity().equals(this.mob)) return;

                            Collection<LivingEntity> nearbyAll = mob.getWorld()
                                    .getNearbyEntities(mob.getLocation(), 20,20,20).stream()
                                    .filter(e -> e instanceof LivingEntity)
                                    .map(e -> (LivingEntity)e)
                                    .collect(Collectors.toList());

                            List<ScriptCommand> commands = eventCommands.get(rawEvent.getEventName());

                            ScriptContext ctx = new ScriptContext(
                                    commands,
                                    mob,
                                    nearbyAll,
                                    rawEvent,
                                    executors,
                                    PrimevalRPG.getInstance()
                            );
                            ctx.run();
                        },
                        PrimevalRPG.getInstance()
                );
    }


    @Override
    public void stop() {
        // no cleanup needed for these scripts
        HandlerList.unregisterAll(this);
    }

}
