package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.Player.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.UUID;

/**
 * Player Login and Logout listener
 * Used for grabbing and saving custom player data
 */
@Events
public class PlayerLoginAndLogoutListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PlayerDataManager mgr = PlayerDataManager.getInstance();
        PlayerData pd = mgr.getPlayerData(uuid);
        if (pd != null) {
            mgr.savePlayerData(pd);
        }

        MenuHistory.clear(player);
    }

    private void resetToDefaultAttributes(Player player) {
        for (Attribute attr : Arrays.asList(
                Attribute.GENERIC_MAX_HEALTH,
                Attribute.GENERIC_ATTACK_DAMAGE,
                Attribute.GENERIC_ARMOR,
                Attribute.GENERIC_MOVEMENT_SPEED
        )) {
            if (player.getAttribute(attr) != null) {
                player.getAttribute(attr).getModifiers().stream()
                        .filter(m -> m.getName().startsWith("RPGControl-"))
                        .forEach(m -> player.getAttribute(attr).removeModifier(m));
            }
        }
        player.setAbsorptionAmount(0);
    }
}
