package com.example.pmgame;

import android.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

class TasksGenerator {
    private final static int MIN_DAY = 2;
    private final static int MAX_DAY = 34;
    private final static int TASKS_NUM = 5;
    private final static int TASKS_SEQ_NUM = 3;
    private final static List<String> verbs = Arrays.asList(
            "Add","Build", "Test", "Rewrite", "Sample", "Restart", "Design", "Finish"
            );

    private final static List<String> nouns = Arrays.asList(
            "API", "REST", "UI", "SQL", "Linux", "CLI"
    );

    private Random rand = new Random();

    ArrayList<Task> generate() {
        ArrayList<Task> tasks = new ArrayList<>();
        List<String> names = new ArrayList<>(TASKS_NUM);
        while (names.size() < TASKS_NUM) {
            String noun = nouns.get(rand.nextInt(nouns.size()));
            String task = verbs.get(rand.nextInt(verbs.size())) + " " + noun;
            names.add(task);
        }


        List<Integer> seqTasksEndDay = new ArrayList<>();
        Set<Integer> seqTasksInds = new ArraySet<>();

        while (seqTasksInds.size() < TASKS_SEQ_NUM) {
            seqTasksInds.add(rand.nextInt(TASKS_NUM - 1));
        }

        for(int i = 0; i < TASKS_NUM; i++) {
            int startDay, endDay;
            if (seqTasksInds.contains(i)) {
                startDay = seqTasksEndDay.isEmpty() ? 2 : (seqTasksEndDay.get(seqTasksEndDay.size() - 1) + 1);
                 endDay = seqTasksEndDay.size() == 2 ? 34 : startDay + rand.nextInt(10) + 6;
                seqTasksEndDay.add(endDay);

            } else {
                startDay = rand.nextInt((MAX_DAY - MIN_DAY) / 2 + 1) + MIN_DAY;
                endDay = rand.nextInt(10) + 4 + startDay;
            }
            if (endDay > MAX_DAY) {
                endDay = MAX_DAY;
            }
            tasks.add(new Task(i, names.get(i), startDay, endDay));

        }
        return tasks;
    }
}
