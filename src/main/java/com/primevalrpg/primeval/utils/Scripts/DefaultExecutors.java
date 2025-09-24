package com.primevalrpg.primeval.utils.Scripts;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.CustomMobManager;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.Handlers.FlagManager;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultExecutors {
    private final Map<String, CommandExecutor> executors = new HashMap<>();
    private final Map<String, BossBar> bossBars = new HashMap<>();

    private static final Map<UUID, Integer> playerSpawnCounts = new HashMap<>();

    private static final double DEFAULT_TARGET_RADIUS = 16.0;
    private static final int DEFAULT_MAX_PER_PLAYER = 10;

    public static List<String> getAllSoundNames() {
        org.bukkit.Sound[] sounds = org.bukkit.Sound.values();
        List<String> names = new ArrayList<>(sounds.length);
        for (org.bukkit.Sound s : sounds) {
            names.add(s.name());
        }
        return names;
    }

    public DefaultExecutors() {
        executors.put("fireball", this::execFireball);
        executors.put("lightning", this::execLightning);
        executors.put("lightning_safe", this::execSafeLightning);
        executors.put("sound", this::execSound);
        executors.put("teleport", this::execTeleport);
        executors.put("particle", this::execParticle);
        executors.put("heal", this::execHeal);
        executors.put("effect", this::execEffect);
        executors.put("knockback", this::execKnockback);
        executors.put("setblock",  this::execSetBlock);
        executors.put("repeat",        this::execRepeat);

        executors.put("velocity", this::execVelocity);
        executors.put("damage",   this::execDamage);
        executors.put("spawn",    this::execSpawn);
        executors.put("world_time", this::execWorldTime);
        executors.put("weather",    this::execWeather);

        executors.put("drop_item",  this::execDropItem);
        executors.put("invisibility", this::execInvisibility);
        executors.put("message", this::execMessage);
        executors.put("condition", this::execCondition);

        executors.put("ignite", this::execIgnite);
        executors.put("invulnerable", this::execInvulnerable);

        executors.put("title", this::execTitle);
        executors.put("actionbar", this::execActionBar);
        executors.put("attribute", this::execAttribute);
        executors.put("clearEffects", this::execClearEffects);

        executors.put("fallingblock", this::execFallingBlock);

        executors.put("flagSet",       this::execFlagSet);
        executors.put("flagClear",     this::execFlagClear);
        executors.put("cancelEvent",   this::execCancelEvent);
        executors.put("eventWave",     this::execEventWave);

        executors.put("spawnCustomMob", this::execSpawnCustomMob);
        executors.put("command", this::execRunCommand);
        executors.put("removeItem", this::execRemoveItem);

        executors.put("delay", this::execDelay);
        executors.put("schedule", this::execSchedule);

        // More features planned.
    }

    public CommandExecutor get(String name) {
        return executors.get(name);
    }

    /**
     * Executes the logic to spawn and direct a fireball based on the given command.
     * The fireball's direction, speed, and spawn location can be determined by the parameters
     * provided in the command.
     *
     * @param cmd the script command containing parameters for fireball spawning, such as direction, location, speed, and offsets
     * @param ctx the script context providing access to entities, locations, and other environmental context information
     */
    private void execFireball(ScriptCommand cmd, ScriptContext ctx) {
        String dir   = cmd.getArg("direction", "forward").toLowerCase();
        double speed = cmd.getDouble("speed", 1.0);
        Vector velocity;

        if (cmd.hasArg("location")) {
            List<String> locTokens = Collections.singletonList(cmd.getArg("location"));
            Collection<LivingEntity> locEntities = resolveEntities(locTokens, ctx.self, ctx.nearby, ctx.event);

            for (LivingEntity target : locEntities) {
                Location spawnLoc = target.getLocation().clone()
                        .add(
                                cmd.getDouble("offsetX", 0.0),
                                cmd.getDouble("offsetY", 10.0),
                                cmd.getDouble("offsetZ", 0.0)
                        );

                switch (dir) {
                    case "down":
                        velocity = new Vector(0, -1, 0).multiply(speed);
                        break;
                    case "towards":
                        velocity = target.getLocation().toVector()
                                .subtract(ctx.self.getLocation().toVector())
                                .normalize()
                                .multiply(speed);
                        break;
                    case "away":
                        velocity = ctx.self.getLocation().toVector()
                                .subtract(target.getLocation().toVector())
                                .normalize()
                                .multiply(speed);
                        break;
                    default:
                        velocity = ctx.self.getLocation().getDirection().multiply(speed);
                }

                Fireball fb = spawnLoc.getWorld().spawn(spawnLoc, Fireball.class);
                fb.setDirection(velocity);
                fb.setYield(0);
            }
            return;
        }

        Location origin = ctx.self.getLocation().add(0, 1.5, 0);
        if ("towards".equals(dir) || "away".equals(dir)) {
            Location lookLoc = ctx.self.getTargetBlock(null, 50).getLocation();
            Vector delta = lookLoc.toVector().subtract(origin.toVector());
            velocity = delta.normalize().multiply(speed);
            if ("away".equals(dir)) {
                velocity.multiply(-1);
            }
        } else {
            velocity = ctx.self.getLocation().getDirection().multiply(speed);
        }

        Fireball fb = origin.getWorld().spawn(origin, Fireball.class);
        fb.setDirection(velocity);
        fb.setYield(0);
    }

    /**
     * Executes a delay operation for a given script command. This method schedules
     * the execution of a nested script command after a specified delay or processes
     * the remaining script commands after the delay.
     *
     * @param cmd the script command containing details such as the delay duration
     *            (ticks) and the optional nested command to execute after the delay
     * @param ctx the script context providing access to the remaining commands and
     *            contextual data
     */
    private void execDelay(ScriptCommand cmd, ScriptContext ctx) {
        int    ticks  = cmd.getInt("ticks", 0);
        String thenC  = cmd.getArg("then", "").trim();

        if (!thenC.isEmpty()) {
            // schedule exactly one nested sub‐command
            new BukkitRunnable(){
                @Override public void run() {
                    ScriptCommand nested = ScriptParser.parseLine(thenC);
                    CommandExecutor e = get(nested.getName());
                    if (e != null) e.execute(nested, ctx);
                }
            }.runTaskLater(PrimevalRPG.getInstance(), ticks);

        } else {
            // consume the rest of the script now…
            List<ScriptCommand> tail = ctx.takeRemaining();
            new BukkitRunnable(){
                @Override public void run() {
                    for (ScriptCommand next : tail) {
                        CommandExecutor e = get(next.getName());
                        if (e != null) e.execute(next, ctx);
                    }
                }
            }.runTaskLater(PrimevalRPG.getInstance(), ticks);
        }
    }

    /**
     * Schedules and executes a series of commands with optional delay, repetition, or cron expression.
     * Handles delayed execution of commands or repeated execution based on given parameters.
     *
     * @param cmd The {@link ScriptCommand} containing execution parameters and optional commands to run.
     *            Parameters include:
     *            - "delay": Initial delay in ticks before executing the task (default: 0).
     *            - "repeat": Interval in ticks for repeated execution (default: 0).
     *            - "count": Maximum number of times to execute the task (default: -1, meaning unlimited repetitions).
     *            - "cron": Cron-like expression for scheduling the task (optional).
     *            - "then": Subcommand to execute after delay or repetition (optional).
     * @param ctx The {@link ScriptContext} providing the execution context, including reference to the current plugin,
     *            the executing entity, nearby entities, events, and other relevant execution details.
     */
    private void execSchedule(ScriptCommand cmd, ScriptContext ctx) {
        Plugin plugin = ctx.plugin;

        int delayTicks = cmd.getInt("delay", 0);
        int interval   = cmd.getInt("repeat", 0);
        int maxRuns    = cmd.getInt("count", -1);
        String cronExp = cmd.getArg("cron");

        List<ScriptCommand> taskCommands;
        if (cmd.hasArg("then")) {
            taskCommands = Collections.singletonList(
                    ScriptParser.parseLine(cmd.getArg("then"))
            );
        } else {
            taskCommands = ctx.takeRemaining();
        }

        Runnable trigger = () -> {
            ScriptContext child = new ScriptContext(
                    taskCommands, ctx.self, ctx.nearby, ctx.event,
                    ctx.executors, plugin
            );
            child.run();
        };

        if (cronExp != null) {
        } else {
            new BukkitRunnable() {
                int runs = 0;

                @Override
                public void run() {
                    if (maxRuns >= 0 && runs++ >= maxRuns) {
                        cancel();
                        return;
                    }
                    trigger.run();
                }
            }
                    .runTaskTimer(plugin, delayTicks, interval);
        }
    }

    /**
     * Executes a scripted run command for a collection of players. This method interpolates
     * the provided command string with contextual data and executes it for the resolved target players.
     *
     * @param cmd the script command containing the command to be executed and targeting details
     * @param ctx the context in which the command is executed, including the subject, nearby entities, and event data
     */
    private void execRunCommand(ScriptCommand cmd, ScriptContext ctx) {
        String asMode = cmd.args.getOrDefault("as", "target").toLowerCase();
        String raw   = cmd.args.get("cmd");
        if (raw == null) return;

        String interpolated = interpolate(raw, ctx.self, ctx.nearby, ctx.event);

        switch (asMode) {
            case "console":
                ctx.plugin.getServer().dispatchCommand(ctx.plugin.getServer().getConsoleSender(), interpolated);
                break;

            case "self":
                var player = (Player) ctx.self;
                player.performCommand(interpolated);
                break;

            case "target":
            default:
                Collection<Player> players = resolvePlayers(
                        cmd.targets, ctx.self, ctx.nearby, ctx.event
                );
                for (Player p : players) {
                    p.performCommand(interpolated);
                }
                break;
        }
    }

    /**
     * Executes the removal of items from players' inventories based on specific criteria
     * provided in the {@link ScriptCommand}.
     *
     * @param cmd the script command containing arguments such as item key, item value, amount,
     *            and target players. This defines the items to be removed and the context of the operation.
     * @param ctx the script context providing additional information such as the initiator,
     *            nearby entities, and event data needed to resolve targets and handle the operation.
     */
    private void execRemoveItem(ScriptCommand cmd, ScriptContext ctx) {
        String key   = cmd.getArg("itemKey");
        String value = cmd.getArg("itemValue");
        if (key == null || value == null) return;
        int toRemove = parseInt(cmd.getArg("amount"), 1, 1);

        NamespacedKey nk = new NamespacedKey(PrimevalRPG.getInstance(), key);
        Collection<Player> players = resolvePlayers(
                cmd.targets, ctx.self, ctx.nearby, ctx.event
        );

        for (Player p : players) {
            ItemStack[] contents = p.getInventory().getContents();
            for (int i = 0; i < contents.length && toRemove > 0; i++) {
                ItemStack item = contents[i];
                if (item == null) continue;
                ItemMeta im = item.getItemMeta();
                if (im == null) continue;
                String stored = im.getPersistentDataContainer()
                        .get(nk, PersistentDataType.STRING);
                if (value.equals(stored)) {
                    int removeAmt = Math.min(toRemove, item.getAmount());
                    item.setAmount(item.getAmount() - removeAmt);
                    toRemove -= removeAmt;
                    if (item.getAmount() <= 0) {
                        contents[i] = null;
                    }
                }
            }
            p.getInventory().setContents(contents);
        }
    }

    /**
     * Executes the process of setting a flag for the specified entities
     * based on the command details and context provided.
     *
     * @param cmd the ScriptCommand containing arguments and target details necessary for flag setting
     * @param ctx the ScriptContext providing contextual information such as the invoking entity or event
     */
    private void execFlagSet(ScriptCommand cmd, ScriptContext ctx) {
        String flagName = cmd.getArg("name");
        String flagValue = cmd.getArg("value");

        if (flagName == null) {
            return; // No flag name provided
        }

        // Check if @server is in targets
        if (hasServerTarget(cmd.targets)) {
            FlagManager flagManager = PrimevalRPG.getInstance().getFlagManager();

            if (flagValue != null) {
                // Set server flag with value
                flagManager.setServerFlagValue(flagName, flagValue);
            } else {
                // Set server flag as boolean
                flagManager.setServerFlag(flagName);
            }
            return;
        }

        // Handle regular entity targets
        Collection<LivingEntity> entities = resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        for (LivingEntity entity : entities) {
            if (entity instanceof Player player) {
                FlagManager flagManager = PrimevalRPG.getInstance().getFlagManager();

                if (flagValue != null) {
                    // Set flag with value
                    flagManager.setFlagValue(player.getUniqueId(), flagName, flagValue);
                } else {
                    // Set flag as boolean
                    flagManager.setFlag(player.getUniqueId(), flagName);
                }
            }
        }
    }


    /**
     * Clears a specified flag for a list of living entities resolved based on the provided script command and context.
     * The flag to be cleared is determined by the "name" argument from the ScriptCommand.
     * If the flag name is not provided or is null, the method exits without performing any action.
     *
     * @param cmd the ScriptCommand containing the arguments and targets required for resolving entities and specifying the flag to clear
     * @param ctx the ScriptContext providing information about the script execution environment, including self, nearby entities, and event data
     */
    private void execFlagClear(ScriptCommand cmd, ScriptContext ctx) {
        String flagName = cmd.getArg("name");

        if (flagName == null) {
            return; // No flag name provided
        }

        // Check if @server is in targets
        if (hasServerTarget(cmd.targets)) {
            FlagManager flagManager = PrimevalRPG.getInstance().getFlagManager();
            flagManager.clearServerFlag(flagName);
            flagManager.clearServerFlagValue(flagName);
            return;
        }

        // Handle regular entity targets
        Collection<LivingEntity> entities = resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        for (LivingEntity entity : entities) {
            if (entity instanceof Player player) {
                FlagManager flagManager = PrimevalRPG.getInstance().getFlagManager();
                flagManager.clearFlag(player.getUniqueId(), flagName);
                flagManager.clearFlagValue(player.getUniqueId(), flagName);
            }
        }
    }

    /**
     * Cancels the event associated with the provided {@link ScriptContext}
     * if the event is an instance of {@link Cancellable}.
     *
     * @param cmd the script command being executed
     * @param ctx the context containing the event to be potentially cancelled
     */
    private void execCancelEvent(ScriptCommand cmd, ScriptContext ctx) {
        if (ctx.event instanceof Cancellable) {
            ((Cancellable) ctx.event).setCancelled(true);
        }
    }

    /**
     * Executes a wave of entity spawning events based on the provided script command and context.
     * The method supports spawning various entity types, including support for falling blocks with specific materials.
     * Additionally, various parameters define the spawning behavior, such as count, radius, delay, rounds, and optional features
     * like random directions and spawning entities behind solid blocks.
     *
     * @param cmd The {@link ScriptCommand} containing arguments to configure the entity wave execution.
     *            Example arguments: "type" (entity type), "material" (for falling blocks),
     *            "count" (number of entities per wave), "radius" (spawn radius), "delay" (time between rounds),
     *            "rounds" (total number of waves), "yOffset" (vertical offset),
     *            "randomDirection" (spawn with random directions), "spawnBehindBlock" (spawn behind blocks).
     * @param ctx The {@link ScriptContext} containing contextual information, including the
     *            executing entity, nearby entities, and event-related context for correctly resolving and executing the script logic.
     */
    private void execEventWave(ScriptCommand cmd, ScriptContext ctx) {
        String tname = cmd.getArg("type", "ZOMBIE");
        EntityType type;
        try {
            type = EntityType.valueOf(tname.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return;
        }

        boolean isFalling = (type == EntityType.FALLING_BLOCK);
        BlockData bd;
        if (isFalling) {
            String matName = cmd.getArg("material", "STONE").toUpperCase();
            Material mat;
            try {
                mat = Material.valueOf(matName);
            } catch (IllegalArgumentException ex) {
                mat = Material.STONE;
            }
            bd = mat.createBlockData();
        } else {
            bd = null;
        }

        int count         = cmd.getInt("count", 5);
        double rad        = cmd.getDouble("radius", 5.0);
        long delay        = (long) cmd.getDouble("delay", 20);
        int rounds        = cmd.getInt("rounds", 3);
        double yOffset    = cmd.getDouble("yOffset", 0.0);
        boolean randDir   = Boolean.parseBoolean(cmd.getArg("randomDirection", "false"));
        boolean behindBlk = Boolean.parseBoolean(cmd.getArg("spawnBehindBlock", "false"));

        Collection<LivingEntity> centres =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        Random rnd = new Random();
        for (LivingEntity centre : centres) {
            Location base = centre.getLocation();
            new BukkitRunnable() {
                int wave = 0;
                @Override
                public void run() {
                    if (wave++ >= rounds) {
                        cancel();
                        return;
                    }
                    World w = base.getWorld();
                    for (int i = 0; i < count; i++) {
                        double ang = rnd.nextDouble() * Math.PI * 2;
                        double r   = rad * Math.sqrt(rnd.nextDouble());
                        double dx  = r * Math.cos(ang),
                                dz  = r * Math.sin(ang);
                        Location spawnLoc = base.clone().add(dx, yOffset, dz);

                        if (behindBlk) {
                            BlockIterator bi = new BlockIterator(w, spawnLoc.toVector(), new Vector(0,-1,0), 0, 20);
                            while (bi.hasNext()) {
                                Block b = bi.next();
                                if (b.getType().isSolid()) {
                                    spawnLoc = b.getLocation().add(0.5, 1, 0.5);
                                    break;
                                }
                            }
                        }

                        Entity e;
                        if (isFalling) {
                            FallingBlock fb = w.spawnFallingBlock(spawnLoc, bd);
                            fb.setDropItem(false);
                            e = fb;
                        } else {
                            e = w.spawnEntity(spawnLoc, type);
                        }

                        if (randDir) {
                            Vector dir = new Vector(dx, - (isFalling ? 0 : 1), dz).normalize().multiply(0.3);
                            if (e instanceof Projectile) {
                                ((Projectile)e).setVelocity(dir);
                            } else {
                                e.setVelocity(dir);
                            }
                        }
                    }
                }
            }.runTaskTimer(PrimevalRPG.getInstance(), 0, delay);
        }
    }

    /**
     * spawnCustomMob nameId=<mobNameID> targets=@self|@nearby…
     *              [count=<int>] [radius=<double>] [cap=<int>]
     * Won't spawn more than 'cap' total per player (default 10).
     * Currently in work, may change.
     */
    private void execSpawnCustomMob(ScriptCommand cmd, ScriptContext ctx) {
        String mobId    = cmd.getArg("nameId");
        int requested   = parseInt(cmd.getArg("count"), 1, 1);
        double radius   = parseDouble(cmd.getArg("radius"), 5.0, 0.0);
        int capLimit    = parseInt(cmd.getArg("cap"), DEFAULT_MAX_PER_PLAYER, 1);

        List<Player> players = resolveEntities(cmd.targets,
                ctx.self, ctx.nearby, ctx.event).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player)e)
                .toList();

        for (Player player : players) {
            UUID uuid = player.getUniqueId();
            int soFar = playerSpawnCounts.getOrDefault(uuid, 0);
            int available = capLimit - soFar;
            if (available <= 0) {
                RPGLogger.get().log(LoggerLevel.ERROR, "Player " + player.getName() + " reached spawn cap of " + capLimit + ".");
                continue;
            }

            int toSpawn = Math.min(requested, available);
            CustomMob proto = CustomEntityArrayHandler
                    .getRegisteredCustomMobs().values().stream()
                    .filter(m -> mobId.equals(m.getMobNameID()))
                    .findFirst().orElse(null);

            if (proto == null) {
                RPGLogger.get().log(LoggerLevel.ERROR, "Custom-mob '" + mobId + "' not found.");
                continue;
            }

            Location base = player.getLocation();
            for (int i = 0; i < toSpawn; i++) {
                double angle = Math.random() * Math.PI * 2;
                double dx    = Math.cos(angle) * radius;
                double dz    = Math.sin(angle) * radius;
                Location spawnLoc = base.clone().add(dx, 0, dz);
                CustomMobManager.getInstance()
                        .setMobLevelAndSpawn(player, proto, spawnLoc);
            }
            playerSpawnCounts.put(uuid, soFar + toSpawn);
        }
    }

    /**
     * Executes the ignite action on a given set of target entities, setting them on fire
     * for the specified duration.
     *
     * @param cmd the command containing the arguments, including the "duration" parameter
     *            which specifies the time (in ticks) the entities will be set on fire
     * @param ctx the script context providing references to the invoker, nearby entities,
     *            and event context for determining the target entities
     */
    private void execIgnite(ScriptCommand cmd, ScriptContext ctx) {
        String raw = cmd.getArg("duration");
        int duration = 40;
        if (raw != null) {
            try {
                duration = Integer.parseInt(raw);
            } catch (NumberFormatException ignore) { }
        }

        Collection<LivingEntity> targets =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        for (LivingEntity e : targets) {
            e.setFireTicks(duration);
        }
    }

    /**
     * Executes the given command to send a title and subtitle to specified target players.
     *
     * @param cmd The script command containing arguments for title, subtitle, fade-in duration, stay duration,
     *            and fade-out duration. Also contains target entities to whom the title and subtitle will be sent.
     * @param ctx The script context, providing information about the runtime state, including the current actor
     *            and nearby entities.
     */
    private void execTitle(ScriptCommand cmd, ScriptContext ctx) {
        String rawTitle    = cmd.getArg("title", "");
        String rawSubTitle = cmd.getArg("subtitle", "");
        int fadeIn  = cmd.getInt("fadeIn", 10);
        int stay    = cmd.getInt("stay", 70);
        int fadeOut = cmd.getInt("fadeOut", 20);

        String title    = ChatColor.translateAlternateColorCodes('&', rawTitle);
        String subtitle = ChatColor.translateAlternateColorCodes('&', rawSubTitle);

        for (LivingEntity e : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            if (!(e instanceof Player)) continue;
            Player p = (Player) e;
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * Executes an action-bar message to be sent to all targeted players.
     *
     * @param cmd The script command containing the action-bar text and target configuration.
     * @param ctx The context in which the script is executed, providing references to the executor, nearby entities, and the related event.
     */
    private void execActionBar(ScriptCommand cmd, ScriptContext ctx) {
        String text = cmd.getArg("text", "");
        for (LivingEntity e : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            if (!(e instanceof Player)) continue;
            Player p = (Player) e;
            p.sendActionBar(ColourCode.colour(text));
        }
    }

    /**
     * Adjusts an attribute for targeted living entities by applying a specified modifier.
     *
     * @param cmd The ScriptCommand containing information about the attribute to modify,
     *            the amount of the modification, the operation type, and the targeted entities.
     * @param ctx The ScriptContext providing execution context, such as the entity executing
     *            the script, nearby entities, and relevant event data.
     */
    private void execAttribute(ScriptCommand cmd, ScriptContext ctx) {
        String name      = cmd.getArg("name", "");
        double amount    = cmd.getDouble("amount", 0);
        String opName    = cmd.getArg("operation", "ADD_NUMBER");
        Attribute attrib;
        try {
            attrib = Attribute.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return;
        }

        AttributeModifier.Operation operation;
        try {
            operation = AttributeModifier.Operation.valueOf(opName);
        } catch (IllegalArgumentException ex) {
            operation = AttributeModifier.Operation.ADD_NUMBER;
        }

        for (LivingEntity e : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            AttributeInstance inst = e.getAttribute(attrib);
            if (inst == null) continue;

            AttributeModifier mod = new AttributeModifier(
                    UUID.randomUUID(), "script-"+name, amount, operation
            );
            inst.addModifier(mod);
        }
    }

    // -----------------------
    // Remove potion effects
    // -----------------------
    private void execClearEffects(ScriptCommand cmd, ScriptContext ctx) {
        String typeName = cmd.getArg("type", "");
        PotionEffectType specific = null;
        if (!typeName.isEmpty()) {
            specific = PotionEffectType.getByName(typeName);
        }

        for (LivingEntity e : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            if (specific != null) {
                e.removePotionEffect(specific);
            } else {
                for (PotionEffect pe : e.getActivePotionEffects()) {
                    e.removePotionEffect(pe.getType());
                }
            }
        }
    }


    private void execInvulnerable(ScriptCommand cmd, ScriptContext ctx) {
        String raw = cmd.getArg("duration");
        int duration = 60;
        if (raw != null) {
            try {
                duration = Integer.parseInt(raw);
            } catch (NumberFormatException ignore) { }
        }

        Collection<LivingEntity> targets =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        for (LivingEntity e : targets) {
            e.addPotionEffect(
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 255, true, false)
            );
        }
    }

    private void execLightning(ScriptCommand cmd, ScriptContext ctx) {
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            ctx.self.getWorld().strikeLightning(target.getLocation());
        }
    }

    private void execSafeLightning(ScriptCommand cmd, ScriptContext ctx) {
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            ctx.self.getWorld().strikeLightningEffect(target.getLocation());
        }
    }

    private void execSound(ScriptCommand cmd, ScriptContext ctx) {

        String rawName = cmd.getArg("name", "").toUpperCase().trim();
        if (rawName.isEmpty()) {
            PrimevalRPG.getInstance().getLogger().warning("execSound skipped: no 'name' provided in " + cmd);
            return;
        }

        String mappedName = switch (rawName) {
            case "ENDER_FLAP"           -> "ENTITY_ENDERDRAGON_FLAP";
            case "PORTAL_TRAVEL"        -> "BLOCK_PORTAL_TRAVEL";
            default                      -> rawName;
        };

        Sound sound;
        try {
            sound = Sound.valueOf(mappedName);
        } catch (IllegalArgumentException iae) {
            PrimevalRPG.getInstance().getLogger().warning("execSound skipped: invalid Sound '" + rawName + "'");
            return;
        }

        float volume = 1.0f;
        float pitch  = 1.0f;
        try {
            volume = Float.parseFloat(cmd.getArg("volume", "1.0"));
            pitch  = Float.parseFloat(cmd.getArg("pitch",  "1.0"));
        } catch (NumberFormatException nfe) {
            PrimevalRPG.getInstance().getLogger().warning("execSound: bad volume/pitch format in " + cmd);
        }

        for (Player p : resolvePlayers(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            p.playSound(ctx.self.getLocation(), sound, volume, pitch);
        }
    }

    private void execTeleport(ScriptCommand cmd, ScriptContext ctx) {
        double dx = Double.parseDouble(cmd.args.getOrDefault("offsetX","0"));
        double dy = Double.parseDouble(cmd.args.getOrDefault("offsetY","0"));
        double dz = Double.parseDouble(cmd.args.getOrDefault("offsetZ","0"));
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            Location dest = target.getLocation().add(dx, dy, dz);
            ctx.self.teleport(dest);
        }
    }

    private void execParticle(ScriptCommand cmd, ScriptContext ctx) {
        World world = ctx.self.getWorld();

        String rawName = cmd.getArg("name", "").toUpperCase().trim();
        if (rawName.isEmpty()) {
            PrimevalRPG.getInstance().getLogger().warning("execParticle skipped: no 'name' provided in " + cmd);
            return;
        }

        String mappedName = switch (rawName) {
            case "EXPLOSION" -> "EXPLOSION_HUGE";
            case "SMOKE"     -> "SMOKE_NORMAL";
            default           -> rawName;
        };

        Particle particle;
        try {
            particle = Particle.valueOf(mappedName);
        } catch (IllegalArgumentException iae) {
            PrimevalRPG.getInstance().getLogger().warning("execParticle skipped: invalid Particle '" + rawName + "'");
            return;
        }

        int count = (int) cmd.getDouble("count", 1.0);

        double radius = cmd.getDouble("radius", 0.0);

        Location loc = ctx.self.getLocation();

        // Some particles require extra data (e.g. BLOCK_CRACK → BlockData)
        Class<?> dataType = particle.getDataType();
        if (dataType == BlockData.class) {
            String matName = cmd.getArg("data", null);
            BlockData bd;
            if (matName != null) {
                try {
                    bd = Bukkit.createBlockData(Material.valueOf(matName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    PrimevalRPG.getInstance().getLogger().warning("execParticle: invalid material for data='" + matName + "'");
                    bd = loc.getBlock().getBlockData();
                }
            } else {
                bd = loc.getBlock().getBlockData();
            }
            world.spawnParticle(particle,
                    loc,
                    count,
                    radius, radius, radius,
                    0.0,
                    bd);
        } else {
            world.spawnParticle(particle,
                    loc,
                    count,
                    radius, radius, radius,
                    0.0);
        }
    }

    private void execHeal(ScriptCommand cmd, ScriptContext ctx) {
        double amt = Double.parseDouble(cmd.args.getOrDefault("amount","1"));
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            if (target instanceof Damageable) {
                Damageable dm = (Damageable) target;
                dm.setHealth(Math.min(dm.getMaxHealth(), dm.getHealth() + amt));
            }
        }
    }

    private void execEffect(ScriptCommand cmd, ScriptContext ctx) {
        PotionEffectType pet = PotionEffectType.getByName(cmd.args.get("type"));
        if (pet == null) return;
        int dur = Integer.parseInt(cmd.args.getOrDefault("duration","60"));
        int amp = Integer.parseInt(cmd.args.getOrDefault("amplifier","0"));
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            target.addPotionEffect(new PotionEffect(pet, dur, amp));
        }
    }

    private void execFallingBlock(ScriptCommand cmd, ScriptContext ctx) {
        String matName = cmd.getArg("material", "SAND");
        Material mat;
        try {
            mat = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return;
        }
        BlockData bd = Bukkit.createBlockData(mat);

        boolean dropItem = cmd.getArg("dropItem", "false")
                .equalsIgnoreCase("true");
        int count        = cmd.getInt("count", 10);
        double radius    = cmd.getDouble("radius", 3.0);
        double height    = cmd.getDouble("height", 5.0);

        World world = ctx.self.getWorld();
        Vector down = new Vector(0, -0.1, 0);

        Collection<LivingEntity> centres =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        Random rnd = new Random();
        for (LivingEntity centre : centres) {
            Location base = centre.getLocation();
            for (int i = 0; i < count; i++) {
                // random polar coords
                double angle = rnd.nextDouble() * Math.PI * 2;
                double r     = radius * Math.sqrt(rnd.nextDouble());
                double dx    = r * Math.cos(angle);
                double dz    = r * Math.sin(angle);

                Location spawnLoc = base.clone()
                        .add(dx, height, dz)
                        .getBlock()
                        .getLocation()
                        .add(0.5, 0, 0.5);

                FallingBlock fb = world.spawnFallingBlock(spawnLoc, bd);
                fb.setMetadata("cmc_shockwave", new FixedMetadataValue(PrimevalRPG.getInstance(), true));
                fb.setDropItem(dropItem);
                fb.setVelocity(down);
            }
        }
    }

    private void execKnockback(ScriptCommand cmd, ScriptContext ctx) {
        double strength      = cmd.getDouble("strength", 1.0);
        String direction     = cmd.getArg("direction", "away");
        boolean hasX         = cmd.hasArg("x");
        boolean hasY         = cmd.hasArg("y");
        boolean hasZ         = cmd.hasArg("z");

        Collection<LivingEntity> targets =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        for (LivingEntity target : targets) {
            Vector vec;

            if (hasX || hasY || hasZ) {
                double x = cmd.getDouble("x", 0),
                        y = cmd.getDouble("y", 0),
                        z = cmd.getDouble("z", 0);
                vec = new Vector(x, y, z);
                if (vec.lengthSquared() < 1e-6) {
                    vec = new Vector(0, 0.2, 0);
                } else {
                    vec = vec.normalize().multiply(strength);
                }
            } else {
                vec = target.getLocation().toVector()
                        .subtract(ctx.self.getLocation().toVector());
                if ("towards".equalsIgnoreCase(direction)) {
                    vec.multiply(-1);
                }
                if (!Double.isFinite(vec.getX()) ||
                        !Double.isFinite(vec.getY()) ||
                        !Double.isFinite(vec.getZ()) ||
                        vec.lengthSquared() < 1e-6) {
                    vec = new Vector(0, 0.2, 0);
                } else {
                    vec = vec.normalize().multiply(strength);
                }
            }

            target.setVelocity(vec);
        }
    }

    private void execSetBlock(ScriptCommand cmd, ScriptContext ctx) {
        String matName = cmd.hasArg("type")
                ? cmd.getArg("type")
                : cmd.getArg("material", "STONE");
        Material mat = Material.matchMaterial(matName.toUpperCase());
        if (mat == null) mat = Material.STONE;

        String shape  = cmd.getArg("shape", "single").toLowerCase();
        int radius    = (int) cmd.getDouble("radius", 0.0);

        List<Location> centers = new ArrayList<>();
        if (cmd.hasArg("x") && cmd.hasArg("y") && cmd.hasArg("z")) {
            World world = Bukkit.getWorld(cmd.getArg("world", ctx.self.getWorld().getName()));
            double x = cmd.getDouble("x", 0.0);
            double y = cmd.getDouble("y", 0.0);
            double z = cmd.getDouble("z", 0.0);
            centers.add(new Location(world, x, y, z));
        } else {
            String locToken = cmd.getArg("location", "@self");
            List<String> locs = Collections.singletonList(locToken);
            resolveEntities(locs, ctx.self, ctx.nearby, ctx.event)
                    .forEach(ent -> centers.add(ent.getLocation().clone()));
        }

        for (Location center : centers) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        boolean place;
                        switch (shape) {
                            case "cube":
                                place = true;
                                break;
                            case "sphere":
                                place = dx*dx + dy*dy + dz*dz <= radius*radius;
                                break;
                            default:
                                place = (dx == 0 && dy == 0 && dz == 0);
                        }
                        if (!place) continue;
                        Block b = center.clone().add(dx, dy, dz).getBlock();
                        b.setType(mat, false);
                    }
                }
            }
        }
    }

    private void execVelocity(ScriptCommand cmd, ScriptContext ctx) {
        double x = cmd.getDouble("x", 0);
        double y = cmd.getDouble("y", 0);
        double z = cmd.getDouble("z", 0);
        Vector v = new Vector(x, y, z);
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            target.setVelocity(v);
        }
    }

    private void execDamage(ScriptCommand cmd, ScriptContext ctx) {
        double amount = cmd.getDouble("amount", 1.0);
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            if (target instanceof Damageable) {
                ((Damageable) target).damage(amount, ctx.self);
            }
        }
    }

    private void execSpawn(ScriptCommand cmd, ScriptContext ctx) {
        EntityType type = EntityType.valueOf(
                cmd.getArg("type", "ZOMBIE").toUpperCase()
        );

        int count = (int) cmd.getDouble("count", 1);


        Collection<LivingEntity> raw = resolveEntities(
                cmd.targets, ctx.self, ctx.nearby, ctx.event
        );

        Collection<LivingEntity> targets;
        if (cmd.targets.contains("@self")) {
            targets = Collections.singleton(ctx.self);
        } else {
            targets = new HashSet<>(raw);
        }

        String metaKey = "spawned:" + cmd.hashCode();
        for (LivingEntity target : targets) {
            if (target.hasMetadata(metaKey)) {
                continue;
            }
            target.setMetadata(
                    metaKey,
                    new FixedMetadataValue(PrimevalRPG.getInstance(), true)
            );

            Location loc = target.getLocation();
            for (int i = 0; i < count; i++) {
                loc.getWorld().spawnEntity(loc, type);
            }
        }
    }

    private void execWorldTime(ScriptCommand cmd,ScriptContext ctx) {
        String rawTimeArg = cmd.getArg("time", "day");
        long time;

        if (rawTimeArg != null) {
            switch (rawTimeArg.toLowerCase()) {
                case "night":
                    time = 13000;
                    break;
                case "day":
                    time = 1000;
                    break;
                case "dawn":
                    time = 23000;
                    break;
                case "dusk":
                    time = 12000;
                    break;
                default:
                    time = (long) cmd.getDouble("time", ctx.self.getWorld().getTime());
                    break;
            }
        } else {
            time = ctx.self.getWorld().getTime();
        }

        ctx.self.getWorld().setTime(time);
    }

    private void execWeather(ScriptCommand cmd, ScriptContext ctx) {
        String w = cmd.getArg("type", "CLEAR").toUpperCase();
        World world = ctx.self.getWorld();
        switch (w) {
            case "RAIN":
                world.setStorm(true);
                world.setThundering(false);
                break;
            case "THUNDER":
                world.setStorm(true);
                world.setThundering(true);
                break;
            default: // CLEAR
                world.setStorm(false);
                world.setThundering(false);
        }
    }

    private void execDropItem(ScriptCommand cmd, ScriptContext ctx) {

        String itemName = cmd.getArg("item", "STONE").toUpperCase();
        int amount      = (int) cmd.getDouble("amount", 1);
        boolean clear   = Boolean.parseBoolean(cmd.getArg("clear", "false"));

        Material mat;
        try {
            mat = Material.valueOf(itemName);
        } catch (IllegalArgumentException ex) {
            PrimevalRPG.getInstance()
                    .getLogger().warning("drop_item: invalid material '" + itemName + "'");
            return;
        }

        boolean glow       = Boolean.parseBoolean(cmd.getArg("glow", "false"));
        String  display    = cmd.getArg("displayName", null);
        String  singleLore = cmd.getArg("lore", null);
        String  loresArg   = cmd.getArg("lores", null);
        List<String> multiLore = (loresArg != null)
                ? Arrays.asList(loresArg.split("\\|"))
                : null;

        String metaArg = cmd.getArg("meta", null);
        Map<String, String> metaDataMap = new HashMap<>();
        if (metaArg != null) {
            for (String pair : metaArg.split("\\|")) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    metaDataMap.put(kv[0], kv[1]);
                }
            }
        }

        ItemStack stack;
        if (display != null && multiLore != null) {
            stack = ItemBuilder.CreateMultiLoreItem(mat, glow, display, multiLore.toArray(new String[0]));
        } else if (display != null && singleLore != null) {
            stack = ItemBuilder.CreateCustomItem(mat, glow, display, singleLore);
        } else if (!metaDataMap.isEmpty()) {
            stack = ItemBuilder.createMetaItem(mat, glow, display != null ? display : mat.name(), metaDataMap);
        } else {
            stack = new ItemStack(mat);
        }

        stack.setAmount(Math.max(1, amount));

        ItemMeta meta = stack.getItemMeta();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            meta.getPersistentDataContainer().remove(key);
        }
        stack.setItemMeta(meta);

        if (!metaDataMap.isEmpty()) {
            ItemMeta im = stack.getItemMeta();
            for (var e : metaDataMap.entrySet()) {
                NamespacedKey key = new NamespacedKey(PrimevalRPG.getInstance(), e.getKey());
                im.getPersistentDataContainer()
                        .set(key, PersistentDataType.STRING, e.getValue());
            }
            stack.setItemMeta(im);
        }

        if (ctx.event instanceof EntityDeathEvent deathEvent) {
            if (clear) {
                deathEvent.getDrops().clear();
            }
            deathEvent.getDrops().add(stack);
            return;
        }

        Location loc = ctx.self.getLocation();
        if (cmd.hasArg("location")) {
            String locArg = cmd.getArg("location");
            if (locArg.startsWith("@")) {
                Collection<LivingEntity> t = resolveEntities(
                        Collections.singletonList(locArg), ctx.self, ctx.nearby, ctx.event);
                if (!t.isEmpty()) loc = t.iterator().next().getLocation();
            } else {
                String[] p = locArg.split(",");
                if (p.length >= 3) {
                    try {
                        double x = Double.parseDouble(p[0]),
                                y = Double.parseDouble(p[1]),
                                z = Double.parseDouble(p[2]);
                        loc = new Location(ctx.self.getWorld(), x, y, z);
                    } catch (NumberFormatException ignored) { }
                }
            }
        }

        ctx.self.getWorld().dropItemNaturally(loc, stack);
    }

    // Currently trying to resolve this method call for fixing as their is an issue with the nearby flag
    private void execInvisibility(ScriptCommand cmd, ScriptContext ctx) {
        int duration = (int) cmd.getDouble("duration", 100);
        int amp      = (int) cmd.getDouble("amplifier", 0);
        for (LivingEntity target : resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event)) {
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    duration,
                    amp,
                    false,
                    false
            ));
        }
    }

    private void execMessage(ScriptCommand cmd, ScriptContext ctx) {
        String rawText = cmd.getArg("text", "");

        Collection<LivingEntity> rawTargets =
                resolveEntities(cmd.targets, ctx.self, ctx.nearby, ctx.event);

        String text = interpolate(rawText, ctx.self, rawTargets, ctx.event);

        for (LivingEntity ent : new HashSet<>(rawTargets)) {
            if (ent instanceof Player p) {
                p.sendMessage(colorize(text));
            }
        }
    }

    private void execCondition(ScriptCommand cmd, ScriptContext ctx) {
        String startExp = cmd.getArg("if", "");
        String thenCmd  = cmd.getArg("then", "");
        String elseCmd  = cmd.getArg("else", "");

        boolean pass = evaluate(startExp, ctx.self, ctx.nearby, ctx.event);

        String toRun = pass ? thenCmd : elseCmd;
        if (toRun.isBlank()) return;

        ScriptCommand sub = ScriptParser.parseLine(toRun);
        CommandExecutor executor = get(sub.name);

        if (executor != null) executor.execute(sub, ctx);
    }


    private void execRepeat(ScriptCommand cmd, ScriptContext ctx) {
        int count = Integer.parseInt(cmd.args.getOrDefault("count", "1"));
        long delay = Long.parseLong(cmd.args.getOrDefault("delay", "0"));
        String raw = cmd.args.get("action");
        if (raw == null) return;

        raw = raw.trim();

        ScriptCommand sub = ScriptParser.parseLine(raw);
        CommandExecutor subEx = executors.get(sub.name);
        if (subEx == null) {
            PrimevalRPG.getInstance().CustomMobLogger("[Repeat] Unknown sub-command: " + sub.name, LoggerLevel.ERROR);
            return;
        }

        new BukkitRunnable() {
            int left = count;
            @Override
            public void run() {
                if (left-- <= 0) {
                    cancel();
                    return;
                }
                subEx.execute(sub, ctx);
            }
        }.runTaskTimer(PrimevalRPG.getInstance(), delay, delay);
    }

    private String interpolate(String input,
                               LivingEntity self,
                               Collection<LivingEntity> nearbyAll,
                               Event event) {

        if (input.contains("%biome%") && self instanceof Player) {
            Player p = (Player) self;
            String biomeName = p.getLocation().getBlock().getBiome().name();
            input = input.replace("%biome%", biomeName);
        }

        String out = input;
        Random rnd = new Random();

        Collection<LivingEntity> allEntities =
                resolveEntities(List.of("@allEntities"), self, nearbyAll, event);
        Collection<LivingEntity> allPlayers =
                resolveEntities(List.of("@allPlayers"),  self, nearbyAll, event);
        Collection<LivingEntity> allMobs =
                resolveEntities(List.of("@allMobs"),    self, nearbyAll, event);
        Collection<LivingEntity> nearestSet =
                resolveEntities(List.of("@nearest"),    self, nearbyAll, event);
        LivingEntity nearest = nearestSet.stream().findFirst().orElse(null);

        LivingEntity attacker = null;
        if (event instanceof EntityDamageByEntityEvent ede) {
            Entity dam = ede.getDamager();
            if (dam instanceof LivingEntity le) attacker = le;
        }

        LivingEntity killer = null;
        if (event instanceof EntityDeathEvent ede2) {
            Entity rawK = ede2.getEntity().getKiller();
            if (rawK instanceof LivingEntity le2) killer = le2;
        }

        Function<Collection<LivingEntity>,String> names = col ->
                col.stream()
                        .map(e -> e.getCustomName() != null
                                ? e.getCustomName()
                                : e.getName())
                        .collect(Collectors.joining(", "));

        if (self != null) {
            String selfName = self.getCustomName() != null
                    ? self.getCustomName()
                    : self.getName();
            out = out.replaceAll(
                    "@self\\.getName\\(\\)",
                    Matcher.quoteReplacement(selfName)
            );

            out = out.replaceAll(
                    "@self\\.health\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", self.getHealth()))
            );
            out = out.replaceAll(
                    "@self\\.maxHealth\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", self.getMaxHealth()))
            );
            out = out.replaceAll(
                    "@self\\.healthPercent\\(\\)",
                    Matcher.quoteReplacement(
                            String.valueOf((int)(self.getHealth() / self.getMaxHealth() * 100))
                    )
            );

            out = out.replaceAll(
                    "@self\\.x\\(\\)",
                    Integer.toString(self.getLocation().getBlockX())
            );
            out = out.replaceAll(
                    "@self\\.y\\(\\)",
                    Integer.toString(self.getLocation().getBlockY())
            );
            out = out.replaceAll(
                    "@self\\.z\\(\\)",
                    Integer.toString(self.getLocation().getBlockZ())
            );
            // world
            World w = self.getWorld();
            out = out.replaceAll(
                    "@self\\.world\\(\\)",
                    Matcher.quoteReplacement(w.getName())
            );

            // facing‐vector (direction player is looking)
            Vector dir = self.getLocation().getDirection();
            out = out.replaceAll(
                    "@self\\.facingX\\(\\)",
                    Matcher.quoteReplacement(String.format("%.3f", dir.getX()))
            );
            out = out.replaceAll(
                    "@self\\.facingY\\(\\)",
                    Matcher.quoteReplacement(String.format("%.3f", dir.getY()))
            );
            out = out.replaceAll(
                    "@self\\.facingZ\\(\\)",
                    Matcher.quoteReplacement(String.format("%.3f", dir.getZ()))
            );

            Pattern p = Pattern.compile("@self\\.flag\\(([^)]+)\\)");
            Matcher m = p.matcher(out);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String flagName = m.group(1);
                boolean has = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasFlag(self.getUniqueId(), flagName);
                m.appendReplacement(sb, Boolean.toString(has));
            }
            m.appendTail(sb);
            out = sb.toString();
        }

        if (attacker != null) {
            String aName = attacker.getCustomName() != null
                    ? attacker.getCustomName()
                    : attacker.getName();
            out = out.replaceAll(
                    "@attacker\\.getName\\(\\)",
                    Matcher.quoteReplacement(aName)
            );
            out = out.replaceAll(
                    "@attacker\\.health\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", attacker.getHealth()))
            );
            out = out.replaceAll(
                    "@attacker\\.healthPercent\\(\\)",
                    Matcher.quoteReplacement(
                            String.valueOf((int)(attacker.getHealth() / attacker.getMaxHealth() * 100))
                    )
            );

            Pattern p = Pattern.compile("@attacker\\.flag\\(([^)]+)\\)");
            Matcher m = p.matcher(out);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String flagName = m.group(1);
                boolean has = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasFlag(attacker.getUniqueId(), flagName);
                m.appendReplacement(sb, Boolean.toString(has));
            }
            m.appendTail(sb);
            out = sb.toString();
        }

        if (nearest != null) {
            String nName = nearest.getCustomName() != null
                    ? nearest.getCustomName()
                    : nearest.getName();
            out = out.replaceAll(
                    "@nearest\\.getName\\(\\)",
                    Matcher.quoteReplacement(nName)
            );
            out = out.replaceAll(
                    "@nearest\\.health\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", nearest.getHealth()))
            );
            out = out.replaceAll(
                    "@nearest\\.maxHealth\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", nearest.getMaxHealth()))
            );
            out = out.replaceAll(
                    "@nearest\\.healthPercent\\(\\)",
                    Matcher.quoteReplacement(
                            String.valueOf((int)(nearest.getHealth() / nearest.getMaxHealth() * 100))
                    )
            );

            out = out.replaceAll(
                    "@nearest\\.x\\(\\)",
                    Matcher.quoteReplacement(
                            Integer.toString(nearest.getLocation().getBlockX())
                    )
            );
            out = out.replaceAll(
                    "@nearest\\.y\\(\\)",
                    Matcher.quoteReplacement(
                            Integer.toString(nearest.getLocation().getBlockY())
                    )
            );
            out = out.replaceAll(
                    "@nearest\\.z\\(\\)",
                    Matcher.quoteReplacement(
                            Integer.toString(nearest.getLocation().getBlockZ())
                    )
            );
            World w = nearest.getWorld();
            out = out.replaceAll(
                    "@nearest\\.world\\(\\)",
                    Matcher.quoteReplacement(w.getName())
            );

            Pattern p = Pattern.compile("@nearest\\.flag\\(([^)]+)\\)");
            Matcher m = p.matcher(out);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String flagName = m.group(1);
                boolean has = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasFlag(nearest.getUniqueId(), flagName);
                m.appendReplacement(sb, Boolean.toString(has));
            }
            m.appendTail(sb);
            out = sb.toString();
        }


        if (killer != null) {
            String kName = killer.getCustomName() != null
                    ? killer.getCustomName()
                    : killer.getName();
            out = out.replaceAll(
                    "@killer\\.getName\\(\\)",
                    Matcher.quoteReplacement(kName)
            );
            out = out.replaceAll(
                    "@killer\\.health\\(\\)",
                    Matcher.quoteReplacement(String.format("%.1f", killer.getHealth()))
            );
            out = out.replaceAll(
                    "@killer\\.healthPercent\\(\\)",
                    Matcher.quoteReplacement(
                            String.valueOf((int)(killer.getHealth() / killer.getMaxHealth() * 100))
                    )
            );
            out = out.replaceAll(
                    "@killer\\.x\\(\\)",
                    Integer.toString(killer.getLocation().getBlockX())
            );
            out = out.replaceAll(
                    "@killer\\.y\\(\\)",
                    Integer.toString(killer.getLocation().getBlockY())
            );
            out = out.replaceAll(
                    "@killer\\.z\\(\\)",
                    Integer.toString(killer.getLocation().getBlockZ())
            );
            World kw = killer.getWorld();
            out = out.replaceAll(
                    "@killer\\.world\\(\\)",
                    Matcher.quoteReplacement(kw.getName())
            );

            Pattern p = Pattern.compile("@killer\\.flag\\(([^)]+)\\)");
            Matcher m = p.matcher(out);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String flagName = m.group(1);
                boolean has = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasFlag(killer.getUniqueId(), flagName);
                m.appendReplacement(sb, Boolean.toString(has));
            }
            m.appendTail(sb);
            out = sb.toString();
        }

        out = out.replaceAll(
                "@nearbyPlayers\\.size\\(\\)",
                Integer.toString(
                        (int) nearbyAll.stream().filter(e->e instanceof Player).count()
                )
        );

        out = out.replaceAll(
                "@allEntities\\.size\\(\\)",
                Integer.toString(allEntities.size())
        );
        out = out.replaceAll(
                "@allPlayers\\.size\\(\\)",
                Integer.toString(allPlayers.size())
        );
        out = out.replaceAll(
                "@allMobs\\.size\\(\\)",
                Integer.toString(allMobs.size())
        );

        out = out.replaceAll(
                "@allEntities\\.names\\(\\)",
                Matcher.quoteReplacement(names.apply(allEntities))
        );
        out = out.replaceAll(
                "@allPlayers\\.names\\(\\)",
                Matcher.quoteReplacement(names.apply(allPlayers))
        );
        out = out.replaceAll(
                "@allMobs\\.names\\(\\)",
                Matcher.quoteReplacement(names.apply(allMobs))
        );

        out = out.replaceAll(
                "@world\\.name\\(\\)",
                Matcher.quoteReplacement(self.getWorld().getName())
        );

        out = out.replaceAll(
                "@world\\.time\\(\\)",
                Matcher.quoteReplacement(self.getWorld().getTime() + "")
        );

        out = out.replaceAll(
                "@server\\.onlineCount\\(\\)",
                Matcher.quoteReplacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
        );
        out = out.replaceAll(
                "@server\\.maxPlayers\\(\\)",
                Matcher.quoteReplacement(String.valueOf(Bukkit.getMaxPlayers()))
        );
        out = out.replaceAll(
                "@server\\.tps\\(\\)",
                Matcher.quoteReplacement(String.format("%.2f", PrimevalRPG.getInstance().getServer().getTPS()[0]))
        );


        if (!allPlayers.isEmpty()) {
            LivingEntity pick = allPlayers.stream()
                    .skip(rnd.nextInt(allPlayers.size()))
                    .findFirst().get();
            String pName = pick.getCustomName()!=null ? pick.getCustomName() : pick.getName();
            out = out.replaceAll(
                    "@allPlayers\\.randomName\\(\\)",
                    Matcher.quoteReplacement(pName)
            );
        }
        if (!allMobs.isEmpty()) {
            LivingEntity pick = allMobs.stream()
                    .skip(rnd.nextInt(allMobs.size()))
                    .findFirst().get();
            String mName = pick.getCustomName()!=null ? pick.getCustomName() : pick.getName();
            out = out.replaceAll(
                    "@allMobs\\.randomName\\(\\)",
                    Matcher.quoteReplacement(mName)
            );
        }

        if (event instanceof PlayerInteractEvent pie) {
            Block clicked = pie.getClickedBlock();
            if (clicked != null) {
                Location lc = clicked.getLocation();
                String coord = lc.getBlockX() + " "
                        + lc.getBlockY() + " "
                        + lc.getBlockZ();
                out = out.replaceAll("@clickedBlock", coord);
            }
        }

        return out;
    }

    /**
     * Evaluates a given logical or comparison expression based on the provided parameters.
     *
     * @param expr the expression to be evaluated, consisting of a left operand, an operator, and a right operand
     * @param self the main living entity used in the evaluation
     * @param nearbyAll a collection of nearby living entities that can influence the evaluation
     * @param event the context event within which the evaluation is performed
     * @return true if the expression evaluates to true based on the provided parameters, false otherwise
     */
    private boolean evaluate(String expr,
                             LivingEntity self,
                             Collection<LivingEntity> nearbyAll,
                             Event event) {
        expr = expr.trim();
        // split into left / operator / right
        Matcher m = Pattern.compile("(.+?)(<=|>=|==|<|>)(.+)").matcher(expr);
        if (!m.matches()) return false;

        String leftToken  = m.group(1).trim();
        String operator   = m.group(2);
        String rightToken = m.group(3).trim();

        double lhs = resolveVariable(leftToken, self, nearbyAll, event);
        double rhs = resolveVariable(rightToken, self, nearbyAll, event);

        switch (operator) {
            case "<=": return lhs <= rhs;
            case ">=": return lhs >= rhs;
            case "==": return lhs == rhs;
            case "<" : return lhs <  rhs;
            case ">" : return lhs >  rhs;
            default:   return false;
        }
    }

    /**
     * Turn a token like "@self.health()" or "12.5" or "@nearbyPlayers.size()"
     * into a numeric value.
     */
    private double resolveVariable(String token,
                                   LivingEntity self,
                                   Collection<LivingEntity> nearbyAll,
                                   Event event) {
        token = token.replaceAll("@", "")
                .replaceAll("\\(\\)", "")
                .trim();
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException ex) {}

        switch (token) {
            case "self.health":
                return self.getHealth();
            case "self.maxHealth":
                return self.getMaxHealth();
            case "self.x":
                return self.getLocation().getX();
            case "self.y":
                return self.getLocation().getY();
            case "self.z":
                return self.getLocation().getZ();
            case "world.time":
                return self.getWorld().getTime();
            case "nearbyPlayers.size":
                return nearbyAll.stream()
                        .filter(e -> e instanceof Player)
                        .count();
            case "nearbyAll.size":
                return nearbyAll.size();
            default:
                return 0;
        }
    }


    /**
     * Very general target‐finder. Understands @self, @allEntities, @allPlayers, @allMobs,
     * @nearby, @nearbyPlayers, @nearest, @nearestPlayer, etc.
     */
    public static Collection<LivingEntity> resolveEntities(
            List<String> targets,
            LivingEntity self,
            Collection<LivingEntity> nearbyAll,
            Event event
    ) {
        boolean explicitSelf = targets.stream().anyMatch(t ->
                t.equalsIgnoreCase("@self") || t.toLowerCase().startsWith("@self[")
        );

        Set<LivingEntity> out = new HashSet<>();

        for (String t : targets) {
            String base = t.contains("[") ? t.substring(0, t.indexOf('[')) : t;
            List<Filter> filters = parseFilters(t);
            double radius = extractRadius(t);
            double r2 = radius * radius;

            switch (base.toLowerCase()) {

                case "@server" -> {
                    RPGLogger.get().debug("  - Skipping @server target");
                    continue;
                }

                case "@nearbyplayers" -> {
                    RPGLogger.get().debug("  - Processing @nearbyplayers");
                    for (LivingEntity e : nearbyAll) {
                        if (e instanceof Player
                                && e.getLocation().distanceSquared(self.getLocation()) <= r2
                                && matchesFilters(e, filters)) {
                            out.add(e);
                        }
                    }
                }
                case "@nearby" -> {
                    RPGLogger.get().debug("  - Processing @nearby");
                    for (LivingEntity e : nearbyAll) {
                        if (e.getLocation().distanceSquared(self.getLocation()) <= r2
                                && matchesFilters(e, filters)) {
                            out.add(e);
                        }
                    }
                }
                case "@nearest", "@nearestentity", "@nearestplayer" -> {
                    RPGLogger.get().debug("  - Processing @nearest");
                    nearbyAll.stream()
                            .filter(e -> !base.contains("player") || e instanceof Player)
                            .filter(e -> e.getLocation().distanceSquared(self.getLocation()) <= r2)
                            .filter(e -> matchesFilters(e, filters))
                            .min(Comparator.comparingDouble(
                                    e -> e.getLocation().distanceSquared(self.getLocation())
                            ))
                            .ifPresent(out::add);
                }
                case "@attacker" -> {
                    RPGLogger.get().debug("  - Processing @attacker");
                    if (event instanceof EntityDamageByEntityEvent ede) {
                        Entity dam = ede.getDamager();
                        if (dam instanceof LivingEntity le && matchesFilters(le, filters)) {
                            out.add(le);
                        }
                    }
                }
                case "@killer" -> {
                    RPGLogger.get().debug("  - Processing @killer");
                    if (event instanceof EntityDeathEvent ede) {
                        Player k = ede.getEntity().getKiller();
                        if (k instanceof LivingEntity le && matchesFilters(le, filters)) {
                            out.add(le);
                        }
                    }
                }
                case "@self", "@entity" -> {
                    RPGLogger.get().debug("  - Processing @self");
                    if (matchesFilters(self, filters)) {
                        out.add(self);
                    }
                }
                case "@allentities" -> {
                    RPGLogger.get().debug("  - Processing @allentities");
                    for (LivingEntity e : nearbyAll) {
                        if (matchesFilters(e, filters)) {
                            out.add(e);
                        }
                    }
                }
                case "@allplayers" -> {
                    RPGLogger.get().debug("  - Processing @allplayers");
                    for (LivingEntity e : nearbyAll) {
                        if (e instanceof Player && matchesFilters(e, filters)) {
                            out.add(e);
                        }
                    }
                }
                case "@allmobs" -> {
                    RPGLogger.get().debug("  - Processing @allmobs");
                    for (LivingEntity e : nearbyAll) {
                        if (!(e instanceof Player) && matchesFilters(e, filters)) {
                            out.add(e);
                        }
                    }
                }
                default -> {
                    // unknown token--ignore
                    RPGLogger.get().error(" >>> Unknown token: " + t + " <<<");
                }
            }
        }

        if (!explicitSelf) {
            out.remove(self);
        }
        return out;
    }

    public static boolean hasServerTarget(List<String> targets) {
        return targets.stream().anyMatch(t -> t.equalsIgnoreCase("@server"));
    }

    private static double extractRadius(String token) {
        if (!token.contains("[")) {
            return DEFAULT_TARGET_RADIUS;
        }

        // Look for all bracket pairs and find numeric ones (radius)
        int start = 0;
        while ((start = token.indexOf('[', start)) != -1) {
            int end = token.indexOf(']', start);
            if (end == -1) break;

            String content = token.substring(start + 1, end);
            //treat number as radius
            try {
                double radius = Double.parseDouble(content);
                return Math.max(1.0, radius);
            } catch (NumberFormatException ignored) {
            }
            start = end + 1;
        }

        return DEFAULT_TARGET_RADIUS;
    }


    // small helpers
    private int parseInt(String raw, int def, int min) {
        if (raw == null) return def;
        try { return Math.max(min, Integer.parseInt(raw)); }
        catch (NumberFormatException e) { return def; }
    }
    private double parseDouble(String raw, double def, double min) {
        if (raw == null) return def;
        try { return Math.max(min, Double.parseDouble(raw)); }
        catch (NumberFormatException e) { return def; }
    }

    /**
     * If an executor really only wants Players, just filter the generic result.
     */
    public Collection<Player> resolvePlayers(
            List<String> targets,
            LivingEntity self,
            Collection<LivingEntity> nearbyAll, Event event
    ) {
        return resolveEntities(targets, self, nearbyAll, event).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player)e)
                .collect(Collectors.toSet());
    }

    public interface CommandExecutor {
        void execute(ScriptCommand cmd, ScriptContext ctx);
    }

    // helper to translate color codes
    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('§', s);
    }

    /**
     * Extracts filters of the form key=val, key<val or key>val from inside [...].
     */
    private static List<Filter> parseFilters(String token) {
        List<Filter> filters = new ArrayList<>();
        if (!token.contains("[")) return filters;

        int start = 0;
        while ((start = token.indexOf('[', start)) != -1) {
            int end = token.indexOf(']', start);
            if (end == -1) break;

            String content = token.substring(start + 1, end);

            try {
                Double.parseDouble(content);
                start = end + 1;
                continue;
            } catch (NumberFormatException exception) {}

            String op = null;
            for (String candidate : List.of(">=", "<=", "!=", "==", "=", ">", "<")) {
                if (content.contains(candidate)) {
                    op = candidate;
                    break;
                }
            }

            if (op != null) {
                String[] parts = content.split(Pattern.quote(op), 2);
                if (parts.length == 2) {
                    filters.add(new Filter(parts[0].trim(), op, parts[1].trim()));
                }
            }

            start = end + 1;
        }

        return filters;

    }

    /**
     * Checks whether the given {@link LivingEntity} matches all the specified filters.
     *
     * @param e the {@link LivingEntity} to be checked against the filters
     * @param filters a list of {@link Filter} objects, each specifying a key, operator, and value
     * @return {@code true} if the entity matches all the filters, {@code false} otherwise
     */
    private static boolean matchesFilters(LivingEntity e, List<Filter> filters) {
        for (Filter f : filters) {
            String key = f.key().toLowerCase();
            String op  = f.operator();
            String val = f.value();
            switch (key) {
                case "type" -> {
                    if (!e.getType().name().equalsIgnoreCase(val)) return false;
                }
                case "health" -> {
                    double hv = Double.parseDouble(val);
                    double eh = e.getHealth();
                    if (op.equals("<") && !(eh < hv)) return false;
                    if (op.equals(">") && !(eh > hv)) return false;
                    if (op.equals("=") && !(eh == hv)) return false;
                }
                case "level" -> {
                    if (!(e instanceof Player p)) return false;
                    int lv = Integer.parseInt(val);
                    int pl = PlayerDataManager.getInstance().getPlayerData(p.getUniqueId()).getLevel();
                    if (op.equals("<") && !(pl < lv)) return false;
                    if (op.equals(">") && !(pl > lv)) return false;
                    if (op.equals("=") && !(pl == lv)) return false;
                }
                default -> {
                    // unknown filter key
                    RPGLogger.get().error(" >>> Unknown filter key: " + key + " <<<");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Executes a series of script commands within the given context.
     *
     * @param commands   the list of script commands to execute
     * @param self       the entity that acts as the executor of the script
     * @param nearbyAll  the collection of nearby entities that can be referenced by the script
     * @param event      the event associated with the script execution
     */
    public void runScript(List<ScriptCommand> commands,
                          LivingEntity self,
                          Collection<LivingEntity> nearbyAll,
                          Event event) {
        ScriptContext ctx = new ScriptContext(
                commands, self, nearbyAll, event, this, PrimevalRPG.getInstance()
        );
        ctx.run();
    }

    @Deprecated
    public static void execLine(String line, LivingEntity self, Collection<LivingEntity> nearbyAll, Event event) {
        ScriptCommand cmd = ScriptParser.parseLine(line);
        new DefaultExecutors().runScript(Collections.singletonList(cmd), self, nearbyAll, event);
    }

    /**
     * Registers a command executor with the given name. The name is case-insensitively
     * stored in lowercase. Once registered, the command executor can be retrieved and used
     * for executing commands by its associated name.
     *
     * @param name the unique name to associate with the command executor.
     *             This name will be converted to lower case.
     * @param exec the command executor instance to register.
     */
    public static void registerExecutor(String name, CommandExecutor exec) {
        SHARED.executors.put(name.toLowerCase(Locale.ROOT), exec);
    }


    /**
     * Singleton Instance of DefaultExecutors
     */
    private static final DefaultExecutors SHARED = new DefaultExecutors();

    /**
     *
     * @return @DefaultExecutors object for use.
     */
    public static DefaultExecutors getShared() {
        return SHARED;
    }

    private record Filter(String key, String operator, String value) {}
}
