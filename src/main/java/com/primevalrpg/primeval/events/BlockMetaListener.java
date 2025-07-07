package com.primevalrpg.primeval.events;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Annotations.Events;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Events
public class BlockMetaListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemMeta im = e.getItemInHand().getItemMeta();
        if (im == null) return;

        PersistentDataContainer itemData = im.getPersistentDataContainer();
        String placeType = itemData.get(
                new NamespacedKey(PrimevalRPG.getInstance(), "blockType"),
                PersistentDataType.STRING
        );
        // not one of our special items?
        if (placeType == null) return;
        // not placing the declared block type?
        if (!e.getBlockPlaced().getType().name().equals(placeType)) return;

        // need a TileState to store PDC on the block
        if (!(e.getBlockPlaced().getState() instanceof TileState ts)) return;
        PersistentDataContainer blockData = ts.getPersistentDataContainer();

        // copy every string‐value key from item → block
        for (NamespacedKey key : itemData.getKeys()) {
            if (itemData.has(key, PersistentDataType.STRING)) {
                String value = itemData.get(key, PersistentDataType.STRING);
                blockData.set(key, PersistentDataType.STRING, value);
            }
        }
        ts.update();
    }
}