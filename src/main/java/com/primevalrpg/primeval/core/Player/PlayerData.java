package com.primevalrpg.primeval.core.Player;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Inventory.PlayerMenu.AbilityUnlockMenu;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.NameTagUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.UUID;

/**
 * Player Data object for dealing with and managing player data.
 * Supports per-element leveling (Fire, Water, Earth, Wind), each capped at 50,
 * plus an overall average level (also capped at 50).
 */
public class PlayerData {
    public static final int MAX_LEVEL     = 500;
    public static final int BASE_XP_PER_LVL = 100; // flat XP per level

    public static final int MAX_OVERALL_LEVEL = 50;

    private static final double XP_GROWTH_RATE = 1.15;

    private final Set<String> unlockedAbilities = new HashSet<>();
    private PlayerAbilityManager.Ability equippedAbility;

    private ElementType activeElement;

    /** Tracks level & xp for a single element. */
    private static class ElementProgress {
        private int level = 1;
        private int xp    = 0;

        // XP needed to go from `level` → `level+1`
        private int xpToNext() {
            return xpThreshold(level);
        }

        void addXp(int amount) {
            xp += amount;
            // loop in case we gain enough to go several levels at once
            while (level < MAX_LEVEL && xp >= xpToNext()) {
                xp -= xpToNext();
                level++;
            }
            if (level >= MAX_LEVEL) {
                level = MAX_LEVEL;
                xp = 0;
            }
        }

        int getLevel() { return level; }
        int getXp()    { return xp;   }
    }

    private final UUID playerUUID;
    private final Map<ElementType,ElementProgress> elements =
            new EnumMap<>(ElementType.class);
    private int overallLevel = 1;

    // Example RPG stats
    private double strength    = 1.0;
    private double defense     = 0.0;
    private double bonusHealth = 0.0;

