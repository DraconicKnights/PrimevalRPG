package com.primevalrpg.primeval.core.RPGData;

import com.primevalrpg.primeval.utils.API.Interface.MobAbility;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.RPGMobs.MobAbilities.*;
import com.primevalrpg.primeval.utils.Arrays.CustomEntityArrayHandler;
import com.primevalrpg.primeval.utils.Data.CustomItemHandler;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.utils.Data.SerializableItemStack;
import com.primevalrpg.primeval.utils.ItemBuilder;
import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom Mob Creation object
 * Used for creating a valid Custom Mob using the mobs.yml file
 */
public class CustomMobCreation {

    public static CustomMob fromMap(Map<?, ?> mobMap) {

        String name = (String) mobMap.get("name");
        String mobNameID = (String) mobMap.get("mobID");
        boolean isChampion = getBooleanFromMap(mobMap, "champion", false);
        double maxHealth = getDoubleFromMap(mobMap, "maxHealth", 20.0); // default value example

        // New Combat Attributes (with defaults provided via getOrDefault).
        double baseAttackDamage = getDoubleFromMap(mobMap, "baseAttackDamage", 1.0); // default value example
        double attackRange = getDoubleFromMap(mobMap, "attackRange", 1.0); // default value example
        double criticalHitChance = getDoubleFromMap(mobMap, "criticalHitChance", 1.0); // default value example
        double defenseValue = getDoubleFromMap(mobMap, "defenseValue", 1.0); // default value example

        // New Behavior Settings.
        double movementSpeed = getDoubleFromMap(mobMap, "movementSpeed", 1.0); // default value example
        int aggressionLevel = getIntegerFromMap(mobMap, "aggressionLevel", 1); // 50 as an example default
        String behaviorPattern = (String) mobMap.get("behaviorPattern");

        int spawnChance = getIntegerFromMap(mobMap, "spawnChance", 50); // 50 as an example default
        EntityType entityType = EntityType.valueOf((String) mobMap.get("entityType"));
        ItemDrop[] lootDrops = convertToItemDropArray(mobMap.get("lootDrops"));

        Material weaponMaterial = null;
        int weaponAmount = 0;
        Map<Enchantment, Integer> enchantmentMap;
        boolean glow = false;
        boolean unbreakable = false;
        boolean hide = false;
        String weaponName = null;
        List<String> weaponLore = List.of();
        Double weaponDropChance = null;

        // Extract weapon properties
        if (mobMap.containsKey("weapon")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> weaponMap = (Map<String, Object>) mobMap.get("weapon");
            weaponMaterial = Material.valueOf((String) weaponMap.get("material"));
            weaponAmount   = (int) weaponMap.get("amount");

            // always start with an empty map
           enchantmentMap = new LinkedHashMap<>();

            Object rawEnchants = weaponMap.get("enchantments");
            if (rawEnchants instanceof Map<?,?>) {
                // existing map-based parsing:
                @SuppressWarnings("unchecked")
                Map<String,Object> raw = (Map<String,Object>) rawEnchants;
                raw.forEach((k,v) -> {
                    if (v instanceof Number num) {
                        Enchantment e = Enchantment.getByName(k.toUpperCase());
                        if (e != null) enchantmentMap.put(e, num.intValue());
                    }
                });
            }
            else if (rawEnchants instanceof List<?>) {
                // fallback: two parallel lists (names + levels)
                List<?> enchantNames  = (List<?>) rawEnchants;
                Object rl      = weaponMap.get("enchantmentLevels");
                List<?> levels = rl instanceof List<?> ? (List<?>) rl : Collections.emptyList();

                for (int i = 0; i < enchantNames.size(); i++) {
                    String  ecname = enchantNames.get(i).toString();
                    int     lvl  = (i < levels.size() && levels.get(i) instanceof Number)
                            ? ((Number)levels.get(i)).intValue()
                            : 1;
                    Enchantment e = Enchantment.getByName(ecname.toUpperCase());
                    if (e != null) enchantmentMap.put(e, lvl);
                }
            }

            glow         = (boolean) weaponMap.get("glow");
            unbreakable  = (boolean) weaponMap.get("unbreakable");
            hide         = (boolean) weaponMap.get("hide");
            weaponName   = (String) weaponMap.get("weaponName");

            weaponLore = getStringListFromMap(weaponMap, "weaponLore");
            

        } else {
            enchantmentMap = null;
        }

        // pull this from the outer mobMap, not from weaponMap:
        if (mobMap.containsKey("weaponDropChance")) {
            weaponDropChance = (Double) mobMap.get("weaponDropChance");
        }

        // ── ARMOUR BLOCK ────────────────────────────────────
        boolean hasArmour = Boolean.TRUE.equals(mobMap.get("hasArmour"));
        List<ItemStack> armour = getArmourItems(mobMap, hasArmour);

        // ── FINAL CONSTRUCTION ───────────────────────────────
        int mobID = CustomEntityArrayHandler.getRegisteredCustomMobs().size();
        ItemStack weapon = null;
        if (weaponMaterial != null) {
            ItemStack raw = ItemBuilder.createEnchantItem(
                    weaponMaterial, weaponAmount,
                    enchantmentMap, glow, unbreakable, hide,
                    weaponName, weaponLore
            );
            weapon = new SerializableItemStack(raw).toItemStack();
        }

        // ── ABILITIES BLOCK ──────────────────────────────────
        List<String> abilityKeys = new ArrayList<>();
        List<Long>   cooldowns   = new ArrayList<>();
        Object rawAbilities = mobMap.get("abilities");
        if (rawAbilities instanceof List<?>) {
            for (Object entry : (List<?>) rawAbilities) {
                if (entry instanceof Map<?,?> am) {
                    String type = (String) am.get("type");
                    Number cd   = (Number) am.get("cooldown");
                    abilityKeys.add(type);
                    cooldowns.add(cd == null ? 0L : cd.longValue());
                }
                else if (entry instanceof String type) {
                    // legacy single‐string entry
                    abilityKeys.add(type);
                    cooldowns.add(0L);
                }
            }
        }

        // ── DIMENSIONS PARSE ─────────────────────────────────
        @SuppressWarnings("unchecked")
        List<String> rawDims = getStringListFromMap(mobMap, "spawnDimensions");
        List<World.Environment> spawnDims = new ArrayList<>();
        if (rawDims != null) {
            for (String d : rawDims) {
                // use your helper to normalize & parse
                spawnDims.add(parseEnvironment(d));
            }
        }
        if (spawnDims.isEmpty()) {
            // default back to Overworld
            spawnDims.add(World.Environment.NORMAL);
        }

        // Create the CustomMob instance
        // Construct and return the CustomMob instance.
        return new CustomMob(
                name,
                mobNameID,
                isChampion,
                maxHealth,
                spawnChance,
                entityType,
                weapon,
                weaponDropChance,
                ItemBuilder.makeArmourSet(armour),
                mobID,
                lootDrops,
                baseAttackDamage,
                attackRange,
                criticalHitChance,
                defenseValue,
                movementSpeed,
                aggressionLevel,
                behaviorPattern,
                abilityKeys,
                cooldowns,
                spawnDims
        );
    }

