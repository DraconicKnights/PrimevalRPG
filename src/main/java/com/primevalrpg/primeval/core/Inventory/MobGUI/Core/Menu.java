package com.primevalrpg.primeval.core.Inventory.MobGUI.Core;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.MobConfigMenu;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class Menu extends BaseMenu {

    public Menu() {
        super(9, ColourCode.colour("&6 Custom Mob Control"));

        addItem(ColourCode.colour("&e&lSpawn Custom Mob"), Material.DIAMOND, 1);
        addItem(ColourCode.colour("&e&lRemove Custom Mob"), Material.BARRIER, 3);
        addItem(ColourCode.colour("&e&lCreate Custom Mob"), Material.ANVIL, 5);
        addItem(ColourCode.colour("&e&lConfig Options"), Material.COMPARATOR, 7);
    }

    private void addItem(String info, Material material, int slot) {
        var item = ItemBuilder.CreateCustomItem(material, false, info, null);
        inventory.setItem(slot, item);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot == 1) {
            MenuHistory.open(player, new MobListMenu());
            //new MobListMenu().open(player);
            player.updateInventory();

        } else if (slot == 3) {
            MenuHistory.open(player, new MobRemovalMenu());
            //new MobRemovalMenu().open(player);
            player.updateInventory();

        } else if (slot == 5) {
            CreationManager.startNewSession(player);
            player.updateInventory();

        } else if (slot == 7) {
            MenuHistory.open(player, new MobConfigMenu());
            //new ConfigMenu().open(player);
            player.updateInventory();
        }
    }
}
