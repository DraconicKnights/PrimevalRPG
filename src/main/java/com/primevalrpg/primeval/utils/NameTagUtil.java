package com.primevalrpg.primeval.utils;

import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Data.CoreDataHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagUtil {
    private static final Scoreboard BOARD = Bukkit.getScoreboardManager().getMainScoreboard();

    /** scoreboard nametag: 🔥 [10] PlayerName */
    public static void updateNameTag(Player player) {

        if (!CoreDataHandler.levelingEnable) return;

        PlayerData d  = PlayerDataManager.getInstance().getPlayerData(player.getUniqueId());
        ElementType e = d.getActiveElement();
        int level     = d.getOverallLevel();

        // element icon, then reset to white for bracket
        ChatColor elemColor   = e != null ? e.getColor() : ChatColor.WHITE;
        String elementIcon    = elemColor + e.getIcon() + ChatColor.WHITE + " ";

        // tier‐based color for level
        ChatColor tierColor;
        if (level <= 10)      tierColor = ChatColor.WHITE;
        else if (level <= 20) tierColor = ChatColor.GREEN;
        else if (level <= 30) tierColor = ChatColor.GOLD;
        else if (level <= 40) tierColor = ChatColor.DARK_PURPLE;
        else if (level <= 50) tierColor = ChatColor.AQUA;
        else                   tierColor = ChatColor.RED;

        // gray brackets
        ChatColor bracketColor = ChatColor.GOLD;
        String bracketOpen  = bracketColor + "[";
        String bracketClose = bracketColor + "] ";

        // build: elementIcon + [level] + name color
        String prefix = elementIcon
                + bracketOpen
                + tierColor + level
                + bracketClose
                + ChatColor.BLUE;

        String teamName = "ptag_" + player.getName();
        Team t = BOARD.getTeam(teamName);
        if (t == null) {
            t = BOARD.registerNewTeam(teamName);
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
        t.setPrefix(prefix);
        if (!t.hasEntry(player.getName())) {
            t.addEntry(player.getName());
        }
    }

    /** chat prefix: 🔥 [10] PlayerName: */
    public static String getChatPrefix(Player player) {

        PlayerData d  = PlayerDataManager.getInstance().getPlayerData(player.getUniqueId());
        ElementType e = d.getActiveElement();
        int level     = d.getOverallLevel();

        // element icon up front
        ChatColor elemColor = e != null ? e.getColor() : ChatColor.WHITE;
        String elementIcon  = elemColor + e.getIcon() + ChatColor.WHITE + " ";

        // tier‐based color for level
        String tierCode;
        if (level <= 10)      tierCode = "&f";
        else if (level <= 20) tierCode = "&a";
        else if (level <= 30) tierCode = "&6";
        else if (level <= 40) tierCode = "&5";
        else if (level <= 50) tierCode = "&b";
        else                  tierCode = "&c";

        // bracket styling
        String open       = ColourCode.colour("&6[&r");
        String levelPart  = ColourCode.colour(tierCode + level + "&r");
        String close      = ColourCode.colour("&6]&r ");

        return elementIcon
                + open + levelPart + close
                + ChatColor.BLUE + player.getName()
                + ChatColor.WHITE + ": ";
    }

}