    public static MobAbility resolveAbility(LivingEntity entity, String type) {
        return switch(type.toLowerCase()) {
            // … add as many as you need
            default -> {
                    ConfigurationSection node = MobDataHandler.getAbilitiesConfig().getConfigurationSection("abilities." + type);
                if (node == null) {
                    throw new IllegalArgumentException("Unknown ability type: " + type);
                }

                List<String> scriptLines = node.getStringList("script");
                yield new ScriptAbility(entity, scriptLines);
                }
        };
    }

    private static List<String> getStringListFromMap(Map<?,?> map, String key) {
        Object o = map.get(key);
        if (o instanceof List<?>) {
            //noinspection unchecked
            return ((List<?>) o).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else if (o != null) {
            return Collections.singletonList(o.toString());
        } else {
            return Collections.emptyList();
        }
    }

    private static int getIntegerFromMap(Map<?, ?> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        // Optionally, log a warning that the key was missing or not a number.
        return defaultValue;
    }


    private static boolean getBooleanFromMap(Map<?, ?> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        // Optionally log a warning that "key" is missing or misconfigured.
        return defaultValue;
    }

    private static double getDoubleFromMap(Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        // You could log a warning here if needed:
        // Logger.warn("Missing or invalid double value for key: " + key);
        return defaultValue;
    }

    private static Map<Enchantment, Integer> extractEnchantments(Map<?, ?> weaponMap) {
        Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if ((boolean) weaponMap.get("enchanted")) {
            List<String> enchantmentNames = (List<String>) weaponMap.get("enchantments"); // Read enchantment names as strings
            List<Integer> enchantmentLevels = (List<Integer>) weaponMap.get("enchantmentLevels"); // Reads the Enchantment Levels and streams them to int[] Array

            for (int i = 0; i < enchantmentNames.size(); i++) {
                Enchantment enchantment = Enchantment.getByName(enchantmentNames.get(i));

                if (enchantment != null) {
                    enchantmentMap.put(enchantment, enchantmentLevels.get(i));
                }
            }
        }
        return enchantmentMap;
    }

    private static List<ItemStack> getArmourItems(Map<?, ?> mobMap, boolean hasArmour) {
        List<ItemStack> armour = new ArrayList<>();

        if (hasArmour) {
            List<String> armourNames = (List<String>) mobMap.get("armour");

            for (String armourName : armourNames) {
                Material material = Material.matchMaterial(armourName);

                if (material != null) {
                    ItemStack itemStack = new ItemStack(material, 1);
                    armour.add(itemStack);
                }
            }
        }
        return armour;
    }

    private static ItemDrop[] convertToItemDropArray(Object obj) {
        if (!(obj instanceof List<?>)) return new ItemDrop[0];
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;

        return list.stream().map(itemMap -> {
                    double chance = ((Number) itemMap.getOrDefault("dropChance", 1.0)).doubleValue();

                    // 1) custom‐item branch
                    if ("custom".equals(itemMap.get("type")) && itemMap.containsKey("id")) {
                        String id = (String) itemMap.get("id");
                        ItemStack custom = CustomItemHandler.getItem(id);
                        if (custom != null) {
                            return new ItemDrop(custom, chance);
                        }
                    }

                    // 2) vanilla‐material branch
                    String matName = (String) itemMap.get("item");
                    int count    = ((Number) itemMap.getOrDefault("count", 1)).intValue();
                    Material mat = Material.getMaterial(matName);
                    if (mat != null) {
                        return new ItemDrop(new ItemStack(mat, count), chance);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .toArray(ItemDrop[]::new);
    }

    private static String stripAmpersandColors(String raw) {
        // removes any &-style codes like &6, &a, &l, etc.
        return raw.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    // 1) A little helper to parse strings into the enum
    private static World.Environment parseEnvironment(String raw) {
        switch (raw.trim().toLowerCase()) {
            case "overworld":
            case "normal":     // if you also want to accept the enum name
                return World.Environment.NORMAL;
            case "nether":
                return World.Environment.NETHER;
            case "end":
            case "the_end":
                return World.Environment.THE_END;
            default:
                return null;   // unknown → skip
        }
    }

    // 2) A helper to go back from enum → friendly name
    private static String envToString(World.Environment env) {
        if (env == World.Environment.NORMAL)   return "Overworld";
        if (env == World.Environment.NETHER)   return "Nether";
        if (env == World.Environment.THE_END)  return "End";
        return env.name();
    }

    /**
     * Convert a CustomMob into a Map ready for dumping to YAML.
     * This version strips color‐codes out of mobID and adds a "glow" flag
     * when the item has HIDE_ENCHANTS set.
     */
    public static Map<String, Object> toMap(CustomMob mob) {
        Map<String, Object> map = new LinkedHashMap<>();

        // ── CORE FIELDS ─────────────────────────────────────
        map.put("name",      mob.getName());
        map.put("mobID",     stripAmpersandColors(mob.getMobNameID()));
        map.put("champion",  mob.getChampion());
        map.put("maxHealth", mob.getMaxHealth());

        // ── COMBAT ATTRIBUTES ────────────────────────────────
        map.put("baseAttackDamage",  mob.getBaseAttackDamage());
        map.put("attackRange",       mob.getAttackRange());
        map.put("criticalHitChance", mob.getCriticalHitChance());
        map.put("defenseValue",      mob.getDefenseValue());

        // ── BEHAVIOR SETTINGS ───────────────────────────────
        map.put("movementSpeed",   mob.getMovementSpeed());
        map.put("aggressionLevel", mob.getAggressionLevel());
        map.put("behaviorPattern", mob.getBehaviorPattern());

        // ── SPAWN / TYPE ────────────────────────────────────
        map.put("spawnChance", mob.getSpawnChance());
        map.put("entityType",  mob.getEntityType().name());

        // ── WEAPON BLOCK ────────────────────────────────────
        SerializableItemStack w = mob.getWeapon();
        if (w != null) {
            Map<String, Object> wmap = new LinkedHashMap<>();
            ItemStack stack = w.toItemStack();
            ItemMeta meta  = stack.getItemMeta();

            wmap.put("material",     stack.getType().name());
            wmap.put("amount",       stack.getAmount());
            wmap.put("enchanted",    meta != null && meta.hasEnchants());

            List<String> enchantments = new ArrayList<>();
            List<Integer> enchantmentLevels = new ArrayList<>();
            stack.getEnchantments().forEach((ench, lvl) -> {
                // ench.getName() returns the constant name, e.g. "ARROW_INFINITE"
                enchantments.add(ench.getName());
                enchantmentLevels.add(lvl);
            });

            wmap.put("enchantments",       enchantments);
            wmap.put("enchantmentLevels",  enchantmentLevels);

            wmap.put("glow",         meta != null && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS));
            wmap.put("unbreakable",  meta != null && meta.isUnbreakable());
            wmap.put("hide",         meta != null && meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE));
            wmap.put("weaponName",   meta == null ? null : meta.getDisplayName());
            wmap.put("weaponLore",   meta == null ? null : meta.getLore());

            map.put("weapon", wmap);
        }
        map.put("weaponDropChance", mob.getWeaponDropChance());

        // ── ARMOUR BLOCK ────────────────────────────────────
        SerializableItemStack[] armour = mob.getSerializableArmour();
        boolean hasArm = armour != null && armour.length > 0;
        map.put("hasArmour", hasArm);
        if (hasArm) {
            List<String> armMaterials = new ArrayList<>();
            for (SerializableItemStack a : armour) {
                if (a != null) armMaterials.add(a.toItemStack().getType().name());
            }
            map.put("armour", armMaterials);
        }

        // ── LOOT DROPS ───────────────────────────────────────
        ItemDrop[] drops = mob.getLootDrops();
        if (drops != null && drops.length > 0) {
            List<Map<String, Object>> dropList = new ArrayList<>();
            // grab all custom‐item definitions so we can detect them
            Map<String, ItemStack> customItems = CustomItemHandler.getAllItems();

            for (ItemDrop d : drops) {
                ItemStack is = d.getItem();
                double chance = d.getDropChance();

                Map<String, Object> dm = new LinkedHashMap<>();
                // see if this stack matches one of our custom IDs
                String customId = customItems.entrySet().stream()
                        .filter(e -> e.getValue().isSimilar(is))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (customId != null) {
                    dm.put("type",       "custom");
                    dm.put("id",         customId);
                } else {
                    dm.put("item",       is.getType().name());
                    dm.put("count",      is.getAmount());
                }
                dm.put("dropChance",  chance);
                dropList.add(dm);
            }
            map.put("lootDrops", dropList);
        }

        // ── ABILITIES BLOCK ─────────────────────────────────
        // before you had List<MobAbility>; now you store List<String> + List<Long>:
        List<String> types     = mob.getAbilities();
        List<Long>   cooldowns = mob.getAbilityCooldowns();

        List<Map<String,Object>> out = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String key = types.get(i);
            long   cd  = (i < cooldowns.size() ? cooldowns.get(i) : 0L);

            Map<String,Object> am = new LinkedHashMap<>();
            // write exactly the saved key + cooldown
            am.put("type",     key);
            am.put("cooldown", cd);
            out.add(am);
        }
        map.put("abilities", out);


        // ── NEW: spawn dimensions ─────────────────────────────────────
        List<String> dims = mob.getSpawnDimensions().stream()
                .map(CustomMobCreation::envToString)    // your helper to turn ONE enum into "Overworld", "Nether", etc
                .collect(Collectors.toList());
        map.put("spawnDimensions", dims);


        return map;
    }
}