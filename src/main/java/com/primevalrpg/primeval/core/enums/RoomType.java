package com.primevalrpg.primeval.core.enums;

public enum RoomType {
    START,
    EMPTY,    // No special behavior.
    MID_LOOT,
    HIGH_LOOT,
    MOB,      // Spawns a set of custom mobs when a player enters.
    BOSS;     // Spawns a custom boss.
}