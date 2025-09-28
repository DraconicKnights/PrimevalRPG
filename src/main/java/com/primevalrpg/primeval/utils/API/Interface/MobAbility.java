package com.primevalrpg.primeval.utils.API.Interface;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Pretty much obsolete outside a small use case with the new scripting logic.
 */
public interface MobAbility {
    void apply(LivingEntity mob, JavaPlugin plugin, int mobLevel);
    void stop();
}
