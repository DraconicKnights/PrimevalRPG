package com.primevalrpg.primeval.core.RPGMobs;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Boss.LootTable;
import com.primevalrpg.primeval.core.RPGData.CustomMobCreation;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.API.Interface.MobAbility;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Data.SerializableItemStack;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.Desing.MobHeads;
import com.primevalrpg.primeval.utils.Handlers.Region;
import com.primevalrpg.primeval.utils.Handlers.RegionManager;
import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.primevalrpg.primeval.core.enums.ElementType.ELEMENT_COLORS;

/**
 * Custom mob object.
 * <p>
 * Constructs the custom mob via the provided data and then is used to manage the custom mob.
 * This updated version includes combat attributes and behavior settings.
 */
public class CustomMob implements Serializable {

    private String name;
    private String displayName;
    private String mobNameID;
    private boolean champion;
    private int level;
    private double maxHealth;
    private int spawnChance;
    private transient EntityType entityType;
    private String entityTypeName;
    private SerializableItemStack weapon;
    private Double weaponDropChance;
    private SerializableItemStack[] armour;
    private int mobID;
    private transient Entity entity;
    private UUID entityUUID;
    private transient Region region;
    private double baseHealth;
    private final ItemDrop[] lootDrops;
    private LootTable lootTable;

    // New Combat Attributes
    private double baseAttackDamage;
    private double attackRange;
    private double criticalHitChance;
    private double defenseValue;

    // New Behavior Settings
    private double movementSpeed;
    private int aggressionLevel;
    private String behaviorPattern;

    private final List<String> abilities;
    private final List<Long> abilityCooldowns;

    private List<World.Environment> spawnDimensions = new ArrayList<>();

    private ElementType elementType;

    /**
     * Updated constructor that includes new combat and behavior settings.
     */
    public CustomMob(String name, String mobNameID, boolean champion, double maxHealth, int spawnChance, EntityType entityType,
                     ItemStack weapon, Double weaponDropChance, ItemStack[] armour, int mobID, ItemDrop[] lootDrops,
                     double baseAttackDamage, double attackRange, double criticalHitChance, double defenseValue,
                     double movementSpeed, int aggressionLevel, String behaviorPattern, List<String> abilities, List<Long> abilityCooldowns, List<World.Environment> spawnDimensions
    ) {

        this.name = name;
        this.mobNameID = mobNameID;
        this.champion = champion;
        this.maxHealth = maxHealth;
        this.spawnChance = spawnChance;
        this.entityType = entityType;


        this.weapon = (weapon != null ? new SerializableItemStack(weapon) : null);


        this.weaponDropChance = weaponDropChance;
        this.armour = fromItemStackArray(armour);
        this.mobID = mobID;
        this.lootDrops = lootDrops;

        // Combat attributes
        this.baseAttackDamage = baseAttackDamage;
        this.attackRange = attackRange;
        this.criticalHitChance = criticalHitChance;
        this.defenseValue = defenseValue;

        // Behavior settings
        this.movementSpeed = movementSpeed;
        this.aggressionLevel = aggressionLevel;
        this.behaviorPattern = behaviorPattern;

        this.entityUUID = UUID.randomUUID();
        this.entityTypeName = entityType.name();
        this.baseHealth = maxHealth;

        this.abilities = abilities    != null ? abilities    : Collections.emptyList();
        this.abilityCooldowns = abilityCooldowns;

        this.spawnDimensions = spawnDimensions != null ? spawnDimensions : Collections.emptyList();
    }

    public CustomMob(CustomMob other)
    {
        this.name               = other.name;
        this.mobNameID          = other.mobNameID;
        this.champion           = other.champion;
        this.level              = other.level;
        this.maxHealth          = other.maxHealth;
        this.spawnChance        = other.spawnChance;
        this.entityType         = other.entityType;
        this.entityTypeName     = other.entityTypeName;
        this.weapon             = other.weapon;
        this.weaponDropChance   = other.weaponDropChance;
        this.armour             = other.armour;
        this.mobID              = other.mobID;
        this.baseHealth         = other.baseHealth;
        this.lootDrops          = other.lootDrops;
        this.lootTable          = other.lootTable;
        this.baseAttackDamage   = other.baseAttackDamage;
        this.attackRange        = other.attackRange;
        this.criticalHitChance  = other.criticalHitChance;
        this.defenseValue       = other.defenseValue;
        this.movementSpeed      = other.movementSpeed;
        this.aggressionLevel    = other.aggressionLevel;
        this.behaviorPattern    = other.behaviorPattern;
        this.abilities          = new ArrayList<>(other.abilities);
        this.abilityCooldowns   = new ArrayList<>(other.abilityCooldowns);
        this.spawnDimensions    = new ArrayList<>(other.spawnDimensions);
    }

