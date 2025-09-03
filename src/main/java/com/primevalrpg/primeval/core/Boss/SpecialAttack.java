package com.primevalrpg.primeval.core.Boss;

import org.bukkit.entity.LivingEntity;

/**
 * Abstract special attack object to be used for all Boss mob attacks
 */
public abstract class SpecialAttack {

    protected LivingEntity target;

    public SpecialAttack(LivingEntity target) {
        this.target = target;
    }

    protected abstract void executeAttack();

}
