package com.primevalrpg.primeval.utils.Desing;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class ColourCode {

    public static String colour(String string) {
        if (string == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * New colour and send method
     * Simple use for sending a formated message to the target player
     * @param string
     * @param player
     */
    public static void colourAndSend(String string, Player player) {
        player.sendMessage(colour(string));
    }

}
