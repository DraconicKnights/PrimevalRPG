package com.primevalrpg.primeval.core.RPGMobs.listeners;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Events;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.entity.FallingBlock;
import org.bukkit.metadata.MetadataValue;

@Events
public class ShockwaveCleanupListener implements Listener {

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fb)) return;
        // check our metadata tag
        for (MetadataValue mv : fb.getMetadata("cmc_shockwave")) {
            if (mv.getOwningPlugin() == PrimevalRPG.getInstance() && mv.asBoolean()) {
                // cancel the placement entirely:
                e.setCancelled(true);
                // or, if you WANT the block to appear briefly then disappear:
                // Location loc = e.getBlock().getLocation();
                // new BukkitRunnable(){
                //   public void run(){ loc.getBlock().setType(Material.AIR, false); }
                // }.runTaskLater(plugin, 2);
                break;
            }
        }
    }
}