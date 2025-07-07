package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.core.enums.EditorAction;
import com.primevalrpg.primeval.utils.Handlers.EditorModeManager;
import com.primevalrpg.primeval.utils.Handlers.Region;
import com.primevalrpg.primeval.utils.Handlers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Events
public class RegionEditorListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (EditorModeManager.isInEditorMode(player)) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (!item.hasItemMeta()) return;

            String itemName = item.getItemMeta().getDisplayName();

            if (itemName.contains("Region Name Tool")) {
                EditorModeManager.setPlayerAction(player, EditorAction.SETTINGS_REGION_NAME);
                player.sendMessage(ChatColor.GREEN + "Please type the name of the new region:");
            }
            else if (itemName.contains("Mob Spawn Tool")) {
                EditorModeManager.setRegionMobs( player);
            }
            else if (itemName.contains("Level Tool")) {
                EditorModeManager.setPlayerAction(player, EditorAction.SETTINGS_REGION_LEVELS);
                player.sendMessage(ChatColor.GREEN + "Please type the level values for the new region:");
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();

                if (clickedBlock != null) {
                    Location clickedLocation = clickedBlock.getLocation();

                    if (itemName.contains("Region Selector")) {
                        // Check if this is first or second corner
                        EditorModeManager.setRegionCorner(player, clickedLocation, !player.isSneaking());
                    }
                }

            } else if (itemName.contains("Save Region")) {
                // Save the region when Nether Star is clicked
                EditorModeManager.saveRegion(player);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (EditorModeManager.isInEditorMode(player)) {
            EditorAction action = EditorModeManager.getPlayerAction(player);

            switch (action) {
                case SETTINGS_REGION_NAME:
                    EditorModeManager.setRegionName(player, event.getMessage());
                    EditorModeManager.setPlayerAction(player, EditorAction.NONE);
                    event.setCancelled(true);
                    break;

                case SETTINGS_REGION_LEVELS:
                    try {
                        String[] levels = event.getMessage().split(" ");
                        if (levels.length != 2) {
                            player.sendMessage(ChatColor.RED + "You must specify two levels only.");
                            return;
                        }

                        int minLevel = Integer.parseInt(levels[0]);
                        int maxLevel = Integer.parseInt(levels[1]);

                        EditorModeManager.setRegionLevels(player, minLevel, maxLevel);
                        EditorModeManager.setPlayerAction(player, EditorAction.NONE);
                        event.setCancelled(true);
                    } catch (NumberFormatException nfe) {
                        player.sendMessage(ChatColor.RED + "Levels must be integers.");
                    }
                    break;
            }
        }
    }

    private static Map<UUID, Region> playerCurrentRegion = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Region playerRegion = RegionManager.getInstance().getRegionFromLocation(player.getLocation());

        // check if playerRegion has changed
        if(playerCurrentRegion.get(player.getUniqueId()) != playerRegion){
            playerCurrentRegion.put(player.getUniqueId(), playerRegion);

            if(playerRegion != null){
                // player entered a region
                player.sendTitle(ChatColor.GREEN + playerRegion.getRegionName(), "", 10, 70, 20);
            }else{
                // player left the region
                player.sendTitle(ChatColor.RED + "Left the region", "", 10, 70, 20);
            }
        }
    }

    // preventing item movement in inventory if player is in Editor Mode
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (EditorModeManager.isInEditorMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    // preventing item drop if player is in Editor Mode
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (EditorModeManager.isInEditorMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (EditorModeManager.isInEditorMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
