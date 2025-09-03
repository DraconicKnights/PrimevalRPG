package com.primevalrpg.primeval.core.NPCCore;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class NPC implements Listener {
    private final String id;
    private String name;
    private Location location;
    private List<String> dialogue;
    private String questFlagToSet;
    private Entity entity;

    public NPC(String id, String name, Location location, List<String> dialogue, String questFlagToSet) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.dialogue = dialogue;
        this.questFlagToSet = questFlagToSet;
    }

    public void spawn() {

        // Register this NPC as a listener
        PrimevalRPG.getInstance().getServer()
                .getPluginManager()
                .registerEvents(this, PrimevalRPG.getInstance());
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getDialogue() {
        return dialogue;
    }

    public void setDialogue(List<String> dialogue) {
        this.dialogue = dialogue;
    }

    public String getQuestFlagToSet() {
        return questFlagToSet;
    }

    public void setQuestFlagToSet(String questFlagToSet) {
        this.questFlagToSet = questFlagToSet;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked().equals(entity))) return;
        event.setCancelled(true);
        Player p = event.getPlayer();

        List<String> script = new ArrayList<>(dialogue);
        script.add("It’s dangerous to go alone!");
        script.add("Take this…");

        // Schedule one line every 100 ticks (=5 seconds)
        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (index >= script.size()) {
                    cancel(); // all done
                    return;
                }
                p.sendMessage(ColourCode.colour("&6&l["+ name + "&6&l]" + "&r: " + script.get(index++)));
            }
        }.runTaskTimer(PrimevalRPG.getInstance(), 0L, 100L);
    }

}
