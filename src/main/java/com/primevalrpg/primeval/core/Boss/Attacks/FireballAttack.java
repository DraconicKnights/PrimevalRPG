package com.primevalrpg.primeval.core.Boss.Attacks;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Boss.SpecialAttack;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fireball attack will spawn fireballs and launch them towards the players location from the sky
 */
public class FireballAttack extends SpecialAttack {

    public FireballAttack(LivingEntity target) {
        super(target);
    }

    @Override
    protected void executeAttack() {
        List<Player> nearbyPlayers = getAllNearbyPlayers(target.getLocation(), 20);

        for (Player player : nearbyPlayers) {

            final double fireballHeight = player.getLocation().getY() + 20 + new Random().nextInt(11);
            final Location fireballLocation = player.getLocation().clone().add(0, fireballHeight, 0);
            final Fireball fireball = player.getWorld().spawn(fireballLocation, Fireball.class);

            final Vector direction = player.getLocation().toVector().subtract(fireballLocation.toVector()).normalize();

            fireball.setDirection(direction);
            player.sendTitle("§cBOSS ACTION", "§6Boss has used Meteor Storm!", 10, 70, 20);  // Alert player about the event

            new BukkitRunnable() {
                double targetvar = 0;
                Location location, first, second, third, fourth;
                @Override
                public void run() {
                    if (fireball.isDead()) {
                        this.cancel();
                    }

                    targetvar += Math.PI / 16;

                    location = target.getLocation();
                    first = location.clone().add(Math.cos(targetvar), Math.sin(targetvar) + 1, Math.sin(targetvar));
                    second = location.clone().add(Math.cos(targetvar + Math.PI), Math.sin(targetvar) + 1, Math.sin(targetvar + Math.PI));
                    third = location.clone().add( Math.sin(targetvar + Math.PI), Math.cos(targetvar) + 1, Math.sin(targetvar));
                    fourth = location.clone().add(Math.cos(targetvar), Math.cos(targetvar) + 1, Math.sin(targetvar + Math.PI));

                    player.getWorld().spawnParticle(Particle.SMALL_FLAME, first, 0);
                    player.getWorld().spawnParticle(Particle.SMALL_FLAME, second, 0);
                    player.getWorld().spawnParticle(Particle.SMALL_FLAME, third, 0);
                    player.getWorld().spawnParticle(Particle.SMALL_FLAME, fourth, 0);

                }
            }.runTaskTimer(PrimevalRPG.getInstance(), 0L, 1L);

            new BukkitRunnable() {
                double targetvar = 0;
                Location location, first, second;
                @Override
                public void run() {
                    // Check if the fireball has hit the ground
                    if (fireball.isDead()) {
                        this.cancel();
                    }

                    targetvar += Math.PI / 16;

                    location = fireball.getLocation();
                    first = location.clone().add(Math.cos(targetvar), Math.sin(targetvar) + 1, Math.sin(targetvar));
                    second = location.clone().add(Math.cos(targetvar + Math.PI), Math.sin(targetvar) + 1, Math.sin(targetvar + Math.PI));

                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, first, 0);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, second, 0);

                    // Spawn particles at the fireball's location
                    player.getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 10);
                }
            }.runTaskTimer(PrimevalRPG.getInstance(), 0L, 1L);
        }
    }

    protected List<Player> getAllNearbyPlayers(Location location, int radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }
}
