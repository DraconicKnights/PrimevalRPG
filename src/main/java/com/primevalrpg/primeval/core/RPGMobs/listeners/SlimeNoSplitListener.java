package com.primevalrpg.primeval.core.RPGMobs.listeners;

import com.primevalrpg.primeval.core.Annotations.Events;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

@Events
public class SlimeNoSplitListener implements Listener {

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent evt) {
        // Both Slimes and MagmaCubes fire this event.
        if (evt.getEntity().getScoreboardTags().contains("no-split")) {
            evt.setCancelled(true);
        }
    }
}