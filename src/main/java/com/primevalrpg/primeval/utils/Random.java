package com.primevalrpg.primeval.utils;

public abstract class Random {
    private static final java.util.Random random = new java.util.Random();

    public static boolean CustomSpawn(int value) {
        return random.nextInt(value) < 40;
    }

    public static boolean SpawnChance(int value) {
        return random.nextInt(value) < 15;
    }

    public static int RandomValue(int value) {
        return random.nextInt(value);
    }

}
