package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.utils.Data.CustomItemHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

@Commands
public class ItemCommand extends CommandCore {
    public ItemCommand() {
        super("primeval item", "Will allow for giving or creating a custom item", "primeval.admin", 4);
    }

    @Override
    protected void execute(Player sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("§cUsage: /item create <itemMat> <placeMat> <display_name> <glow:true|false> [<metaKey> <metaValue>...]"));
            sender.sendMessage(Component.text("§c       /item give <id>"));
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("give")) {
            if (args.length != 2) {
                sender.sendMessage(ColourCode.colour("&cUsage: /item give <id>"));
                return;
            }
            String key = args[1];
            ItemStack template = CustomItemHandler.getItem(key);
            if (template == null) {
                sender.sendMessage(ColourCode.colour("&cNo custom item found with id '" + key + "'"));
                return;
            }
            sender.getInventory().addItem(template.clone());
            sender.sendMessage(ColourCode.colour("&3Gave you custom item '" + key + "'"));
            return;
        }

        if (sub.equals("create")) {
            if (args.length < 5 || (args.length - 5) % 2 != 0) {
                sender.sendMessage(Component.text("§cUsage: /item create <itemMat> <placeMat> <display_name> <glow:true|false> [<metaKey> <metaValue>...]"));
                return;
            }

            Material itemMat, placeMat;
            try {
                itemMat = Material.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("§cUnknown item material: " + args[1]));
                return;
            }
            try {
                placeMat = Material.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("§cUnknown place‐block material: " + args[2]));
                return;
            }

            String displayName = args[3].replace('_', ' ');
            boolean glow = Boolean.parseBoolean(args[4]);

            Map<String, String> metaMap = new HashMap<>();
            for (int i = 5; i < args.length; i += 2) {
                metaMap.put(args[i], args[i + 1]);
            }
            if (placeMat.isBlock()) {
                metaMap.put("blockType", placeMat.name());
            }

            ItemStack item = ItemBuilder.createMetaItem(itemMat, glow, displayName, metaMap);

            sender.getInventory().addItem(item);

            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            try {
                CustomItemHandler.saveCustomItem(uniqueId, item);
                sender.sendMessage(Component.text("§aCreated custom item §f" + displayName + " §awith ID: §f" + uniqueId));
            } catch (IOException ex) {
                sender.sendMessage(Component.text("§cFailed to save custom item: " + ex.getMessage()));
                RPGLogger.get().error("Failed saving custom item: " + ex.getMessage());
            }
            return;
        }

        sender.sendMessage(ColourCode.colour("&cUnknown subcommand '" + sub + "'. Use create or give."));
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] args) {
        List<String> completions = new ArrayList<>();
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);

        if (args.length == 1) {
            // suggest subcommands
            for (String s : List.of("create", "give")) {
                if (s.startsWith(last)) completions.add(s);
            }
        } else if (args[0].equalsIgnoreCase("give") && args.length == 2) {
            // suggest existing IDs
            for (String id : CustomItemHandler.getAllItems().keySet()) {
                if (id.toLowerCase(Locale.ROOT).startsWith(last)) {
                    completions.add(id);
                }
            }
        } else if (args[0].equalsIgnoreCase("create")) {
            switch (args.length) {
                case 2: // itemMat
                case 3: // placeMat
                    for (Material m : Material.values()) {
                        String name = m.name().toLowerCase();
                        if (name.startsWith(last)) completions.add(name);
                    }
                    break;
                case 5: // glow flag
                    for (String b : List.of("true", "false")) {
                        if (b.startsWith(last)) completions.add(b);
                    }
                    break;
                default:
                    // no suggestions for display name or meta KV
                    break;
            }
        }

        return completions;
    }
}