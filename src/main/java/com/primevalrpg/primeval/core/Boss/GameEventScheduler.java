package com.primevalrpg.primeval.core.Boss;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Boss.Attacks.FireballAttack;
import com.primevalrpg.primeval.core.Boss.Attacks.MultiMeteorAttack;
import com.primevalrpg.primeval.core.Boss.Attacks.TeleportAttack;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Runnable.RunnableCore;
import org.bukkit.entity.LivingEntity;

import java.util.*;

/**
 *  GameEventScheduler used for managing and handling custom boss attacks via the in-built RunnableCore
 *  This was the original test for mob abilities which follow a hardcoded apporach.
 */
@Deprecated
public class GameEventScheduler extends RunnableCore {
    private static Map<UUID, SpecialAttack> specialAttacks = new HashMap<>();
    private LivingEntity target;
    private String[] attacks;

    public GameEventScheduler(LivingEntity target, String... attacks) {
        super(PrimevalRPG.getInstance(), 0, 20);

        this.target = target;
        this.attacks = attacks;
        this.startTimedTask();
    }

    @Override
    protected void event() {
        if (!target.isDead()) {
            scheduleSpecialAttack(target, attacks);
            PrimevalRPG.getInstance().CustomMobLogger("Scheduled a special attack for: " + target.getName(), LoggerLevel.INFO);
        } else {
            this.cancel();
            PrimevalRPG.getInstance().CustomMobLogger("The target is dead. Stopped scheduling attacks for: " + target.getName(), LoggerLevel.INFO);
        }
    }

    public static void scheduleSpecialAttack(LivingEntity target, String... attacks) {
        Random random = new Random();
        String selectedAttack = attacks[random.nextInt(attacks.length)];

        SpecialAttack attack;
        switch (selectedAttack) {
            case "FIREBALL_ATTACK":
                attack = new FireballAttack(target);
                PrimevalRPG.getInstance().CustomMobLogger("Fireball Attack has been set Active for: " + target.getName(), LoggerLevel.INFO);
                break;

            case "TELEPORT_ATTACK":
                attack = new TeleportAttack(target);
                PrimevalRPG.getInstance().CustomMobLogger("Teleport Attack has been set Active for: " + target.getName(), LoggerLevel.INFO);
                break;

            case "METEOR_ATTACK":
                attack = new MultiMeteorAttack(target);
                PrimevalRPG.getInstance().CustomMobLogger("Meteor Attack has been set Active for: " + target.getName(), LoggerLevel.INFO);
                break;


            default:
                System.err.println("Unknown attack type: " + selectedAttack);
                return;
        }

        attack.executeAttack();
        specialAttacks.put(target.getUniqueId(), attack);
    }
}
