package com.primevalrpg.primeval.utils.Handlers;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Data.RegionDataHandler;
import com.primevalrpg.primeval.utils.Scripts.ScriptUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventManager implements Listener {
    private final Plugin plugin;
    private final List<GlobalEvent> events = new ArrayList<>();

    public EventManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
        registerEvents();
        scheduleIntervalEvents();
    }

    private void loadConfig() {
        File f = new File(plugin.getDataFolder(), "globalEvents.yml");

        PrimevalRPG.getInstance().CustomMobLogger("Looking for globalEvents.yml at: " + f.getAbsolutePath(), LoggerLevel.DEBUG);
        PrimevalRPG.getInstance().CustomMobLogger("File exists: " + f.exists(), LoggerLevel.DEBUG);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

        PrimevalRPG.getInstance().CustomMobLogger("Config keys found: " + cfg.getKeys(false), LoggerLevel.DEBUG);

        for (String key : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(key);
            if (sec == null) continue;

            // read either event-based or interval-based
            String evName = sec.getString("event", "").trim();
            Class<? extends Event> cls = evName.isEmpty()
                    ? null
                    : mapEventName(evName);

            int interval = sec.getInt("interval", 0);
            String condition = sec.getString("condition", "").trim();
            boolean cancel = sec.getBoolean("cancel", false);
            String message = sec.getString("message", "").trim();
            List<String> script = sec.getStringList("script");

            events.add(new GlobalEvent(cls, interval, condition, cancel, message, script));

            PrimevalRPG.getInstance().CustomMobLogger("Loaded global-event: '"
                    + key + "' ->" + (cls != null ? cls.getSimpleName() : ("interval=" + interval)) + ", condition=" + condition, LoggerLevel.DEBUG);
        }
        PrimevalRPG.getInstance().CustomMobLogger("Total global-events: " + events.size(), LoggerLevel.INFO);
    }

    private void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (Class<? extends Event> cls : events.stream()
                .map(e -> e.eventClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())) {
            pm.registerEvent(cls, this, EventPriority.NORMAL, this::handle, plugin);
        }
    }

    private void scheduleIntervalEvents() {
        for (GlobalEvent ge : events) {
            if (ge.interval <= 0) continue;
            new BukkitRunnable() {
                public void run() {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (!passesCondition(ge.condition, player, player, null)) continue;

                        if (!ge.message.isEmpty()) {
                            player.sendMessage(
                                    ChatColor.translateAlternateColorCodes('&', ge.message));
                        }
                        // execute scripts
                        Collection<LivingEntity> nearby =
                                player.getWorld().getNearbyLivingEntities(
                                        player.getLocation(), 32, 32, 32);

                        ScriptUtils.execScript(ge.commands, player, nearby, null);
                    }
                }
            }.runTaskTimer(plugin, ge.interval, ge.interval);
        }
    }

    private void handle(Listener __, Event e) {
        for (GlobalEvent ge : events) {
            if (ge.eventClass == null || !ge.eventClass.isInstance(e)) continue;

            LivingEntity self = null;
            Player player = null;

            if (e instanceof PlayerEvent) {
                player = ((PlayerEvent) e).getPlayer();
                self = player;
            } else if (e instanceof EntityEvent
                    && ((EntityEvent) e).getEntity() instanceof LivingEntity) {
                self = (LivingEntity) ((EntityEvent) e).getEntity();
            }

            if (!passesCondition(ge.condition, self, player, e)) continue;

            if (ge.cancel && e instanceof Cancellable) {
                ((Cancellable) e).setCancelled(true);
            }

            if (player != null && !ge.message.isEmpty()) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', ge.message));
            }

            if (self != null) {
                Collection<LivingEntity> nearby =
                        self.getWorld().getNearbyLivingEntities(
                                self.getLocation(), 32, 32, 32);

                ScriptUtils.execScript(ge.commands, self, nearby, e);

            }
        }
    }

    private boolean passesCondition(String cond,
                                    LivingEntity self,
                                    Player player,
                                    Event event) {
        if (cond == null || cond.isEmpty()) return true;
        String[] andParts = cond.split("\\s*&\\s*");
        for (String part : andParts) {
            if (!evaluateSingleCondition(part.trim(), self, player, event)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateSingleCondition(String clause,
                                            LivingEntity self,
                                            Player player,
                                            Event event) {
        clause = clause.trim();
        if (clause.contains("|")) {
            return Stream.of(clause.split("\\s*\\|\\s*"))
                    .anyMatch(sub -> evaluateSingleCondition(sub, self, player, event));
        }

        if (clause.startsWith("to_world:") && event instanceof PlayerPortalEvent) {
            String want = clause.substring(9).trim();
            Location dest = ((PlayerPortalEvent) event).getTo();
            return dest != null
                    && dest.getWorld().getName().equalsIgnoreCase(want);
        }

        if (clause.startsWith("from_world:") && event instanceof PlayerPortalEvent) {
            String want = clause.substring("from_world:".length()).trim();
            Location src = ((PlayerPortalEvent) event).getFrom();
            return src != null
                    && src.getWorld().getName().equalsIgnoreCase(want);
        }

        if (clause.startsWith("!flag:") && player != null) {
            String flag = clause.substring(6).trim();

            // Check for server flag syntax: flag:@server:flagname
            if (flag.startsWith("@server:")) {
                String serverFlag = flag.substring(8).trim();
                return PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasServerFlag(serverFlag);
            }

            // Check for server flag value syntax: flag:@server:flagname=value
            if (flag.contains("=") && flag.startsWith("@server:")) {
                String[] parts = flag.split("=", 2);
                String serverFlagName = parts[0].substring(8).trim();
                String wantValue = parts[1].trim();
                String actualValue = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .getServerFlagValue(serverFlagName);
                return !wantValue.equals(actualValue);
            }

            // Regular player flag
            return PrimevalRPG.getInstance()
                    .getFlagManager()
                    .hasFlag(player.getUniqueId(), flag);
        }

        if (clause.startsWith("flag:") && player != null) {
            String flag = clause.substring(5).trim();

            // Check for server flag syntax: flag:@server:flagname
            if (flag.startsWith("@server:")) {
                String serverFlag = flag.substring(8).trim();
                return PrimevalRPG.getInstance()
                        .getFlagManager()
                        .hasServerFlag(serverFlag);
            }

            // Check for server flag value syntax: flag:@server:flagname=value
            if (flag.contains("=") && flag.startsWith("@server:")) {
                String[] parts = flag.split("=", 2);
                String serverFlagName = parts[0].substring(8).trim();
                String wantValue = parts[1].trim();
                String actualValue = PrimevalRPG.getInstance()
                        .getFlagManager()
                        .getServerFlagValue(serverFlagName);
                return wantValue.equals(actualValue);
            }

            // Regular player flag
            return PrimevalRPG.getInstance()
                    .getFlagManager()
                    .hasFlag(player.getUniqueId(), flag);
        }

        if (clause.startsWith("biome:") && player != null) {
            String want = clause.substring(6).trim();
            Biome cur = player.getLocation().getBlock().getBiome();
            return Arrays.stream(want.split("\\s*\\|\\s*"))
                    .anyMatch(b -> b.equalsIgnoreCase(cur.name()));
        }

        if (clause.startsWith("time:")) {
            String t = clause.substring(5).trim().toLowerCase();
            World w = (self != null ? self.getWorld()
                    : (player != null ? player.getWorld() : null));
            if (w == null) return false;
            long tick = w.getTime();
            switch (t) {
                case "day":
                    return tick < 12000;
                case "night":
                    return tick >= 12000;
                default:
                    return false;
            }
        }

        if (clause.startsWith("mob:") && self != null) {
            String want = clause.substring(4).trim();
            return self.getType().name().equalsIgnoreCase(want);
        }

        if (clause.startsWith("item:") && event instanceof PlayerInteractEvent pie2) {
            String want = clause.substring("item:".length()).trim();
            ItemStack inHand = pie2.getItem();
            if (inHand == null) return false;
            return inHand.getType().name().equalsIgnoreCase(want);
        }

        if (clause.startsWith("itemMeta:") && event instanceof PlayerInteractEvent) {
            String payload = clause.substring("itemMeta:".length());
            String[] parts = payload.split("=", 2);
            if (parts.length != 2) {
                return false;
            }

            String metaKey = parts[0];
            String wantVal = parts[1];

            PlayerInteractEvent pie = (PlayerInteractEvent) event;
            ItemStack inHand = pie.getItem();
            if (inHand == null || inHand.getType() == Material.AIR) {
                return false;
            }

            ItemMeta im = inHand.getItemMeta();
            if (im == null) {
                return false;
            }

            NamespacedKey nk = new NamespacedKey(PrimevalRPG.getInstance(), metaKey);
            PersistentDataContainer pdc = im.getPersistentDataContainer();

            if (!pdc.has(nk, PersistentDataType.STRING)) {
                return false;
            }

            String actual = pdc.get(nk, PersistentDataType.STRING);
            PrimevalRPG.getInstance().getLogger()
                    .info("[cond:itemMeta] key=" + metaKey + " actual=" + actual + " want=" + wantVal);

            return wantVal.equals(actual);
        }

        if (clause.startsWith("weather:") && player != null) {
            String w = clause.substring(8).trim().toLowerCase();
            World world = player.getWorld();
            switch (w) {
                case "rain":
                    return world.hasStorm() && !world.isThundering();
                case "thunder":
                    return world.isThundering();
                default:
                    return false;
            }
        }

        String lc = clause.toLowerCase(Locale.ROOT);

        if (lc.startsWith("at_block:") && event instanceof PlayerInteractEvent pie) {
            String want = clause.substring("at_block:".length()).trim();
            Block clicked = pie.getClickedBlock();
            return clicked != null
                    && clicked.getType().name().equalsIgnoreCase(want);
        }

        if (lc.startsWith("blockmeta:") && event instanceof PlayerInteractEvent pie) {
            String payload = clause.substring("blockmeta:".length());
            String[] parts = payload.split("=", 2);
            if (parts.length != 2) return false;

            String keyName = parts[0].trim();
            String want    = parts[1].trim();
            Block clicked  = pie.getClickedBlock();
            if (clicked == null) return false;

            BlockState state = clicked.getState();
            if (!(state instanceof TileState ts)) return false;

            NamespacedKey nsKey = new NamespacedKey(plugin, keyName);
            String actual = ts.getPersistentDataContainer()
                    .get(nsKey, PersistentDataType.STRING);
            return want.equals(actual);
        }

        if (clause.startsWith("action:") && event instanceof PlayerInteractEvent) {
            String want = clause.substring("action:".length()).trim();
            PlayerInteractEvent pie = (PlayerInteractEvent) event;
            return pie.getAction().name().equalsIgnoreCase(want);
        }

        // new: at_chunk:<X>:<Z>
        if (clause.startsWith("at_chunk:") && player != null) {
            String[] parts = clause.substring("at_chunk:".length()).trim().split(":");
            if (parts.length != 2) return false;
            try {
                int wantX = Integer.parseInt(parts[0]);
                int wantZ = Integer.parseInt(parts[1]);
                Chunk c = player.getLocation().getChunk();
                return c.getX() == wantX && c.getZ() == wantZ;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (clause.startsWith("at_structure:") && player != null) {
            String list = clause.substring("at_structure:".length()).trim();
            String[] names = list.split("\\s*\\|\\s*");
            Location loc = player.getLocation();
            Chunk playerChunk = loc.getChunk();

            for (String raw : names) {
                String keyName = raw.toLowerCase();
                NamespacedKey nsKey = NamespacedKey.minecraft(keyName);
                StructureType type = Registry.STRUCTURE_TYPE.get(nsKey);
                if (type == null) continue;  // unknown structure

                Location nearest = loc.getWorld()
                        .locateNearestStructure(loc, type, 16, false).getLocation();
                if (nearest == null) continue;

                Chunk structChunk = nearest.getChunk();
                if (playerChunk.getX() == structChunk.getX()
                        && playerChunk.getZ() == structChunk.getZ()) {
                    return true;
                }
            }
            return false;
        }

        if (clause.startsWith("in_region:")) {
            String regionName = clause.substring("in_region:".length());
            return RegionDataHandler.getInstance().isInside(player, regionName);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> mapEventName(String name) {
        // Try player events first
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.player." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try entity events
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.entity." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try block events
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.block." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try server events
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.server." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try world events
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.world." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try inventory events
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event.inventory." + name);
        } catch (ClassNotFoundException ignored) { }

        // Try root event package last
        try {
            return (Class<? extends Event>)
                    Class.forName("org.bukkit.event." + name);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Unknown event type: " + name, ex);
        }
    }

    private static class GlobalEvent {
        final Class<? extends Event> eventClass;
        final int interval;
        final String condition;
        final boolean cancel;
        final String message;
        final List<String> commands;

        GlobalEvent(Class<? extends Event> eventClass,
                    int interval,
                    String condition,
                    boolean cancel,
                    String message,
                    List<String> commands) {
            this.eventClass = eventClass;
            this.interval   = interval;
            this.condition  = condition;
            this.cancel     = cancel;
            this.message    = message;
            this.commands   = commands;
        }
    }
}