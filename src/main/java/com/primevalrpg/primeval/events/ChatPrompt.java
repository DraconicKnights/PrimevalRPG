package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Events
public class ChatPrompt implements Listener {

    // track who is typing and what to do with their input
    private static final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    /**
     * Call this to send your user a prompt and wait for their next chat message.
     * Once they send one message, the callback will fire and the prompt will be removed.
     */
    public static void prompt(Player player, String promptMessage, Consumer<String> callback) {
        // register us the first time
        pending.put(player.getUniqueId(), callback);
        player.sendMessage(promptMessage);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Consumer<String> cb = pending.remove(e.getPlayer().getUniqueId());
        if (cb != null) {
            e.setCancelled(true);           // hide it from global chat
            String message = e.getMessage().trim();
            // run callback back on the main thread
            Bukkit.getScheduler()
                    .runTask(PrimevalRPG.getInstance(), () -> cb.accept(message));
        }
    }
}