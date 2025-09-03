package com.primevalrpg.primeval.core.Inventory.MobGUI.Weapon;

import com.primevalrpg.primeval.core.Inventory.Core.CreationSession;
import com.primevalrpg.primeval.core.Inventory.Core.CreationManager;
import com.primevalrpg.primeval.core.Inventory.Core.MenuHistory;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Enchant.EnchantmentListMenu;
import com.primevalrpg.primeval.events.ChatPrompt;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponSettingsMenu extends BaseMenu {

    private static final int   ROWS    = 5;
    private static final int   COLS    = 9;
    private static final String TITLE   = ColourCode.colour("&6&lWeapon Settings");

    private final CreationSession session;

    public WeaponSettingsMenu(Player p) {
        super(ROWS * COLS, TITLE);
        this.session = CreationManager.get(p.getUniqueId());
        render();
    }

    @Override
    public void open(Player p) {
        render();
        super.open(p);
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

        inventory.setItem(10, toggleMaterialItem());
        inventory.setItem(11, amountItem());
        inventory.setItem(12, enchantEditorItem());
        inventory.setItem(13, buildPreview());
        inventory.setItem(14, dropChanceItem());
        inventory.setItem(15, toggleGlowItem());
        inventory.setItem(16, toggleUnbreakableItem());

        inventory.setItem(19, chatPromptItem(Material.NAME_TAG,
                "&aName", session.getWeaponDisplayName(), "Click to rename"));
        inventory.setItem(20, chatPromptItem(Material.BOOK,
                "&aLore", loreString(), "Click to edit lore"));
        inventory.setItem(21, toggleHideFlagsItem());
        inventory.setItem(22, toggleItem(Material.ENCHANTED_BOOK,
                "&aENCHANTED: &f" + session.isWeaponEnchanted()));
        inventory.setItem(44, doneButton());
        inventory.setItem(36, backButton());
    }

    // ——— Helpers for each control ———

    private ItemStack toggleMaterialItem() {
        return toggleItem(
                Material.IRON_SWORD,
                "&aMaterial: &f" + session.getWeapon().getType().name());
    }

    private ItemStack amountItem() {
        return toggleItem(
                Material.ITEM_FRAME,
                "&aAmount: &f" + session.getWeaponAmount());
    }

    private ItemStack enchantEditorItem() {
        String enchList = session.getWeaponEnchants().isEmpty()
                ? "none"
                : session.getWeaponEnchants().entrySet().stream()
                .map(e -> e.getKey().getKey().getKey() + ":" + e.getValue())
                .collect(Collectors.joining(", "));
        return toggleItem(Material.ANVIL, "&aEnchants: &f" + enchList);
    }

    private ItemStack dropChanceItem() {
        return chatPromptItem(
                Material.DIAMOND,
                "&aDrop Chance", String.valueOf(session.getWeaponDropChance()),
                "Click to change");
    }

    private ItemStack toggleGlowItem() {
        return toggleItem(
                Material.SHULKER_SHELL,
                "&aGlow: &f" + session.isWeaponGlow());
    }

    private ItemStack toggleUnbreakableItem() {
        return toggleItem(
                Material.BARRIER,
                "&aUnbreakable: &f" + session.isWeaponUnbreakable());
    }

    private ItemStack toggleHideFlagsItem() {
        return toggleItem(
                Material.PAINTING,
                "&aHide Flags: &f" + session.isWeaponHideFlags());
    }

    private ItemStack doneButton() {
        return ItemBuilder.CreateCustomItem(
                Material.GREEN_STAINED_GLASS_PANE,
                false,
                ColourCode.colour("&eDONE"),
                ColourCode.colour("&7Save & Return"));
    }

    private ItemStack backButton() {
        return ItemBuilder.CreateCustomItem(
                Material.BARRIER,
                false,
                ColourCode.colour("&cBACK"),
                ColourCode.colour("&7Cancel & Return"));
    }

    private ItemStack toggleItem(Material mat, String title) {
        return ItemBuilder.CreateCustomItem(
                mat, false, ColourCode.colour(title), null);
    }

    private ItemStack chatPromptItem(Material mat,
                                     String label, String current, String prompt) {
        List<String> lore = List.of(
                ColourCode.colour("&f" + current),
                ColourCode.colour("&7" + prompt)
        );
        return ItemBuilder.CreateCustomItem(
                mat, false,
                ColourCode.colour(label),
                String.join("\n", lore));
    }

    private String loreString() {
        return session.getWeaponLore().isEmpty()
                ? "none"
                : String.join(" | ", session.getWeaponLore());
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        switch (e.getRawSlot()) {
            case 10 -> cycleMaterial();
            case 11 -> promptAmount(p);
            case 12 -> {
                p.closeInventory();
                MenuHistory.open(p, new EnchantmentListMenu(p, session,null));
            }
            case 14 -> promptDropChance(p);
            case 15 -> toggleAndRerender(session::toggleWeaponGlow);
            case 16 -> toggleAndRerender(
                    () -> session.setWeaponUnbreakable(!session.isWeaponUnbreakable()));

            // Row 2
            case 19 -> promptName(p);
            case 20 -> promptLore(p);
            case 21 -> toggleAndRerender(session::toggleWeaponHideFlags);
            case 22 -> toggleAndRerender(session::toggleWeaponEnchanted);
            case 44 -> {
                MenuHistory.goBack(p);
            }
            case 36 -> {
                MenuHistory.goBack(p);
            }
            default -> {
                // all other slots are border
            }
        }
    }

    /**
     * Builds a true preview ItemStack from the session:
     * – applies colour‐coded name & lore
     * – adds real enchantments
     * – toggles glow via a fake enchant + hide‐enchants
     * – sets unbreakable & hides flags if requested
     */
    private ItemStack buildPreview() {
        ItemStack item = new ItemStack(
                session.getWeapon().getType(),
                session.getWeaponAmount()
        );
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String displayName = session.getWeaponDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            meta.setDisplayName(ColourCode.colour(displayName));
        }

        List<String> lore = session.getWeaponLore().stream()
                .map(ColourCode::colour)
                .collect(Collectors.toList());
        meta.setLore(lore);

        session.getWeaponEnchants().forEach((ench, lvl) ->
                meta.addEnchant(ench, lvl, true)
        );

        if (session.isWeaponGlow()) {
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setUnbreakable(session.isWeaponUnbreakable());

        if (session.isWeaponHideFlags()) {
            meta.addItemFlags(ItemFlag.values());
        }

        item.setItemMeta(meta);
        return item;
    }

    private void cycleMaterial() {
        Material[] mats = {Material.WOODEN_SWORD, Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL,
        Material.WOODEN_HOE, Material.STONE_SWORD, Material.STONE_AXE, Material.STONE_PICKAXE, Material.STONE_SHOVEL,
        Material.STONE_HOE, Material.IRON_SWORD, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SHOVEL,
        Material.IRON_HOE, Material.GOLDEN_SWORD, Material.GOLDEN_AXE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL,
        Material.GOLDEN_HOE, Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL,
        Material.DIAMOND_HOE, Material.NETHERITE_SWORD, Material.NETHERITE_AXE, Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL,
        Material.TRIDENT, Material.BOW, Material.CROSSBOW};
        Material cur = session.getWeapon().getType();
        int next = (Arrays.asList(mats).indexOf(cur) + 1) % mats.length;
        session.setWeapon(new ItemStack(mats[next]));
        render();
    }

    private void promptAmount(Player p) {
        p.closeInventory();
        ChatPrompt.prompt(p, "Enter amount:", txt -> {
            try { session.setWeaponAmount(Integer.parseInt(txt)); }
            catch (Exception ex) { p.sendMessage(ChatColor.RED + "Bad number"); }
            reopen(p);
        });
    }

    private void promptName(Player p) {
        p.closeInventory();
        ChatPrompt.prompt(p, "Enter weapon name:", s -> {
            session.setWeaponDisplayName(s);
            reopen(p);
        });
    }

    private void promptLore(Player p) {
        p.closeInventory();
        ChatPrompt.prompt(p, "Enter lore lines separated by '|':", txt -> {
            session.setWeaponLore(Arrays.stream(txt.split("\\|"))
                    .map(String::trim)
                    .toList());
            reopen(p);
        });
    }

    private void promptDropChance(Player p) {
        p.closeInventory();
        ChatPrompt.prompt(p, "Enter drop chance (0.0–1.0):", txt -> {
            try { session.setWeaponDropChance(Double.parseDouble(txt)); }
            catch (Exception ex) { p.sendMessage(ChatColor.RED + "Bad number"); }
            reopen(p);
        });
    }

    private void toggleAndRerender(Runnable toggleAction) {
        toggleAction.run();
        render();
    }

    private void reopen(Player p) {
        new WeaponSettingsMenu(p).open(p);
    }
}