    // Ability tracking
    private final Set<String> abilities = new HashSet<>();
    private String selectedAbility;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        for (ElementType type : ElementType.values()) {
            elements.put(type, new ElementProgress());
        }
        this.activeElement = ElementType.FIRE;
    }

    public void onMobKill(Player player, int multiplier) {
        addElementXp(ElementType.FIRE, 10 * multiplier, player);
    }

    public void onBlockMine(Player player, int multiplier) {
        addElementXp(ElementType.EARTH, 5 * multiplier, player);
    }

    /**
     * Add XP to a specific element, handle sub‐level‐up, ability unlocks,
     * recalc overall level & stats, then save.
     */
    public void addElementXp(ElementType type, int amount, Player player) {
        ElementProgress prog = elements.get(type);
        int oldLvl = prog.getLevel();
        prog.addXp(amount);
        int newLvl = prog.getLevel();

        if (newLvl > oldLvl) {
            // 1) Fancy title announcement
            String title    = ChatColor.GREEN + "Level Up!";
            String subtitle = ChatColor.GOLD + type.name() + " " + ChatColor.YELLOW + newLvl;
            player.sendTitle(title, subtitle, 10, 70, 20);

            // 2) Play level‐up sound
            player.playSound(player.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP,
                    1.0f, 1.0f);

            // 3) Spawn particles around the player
            player.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    player.getLocation().add(0, 1, 0),
                    50,    // count
                    0.5,   // offsetX
                    1.0,   // offsetY
                    0.5,   // offsetZ
                    0.1    // extra
            );

            // 4) Fallback chat message
            player.sendMessage(
                    ChatColor.GREEN + "Your " + type.name() +
                            " skill just hit " + ChatColor.GOLD + newLvl + ChatColor.GREEN + "!"
            );

            checkAbilityUnlocks(player, type, newLvl);
        }
        recalcOverallLevel(player);
        recalcStats(player);

        // persist
        PlayerDataManager.getInstance().savePlayerData(this);
    }

    /**
     * Every 50 levels you get to choose a new ability in this element.
     */
    private void checkAbilityUnlocks(Player player, ElementType type, int newLevel) {
        if (newLevel % 50 != 0) return;

        player.sendMessage(ChatColor.GREEN +
                "Congratulations! Your " + type.name() +
                " level is now " + newLevel +
                ". Choose a new ability:");
        // pops the GUI
        new AbilityUnlockMenu(type, player).open(player);
    }

    /**
     * Recalculate overallLevel as the average of all element‐levels,
     * then clamp it to MAX_OVERALL_LEVEL.
     */
    private void recalcOverallLevel(Player player) {
        // sum all element levels
        int sum = elements.values().stream()
                .mapToInt(ElementProgress::getLevel)
                .sum();
        // compute average (rounded down)
        int avg = sum / elements.size();

        // clamp to max
        int newOverall = Math.min(avg, MAX_OVERALL_LEVEL);

        if (newOverall != this.overallLevel) {
            this.overallLevel = newOverall;
            // recalc strength/defense or notify player
            recalcStats(player);
            Bukkit.getScheduler().runTask(PrimevalRPG.getInstance(), () ->
                    NameTagUtil.updateNameTag(player)
            );
            // you could send a level-up message here if desired
        }
    }

    /**
     * Recalculate player stats on level‐up: only adjust strength and defense.
     * Do not modify health (no extra hearts).
     */
    private void recalcStats(Player player) {
        // compute new stats based solely on overallLevel
        // small incremental bonuses per level
        this.strength = 1.0 + overallLevel * 0.1;   // +0.1 damage per level
        this.defense  = overallLevel * 0.05;        // +0.05 armor per level
        this.bonusHealth = 0;                       // no extra health bonus

        // apply to Bukkit attributes
        if (player.isOnline()) {
            // Attack damage
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(this.strength);
            // Armor / defense
            player.getAttribute(Attribute.GENERIC_ARMOR)
                    .setBaseValue(this.defense);
            // skip Attribute.GENERIC_MAX_HEALTH,
            // so health (hearts) remains unchanged.
        }
    }

    // helper for “what’s the XP threshold at level L → L+1?”
    private static int xpThreshold(int level) {
        return (int)(BASE_XP_PER_LVL * Math.pow(XP_GROWTH_RATE, level - 1));
    }

    /* --- Element Getters --- */

    // 3) getters/setters
    public ElementType getActiveElement() {
        return activeElement;
    }

    public void setActiveElement(ElementType element) {
        this.activeElement = element;

        if (equippedAbility != null) {
            // clear if the equipped ability’s type does NOT match the new element
            if (!equippedAbility.type.equalsIgnoreCase(element.name())) {
                selectedAbility   = null;
                equippedAbility   = null;
            }
        }

        Bukkit.getScheduler().runTask(PrimevalRPG.getInstance(), () ->
                NameTagUtil.updateNameTag(Bukkit.getPlayer(playerUUID))
        );

        PlayerDataManager.getInstance().savePlayerData(this);

       /* if (selectedAbility != null) {
            // fetch ability by key
            PlayerAbilityManager.Ability ability =
                    PlayerAbilityManager.getInstance()
                            .getAlibilityMap()
                            .get(selectedAbility);

            // if ability missing or its type doesn’t match → clear
            if (ability == null
                    || ElementType.fromString(ability.type)
                    .map(at -> !at.equals(element))
                    .orElse(true)
            ) {
                selectedAbility = null;
            }
        }*/
    }


    /* --- Compatibility getters --- */

    /** overall‐level alias for getOverallLevel() */
    public int getLevel() {
        return getOverallLevel();
    }

    /** total xp across all elements (compat) */
    public int getXp() {
        return elements.values().stream().mapToInt(ElementProgress::getXp).sum();
    }

    /* --- New getters for sub‐levels & xp --- */

    public int getOverallLevel() {
        return overallLevel;
    }

    public int getElementLevel(ElementType type) {
        return elements.get(type).getLevel();
    }

    public int getElementXp(ElementType type) {
        return elements.get(type).getXp();
    }

    public int getXpToNext(ElementType element, int level) {
        // element is unused in our simple per‐level formula, but kept for signature
        return xpThreshold(level);
    }

    /* --- Ability management --- */

    public Set<String> getAbilities() {
        return Collections.unmodifiableSet(abilities);
    }


    // 2) Check if the player already has this ability
    public boolean hasAbility(String abilityId) {
        return unlockedAbilities.contains(abilityId);
    }

    // 3) (Optional) Mark an ability as unlocked
    public void addAbility(String abilityId) {
        unlockedAbilities.add(abilityId);
        // if you want to persist immediately:
        // PlayerDataManager.getInstance().savePlayerData(this);
    }

    // 4) (If you’re serializing PlayerData, expose the field)
    public Set<String> getUnlockedAbilities() {
        return unlockedAbilities;
    }

    // new getter for the object
    public PlayerAbilityManager.Ability getEquippedAbility() {
        return equippedAbility;
    }

    public void setSelectedAbility(String ability) {
        if (abilities.contains(ability)) {
            this.selectedAbility = ability;
        }
    }

    // optional convenience if you ever want to equip by object directly:
    public void equipAbility(PlayerAbilityManager.Ability ability) {
        if (ability != null) {
            this.equippedAbility = ability;
            this.selectedAbility = ability.getName();
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void resetAll() {
        elements.values().forEach(ep -> {
            ep.level = 1;
            ep.xp = 0;
        });
        overallLevel = 1;
        abilities.clear();
        selectedAbility = null;
        Player p = PrimevalRPG.getInstance()
                .getServer()
                .getPlayer(playerUUID);
        if (p != null) recalcStats(p);
        PlayerDataManager.getInstance().savePlayerData(this);
    }

    /** ADMIN: forcibly set overall level (no per‐element change). */
    public void setOverallLevel(int lvl, Player player) {
        this.overallLevel = Math.min(MAX_LEVEL, Math.max(1, lvl));
        recalcStats(player);
        PlayerDataManager.getInstance().savePlayerData(this);
    }

    /** ADMIN: clear all unlocked abilities. */
    public void clearUnlockedAbilities() {
        abilities.clear();
        PlayerDataManager.getInstance().savePlayerData(this);
    }

    /** ADMIN: reset everything (levels, xp, abilities, etc). */
    // already exists as resetAll(), but expose alias:
    public void resetAttributes(Player player) {
        resetAll();
        // recalcStats in resetAll already called
        if (player != null) {
            recalcStats(player);
        }
    }

    public String getSelectedAbility() {
        return selectedAbility;
    }

}