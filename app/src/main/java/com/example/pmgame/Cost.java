package com.example.pmgame;

import java.util.Random;

enum Cost {
    CHEAP, AVERAGE, EXPENSIVE;
    private static final Cost[] VALUES = values();
    private static final int SIZE = VALUES.length;
    private static final Random RANDOM = new Random();

    public static Cost getRandomSpeed()  {
        return VALUES[RANDOM.nextInt(SIZE)];
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
