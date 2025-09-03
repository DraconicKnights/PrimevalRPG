package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityTypeMenu extends BaseMenu {

    private static final int ROWS = 4;
    private static final int BACK_SLOT = (ROWS - 1) * 9 + 4;  // bottom-middle

    private final CreationSession session;

    private static final List<EntityType> TYPES = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER,
            EntityType.MAGMA_CUBE, EntityType.SLIME, EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE, EntityType.SPIDER, EntityType.CAVE_SPIDER,
            EntityType.PILLAGER, EntityType.ENDERMAN, EntityType.WITHER_SKELETON,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ENDER_DRAGON, EntityType.WITHER,
            EntityType.BLAZE, EntityType.BREEZE, EntityType.HOGLIN, EntityType.ENDERMITE
    );

    public EntityTypeMenu(Player p) {
        super(ROWS * 9, "§3 Select Entity Type");
        this.session = CreationManager.get(p.getUniqueId());
        render();
    }

    private void render() {
        inventory.clear();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == ROWS - 1 || col == 0 || col == 8) {
                Material color = ((row + col) % 2 == 0
                        ? Material.LIGHT_BLUE_STAINED_GLASS_PANE
                        : Material.BLUE_STAINED_GLASS_PANE);
                inventory.setItem(slot,
                        ItemBuilder.CreateCustomItem(color, true, " ", List.of().toString())
                );
            } else {
                inventory.setItem(slot, null);
            }
        }

        for (int i = 0; i < TYPES.size(); i++) {
            EntityType type = TYPES.get(i);
            Material egg = Material.valueOf(type.name() + "_SPAWN_EGG");
            String title = "§a" + type.name();
            String lore  = type == session.getEntityType()
                    ? "§7(current)"
                    : "§7Click to select";
            ItemStack icon = ItemBuilder.CreateCustomItem(egg, false, title, lore);
            if (type == session.getEntityType()) {
                icon.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            inventory.setItem(9 + i, icon);
        }

        inventory.setItem(
                BACK_SLOT,
                ItemBuilder.CreateCustomItem(
                        Material.BARRIER,
                        false,
                        "§cBack",
                        "§7Return to creator"
                )
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot >= 9 && slot < 9 + TYPES.size()) {
            session.setEntityType(TYPES.get(slot - 9));
            render();
            return;
        }

        if (slot == BACK_SLOT) {
            MenuHistory.goBack(p);
        }
    }
}