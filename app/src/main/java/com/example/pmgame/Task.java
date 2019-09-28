package com.example.pmgame;

class Task {

    private int id;
    private String taskName;
    private int startDay;
    private int endDay;

    Task(int id, String name, int startDay, int endDay) {
        this.id = id;
        this.taskName = name;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    int getId() {return id;}
    String getTaskName() {
        return taskName;
    }

    int getStartDay() {
        return startDay;
    }

    int getEndDay() {
        return endDay;
    }
}
