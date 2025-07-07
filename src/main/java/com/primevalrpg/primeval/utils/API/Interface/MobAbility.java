package com.primevalrpg.primeval.utils.API.Interface;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public interface MobAbility {
    /**
     * Register/apply this ability to the given mob.
     * E.g. schedule repeating tasks, potion effects, etc.
     */
    void apply(LivingEntity mob, JavaPlugin plugin, int mobLevel);
    void stop();             // Optional: to cancel any scheduled tasks

}
