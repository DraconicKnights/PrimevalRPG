package com.primevalrpg.primeval.utils.Data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SerializableItemStack implements Serializable {
    private final Material type;
    private final int      amount;

    // new fields
    private final Map<String,Integer> enchantments;
    private final String              displayName;
    private final List<String>        lore;
    private final boolean             unbreakable;
    private final Set<ItemFlag>       itemFlags;

    public SerializableItemStack(ItemStack item) {
        this.type   = item.getType();
        this.amount = item.getAmount();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            this.displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
            this.lore        = meta.hasLore()        ? new ArrayList<>(meta.getLore()) : null;
            this.unbreakable = meta.isUnbreakable();
            this.itemFlags   = new HashSet<>(meta.getItemFlags());
            // store enchants by their NamespacedKey string
            this.enchantments = meta.getEnchants().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().getKey().toString(),
                            Map.Entry::getValue
                    ));
        } else {
            this.displayName  = null;
            this.lore         = null;
            this.unbreakable  = false;
            this.itemFlags    = Collections.emptySet();
            this.enchantments = Collections.emptyMap();
        }
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(type, amount);
        ItemMeta meta  = stack.getItemMeta();
        if (meta == null) return stack;

        // restore name & lore
        if (displayName != null) meta.setDisplayName(displayName);
        if (lore        != null) meta.setLore(new ArrayList<>(lore));

        // restore unbreakable & flags
        meta.setUnbreakable(unbreakable);
        itemFlags.forEach(meta::addItemFlags);

        // restore enchants
        for (var entry : enchantments.entrySet()) {
            // try by NamespacedKey
            Enchantment ench = null;
            NamespacedKey key = NamespacedKey.fromString(entry.getKey());
            if (key != null) ench = Enchantment.getByKey(key);
            // fallback to vanilla name
            if (ench == null) ench = Enchantment.getByName(entry.getKey().toUpperCase());
            if (ench != null) meta.addEnchant(ench, entry.getValue(), true);
        }

        stack.setItemMeta(meta);
        return stack;
    }
}