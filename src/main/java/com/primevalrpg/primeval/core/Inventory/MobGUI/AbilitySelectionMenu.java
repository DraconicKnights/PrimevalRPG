package com.primevalrpg.primeval.core.Inventory.MobGUI;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.events.ChatPrompt;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.stream.IntStream;

public class AbilitySelectionMenu extends BaseMenu {

    private static final int ROWS = 4, COLS = 9;
    private static final int[] ABILITY_SLOTS = { 11, 13, 15, 20, 22, 24 };
    private static final int PREV_SLOT = 28, NEXT_SLOT = 34, DONE_SLOT = 31;

    private static final Map<String,Long> BUILT_IN_COOLDOWNS = Map.of(
    );

    private final CreationSession session;
    private final List<String> allAbilities;
    private final Map<String,Long> defaultCooldowns;
    private final int page, totalPages;

    public AbilitySelectionMenu(Player player) {
        this(player, 0);
    }

    private AbilitySelectionMenu(Player player, int page) {
        super(ROWS * COLS, ColourCode.colour("&6&lSelect Abilities"));
        this.session = CreationManager.get(player.getUniqueId());
        this.page    = page;

        ConfigurationSection sec = MobDataHandler
                .getAbilitiesConfig()
                .getConfigurationSection("abilities");

        defaultCooldowns = new HashMap<>(BUILT_IN_COOLDOWNS);
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                long cd = sec.getLong(key + ".cooldown", 0L);
                defaultCooldowns.put(key, cd);
            }
        }

        allAbilities = new ArrayList<>(defaultCooldowns.keySet());
        Collections.sort(allAbilities);

        totalPages = (int)Math.ceil(allAbilities.size() / (double)ABILITY_SLOTS.length);
        render();
    }

    private void render() {
        for (int i = 0; i < ROWS * COLS; i++) {
            int r = i / COLS, c = i % COLS;
            if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                Material pane = ((r + c) % 2 == 0
                        ? Material.LIGHT_BLUE_STAINED_GLASS_PANE
                        : Material.BLUE_STAINED_GLASS_PANE);
                inventory.setItem(i, ItemBuilder.CreateCustomItem(pane, true, " ", ""));
            } else {
                inventory.setItem(i, null);
            }
        }

        int start = page * ABILITY_SLOTS.length;
        for (int idx = 0; idx < ABILITY_SLOTS.length; idx++) {
            int globalIdx = start + idx;
            if (globalIdx >= allAbilities.size()) break;

            String key      = allAbilities.get(globalIdx);
            boolean selected= session.getAbilityTypes().contains(key);
            long   defCd    = defaultCooldowns.getOrDefault(key, 0L);

            String name = ColourCode.colour((selected ? "&a✔ " : "&e● ") + key);
            List<String> lore = new ArrayList<>();
            if (selected) {
                lore.add(ColourCode.colour("&7Click to remove"));
                lore.add(ColourCode.colour("&7Cooldown: &f" + session.getAbilityCooldown(key) + " ticks"));
            } else {
                lore.add(ColourCode.colour("&7Default CD: &f" + defCd + " ticks"));
                lore.add(ColourCode.colour("&7Click to add & set CD"));
            }

            inventory.setItem(
                    ABILITY_SLOTS[idx],
                    ItemBuilder.CreateMultiLoreItem(
                            Material.ENCHANTED_BOOK,
                            selected,
                            name,
                            lore.toArray(new String[0])
                    )
            );
        }

        if (page > 0) {
            inventory.setItem(PREV_SLOT,
                    ItemBuilder.CreateCustomItem(Material.ARROW, false,
                            ColourCode.colour("&e<< Prev"), ColourCode.colour("&7Page " + page + " of " + totalPages))
            );
        }
        if (page < totalPages - 1) {
            inventory.setItem(NEXT_SLOT,
                    ItemBuilder.CreateCustomItem(Material.ARROW, false,
                            ColourCode.colour("&eNext >>"), ColourCode.colour("&7Page " + (page+2) + " of " + totalPages))
            );
        }

        inventory.setItem(DONE_SLOT,
                ItemBuilder.CreateCustomItem(Material.BARRIER, false,
                        ColourCode.colour("&cDone"),
                        ColourCode.colour("&7Return to creation menu"))
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null
                || !e.getView().getTopInventory().equals(inventory)) return;
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == DONE_SLOT) {
            MenuHistory.goBack(p);
            return;
        }

        if (slot == PREV_SLOT && page > 0) {
            new AbilitySelectionMenu(p, page - 1).open(p);
            return;
        }

        if (slot == NEXT_SLOT && page < totalPages - 1) {
            new AbilitySelectionMenu(p, page + 1).open(p);
            return;
        }

        int idx = IntStream.range(0, ABILITY_SLOTS.length)
                .filter(i -> ABILITY_SLOTS[i] == slot)
                .findFirst().orElse(-1);
        if (idx < 0) return;

        int globalIdx = page * ABILITY_SLOTS.length + idx;
        if (globalIdx >= allAbilities.size()) return;

        String key = allAbilities.get(globalIdx);
        boolean selected = session.getAbilityTypes().contains(key);
        if (selected) {
            session.removeAbility(key);
            p.sendMessage(ColourCode.colour("&cRemoved &f" + key));
            render();
        } else {
            ChatPrompt.prompt(p,
                    ColourCode.colour("Enter cooldown (ticks) for &6" + key),
                    input -> {
                        try {
                            long cd = Long.parseLong(input);
                            session.addAbility(key, cd);
                            p.sendMessage(ColourCode.colour("&aAdded &f" + key + " &a(" + cd + " ticks)"));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(ColourCode.colour("&cInvalid number, try again."));
                        }
                        new AbilitySelectionMenu(p, page).open(p);
                    }
            );
        }
    }
}