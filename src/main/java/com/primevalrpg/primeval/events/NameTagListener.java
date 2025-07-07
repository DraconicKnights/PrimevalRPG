package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.utils.NameTagUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Events
public class NameTagListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        NameTagUtil.updateNameTag(event.getPlayer());
    }

}
