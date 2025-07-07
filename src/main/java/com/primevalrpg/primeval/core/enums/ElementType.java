package com.primevalrpg.primeval.core.enums;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public enum ElementType {
    FIRE, WATER, EARTH, WIND;

    public static final Map<ElementType, String> ELEMENT_COLORS = Map.of(
            ElementType.FIRE, "&c",
            ElementType.WATER, "&b",
            ElementType.EARTH, "&2",
            ElementType.WIND, "&7"
    );

    /**
     * Lookup by name, ignoring case.
     * Returns empty if no match.
     */
    public static Optional<ElementType> fromString(String name) {
        if (name == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get the Bukkit ChatColor for this element.
     */
    public ChatColor getColor() {
        String code = ELEMENT_COLORS.getOrDefault(this, "&f");
        return ChatColor.getByChar(code.substring(1));
    }

    /** A little emoji/icon for this element */
    public String getIcon() {
        switch (this) {
            case FIRE:  return "🔥";
            case WATER: return "💧";
            case EARTH: return "⛰";
            case WIND:  return "💨";
            default:    return "";
        }
    }
}

