package com.example.pmgame;

import java.util.Random;

enum Speed {
    SLOW, AVERAGE, FAST;
    private static final Speed[] VALUES = values();
    private static final int SIZE = VALUES.length;
    private static final Random RANDOM = new Random();

    public static Speed getRandomSpeed()  {
        return VALUES[RANDOM.nextInt(SIZE)];
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