    /**
     * Spawns the entity at the specified location with the given level.
     */
    public void spawnEntity(Location location, int level) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

        if (entity.getType() == EntityType.SLIME
                || entity.getType() == EntityType.MAGMA_CUBE) {
            entity.addScoreboardTag("no-split");
        }

        if (entity instanceof Ageable) {
            ((Ageable) entity).setAdult();
        }

        this.level = level;
        levelScale(level);

        LootTable loot = new LootTable();
        // Check array is not null and length > 0
        if (this.getLootDrops() != null && this.getLootDrops().length > 0) {
            for (ItemDrop itemDrop : this.getLootDrops()) {
                loot.addItem(itemDrop.getItem(), itemDrop.getDropChance());
            }
        } else {
            PrimevalRPG.getInstance().CustomMobLogger("No loot drops items found", LoggerLevel.DEBUG);  // Just for debug
        }

        this.setLootTable(loot);

        if (this.getLootTable() == null) {
        } else {
            StringBuilder lootContent = new StringBuilder();
            for (ItemDrop lootDrop : this.getLootDrops()) {
                ItemStack itemStack = lootDrop.getItem();
                double chance = lootDrop.getDropChance();
                lootContent.append("[").append(itemStack.getType().toString()).append(" x").append(itemStack.getAmount())
                        .append(", Chance: ").append(chance).append("], ");
            }
        }

        // if this is our Magma Mob, make it big and prevent splits
        if (entity instanceof MagmaCube cube) {
            cube.setSize(4);                   // size 4 = large cube
            cube.addScoreboardTag("no-split");
        }

