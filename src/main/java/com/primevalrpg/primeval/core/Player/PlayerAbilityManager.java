package com.primevalrpg.primeval.core.Player;

import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import com.primevalrpg.primeval.utils.Scripts.ScriptUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerAbilityManager implements Listener {
    private final Plugin plugin;
    private final Map<String, Ability> abilities = new LinkedHashMap<>();
    private final Map<UUID, Long> cooldownExpiry = new HashMap<>();

    private static PlayerAbilityManager instance;

    public PlayerAbilityManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
        registerListeners();
        instance = this;
    }

    private void loadConfig() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "playerAbilities.yml"));
        for (String key : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(key);
            String type = sec.getString("type", "None");              // ← read it
            int lvl = sec.getInt("requiredLevel");
            String evt = sec.getString("event");
            String cond = sec.getString("condition");
            int cd  = sec.getInt("cooldown", 0);
            List<String> lines = sec.getStringList("script");
            // if you have interval‐based events, read that here too…
            Ability abl = new Ability(key, type, lvl, evt, cond, lines, 0, cd);
            abilities.put(key, abl);
        }
    }

    private void registerListeners() {
        // 1) catch interact events
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // 2) schedule any interval‐based abilities
        abilities.values().stream()
                .filter(a -> "IntervalEvent".equals(a.eventName) && a.interval > 0)
                .forEach(a -> new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (meetsLevel(p, a.requiredLevel)) {
                                ScriptUtils.execScript(
                                        a.scriptLines,       // lines
                                        p,                   // self
                                        Collections.emptyList(), // nearbyAll
                                        null                 // no actual Event
                                );
                            }
                        }
                    }
                }.runTaskTimer(plugin, a.interval, a.interval));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        Player p = ev.getPlayer();
        // 0) get the one ability the player has equipped
        PlayerData data = PlayerDataManager.getInstance()
                .getPlayerData(p.getUniqueId());
        Ability ability = data.getEquippedAbility();
        if (ability == null) return;

        // 1) check correct event type
        if (!ev.getClass().getSimpleName().equals(ability.getEventName())) {
            return;
        }

        // 2) level requirement
        if (data.getLevel() < ability.getRequiredLevel()) {
            p.sendMessage(ChatColor.RED
                    + "You need level " + ability.getRequiredLevel()
                    + " to use " + ability.getName() + "!");
            return;
        }

        // 3) cooldown (in ms)
        UUID uuid = p.getUniqueId();
        long now = System.currentTimeMillis();
        Long expiry = cooldownExpiry.get(uuid);
        if (expiry != null && now < expiry) {
            return;
        }

        // 4) full‐power condition check
        String cond = ability.getCondition();
        if (!ScriptUtils.evaluateCondition(cond, p, p, ev)) {
            RPGLogger.get().error(">>> " + p.getName()
                    + " tried to use " + ability.getName()
                    + " but failed condition: " + cond + " <<<");
            return;
        }

        // collect nearby living entities
        Collection<LivingEntity> nearby = p.getNearbyEntities(32,32,32).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity)e)
                .collect(Collectors.toList());

        // 5) mark cooldown
        cooldownExpiry.put(uuid, now + ability.getCooldown() * 1000L);

        // 6) execute the ability’s script
        ScriptUtils.execScript(
                ability.getScriptLines(),
                p,
                nearby,
                ev
        );
    }

    /** Very minimal parser: supports action:XYZ and item:ABC joined with & */
    private boolean basicConditionCheck(PlayerInteractEvent ev, String cond) {
        if (cond == null || cond.isBlank()) return true;
        String[] parts = cond.split("&");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("action:")) {
                String want = part.substring("action:".length());
                if (!ev.getAction().name().equalsIgnoreCase(want)) {
                    return false;
                }
            } else if (part.startsWith("item:")) {
                if (ev.getItem() == null) return false;
                String want = part.substring("item:".length());
                if (!ev.getItem().getType().name().equalsIgnoreCase(want)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean meetsLevel(Player p, int req) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(p.getUniqueId());
        return data != null && data.getLevel() >= req;
    }

    public Map<String, Ability> getAbilities() {
        return Collections.unmodifiableMap(abilities);
    }

    public static PlayerAbilityManager getInstance() {
        return instance;
    }

    public Map<String, Ability> getAlibilityMap() {
        return abilities;
    }

    public static class Ability {
        final String name;
        final String type;           // new!
        final int requiredLevel;
        final String eventName;
        final String condition;
        final List<String> scriptLines;
        final int interval;
        final int cooldown;

        Ability(String name, String type, int lvl, String evt, String cond,
                List<String> lines, int interval, int cooldown) {
            this.name = name;
            this.type = type;
            this.requiredLevel = lvl;
            this.eventName = evt;
            this.condition = cond;
            this.scriptLines = lines;
            this.interval = interval;
            this.cooldown = cooldown;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public String getEventName() {
            return eventName;
        }

        public String getCondition() {
            return condition;
        }

        public List<String> getScriptLines() {
            return scriptLines;
        }

        public int getInterval() {
            return interval;
        }

        public int getCooldown() {
            return cooldown;
        }
    }

}