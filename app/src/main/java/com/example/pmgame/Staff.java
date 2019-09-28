package com.example.pmgame;

public class Staff {

    private String name;
    private Speed speed;
    private Cost cost;

    Staff(String name, Speed speed, Cost cost) {
        this.name = name;
        this.speed = speed;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    Speed getSpeed() {
        return speed;
    }

    Cost getCost() {
        return cost;
    }
}
