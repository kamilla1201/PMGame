package com.example.pmgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Game {
    private final static int MIN_BUDGET = 700;
    private final static int MAX_BUDGET = 1000;
    private final static int STAFF_NUM = 4;

    private ArrayList<Task> tasks;

    private int budget;
    private List<Staff> staff;
    private Map<Task, Staff> assigns;

    Game() {
        TasksGenerator taskGenerator = new TasksGenerator();
        tasks = taskGenerator.generate();

        StaffGenerator staffGenerator = new StaffGenerator();
        staff = staffGenerator.generate(STAFF_NUM);
        Random rand = new Random();


        budget = (rand.nextInt((MAX_BUDGET - MIN_BUDGET) + 1) + MIN_BUDGET) * 1000;

        assigns = new HashMap<>();
    }

    void addAssign(Task task, Staff person) {
        assigns.put(task, person);
    }

    int getBudget() {
        return budget;
    }

    List<Staff> getStaff() {
        return staff;
    }

    ArrayList<Task> getTasks() {
        return tasks;
    }

    Map<Task, Staff> getAssigns() {
        return assigns;
    }
    Staff getStaffByTaskId(int id) {
        for (Task task : assigns.keySet()) {
            if (task.getId() == id) {
                return assigns.get(task);
            }
        }
        return null;
    }
}
