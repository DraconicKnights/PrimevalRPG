package com.primevalrpg.primeval.utils.Desing;

import net.md_5.bungee.api.ChatColor;

public class ColourCode {

    public static String colour(String string) {
        if (string == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
