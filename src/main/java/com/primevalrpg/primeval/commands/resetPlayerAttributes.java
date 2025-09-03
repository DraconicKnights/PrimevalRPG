package com.primevalrpg.primeval.commands;

import com.primevalrpg.primeval.core.Annotations.Commands;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

@Commands
public class resetPlayerAttributes extends CommandCore {

    public resetPlayerAttributes() {
        super("primeval playerattributess","Player Attributes Command","primeval.admin", 0);
    }

    @Override
    protected void execute(Player player, String[] args) {
        PlayerData data = PlayerDataManager
                .getInstance()
                .getPlayerData(player.getUniqueId());

        data.clearUnlockedAbilities();
        data.setSelectedAbility(null);
        data.resetAttributes(player);

        PlayerDataManager.getInstance().savePlayerData(data);

        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            for (AttributeModifier mod : maxHealth.getModifiers()) {
                maxHealth.removeModifier(mod);
            }
            maxHealth.setBaseValue(20.0);
            player.setHealth(Math.min(player.getHealth(), maxHealth.getBaseValue()));
        }
        player.setAbsorptionAmount(0);

        AttributeInstance attackDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(1.0);
            attackDamage.getModifiers()
                    .forEach(attackDamage::removeModifier);
        }

        AttributeInstance moveSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (moveSpeed != null) {
            moveSpeed.setBaseValue(0.1);
            moveSpeed.getModifiers()
                    .forEach(moveSpeed::removeModifier);
        }

        AttributeInstance atkSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (atkSpeed != null) {
            atkSpeed.setBaseValue(4.0);
            atkSpeed.getModifiers()
                    .forEach(atkSpeed::removeModifier);
        }

        player.sendMessage(ColourCode.colour(
                "&aYour RPG stats, extra hearts and vanilla attributes have been reset."
        ));
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
