package com.example.pmgame;

import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

class StaffGenerator {

    private final static List<String> NAMES = Arrays.asList("Ann", "Rudy", "Bob", "Mark", "Vicky", "John", "Marti", "Kathy");

    private Random rand = new Random();

    List<Staff> generate(int count) {
        Set<String> names = new ArraySet<>();
        while (names.size() < count) {
            names.add(NAMES.get(rand.nextInt(NAMES.size())));
        }
        List<Staff> staff = new ArrayList<>();
        for (String name : names) {
            Speed speed;
            Cost cost;
            do {
                speed = Speed.getRandomSpeed();
                cost = Cost.getRandomSpeed();
            } while ((cost == Cost.AVERAGE && speed == Speed.AVERAGE)
                || ((cost == Cost.CHEAP && speed == Speed.FAST)));

            staff.add(new Staff(name, speed, cost));
        }
        return staff;
    }
}
