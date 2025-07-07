package com.primevalrpg.primeval.core.CustomEvents;

import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomMobCombatEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    // The custom mob involved in the combat
    private final CustomMob customMob;

    // The target entity being attacked
    private final LivingEntity target;

    // The base damage value from the mob's attributes
    private final double baseDamage;

    // A modifiable damage value that listeners can adjust, e.g. applying critical multipliers or defense reductions
    private double modifiedDamage;

    private boolean cancelled;

    /**
     * Constructs a new CustomMobCombatEvent.
     *
     * @param customMob      The custom mob that is attacking or being attacked.
     * @param target         The target entity of the combat.
     * @param baseDamage     The base damage value from the mob.
     */
    public CustomMobCombatEvent(CustomMob customMob, LivingEntity target, double baseDamage) {
        this.customMob = customMob;
        this.target = target;
        this.baseDamage = baseDamage;
        this.modifiedDamage = baseDamage;
        this.cancelled = false;
    }

    /**
     * Gets the custom mob involved in this event.
     *
     * @return the custom mob
     */
    public CustomMob getCustomMob() {
        return customMob;
    }

    /**
     * Gets the target entity of the combat.
     *
     * @return the target entity
     */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the base damage value of the combat.
     *
     * @return the base damage value
     */
    public double getBaseDamage() {
        return baseDamage;
    }

    /**
     * Gets the modified damage value that may be adjusted by listeners.
     *
     * @return the modified damage
     */
    public double getModifiedDamage() {
        return modifiedDamage;
    }

    /**
     * Sets the modified damage value. This can be used by event listeners to adjust the final damage.
     *
     * @param modifiedDamage the modified damage to set
     */
    public void setModifiedDamage(double modifiedDamage) {
        this.modifiedDamage = modifiedDamage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
