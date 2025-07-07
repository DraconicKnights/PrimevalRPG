package com.primevalrpg.primeval.core.Inventory.Core;

import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.CreationStage;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class CreationSession {
    private final UUID playerId;
    private CreationStage next = CreationStage.NAME;

    private boolean editMode = false;
    private String originalKey;

    // base attributes
    private String name           = "";
    private boolean champion      = false;
    private double maxHealth      = 30.0;
    private int spawnChance       = 50;
    private EntityType entityType = EntityType.ZOMBIE;

    // base combat
    private double baseAttackDamage   = 6.0;
    private double attackRange        = 1.5;
    private double criticalHitChance  = 0.25;
    private double defenseValue       = 2.0;

    // base behavior
    private double movementSpeed   = 1.2;
    private int aggressionLevel    = 8;
    private String behaviorPattern = "stealth";

    // base equipment
    private ItemStack weapon;
    private double weaponDropChance = 1.0;

    // detailed weapon settings
    private int                             weaponAmount         = 1;
    private boolean                         weaponEnchanted      = false;
    private Map<Enchantment,Integer> weaponEnchants       = new HashMap<>();
    private boolean                         weaponGlow           = false;
    private boolean                         weaponUnbreakable    = true;
    private boolean                         weaponHideFlags      = true;
    private String                          weaponDisplayName    = "";
    private List<String>                    weaponLore           = new ArrayList<>();

    // base armour
    private List<Material> armourPieces = new ArrayList<>();
    private final Map<Material,String> armourNames    = new HashMap<>();

    // Armour colour. not in use atm.
    private final Map<Material,Color>                           armourColours  = new HashMap<>();
    private final Map<Material,Map<Enchantment,Integer>>        armourEnchants = new HashMap<>();

    // base loot
    private final List<ItemDrop> lootDrops = new ArrayList<>();

    private final List<String> abilityTypes = new ArrayList<>();
    private final Map<String, Long> abilityCooldowns = new HashMap<>();

    private final Set<World.Environment> spawnDimensions = new LinkedHashSet<>();

    public CreationSession(UUID playerId) {
        this.playerId = playerId;
        this.weapon = new ItemStack(Material.DIAMOND_SWORD);
    }

    public CreationStage getNext() {
        return next;
    }

    public void setNext(CreationStage s) {
        next = s;
    }

    // toggles champion
    public void toggleChampion() {
        champion = !champion;
    }

    // setters
    public void setName(String s) { name = s; }
    public void setHealth(double h) { maxHealth = h; }
    public void setSpawnChance(int c) { spawnChance = c; }
    public void setEntityType(EntityType t) { entityType = t; }
    public void setCombat(double dmg, double rng, double crit, double def) {
        baseAttackDamage = dmg;
        attackRange       = rng;
        criticalHitChance = crit;
        defenseValue      = def;
    }
    public void setBehavior(double spd, int agr, String pat) {
        movementSpeed   = spd;
        aggressionLevel = agr;
        behaviorPattern = pat;
    }
    public void setWeapon(ItemStack w) { weapon = w; }
    public void setWeaponDropChance(double d) { weaponDropChance = d; }
    public void setArmour(List<Material> mats) { armourPieces = mats; }
    public void setLootDrops(List<ItemDrop> drops) { lootDrops.clear(); lootDrops.addAll(drops); }

    // getters for combat fields
    public double getCombatDamage() {
        return baseAttackDamage;
    }
    public double getCombatRange() {
        return attackRange;
    }
    public double getCombatCrit() {
        return criticalHitChance;
    }
    public double getCombatDef() {
        return defenseValue;
    }

    // getters for behavior fields
    public double getBehaviorSpeed() {
        return movementSpeed;
    }
    public int getBehaviorAggression() {
        return aggressionLevel;
    }
    public String getBehaviorPattern() {
        return behaviorPattern;
    }

    // summary strings for GUI
    public String summaryEquipment() {
        return weapon.getType().name() + " (" + weaponDropChance + ")";
    }
    public String summaryArmour() {
        if (armourPieces.isEmpty()) return "none";
        return armourPieces.toString();
    }
    public String summaryLoot() {
        if (lootDrops.isEmpty()) return "none";
        return lootDrops.toString();
    }

    public void setChampion(boolean value) {
        champion = value;
    }

    public void setAbilties(List<String> keys) {
        abilityTypes.addAll(keys);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public double getWeaponDropChance() {
        return weaponDropChance;
    }

    public void setWeaponAmount(int amt) { weaponAmount = amt; }
    public int getWeaponAmount() { return weaponAmount; }

    public void toggleWeaponEnchanted() { weaponEnchanted = !weaponEnchanted; }
    public boolean isWeaponEnchanted() { return weaponEnchanted; }

    public void setWeaponEnchants(Map<Enchantment,Integer> m) { weaponEnchants = m; }
    public Map<Enchantment,Integer> getWeaponEnchants() { return weaponEnchants; }

    public void toggleWeaponGlow() { weaponGlow = !weaponGlow; }
    public boolean isWeaponGlow() { return weaponGlow; }

    public void setWeaponUnbreakable(boolean b) { weaponUnbreakable = b; }
    public boolean isWeaponUnbreakable() { return weaponUnbreakable; }

    public void toggleWeaponHideFlags() { weaponHideFlags = !weaponHideFlags; }
    public boolean isWeaponHideFlags() { return weaponHideFlags; }

    public void setWeaponDisplayName(String name) { weaponDisplayName = name; }
    public String getWeaponDisplayName() { return weaponDisplayName; }

    public void setWeaponLore(List<String> lore) { weaponLore = lore; }
    public List<String> getWeaponLore() { return weaponLore; }

    // Add these setters/getters for detailed weapon and ability fields:
    public void setWeaponEnchanted(boolean enchanted) {
        this.weaponEnchanted = enchanted;
    }

    public void setWeaponGlow(boolean glow) {
        this.weaponGlow = glow;
    }

    public void setWeaponHideFlags(boolean hideFlags) {
        this.weaponHideFlags = hideFlags;
    }

    public Map<String, Long> getAbilityCooldowns() {
        return abilityCooldowns;
    }


    // when building the final mob, apply these to the ItemStack:
    public ItemStack buildWeapon() {
        ItemStack finalWeapon = ItemBuilder.createWeapon(
                getWeapon(),
                getWeaponAmount(),
                isWeaponEnchanted(),
                getWeaponEnchants(),
                isWeaponGlow(),
                isWeaponUnbreakable(),
                isWeaponHideFlags(),
                getWeaponDisplayName(),
                getWeaponLore()
        );
        return finalWeapon;
    }

    public Map<Material,Color> getArmourColours() {
        return armourColours;
    }
    public Map<Material,Map<Enchantment,Integer>> getArmourEnchants(){
        return armourEnchants;
    }

    public UUID getPlayerId() {
        return playerId;
    }


    public boolean isComplete() {
        return !name.isBlank();
    }

    public List<Material> getArmourPieces() {
        return armourPieces;
    }

    public List<ItemDrop> getLootDrops() {
        return lootDrops;
    }

    public boolean isChampion() {
        return champion;
    }

    public String summaryCombat() {
        return String.format(
                "DMG: %.1f, RNG: %.1f, CRIT: %.2f, DEF: %.1f",
                baseAttackDamage,
                attackRange,
                criticalHitChance,
                defenseValue
        );
    }

    public String summaryBehavior() {
        return String.format(
                "Speed: %.2f, Agg: %d, Pat: %s",
                movementSpeed,
                aggressionLevel,
                behaviorPattern
        );
    }

    // new API for abilities
    public void addAbility(String type, long cooldown) {
        if (!abilityTypes.contains(type)) {
            abilityTypes.add(type);
            abilityCooldowns.put(type, cooldown);
        }
    }
    public void removeAbility(String type) {
        abilityTypes.remove(type);
        abilityCooldowns.remove(type);
    }
    public List<String> getAbilityTypes() {
        return new ArrayList<>(abilityTypes);
    }
    public Long getAbilityCooldown(String type) {
        return abilityCooldowns.get(type);
    }
    public String summaryAbilities() {
        if (abilityTypes.isEmpty()) return "none";
        return abilityTypes.stream()
                .map(t -> t + "(" + abilityCooldowns.get(t) + "t)")
                .collect(Collectors.joining(", "));
    }

    // expose immutable view
    public Set<World.Environment> getSpawnDimensions() {
        return Collections.unmodifiableSet(spawnDimensions);
    }

    // toggle on/off
    public void toggleDimension(World.Environment env) {
        if (!spawnDimensions.add(env)) {
            spawnDimensions.remove(env);
        }
    }

    public void setSpawnDimensions(Collection<World.Environment> dims) {
        spawnDimensions.clear();
        spawnDimensions.addAll(dims);
    }

    public void enableEditMode(String key) {
        this.editMode = true;
        this.originalKey = key;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void setOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }


    public String summaryDimensions() {
        if (spawnDimensions.isEmpty()) {
            return "None";
        }
        return spawnDimensions.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /** Build the real CustomMob */
    public CustomMob buildMob() {

        ItemStack finalWeapon = buildWeapon();

        // convert armour list to array of ItemStack
        ItemStack[] armourArr = armourPieces.stream()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new);

        // build drops array
        ItemDrop[] dropArr = lootDrops.toArray(new ItemDrop[0]);

        // build abilities array list
        List<String> abs = new ArrayList<>(abilityTypes);
        List<Long>   cds = abilityTypes.stream()
                .map(type -> abilityCooldowns.getOrDefault(type, 0L))
                .collect(Collectors.toList());


        // sanitize ID
        // generate a clean id
        String id = name.replaceAll("\\s+","").toLowerCase();


        String displayName = ColourCode.colour(name);

        String stripped = ChatColor.stripColor(displayName);

        String mobNameId = stripped.replaceAll("\\s+", "").toLowerCase();

        return new CustomMob(
                displayName,
                mobNameId,
                champion,
                maxHealth,
                spawnChance,
                entityType,
                finalWeapon,
                weaponDropChance,
                armourArr,
                id.hashCode(),
                dropArr,
                baseAttackDamage,
                attackRange,
                criticalHitChance,
                defenseValue,
                movementSpeed,
                aggressionLevel,
                behaviorPattern,
                abs,
                cds,
                new ArrayList<>(spawnDimensions)
        );
    }
}