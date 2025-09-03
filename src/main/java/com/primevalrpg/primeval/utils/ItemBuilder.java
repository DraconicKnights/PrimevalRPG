package com.primevalrpg.primeval.utils;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Item Builder class used for creating custom item data for other parts of the project
 */
public class ItemBuilder {

    public static ItemStack CreateCustomItem(Material material, boolean glow, String displayName, String metaContent) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            // No meta (e.g. AIR) – return the bare item to avoid NPE
            return itemStack;
        }

        if (glow) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        itemMeta.setDisplayName(displayName);

        if (metaContent != null && !metaContent.isEmpty()) {
            List<String> lore = new ArrayList<>();
            lore.add(metaContent);
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack CreateMultiLoreItem(Material material, boolean glow, String displayName, String... metaContent) {

        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (glow) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        itemMeta.setDisplayName(displayName);

        itemMeta.setLore(Arrays.asList(metaContent));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Create an item with arbitrary persistent‐data metadata tags.
     *
     * @param material    item material
     * @param glow        whether to give it a glow
     * @param displayName coloured display name
     * @param metaData    map of key→value pairs to store in PDC
     */
    public static ItemStack createMetaItem(Material material,
                                           boolean glow,
                                           String displayName,
                                           Map<String,String> metaData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColourCode.colour(displayName));
            if (glow) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            for (var e : metaData.entrySet()) {
                NamespacedKey key = new NamespacedKey(PrimevalRPG.getInstance(), e.getKey());
                meta.getPersistentDataContainer()
                        .set(key, PersistentDataType.STRING, e.getValue());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createEnchantItem(Material type, int amount, Map<Enchantment, Integer> enchantmentMap, boolean glow, boolean unbreakable, boolean hideunbreakable, String name, List<String> lines) {
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();

        if (glow) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        if (enchantmentMap != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantmentMap.entrySet()) {
                PrimevalRPG.getInstance().CustomMobLogger(entry.getKey().toString() + entry.getValue().toString(), LoggerLevel.INFO);
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        if (unbreakable) meta.setUnbreakable(true);
        if (hideunbreakable) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        if (name != null) meta.displayName(Component.translatable(ColourCode.colour(name)));

        if (lines != null) {
            List<String> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(ColourCode.colour(line));
            }
            meta.setLore(lore);

        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createWeapon(
            ItemStack base,
            int amount,
            boolean enchanted,
            Map<Enchantment,Integer> enchants,
            boolean glow,
            boolean unbreakable,
            boolean hideFlags,
            String displayName,
            List<String> lore
    ) {
        ItemStack w = base.clone();
        ItemMeta meta = w.getItemMeta();

        if (!displayName.isBlank()) {
            meta.setDisplayName(ColourCode.colour(displayName));
        }
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(ColourCode::colour).toList());
        }
        if (enchanted) {
            enchants.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));
        }
        if (glow) {
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setUnbreakable(unbreakable);
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        }

        w.setItemMeta(meta);
        w.setAmount(amount);
        return w;
    }

    public static ItemStack[] makeArmourSet(List<ItemStack> armour) {

        if (armour == null || armour.isEmpty()) return null;

        PrimevalRPG.getInstance().CustomMobLogger("Armour Contents: " + armour, LoggerLevel.INFO);

        ItemStack[] armourArray = new ItemStack[armour.size()];

        for (int i = 0; i < armour.size(); i++) {
            armourArray[i] = armour.get(i);
        }

        return armourArray;
    }

    public static ItemDrop[] createItemDropArray(List<ItemStack> items, List<Double> dropChances) {
        List<ItemDrop> itemDropList = createItemDropList(items, dropChances);
        return itemDropList.toArray(new ItemDrop[0]);
    }

    public static List<ItemDrop> createItemDropList(List<ItemStack> items, List<Double> dropChances) {
        if (items.size() != dropChances.size()) {
            throw new IllegalArgumentException("The number of items and drop chances must match!");
        }
        List<ItemDrop> itemDrops = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            // Each item is wrapped with its corresponding drop chance.
            itemDrops.add(new ItemDrop(items.get(i), dropChances.get(i)));
        }
        return itemDrops;
    }
}
