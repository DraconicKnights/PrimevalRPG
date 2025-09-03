package com.primevalrpg.primeval.core.Boss.Attacks;

import com.primevalrpg.primeval.core.Boss.SpecialAttack;
import com.primevalrpg.primeval.core.RPGMobs.BossMob;
import org.bukkit.entity.LivingEntity;

public class BossBarActive extends SpecialAttack {
    public BossBarActive(LivingEntity target) {
        super(target);
    }

    @Override
    protected void executeAttack() {
        if (this.target instanceof BossMob) {
            BossMob boss = (BossMob) this.target;
            boss.updateNearbyPlayers();
            double progress = boss.getHealth() / boss.getMaxHealth();
            boss.getBossBar().progress((float) progress);
        }
    }
}