        // 1) Health
        AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(maxHealth);
            entity.setHealth(maxHealth);
        }

        // 2) Attack Damage
        AttributeInstance attackAttr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(baseAttackDamage);
        }

        // 2b) Tuned Movement Speed
        double speedFactor = 0.2;
        double tunedSpeed  = movementSpeed * speedFactor;
        AttributeInstance speedAttr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(tunedSpeed);
        }

        // 3) Armor/Defense
        AttributeInstance armorAttr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(defenseValue);
        }

        this.displayName = name;

        // Set the custom name with colour codes and health indicator.
        updateDisplayName(entity);

        // Handle equipment (weapon and armour)
        handleEquipment(entity);

        if (entity instanceof Zombie || entity instanceof Skeleton) {
            entity.setFireTicks(0);
        }

        Region region = RegionManager.getInstance().getRegionFromLocation(location);
        setRegion(region);

        setEntity(entity);

        CustomEntityArrayHandler.getCustomEntities().put(getEntity(), this);

        // Not in use atm
        //applyBehaviorPattern(entity);

        MobAbilityManager abilityManager = new MobAbilityManager(entity, PrimevalRPG.getInstance(), 20L, getLevel());

        abilityManager.loadFromConfig();

        List<String> abilities       = getAbilities();
        List<Long>       cooldownsInTicks = getAbilityCooldowns();

        if (abilities.size() != cooldownsInTicks.size()) {
            throw new IllegalStateException(
                    "Mismatch: " + abilities.size() +
                            " abilities but " + cooldownsInTicks.size() + " cooldowns"
            );
        }

        IntStream.range(0, abilities.size())
                .forEach(i -> {
                    String key = abilities.get(i);
                    long   cd  = cooldownsInTicks.get(i);

                    // resolve String → MobAbility
                    MobAbility ability = CustomMobCreation.resolveAbility(entity, key);

                    abilityManager.register(ability, cd);
                });

        abilityManager.start();
    }

    /**
     * Updates the mob's display name with colour codes and a health indicator.
     * The name is translated to proper colour formatting and shows current/maximum health.
     */
    private void updateDisplayName(LivingEntity livingEntity) {
        String elemColor = ELEMENT_COLORS.getOrDefault(this.elementType, "&f");
        String icon;
        switch (this.elementType) {
            case FIRE:  icon = "🔥"; break;
            case WATER: icon = "💧"; break;
            case EARTH: icon = "⛰"; break;
            case WIND:  icon = "💨"; break;
            default:    icon = "";   break;
        }

        double currentHealth = Math.min(livingEntity.getHealth(), maxHealth);
        int barLength = 7;
        int filled    = (int) Math.ceil((currentHealth / maxHealth) * barLength);
        filled = Math.min(filled, barLength);
        int empty     = barLength - filled;

        String greenPart = "|".repeat(filled);
        String redPart   = "|".repeat(empty);
        String healthBar = String.format("&5[&a%s&c%s&5]&r", greenPart, redPart);

        String tierColor;
        String levelDisplay;
        if (level <= 10) {
            tierColor = "&f"; // white
            levelDisplay = String.valueOf(level);
        } else if (level <= 20) {
            tierColor = "&a"; // green
            levelDisplay = String.valueOf(level);
        } else if (level <= 30) {
            tierColor = "&6"; // orange
            levelDisplay = String.valueOf(level);
        } else if (level <= 40) {
            tierColor = "&5"; // purple
            levelDisplay = String.valueOf(level);
        } else if (level <= 50) {
            tierColor = "&b"; // light blue
            levelDisplay = String.valueOf(level);
        } else {
            // skull icon
            if (champion) {
                tierColor ="&4";
            } else {
                tierColor = "&c"; // dark red
            }
            levelDisplay = "☠"; // skull icon
        }
        String levelPart = String.format("%s[%s]&r", tierColor, levelDisplay);

        String customName = String.format(
                "%s%s %s %s %s",
                elemColor, icon, levelPart, name, healthBar
        );

        livingEntity.setCustomName(ColourCode.colour(customName));
        livingEntity.setCustomNameVisible(true);
    }

    private void startPatrolRoutine(LivingEntity livingEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Here, randomly choose a nearby location.
                Location currentLocation = livingEntity.getLocation();
                double offsetX = (Math.random() - 0.5) * 10;
                double offsetZ = (Math.random() - 0.5) * 10;
                Location targetLocation = currentLocation.clone().add(offsetX, 0, offsetZ);

                // Move the mob toward the target location.
                livingEntity.teleport(targetLocation);
            }
        }.runTaskTimer(PrimevalRPG.getInstance(), 0L, 100L);
    }

    /**
     * Example aggressive routine: the mob seeks out and chases the nearest player.
     */
    private void startAggressiveRoutine(LivingEntity mob, CustomMob customMob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player target = findNearestPlayer(mob.getLocation());
                if (target == null || mob.isDead()) return;

                // Compute direction + distance
                Vector toPlayer = target.getLocation().toVector().subtract(mob.getLocation().toVector());
                double distance = toPlayer.length();
                if (distance > 2.0) {
                    toPlayer.normalize();
                    Vector desiredVel = toPlayer.multiply(customMob.getMovementSpeed());
                    Vector currentVel = mob.getVelocity();
                    // Smooth LERP
                    Vector newVel = currentVel.multiply(0.8).add(desiredVel.multiply(0.2));
                    mob.setVelocity(newVel);
                }

                // ~5% chance each tick to get a brief speed boost
                if (Math.random() < 0.05) {
                    mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false));
                }
            }
        }.runTaskTimer(PrimevalRPG.getInstance(), 0L, 1L);
    }


    /**
     * Calculates damage using the combat attributes.
     */
    public double calculateDamage() {
        double damage = this.baseAttackDamage;
        // Apply critical hit bonus if triggered by chance
        if (Math.random() < this.criticalHitChance) {
            damage *= 1.5;
        }
        // Adjust damage with defense value (example logic)
        return Math.max(0, damage - this.defenseValue);
    }


    /**
     * Handles equipping the entity with its weapon and armour.
     * Method will be re-worked to allow for more functionality
     */
    private void handleEquipment(LivingEntity entity) {
        EntityEquipment equip = entity.getEquipment();
        if (equip == null) return;

        if (weapon != null) {
            ItemStack w = weapon.toItemStack();
            equip.setItemInMainHand(w);
        }

        if (armour != null) {
            for (SerializableItemStack sis : armour) {
                ItemStack piece = sis.toItemStack();
                Material mat = piece.getType();

                // HEAD
                if (mat.name().endsWith("_HELMET") || mat == Material.TURTLE_HELMET ||
                        MobHeads.getMobHeads().contains(mat) || MobHeads.getFullBlocks().contains(mat))  {
                    equip.setHelmet(piece);

                    // CHEST
                } else if (mat.name().endsWith("_CHESTPLATE")) {
                    equip.setChestplate(piece);

                    // LEGS
                } else if (mat.name().endsWith("_LEGGINGS")) {
                    equip.setLeggings(piece);

                    // FEET
                } else if (mat.name().endsWith("_BOOTS")) {
                    equip.setBoots(piece);
                }
            }
        }

        // 3) Prevent them from dropping their armour
        equip.setHelmetDropChance(0f);
        equip.setChestplateDropChance(0f);
        equip.setLeggingsDropChance(0f);
        equip.setBootsDropChance(0f);

    }

    private void levelScale(int level) {
        this.maxHealth = this.baseHealth + (level * 1.5);
    }

    private void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public String getBaseName() {
        return displayName;
    }

    public String getMobNameID() {
        return mobNameID;
    }

    public boolean getChampion() {
        return champion;
    }

    public int getLevel() {
        return level;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public double getHealth() {
        return ((LivingEntity)getEntity()).getHealth();
    }

    public int getSpawnChance() {
        return spawnChance;
    }

    public EntityType getEntityType() {
        if (this.entityType == null && this.entityTypeName != null) {
            this.entityType = EntityType.valueOf(this.entityTypeName);
        }
        return this.entityType;    }

    public SerializableItemStack getWeapon() {
        return weapon != null ? weapon : null;
    }

    public double getWeaponDropChance() {
        return weaponDropChance != null ? weaponDropChance : 0;
    }

    public ItemStack[] getArmour() {
        if (armour == null) {
            return null;
        }

        ItemStack[] rArmour = new ItemStack[armour.length];
        for (int i = 0; i < armour.length; i++) {
            SerializableItemStack stack = armour[i];
            rArmour[i] = stack != null ? stack.toItemStack() : null;
        }
        return rArmour;
    }

    /** Expose the internal armour wrappers. */
    public SerializableItemStack[] getSerializableArmour() {
        return this.armour;
    }

    public int getMobID() {
        return mobID;
    }

    public Entity getEntity() {
        if (this.entity == null && this.entityUUID != null) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(this.entityUUID)) {
                        this.entity = entity;
                        break;
                    }
                }
            }
        }
        return this.entity;
    }

    public Region getRegion() {
        return this.region;
    }

    public ItemDrop[] getLootDrops() {
        return lootDrops;
    }

    public void setLootTable(LootTable loot) {
        this.lootTable = loot;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public LootTable getLootTable() {
        return this.lootTable;
    }

    // Getters for the new combat attributes
    public double getBaseAttackDamage() {
        return baseAttackDamage;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public double getCriticalHitChance() {
        return criticalHitChance;
    }

    public double getDefenseValue() {
        return defenseValue;
    }

    // Getters for the new behavior settings
    public double getMovementSpeed() {
        return movementSpeed;
    }

    public int getAggressionLevel() {
        return aggressionLevel;
    }

    public String getBehaviorPattern() {
        return behaviorPattern;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public List<Long> getAbilityCooldowns() {
        return abilityCooldowns;
    }

    public List<World.Environment> getSpawnDimensions() {
        return spawnDimensions;
    }
    public void setSpawnDimensions(List<World.Environment> dims) {
        this.spawnDimensions = dims;
    }

    public void addSpawnDimension(World.Environment env) {
        if (!spawnDimensions.contains(env)) {
            spawnDimensions.add(env);
        }
    }

    public ElementType getElementType() {
        if (this.elementType == null) {
            this.elementType = ElementType.values()[ThreadLocalRandom.current().nextInt(ElementType.values().length)];
        }
        return elementType;
    }


    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    private Player findNearestPlayer(Location currentLocation) {
        if (currentLocation == null) {
            return null;
        }

        // Find the nearest player in the same world
        Player nearestPlayer = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (Player player : currentLocation.getWorld().getPlayers()) {
            // You can add additional filtering here (e.g., ignore players in Creative mode, etc.)
            double distanceSquared = player.getLocation().distanceSquared(currentLocation);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }


    /**
     * Utility conversion from an ItemStack array to a SerializableItemStack array.
     */
    private static SerializableItemStack[] fromItemStackArray(ItemStack[] armour) {
        if (armour == null) {
            return new SerializableItemStack[0];
        }
        SerializableItemStack[] result = new SerializableItemStack[armour.length];
        for (int i = 0; i < armour.length; i++) {
            if (armour[i] != null) {
                result[i] = new SerializableItemStack(armour[i]);
            }
        }
        return result;
    }

    /**
     * Utility conversion back to real ItemStacks.
     */
    private static ItemStack[] toItemStackArray(SerializableItemStack[] serial) {
        if (serial == null) return new ItemStack[0];
        ItemStack[] result = new ItemStack[serial.length];
        for (int i = 0; i < serial.length; i++) {
            if (serial[i] != null) {
                result[i] = serial[i].toItemStack();
            }
        }
        return result;
    }
}