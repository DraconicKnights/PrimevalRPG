package com.primevalrpg.primeval.utils.Data;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomItemHandler {
    private static final File dataFile =
            new File(PrimevalRPG.getInstance().getDataFolder(), "custom-items.yml");
    private static YamlConfiguration cfg;
    private static final Map<String, ItemStack> items = new HashMap<>();

    /**
     * Initializes the custom item handling by loading the custom items configuration.
     * If the custom-items.yml file does not exist, it will be created by saving the default resource.
     * The method also loads the custom items from the configuration file into memory.
     *
     * @param plugin The plugin instance used to save the default configuration file and access the plugin's resources.
     */
    public static void initialize(PrimevalRPG plugin) {
        if (!dataFile.exists()) {
            plugin.saveResource("custom-items.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(dataFile);
        loadItems();
    }

    /**
     * Loads custom items from the configuration file into memory.
     * Reads item definitions from the `customItems` section of the configuration,
     * parses them, and stores them as `ItemStack` objects in a map for later retrieval.
     * The method supports various item attributes such as material, amount, display name, lore,
     * enchantments, unbreakable flag, hide flags, and persistent data tags.
     *
     * Functionality steps:
     * 1. Clears the existing `items` map.
     * 2. Checks for the presence of the `customItems` section in the configuration.
     *    If not found, the method terminates without loading items.
     * 3. Iterates through the `customItems` section, parsing each item's key and configuration.
     *    For each item:
     *      - Sets material and amount.
     *      - Configures display name (if provided).
     *      - Adds lore (if provided).
     *      - Adds enchantments (if provided).
     *      - Sets the unbreakable flag and applies hide flags (if specified).
     *      - Adds persistent data container tags for custom metadata (if specified).
     * 4. Stores the configured `ItemStack` in the `items` map, keyed by the item's name.
     */
    private static void loadItems() {
        items.clear();
        if (!cfg.isConfigurationSection("customItems")) return;
        ConfigurationSection root = cfg.getConfigurationSection("customItems");

        for (String key : root.getKeys(false)) {
            ConfigurationSection sect = root.getConfigurationSection(key);

            Material mat = Material.valueOf(sect.getString("material", "STONE"));
            int amt = sect.getInt("amount", 1);
            ItemStack stack = new ItemStack(mat, amt);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;

            if (sect.isString("displayName")) {
                meta.setDisplayName(sect.getString("displayName"));
            }

            if (sect.isList("lore")) {
                meta.setLore(sect.getStringList("lore"));
            }

            if (sect.isConfigurationSection("enchantments")) {
                for (String enchKey : sect.getConfigurationSection("enchantments").getKeys(false)) {
                    int lvl = sect.getInt("enchantments." + enchKey, 0);
                    Enchantment ench = Enchantment.getByName(enchKey.toUpperCase());
                    if (ench != null && lvl > 0) {
                        meta.addEnchant(ench, lvl, true);
                    }
                }
            }

            if (sect.isBoolean("unbreakable") && sect.getBoolean("unbreakable")) {
                meta.setUnbreakable(true);
            }
            if (sect.isBoolean("hideFlags") && sect.getBoolean("hideFlags")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            }

            // 6) persistent-data for NBT data and use within other parts of the plugin
            if (sect.isConfigurationSection("persistentData")) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                ConfigurationSection pdcs = sect.getConfigurationSection("persistentData");
                for (String ns : pdcs.getKeys(false)) {
                    ConfigurationSection nsSection = pdcs.getConfigurationSection(ns);
                    for (String tag : nsSection.getKeys(false)) {
                        String value = nsSection.getString(tag, "");
                        NamespacedKey nk = new NamespacedKey(PrimevalRPG.getInstance(), tag);
                        pdc.set(nk, PersistentDataType.STRING, value);
                    }
                }
            }

            stack.setItemMeta(meta);
            items.put(key, stack);
        }
    }

    public static ItemStack getItem(String id) {
        return items.get(id);
    }

    public static Map<String, ItemStack> getAllItems() {
        return Map.copyOf(items);
    }

    /**
     * Persist this ItemStack (with all its meta, lore, enchants, PDC tags, etc)
     * under customItems.<key> in custom-items.yml and reload.
     */
    public static void saveCustomItem(String key, ItemStack stack) throws IOException {
        // 1) material & amount
        cfg.set("customItems." + key + ".material", stack.getType().name());
        cfg.set("customItems." + key + ".amount", stack.getAmount());

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // 2) displayName
            if (meta.hasDisplayName()) {
                cfg.set("customItems." + key + ".displayName", meta.getDisplayName());
            } else {
                cfg.set("customItems." + key + ".displayName", null);
            }

            // 3) lore
            if (meta.hasLore()) {
                cfg.set("customItems." + key + ".lore", meta.getLore());
            } else {
                cfg.set("customItems." + key + ".lore", null);
            }

            // 4) enchantments
            cfg.set("customItems." + key + ".enchantments", null);
            for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
                cfg.set("customItems." + key + ".enchantments." + e.getKey().getName(), e.getValue());
            }

            // 5) unbreakable + hideFlags
            cfg.set("customItems." + key + ".unbreakable", meta.isUnbreakable());
            cfg.set("customItems." + key + ".hideFlags",
                    meta.getItemFlags().contains(ItemFlag.HIDE_UNBREAKABLE));

            // 6) persistentData
            cfg.set("customItems." + key + ".persistentData", null);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            for (NamespacedKey nk : pdc.getKeys()) {
                Object val = pdc.get(nk, PersistentDataType.STRING);
                if (val instanceof String) {
                    // group under your plugin‐namespace
                    cfg.set("customItems." + key +
                            ".persistentData." + nk.getNamespace() +
                            "." + nk.getKey(), val);
                }
            }
        }

        // save & reload
        cfg.save(dataFile);
        loadItems();
    }

    /** convenience to generate a random key */
    public static String newItemKey() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8);
        } while (items.containsKey(id));
        return id;
    }

    /** runtime reload */
    public static void reload() {
        try {
            cfg = YamlConfiguration.loadConfiguration(dataFile);
            loadItems();
        } catch (Exception e) {
            RPGLogger.get().error(">>> Error reloading custom-items.yml: " + e.getMessage() + " <<<");
        }
    }
}
