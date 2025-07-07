package com.primevalrpg.primeval.core.Boss;

import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;

/**
 * Custom Entity LootTable for plugin
 */
public class LootTable implements Serializable {

    private final List<ItemDrop> lootItems = new ArrayList<>();

    public void addItem(ItemStack item, double chance) {
        lootItems.add(new ItemDrop(item, chance));
    }

    public List<ItemStack> rollLoot() {
        List<ItemStack> dropItems = new ArrayList<>();
        Random rand = new Random();

        for (ItemDrop lootItem : lootItems) {
            if (rand.nextDouble() <= lootItem.getDropChance()) {
                dropItems.add(lootItem.getItem());
            }
        }

        return dropItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(ItemDrop item : lootItems) {
            sb.append(item.toString()).append(", ");
        }
        return sb.toString();
    }
}
