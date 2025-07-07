package com.primevalrpg.primeval.core.RPGMobs;

import com.primevalrpg.primeval.core.RPGMobs.MobAbilities.ScriptAbility;
import com.primevalrpg.primeval.utils.API.Interface.MobAbility;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobAbilityManager {
    private final LivingEntity mob;
    private final JavaPlugin plugin;
    private final List<AbilitySlot> abilities = new ArrayList<>();
    private final long tickInterval;
    private final int mobLevel;

    private BukkitTask scheduler;

    public MobAbilityManager(LivingEntity mob, JavaPlugin plugin, long tickInterval, int mobLevel) {
        this.mob = mob;
        this.plugin = plugin;
        this.tickInterval = tickInterval;
        this.mobLevel = mobLevel;
    }

    /** Register an ability with its cooldown in ticks */
    public void register(MobAbility ability, long cooldownTicks) {
        abilities.add(new AbilitySlot(ability, cooldownTicks));
    }

    /** Load all abilities defined under “abilities:” in your config */
    public void loadFromConfig() {
        ConfigurationSection global = MobDataHandler.getAbilitiesConfig().getConfigurationSection("abilities");
        if (global == null) {
            plugin.getLogger().warning("No global 'abilities' in config.yml");
            return;
        }

        // lookup your CustomMob wrapper from the running-entities map
        CustomMob customMob = CustomEntityArrayHandler.getCustomEntities().get(mob);
        if (customMob == null) {
            plugin.getLogger().warning("No CustomMob found for entity " + mob.getUniqueId());
            return;
        }

        // only load & register scripts the mob actually declares
        for (String key : customMob.getAbilities()) {
            if (!global.isConfigurationSection(key)) {
                plugin.getLogger().warning("Ability '" + key + "' missing from config");
                continue;
            }
            ConfigurationSection ab = global.getConfigurationSection(key);
            long cdTicks = ab.getLong("cooldown", 0L) * 20L;
            List<String> script = ab.getStringList("script");
            register(new ScriptAbility(mob, script), cdTicks);
        }
    }

    /** Start the scheduler that picks an ability each tickInterval */
    public void start() {
        long tickPeriod = tickInterval * 20L;

        scheduler = new BukkitRunnable() {
            @Override
            public void run() {
                if (!mob.isValid() || mob.isDead()) {
                    // clean up all abilities
                    for (AbilitySlot slot : abilities) {
                        slot.ability.stop();
                    }
                    cancel();                      // cancel this scheduler
                    return;
                }


                long now = System.currentTimeMillis();
                List<AbilitySlot> ready = new ArrayList<>();
                for (AbilitySlot slot : abilities) {
                    if (now >= slot.nextUse) ready.add(slot);
                }
                if (ready.isEmpty()) return;

                AbilitySlot chosen = ready.get(new Random().nextInt(ready.size()));
                chosen.ability.apply(mob, plugin, mobLevel);
                chosen.nextUse = now + chosen.cooldownTicks * 50;
            }
        }.runTaskTimer(plugin, 0L, tickPeriod);
    }


    private static class AbilitySlot {
        final MobAbility ability;
        final long cooldownTicks;
        long nextUse = 0;
        AbilitySlot(MobAbility ability, long cooldownTicks) {
            this.ability = ability;
            this.cooldownTicks = cooldownTicks;
        }
    }

}
