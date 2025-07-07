package com.primevalrpg.primeval.core.Inventory.PlayerMenu;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Player.PlayerAbilityManager;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.Player.PlayerAbilityManager.Ability;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.stream.Collectors;

public class AbilitySelectMenu extends BaseMenu {
    private static final int ROWS = 3, COLS = 9, SIZE = ROWS * COLS;
    private final ElementType element;
    private final Map<Integer, Ability> abilityBySlot = new HashMap<>();

    public AbilitySelectMenu(Player player, ElementType element) {
        super(SIZE, ChatColor.GREEN + element.name() + " Abilities");
        this.element = element;
        render(player);
    }

    @Override
    public void open(Player player) {
        render(player);
        super.open(player);
    }

    private void render(Player player) {
        inventory.clear();
        fillBackground();
        PlayerData data = PlayerDataManager.getInstance()
                .getPlayerData(player.getUniqueId());
        Ability current = data.getEquippedAbility();
        List<Ability> unlocked = fetchUnlocked(data);
        placeAbilities(unlocked, current);
        placeBackButton();
    }

    private void fillBackground() {
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, ItemBuilder.CreateCustomItem(
                    Material.GRAY_STAINED_GLASS_PANE, false, " ", " "
            ));
        }
    }

    private List<Ability> fetchUnlocked(PlayerData data) {
        return data.getUnlockedAbilities().stream()
                .map(id -> PlayerAbilityManager.getInstance().getAbilities().get(id))
                .filter(Objects::nonNull)
                .filter(a -> a.getType().equalsIgnoreCase(element.name()))
                .collect(Collectors.toList());
    }

    private void placeAbilities(List<Ability> list, Ability current) {
        abilityBySlot.clear();
        int count = list.size();
        int start = COLS + (COLS - count) / 2;
        for (int i = 0; i < count; i++) {
            int slot = start + i;
            if (slot >= COLS * 2) break;
            Ability a = list.get(i);
            boolean selected = (current != null && current.getName().equals(a.getName()));
            inventory.setItem(slot, ItemBuilder.CreateCustomItem(
                    Material.PAPER,
                    selected,
                    ChatColor.YELLOW + a.getName(),
                    ChatColor.GRAY + (selected ? "Currently Selected" : "Click to select")
            ));
            abilityBySlot.put(slot, a);
        }
    }

    private void placeBackButton() {
        inventory.setItem(SIZE - 1, ItemBuilder.CreateCustomItem(
                Material.BARRIER, false,
                ChatColor.RED + "Back",
                ChatColor.GRAY + "Return to element menu"
        ));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == SIZE - 1) {
            new PlayerCoreMenu().open(p);
            return;
        }

        Ability picked = abilityBySlot.get(slot);
        if (picked == null) return;

        UUID uuid = p.getUniqueId();
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(uuid);
        data.equipAbility(picked);
        PlayerDataManager.getInstance().savePlayerData(data);

        p.sendMessage(ChatColor.GREEN
                + "Selected " + element.name() + " ability: "
                + ChatColor.GOLD + picked.getName());
        p.closeInventory();
    }
}