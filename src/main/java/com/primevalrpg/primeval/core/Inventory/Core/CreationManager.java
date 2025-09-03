package com.primevalrpg.primeval.core.Inventory.Core;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.MobCreationMenu;
import com.primevalrpg.primeval.core.RPGMobs.CustomMob;
import com.primevalrpg.primeval.utils.ItemDrop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class CreationManager implements Listener {
    private static final Map<UUID,CreationSession> sessions = new HashMap<>();

    public static CreationSession get(UUID id){
        return sessions.computeIfAbsent(id, k-> new CreationSession(id));
    }
    public static void endSession(UUID id){
        sessions.remove(id);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        CreationSession sess = sessions.get(p.getUniqueId());
        if (sess==null) return;

        e.setCancelled(true);
        String msg = e.getMessage().trim();
        switch(sess.getNext()){
            case NAME -> sess.setName(msg);
            case HEALTH -> {
                try { sess.setHealth(Double.parseDouble(msg)); }
                catch(Exception ex){ p.sendMessage("Invalid number"); return; }
            }
            case SPAWN_CHANCE -> {
                try { sess.setSpawnChance(Integer.parseInt(msg)); }
                catch(Exception ex){ p.sendMessage("Invalid percent"); return; }
            }
            case ENTITY_TYPE -> {
                try { sess.setEntityType(EntityType.valueOf(msg.toUpperCase())); }
                catch(Exception ex){ p.sendMessage("Bad entity type"); return; }
            }
            case COMBAT -> {
                String[] a = msg.split(",");
                if (a.length!=4) { p.sendMessage("Use dmg,range,crit,def"); return; }
                try {
                    sess.setCombat(
                            Double.parseDouble(a[0]),
                            Double.parseDouble(a[1]),
                            Double.parseDouble(a[2]),
                            Double.parseDouble(a[3])
                    );
                } catch(Exception ex){
                    p.sendMessage("Invalid combat values"); return;
                }
            }
            case BEHAVIOR -> {
                String[] b = msg.split(",");
                if (b.length!=3){ p.sendMessage("Use speed,aggression,pattern"); return; }
                try {
                    sess.setBehavior(
                            Double.parseDouble(b[0]),
                            Integer.parseInt(b[1]),
                            b[2]
                    );
                } catch(Exception ex){
                    p.sendMessage("Invalid behavior"); return;
                }
            }
            case EQUIPMENT -> {
                String[] c = msg.split(",");
                try {
                    Material mat = Material.valueOf(c[0].toUpperCase());
                    double  dc  = Double.parseDouble(c[1]);
                    sess.setWeapon(new ItemStack(mat));
                    sess.setWeaponDropChance(dc);
                } catch(Exception ex){
                    p.sendMessage("Use MAT,dropChance"); return;
                }
            }
            case ARMOUR -> {
                String[] d = msg.split(",");
                List<Material> mats = new ArrayList<>();
                try {
                    for(String s: d) mats.add(Material.valueOf(s.toUpperCase()));
                    sess.setArmour(mats);
                } catch(Exception ex){
                    p.sendMessage("Invalid armour list"); return;
                }
            }
            case LOOT -> {
                String[] groups = msg.split(";");
                List<ItemDrop> drops = new ArrayList<>();
                try {
                    for(String g: groups){
                        String[] t = g.split(":");
                        Material m = Material.valueOf(t[0]);
                        int     c = Integer.parseInt(t[1]);
                        double  pct = Double.parseDouble(t[2]);
                        drops.add(new ItemDrop(new ItemStack(m,c), pct));
                    }
                    sess.setLootDrops(drops);
                } catch(Exception ex){
                    p.sendMessage("Use ITEM:cnt:chance;..."); return;
                }
            }

        }

        Bukkit.getScheduler().runTask(PrimevalRPG.getInstance(), ()->{
            MenuHistory.goBack(p);
        });
    }

    public static void startNewSession(Player player) {
        UUID id = player.getUniqueId();
        CreationSession session = new CreationSession(id);
        sessions.put(id, session);

        Bukkit.getScheduler().runTask(PrimevalRPG.getInstance(), () -> {
            MenuHistory.open(player, new MobCreationMenu(session));
        });
    }

    /**
     * Initiates an edit session for a given player to modify a CustomMob configuration.
     *
     * @param player The player who will edit the mob's settings.
     * @param mob    The CustomMob instance containing the current configuration to be edited.
     */
    public static void startEditSession(Player player, CustomMob mob) {
        UUID id = player.getUniqueId();
        CreationSession session = new CreationSession(id);

        session.setEditMode(true);
        session.setOriginalKey(mob.getMobNameID());

        // 1) Basic fields
        session.setName(mob.getName());
        session.setChampion(mob.getChampion());
        session.setHealth(mob.getMaxHealth());
        session.setSpawnChance(mob.getSpawnChance());
        session.setEntityType(mob.getEntityType());

        // 2) Combat
        session.setCombat(
                mob.getBaseAttackDamage(),
                mob.getAttackRange(),
                mob.getCriticalHitChance(),
                mob.getDefenseValue()
        );

        // 3) Behavior
        session.setBehavior(
                mob.getMovementSpeed(),
                mob.getAggressionLevel(),
                mob.getBehaviorPattern()
        );

        // 4) Equipment
        ItemStack weaponItem = mob.getWeapon() != null
                ? mob.getWeapon().toItemStack()
                : new ItemStack(Material.AIR);
        session.setWeapon(weaponItem);
        session.setWeaponDropChance(mob.getWeaponDropChance());

        // detailed weapon settings
        ItemStack metaHolder = weaponItem;
        org.bukkit.inventory.meta.ItemMeta meta = metaHolder.hasItemMeta()
                ? metaHolder.getItemMeta()
                : null;

        session.setWeaponAmount(metaHolder.getAmount());
        session.setWeaponEnchanted(meta != null && meta.hasEnchants());
        session.setWeaponGlow(meta != null && meta.hasEnchants()
                && metaHolder.containsEnchantment(Enchantment.MENDING));
        session.setWeaponUnbreakable(meta != null && meta.isUnbreakable());
        session.setWeaponHideFlags(meta != null && meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
        session.setWeaponDisplayName(meta != null && meta.hasDisplayName()
                ? meta.getDisplayName() : "");

        List<String> lore = (meta != null && meta.hasLore())
                ? meta.getLore()
                : Collections.emptyList();
        session.setWeaponLore(new ArrayList<>(lore));

        Map<Enchantment, Integer> enchants = (meta != null && meta.hasEnchants())
                ? meta.getEnchants()
                : Collections.emptyMap();
        session.setWeaponEnchants(new HashMap<>(enchants));

        // 5) Armour
        ItemStack[] armourArray = mob.getArmour();
        List<Material> armourMats = Arrays.stream(
                        armourArray != null ? armourArray : new ItemStack[0]
                )
                .filter(Objects::nonNull)
                .map(ItemStack::getType)
                .collect(Collectors.toList());
        session.setArmour(armourMats);

        // 6) Loot
        ItemDrop[] drops = mob.getLootDrops();
        session.setLootDrops(drops != null
                ? new ArrayList<>(Arrays.asList(drops))
                : new ArrayList<>());

        // 7) Abilities
        List<String> keys = mob.getAbilities();
        session.setAbilties(keys != null
                ? new ArrayList<>(keys)
                : new ArrayList<>());

        List<Long> cds = mob.getAbilityCooldowns();
        session.getAbilityCooldowns().clear();
        if (keys != null && cds != null) {
            for (int i = 0; i < keys.size() && i < cds.size(); i++) {
                session.getAbilityCooldowns().put(keys.get(i), cds.get(i));
            }
        }

        // 8) Spawn dimensions
        Collection<World.Environment> dims = mob.getSpawnDimensions();
        if (dims != null && !dims.isEmpty()) {
            session.setSpawnDimensions(new ArrayList<>(dims));
        } else {
            session.setSpawnDimensions(Collections.emptyList());
        }

        // 9) Mark as edit and remember the original key
        session.enableEditMode(mob.getMobNameID());

        // 10) Store session and open menu
        sessions.put(id, session);
        MenuHistory.open(player, new MobCreationMenu(session));
    }

}