package com.primevalrpg.primeval.core.Inventory.EventCreation.Scripts;


import javax.annotation.Nullable;
import java.util.List;

public class ArgDef {
    public final String key;
    public final @Nullable List<String> choices;

    public ArgDef(String key, @Nullable List<String> choices) {
        this.key = key;
        this.choices = choices;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public List<String> getChoices() {
        return choices;
    }


}

