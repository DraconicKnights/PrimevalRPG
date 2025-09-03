package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.core.Annotations.Events;
import com.primevalrpg.primeval.utils.Data.CoreDataHandler;
import com.primevalrpg.primeval.utils.NameTagUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Events
public class PlayerChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!CoreDataHandler.levelingEnable) return;
        String prefix = NameTagUtil.getChatPrefix(e.getPlayer());
        e.setFormat(prefix + "%2$s");
    }

}
