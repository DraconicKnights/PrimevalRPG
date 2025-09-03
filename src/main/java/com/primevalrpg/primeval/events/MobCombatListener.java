package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

@Events
public class MobCombatListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity) e.getDamager();
        CustomMob custom = CustomEntityArrayHandler.getCustomEntities().get(attacker);
        if (custom == null) return;

        double dmg = custom.getBaseAttackDamage();
        if (Math.random() < custom.getCriticalHitChance()) {
            dmg *= 1.5;
            attacker.getWorld().spawnParticle(Particle.CRIT, attacker.getEyeLocation(), 8);
        }

        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) e.getEntity();
            CustomMob targetCustom = CustomEntityArrayHandler.getCustomEntities().get(target);
            double defense = targetCustom != null
                    ? targetCustom.getDefenseValue()
                    : target.getAttribute(Attribute.GENERIC_ARMOR).getValue();
            dmg = Math.max(0, dmg - defense * 0.1);
        }

        e.setDamage(dmg);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        ElementType attackerElement = null;
        Player attackingPlayer = null;

        CustomMob custom = CustomEntityArrayHandler.getCustomEntities().get(e.getEntity());
        if (custom == null) return;

        if (e.getDamager() instanceof Player) {
            attackingPlayer = (Player)e.getDamager();
            attackerElement = PlayerDataManager.getInstance()
                    .getPlayerData(attackingPlayer.getUniqueId())
                    .getActiveElement();
        }
        else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile)e.getDamager();
            if (proj.getShooter() instanceof Player) {
                attackingPlayer = (Player)proj.getShooter();
                attackerElement = PlayerDataManager.getInstance()
                        .getPlayerData(attackingPlayer.getUniqueId())
                        .getActiveElement();
            }
        }
        else if (e.getDamager() instanceof LivingEntity) {
            attackerElement = CustomEntityArrayHandler.getCustomEntities().get(e.getEntity()).getElementType();
        }

        if (attackerElement == null) return;

        ElementType victimElement = null;
        Player victimPlayer = null;

        if (e.getEntity() instanceof Player) {
            victimPlayer = (Player)e.getEntity();
            victimElement = PlayerDataManager.getInstance()
                    .getPlayerData(victimPlayer.getUniqueId())
                    .getActiveElement();
        }
        else if (e.getEntity() instanceof LivingEntity) {
            victimElement = CustomEntityArrayHandler.getCustomEntities().get(e.getEntity()).getElementType();
        }

        if (victimElement == null) return;

        double base = e.getDamage();
        double mult = getElementMultiplier(attackerElement, victimElement);
        e.setDamage(base * mult);
    }

    /** same advantage rules as before */
    private double getElementMultiplier(ElementType atk, ElementType def) {
        if (atk == def) return 1.0;
        switch (atk) {
            case WATER:
                if (def == ElementType.FIRE) return 1.5;
                if (def == ElementType.WIND) return 0.5;
                break;
            case FIRE:
                if (def == ElementType.EARTH) return 1.5;
                if (def == ElementType.WATER) return 0.5;
                break;
            case EARTH:
                if (def == ElementType.WIND) return 1.5;
                if (def == ElementType.FIRE) return 0.5;
                break;
            case WIND:
                if (def == ElementType.WATER) return 1.5;
                if (def == ElementType.EARTH) return 0.5;
                break;
        }
        return 1.0;
    }
}