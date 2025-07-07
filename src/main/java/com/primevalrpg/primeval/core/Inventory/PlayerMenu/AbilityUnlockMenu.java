package com.primevalrpg.primeval.core.Inventory.PlayerMenu;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.Player.PlayerAbilityManager;
import com.primevalrpg.primeval.core.Player.PlayerAbilityManager.Ability;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.stream.Collectors;

public class AbilityUnlockMenu extends BaseMenu {
    private static final int SIZE = 27;
    private final ElementType type;
    private final Player player;
    private final List<Ability> choices;

    public AbilityUnlockMenu(ElementType type, Player player) {
        super(SIZE, type.name() + " Ability Unlock");
        this.type = type;
        this.player = player;

        PlayerData data = PlayerDataManager.getInstance()
                .getPlayerData(player.getUniqueId());
        int lvl = data.getElementLevel(type);

        this.choices = PlayerAbilityManager.getInstance()
                .getAlibilityMap()
                .values().stream()
                .filter(a ->
                        type.name().equalsIgnoreCase(a.getType())
                                && !data.hasAbility(a.getName())
                                && a.getRequiredLevel() <= lvl
                )
                .collect(Collectors.toList());

        render();
    }

    @Override
    public void open(Player ignored) {
        super.open(player);
    }

    public void render() {
        inventory.clear();
        int slot = 0;

        for (Ability a : choices) {
            if (slot >= SIZE - 1) break;
            inventory.setItem(
                    slot++,
                    ItemBuilder.CreateCustomItem(
                            Material.PAPER,
                            false,
                            "§e" + a.getName(),
                            "§7Unlocks at lvl " + a.getRequiredLevel()
                    )
            );
        }

        // cancel/back button
        inventory.setItem(
                SIZE - 1,
                ItemBuilder.CreateCustomItem(
                        Material.BARRIER,
                        false,
                        "§cCancel",
                        "§7Close without unlocking"
                )
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == SIZE - 1) {
            p.closeInventory();
            return;
        }

        if (slot >= 0 && slot < choices.size()) {
            Ability picked = choices.get(slot);
            PlayerData data = PlayerDataManager.getInstance()
                    .getPlayerData(p.getUniqueId());
            String abilityName = picked.getName();

            if (data.hasAbility(abilityName)) {
                p.sendMessage(ChatColor.RED + "You already have that ability: "
                        + ChatColor.GOLD + abilityName);
                open(p);
            } else {
                data.addAbility(abilityName);
                PlayerDataManager.getInstance().savePlayerData(data);
                p.sendMessage(ChatColor.GREEN + "Unlocked new ability: "
                        + ChatColor.GOLD + abilityName);
            }

            p.closeInventory();
        }
    }
}