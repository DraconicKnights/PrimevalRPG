package com.primevalrpg.primeval.utils.Desing;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class MobHeads {
    /**
     * All player heads and mob heads (including wall variants and skulls).
     */
    private static final List<Material> MOB_HEADS = Arrays.stream(Material.values())
            .filter(m -> m.name().endsWith("_HEAD") || m.name().endsWith("_SKULL"))
            .filter(Material::isItem)
            .toList();

    /**
     * All full (solid) blocks in the game, excluding slabs, stairs, walls, fences, signage, buttons, etc.
     */
    private static final List<Material> FULL_BLOCKS = Arrays.stream(Material.values())
            .filter(Material::isBlock)        // must be a block
            .filter(Material::isOccluding)    // must block light/movement
            .filter(m -> {
                String name = m.name();
                // exclude partial or non-full cubes
                return !name.contains("SLAB")
                        && !name.contains("STAIRS")
                        && !name.contains("WALL")
                        && !name.contains("FENCE")
                        && !name.contains("GATE")
                        && !name.contains("DOOR")
                        && !name.contains("SIGN")
                        && !name.contains("BUTTON")
                        && !name.contains("LEVER")
                        && !name.contains("PRESSURE_PLATE");
            })
            .toList();

    public static List<Material> getMobHeads() {
        return MOB_HEADS;
    }

    public static List<Material> getFullBlocks() {
        return FULL_BLOCKS;
    }
}