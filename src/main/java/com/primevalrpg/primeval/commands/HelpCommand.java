package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Commands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.List;
import java.util.Set;

@Commands
public class HelpCommand extends CommandCore {

    public HelpCommand() {
        super("primeval help", "Base help command for PrimevalRPG","primeval.default", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        ChatColor primary   = ChatColor.GOLD;
        ChatColor header    = ChatColor.GREEN;
        ChatColor nameColor = ChatColor.YELLOW;
        ChatColor cmdColor  = ChatColor.AQUA;
        ChatColor text      = ChatColor.WHITE;

        String bar = primary + "========================================";
        player.sendMessage(bar);
        player.sendMessage(header + "        Primeval Commands Help        ");
        player.sendMessage(bar);

        Reflections reflections = new Reflections(
                "com.primevalrpg.primeval.commands",
                new TypeAnnotationsScanner(),
                new SubTypesScanner()
        );
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Commands.class);
        PrimevalRPG plugin = PrimevalRPG.getInstance();

        for (Class<?> cls : commandClasses) {
            try {
                CommandCore cmd = (CommandCore) cls.getDeclaredConstructor().newInstance();

                String usage = "/" + cmd.rootName +
                        (cmd.subName != null ? " " + cmd.subName : "");
                String desc  = cmd.getDescription();
                String perm  = cmd.getPermission();

                player.sendMessage(
                        nameColor + usage +
                                text + " - " +
                                cmdColor + desc
                               /* text + " (perm: " + perm + ")"*/
                );
            } catch (Exception ignored) { }
        }

        player.sendMessage(bar);
    }


    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return List.of();
    }

}
