package com.primevalrpg.primeval.core.Inventory.EventCreation.Scripts;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ScriptCommandDef {
    public final String name;
    public final ItemStack icon;
    public final List<ArgDef> args;
    public final boolean needsTargets;

    public ScriptCommandDef(String name, ItemStack icon, List<ArgDef> args, boolean needsTargets) {
        this.name = name;
        this.icon = icon;
        this.args = args;
        this.needsTargets = needsTargets;
    }


    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<ArgDef> getArgs() {
        return args;
    }

    public boolean isNeedsTargets() {
        return needsTargets;
    }
}
