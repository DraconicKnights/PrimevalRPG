package com.primevalrpg.primeval.core.Inventory.MobGUI.Core;

import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.*;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Armour.ArmourMenu;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Loot.LootMenu;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Weapon.WeaponSettingsMenu;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.core.enums.CreationStage;
import com.primevalrpg.primeval.events.ChatPrompt;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Base Mob GUI Creation Menu
 * Used for Creating/Modifying mob data and accessing other menus
 */
public class MobCreationMenu extends BaseMenu {

    private static final int ROWS = 5;            // 5 rows × 9 = 45 slots
    private static final String TITLE = ColourCode.colour("&6&lMob Creator");

    private final CreationSession session;

    public MobCreationMenu(CreationSession session) {
        super(ROWS * 9, TITLE);
        this.session = session;
        render();
    }

    @Override
    public void open(Player player) {
        render();
        super.open(player);
    }

    private void render() {
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

        // Slot Logic
        // helper bases for rows 1,2,3,4
        int R1 = 1 * 9;
        int R2 = 2 * 9;
        int R3 = 3 * 9;
        int R4 = 4 * 9;

        setSlot(R1 + 1, Material.NAME_TAG, "Name",
                session.getNext() == CreationStage.NAME
                        ? "CLICK to type…"
                        : session.buildMob().getName());
        setSlot(R1 + 3, Material.DIAMOND, "Champion",
                session.isChampion() ? "✔ Champion" : "✘ Non-Champion");
        setSlot(R1 + 5, Material.GOLDEN_APPLE, "Health",
                session.buildMob().getMaxHealth());
        setSlot(R1 + 7, Material.ENDER_EYE, "Spawn %",
                session.buildMob().getSpawnChance());

        setSlot(R2 + 1, Material.CREEPER_SPAWN_EGG, "Entity",
                session.buildMob().getEntityType().name());
        setSlot(R2 + 3, Material.BOW, "Combat",
                session.summaryCombat());
        setSlot(R2 + 5, Material.POTION, "Behavior",
                session.summaryBehavior());
        setSlot(R2 + 7, Material.IRON_SWORD, "Equipment",
                session.summaryEquipment());

        setSlot(R3 + 1, Material.LEATHER_CHESTPLATE, "Armour",
                session.summaryArmour());
        setSlot(R3 + 3, Material.CHEST, "Loot Drops",
                session.summaryLoot());
        setSlot(R3 + 5, Material.NETHER_STAR, "&aAbility",
                session.getAbilityTypes());
        setSlot(R3 + 7, Material.END_PORTAL_FRAME, "World",
                session.summaryDimensions());

        boolean complete = session.isComplete();
        Material mat = complete ? Material.EMERALD_BLOCK : Material.BARRIER;
        String head = complete ? "&aConfirm" : "&cConfirm";
        String foot = complete
                ? "Finish & register"
                : "You must fill all fields";

        inventory.setItem(R4 + 8,
                ItemBuilder.CreateCustomItem(
                        mat, false,
                        ColourCode.colour(head),
                        ColourCode.colour(foot)
                )
        );

        inventory.setItem(R4 + 0, ItemBuilder.CreateCustomItem(Material.BARRIER, false,
                ColourCode.colour("&cBack"),
                ColourCode.colour("&7Close this menu")));
    }

    private void setSlot(int slot, Material mat, String title, Object val) {
        String display = ColourCode.colour("&a" + title + ": &f" + val);
        ItemStack item = ItemBuilder.CreateCustomItem(mat, false, display, null);
        inventory.setItem(slot, item);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();

        switch (e.getRawSlot()) {
            case 10 -> {
                p.closeInventory();
                ChatPrompt.prompt(p,
                        "§ePlease type your mob name in chat (or 'cancel'):",
                        text -> {
                            if (!text.equalsIgnoreCase("cancel")) {
                                session.setName(text);
                            }
                            reopen();
                        }
                );
            }
            case 12 -> {
                session.toggleChampion();
                render();
            }
            case 14 -> {
                p.closeInventory();
                ChatPrompt.prompt(p,
                        "§eEnter max health (number) or 'cancel':",
                        text -> {
                            if (!text.equalsIgnoreCase("cancel")) {
                                try {
                                    session.setHealth(Double.parseDouble(text));
                                } catch (NumberFormatException ex) {
                                    p.sendMessage("§cInvalid number!");
                                }
                            }
                            reopen();
                        }
                );
            }
            case 16 -> {
                p.closeInventory();
                ChatPrompt.prompt(p,
                        "§eEnter spawn chance (0–100) or 'cancel':",
                        text -> {
                            if (!text.equalsIgnoreCase("cancel")) {
                                try {
                                    session.setSpawnChance(Integer.parseInt(text));
                                } catch (NumberFormatException ex) {
                                    p.sendMessage("§cInvalid percent!");
                                }
                            }
                            reopen();
                        }
                );
            }
            case 19 -> {
                MenuHistory.open(p, new EntityTypeMenu(p));
            }
            case 21 -> {
                MenuHistory.open(p, new CombatSettingsMenu(p));
            }
            case 23 -> {
                MenuHistory.open(p, new BehaviorSettingsMenu(p));
            }
            case 25 -> {
                MenuHistory.open(p, new WeaponSettingsMenu(p));
            }
            case 28 -> {
                MenuHistory.open(p, new ArmourMenu(p));
            }
            case 30 -> {
                MenuHistory.open(p, new LootMenu(p));
            }
            case 32 -> {
                MenuHistory.open(p, new AbilitySelectionMenu(p));
            }
            case 34 -> {
                MenuHistory.open(p, new DimensionsMenu(p));
            }
            case 36 -> {
                //MenuHistory.goBack(p);
                new Menu().open(p);
            }
            case 44 -> {
                if (!session.isComplete()) {
                    p.sendMessage("§cPlease fill required fields!");
                } else {
                    CustomMob mob     = session.buildMob();
                    String oldKey     = session.isEditMode() ? session.getOriginalKey() : null;

                    MobDataHandler.getInstance().saveOrUpdate(mob, oldKey);

                    p.sendMessage("§a" +
                            (session.isEditMode() ? "Updated" : "Created") +
                            " mob §f" + mob.getName()
                    );
                    CreationManager.endSession(p.getUniqueId());
                    p.closeInventory();
                    MobDataHandler.getInstance().ReloadMobsConfig();
                }
            }
            default -> { }
        }
    }

    private void reopen() {
        new MobCreationMenu(session).open(Bukkit.getPlayer(session.getPlayerId()));
    }

}