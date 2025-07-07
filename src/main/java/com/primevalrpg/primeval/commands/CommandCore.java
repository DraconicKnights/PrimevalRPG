package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * CommandCore, Used as a base for all in-game plugins commands with built in cooldown system and perm check
 */
public abstract class CommandCore implements CommandExecutor, TabExecutor {
    protected  String rootName;
    protected String subName;
    protected String permission;
    protected int cooldownDuration;
    protected String description;
    protected Map<UUID, Long> cooldowns = new HashMap<>();

    private static final Map<String, List<CommandCore>> SUBS = new HashMap<>();
    private static final Set<String> ROOTS_REGISTERED = new HashSet<>();


    /**
     * Constructs a new CommandCore instance, representing a command with optional subcommands.
     * Manages command registration, permission requirements, and cooldowns.
     *
     * @param fullName         The full name of the command, which may include a subcommand (e.g., "root sub").
     * @param description      A description of the command, providing details about its purpose or usage.
     * @param perm             The required permission to execute the command, or null if no permission is required.
     * @param cooldownSeconds  The cooldown duration in seconds, preventing rapid re-execution of the command.
     */
    public CommandCore(String fullName, String description, @Nullable String perm, int cooldownSeconds) {
        String[] parts = fullName.split(" ", 2);
        this.rootName        = parts[0].toLowerCase();
        this.subName         = (parts.length > 1 ? parts[1].toLowerCase() : null);
        this.description     = description;
        this.permission      = perm;
        this.cooldownDuration = cooldownSeconds;

        if (subName == null) {
            // single-word command: register normally
            PrimevalRPG.getInstance()
                    .getCommand(rootName)
                    .setExecutor(this);
            PrimevalRPG.getInstance()
                    .getCommand(rootName)
                    .setTabCompleter(this);
        } else {
            // sub-command
            SUBS.computeIfAbsent(rootName, k -> new ArrayList<>())
                    .add(this);

            if (ROOTS_REGISTERED.add(rootName)) {
                PluginCommand rootCmd = PrimevalRPG.getInstance().getCommand(rootName);
                rootCmd.setExecutor(CommandCore::dispatch);
                rootCmd.setTabCompleter(CommandCore::complete);
            }
        }
    }

    private static boolean dispatch(CommandSender sender,
                                    Command cmd,
                                    String label,
                                    String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return true;
        }
        Player p = (Player) sender;
        String root = cmd.getName().toLowerCase();
        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Usage: /" + root + " <" +
                    String.join("|",
                            SUBS.getOrDefault(root, Collections.emptyList())
                                    .stream().map(c->c.subName).toList()
                    ) + ">");
            return true;
        }
        String wanted = args[0].toLowerCase();
        for (CommandCore sub : SUBS.getOrDefault(root, Collections.emptyList())) {
            if (sub.subName.equals(wanted)) {
                // permission
                if (sub.permission != null && !p.hasPermission(sub.permission)) {
                    p.sendMessage(ChatColor.RED +
                            "No permission to use this sub-command");
                    return true;
                }
                // cooldown
                UUID id = p.getUniqueId();
                long now = System.currentTimeMillis();
                Long end = sub.cooldowns.get(id);
                if (end != null && end > now) return true;
                sub.cooldowns.put(id, now + sub.cooldownDuration * 1000L);

                // invoke user’s execute
                String[] tail = Arrays.copyOfRange(args, 1, args.length);
                sub.execute(p, tail);
                return true;
            }
        }
        p.sendMessage(ChatColor.RED + "Unknown sub‐command “" + args[0] + "”");
        return true;
    }

    private static @Nullable List<String> complete(CommandSender sender,
                                                   Command cmd,
                                                   String alias,
                                                   String[] args) {
        String root = cmd.getName().toLowerCase();
        if (args.length == 1) {
            String pref = args[0].toLowerCase();
            return SUBS.getOrDefault(root, Collections.emptyList()).stream()
                    .map(c->c.subName)
                    .filter(n->n.startsWith(pref))
                    .toList();
        }
        if (args.length > 1) {
            String wanted = args[0].toLowerCase();
            for (CommandCore sub : SUBS.getOrDefault(root, Collections.emptyList())) {
                if (sub.subName.equals(wanted)) {
                    String[] tail = Arrays.copyOfRange(args, 1, args.length);
                    return sub.commandCompletion((Player)sender, cmd, tail);
                }
            }
        }
        return Collections.emptyList();
    }

    protected abstract void execute(Player player, String[] args);
    protected abstract List<String> commandCompletion(Player player, Command command, String[] strings);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            if (!player.hasPermission(this.permission)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command, please contact a server administrator");
                return true;
            }

            UUID playerID = player.getUniqueId();
            if (cooldowns.containsKey(playerID)) {
                long cooldownEnds = cooldowns.get(playerID);
                if (cooldownEnds > System.currentTimeMillis()) {
                    return true;
                }
            }
            cooldowns.put(playerID, System.currentTimeMillis() + cooldownDuration * 1000);

            execute(player, strings);
        } else {
            commandSender.sendMessage(ChatColor.RED + "Only players can use this command");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return commandCompletion((Player) commandSender, command, strings);
    }

    /** Expose for HelpCommand */
    public String getDescription() {
        return description.isEmpty() ? "No description set" : description;
    }

    public String getPermission() {
        return permission;
    }

}