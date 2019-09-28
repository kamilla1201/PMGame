package com.example.pmgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {

    private static final int COLUMNS_NUM = 7;
    private static final int ROWS_NUM = 6;
    private static final int WEEKS_NUM = 5;
    private static final Float END_TIME_COORD = 1505f;
    private List<String> headers;
    private Map<Integer, List<TextView>> taskViews;
    private Map<Integer, Coordinates> taskCoordinates;
    private Set<Float> vlineCoordinates;
    public AtomicInteger tasksFinished = new AtomicInteger(0);
    public Integer tasksPlanned = 0;
    public Integer spentBudget = 0;
    boolean gameStarted = false;
    AnimatorSet as;
    List<Spinner> spinners;
    Map<Integer, Pair<Staff, List<Animator>>> taskData;
    Map<String, Integer> staffSpent;
    Map<String, TextView> staffSpentViews;

    static Game game;

    public Integer getCost(Cost cost) {
        Integer budget = game.getBudget();
        switch (cost) {
            case AVERAGE:
                return budget / tasksPlanned;
            case EXPENSIVE:
                return (int) (budget / tasksPlanned * 1.5);
            case CHEAP:
                return budget / tasksPlanned / 2;
        }
        throw new IllegalArgumentException("Invalid argument: " + cost);
    }

    @SuppressLint("DefaultLocale")
    public void calculateBudget(int start, int end) {
        for (Integer taskInd : taskCoordinates.keySet()) {
            Coordinates coordinates = taskCoordinates.get(taskInd);
            if (coordinates != null && Math.round(coordinates.getStartX()) == start
                    && coordinates.getDaysEnds().contains((float) end)) {
                String name = game.getStaffByTaskId(taskInd).getName();
                int spent = getCost(game.getStaffByTaskId(taskInd).getCost());
                assert staffSpent != null;
                int wasSpent = staffSpent.get(name);
                staffSpent.put(name, wasSpent + spent);
                String text = name;
                text += " : $" + (staffSpent.get(name) / 1000) + "k";
                Objects.requireNonNull(staffSpentViews.get(name)).setText(text);

                spentBudget += spent;
                Integer localSpent = spentBudget;
                TextView tv = findViewById(R.id.budget);
                tv.setText(String.format("Budget spent: $%dk", localSpent / 1000));
                Log.d("BUDGET", name + " spent " + (wasSpent + spent));
                if (game.getBudget() < localSpent) {
                    tv.setTextColor(Color.RED);
                    tv.setTypeface(null, Typeface.BOLD);
                }
            }
        }
    }

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Set No Title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        taskViews = new HashMap<>();
        taskCoordinates = new HashMap<>();
        vlineCoordinates = new TreeSet<>();
        spinners = new ArrayList<>();
        as = new AnimatorSet();
        taskData = new HashMap<>();
        staffSpent = new HashMap<>();
        staffSpentViews = new HashMap<>();

        headers = new ArrayList<>();
        headers.add("Task name");
        for (int i = 1; i <= WEEKS_NUM; i++) {
            headers.add("Week " + i);
        }
        headers.add("       Assign");
        assert headers.size() == COLUMNS_NUM;
        game = new Game();
        for (Staff staff : game.getStaff()) {
            staffSpent.putIfAbsent(staff.getName(), 0);
        }
        createResources();
        createInstructions();
        createSpentTable();
        createTable();

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void createSpentTable() {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams smallMargin = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        smallMargin.setMargins(15, 5, 15, 5);
        TableLayout table = findViewById(R.id.spent);

        TableRow titleRow = new TableRow(this);
        titleRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tTitle = new TextView(this);
        tTitle.setText("Money spent");
        tTitle.setGravity(Gravity.CENTER);
        tTitle.setTypeface(null, Typeface.BOLD);
        titleRow.addView(tTitle, smallMargin);
        table.addView(titleRow, rowParams);

        for (Staff staff : game.getStaff()) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
            TextView name = new TextView(this);
            String text = staff.getName();
            text += String.format(" : $%dk", staffSpent.get(staff.getName()) / 1000);
            name.setText(text);
            row.addView(name, smallMargin);
            table.addView(row, rowParams);
            staffSpentViews.put(staff.getName(), name);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void createResources() {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams smallMargin = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        smallMargin.setMargins(15, 5, 15, 5);
        TableRow.LayoutParams bigMargin = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        bigMargin.setMargins(115, 5, 115, 5);
        TableLayout table = findViewById(R.id.resourcesTableLayout);
        TableRow titleRow = new TableRow(this);
        titleRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tTitle = new TextView(this);
        tTitle.setText("Your resources");
        tTitle.setGravity(Gravity.CENTER);
        tTitle.setTypeface(null, Typeface.BOLD);
        titleRow.addView(tTitle);
        table.addView(titleRow, rowParams);

        TableRow budgetRow = new TableRow(this);
        budgetRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tBudget = new TextView(this);
        tBudget.setText(String.format("Your budget: $%d", game.getBudget()));
        budgetRow.addView(tBudget, smallMargin);
        table.addView(budgetRow, rowParams);

        TableRow staffRow = new TableRow(this);
        staffRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tStaff = new TextView(this);
        tStaff.setText("Your staff:");
        staffRow.addView(tStaff, smallMargin);
        table.addView(staffRow, rowParams);

        for (Staff staff : game.getStaff()) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
            TextView name = new TextView(this);
            String text = staff.getName();
            boolean costAdded = false;
            if (staff.getCost() != Cost.AVERAGE) {
                text += " is " + staff.getCost();
                costAdded = true;
            }
            if (staff.getSpeed() != Speed.AVERAGE) {
                text += (costAdded ? (" and ") : (" is ")) + staff.getSpeed();
            }
            name.setText(text);
            row.addView(name, bigMargin);
            table.addView(row, rowParams);

        }
    }

    @SuppressLint("SetTextI18n")
    private void createInstructions() {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams smallMargin = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        smallMargin.setMargins(15, 5, 15, 5);
        TableRow.LayoutParams bigMargin = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        bigMargin.setMargins(115, 5, 115, 5);
        TableLayout table = findViewById(R.id.instructions);
        TableRow titleRow = new TableRow(this);
        titleRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tTitle = new TextView(this);
        tTitle.setText("Instructions");
        tTitle.setGravity(Gravity.CENTER);
        tTitle.setTypeface(null, Typeface.BOLD);
        titleRow.addView(tTitle);
        table.addView(titleRow, rowParams);

        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tv1 = new TextView(this);
        tv1.setText("1. Assign staff to each task");
        tr1.addView(tv1, smallMargin);
        table.addView(tr1, rowParams);


        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tv2 = new TextView(this);
        tv2.setText("2. Click Play button");
        tr2.addView(tv2, smallMargin);
        table.addView(tr2, rowParams);

        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tv3 = new TextView(this);
        tv3.setText("Your goals are to:");
        tr3.addView(tv3, smallMargin);
        table.addView(tr3, rowParams);

        TableRow tr4 = new TableRow(this);
        tr4.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tv4 = new TextView(this);
        tv4.setText("1. complete on time");
        tr4.addView(tv4, bigMargin);
        table.addView(tr4, rowParams);

        TableRow tr5 = new TableRow(this);
        tr5.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
        TextView tv5 = new TextView(this);
        tv5.setText("2. complete under budget");
        tr5.addView(tv5, bigMargin);
        table.addView(tr5, rowParams);
    }

    private void createTable() {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        TableLayout table = findViewById(R.id.gameTableLayout);
        table.setStretchAllColumns(true);
        for (int r = 0; r < ROWS_NUM; r++) {
            // create a new TableRow
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            for (int c = 0; c < COLUMNS_NUM; c++) {
                // create a new TextView
                // set the text to "text xx"

                if (r == 0 || c == 0) {
                    TextView t = new TextView(this);
                    String text = generateText(r, c, game);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    params.span = 7;

                    t.setText(text);
                    if (c != COLUMNS_NUM - 1) {
                        t.setGravity(Gravity.CENTER);
                    }
                    row.addView(t, params);
                    if (c < COLUMNS_NUM - 1) {
                        t.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cell_all_borders, null));
                    }
                } else if (c == COLUMNS_NUM - 1) {
                    final Spinner spinner = new Spinner(this);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                    params.span = 1;
                    final List<String> personNames = new ArrayList<String>();
                    personNames.add("");
                    for (Staff staff : game.getStaff()) {
                        personNames.add(staff.getName());
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, personNames);
                    spinner.setAdapter(arrayAdapter);
                    spinner.setTag(r - 1);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Staff staff;
                            staff = (position != 0) ? game.getStaff().get(position - 1) : null;
                            game.addAssign(game.getTasks().get((int) parent.getTag()), staff);
                            boolean nullValues = false;
                            for (Staff s : game.getAssigns().values()) {
                                if (s == null) {
                                    nullValues = true;
                                    break;
                                }
                            }
                            if (!nullValues) {
                                Button play = findViewById(R.id.button_play);
                                play.setEnabled(true);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    // Add Spinner to LinearLayout
                    row.addView(spinner, params);
                    spinners.add(spinner);

                } else {
                    for (int d = 0; d < 7; d++) {
                        TextView t = new TextView(this);
                        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                        t.setLayoutParams(params); // causes layout update
                        row.addView(t, params);
                        int taskInd = r - 1;
                        taskViews.putIfAbsent(taskInd, new ArrayList<TextView>());
                        Objects.requireNonNull(taskViews.get(taskInd)).add(t);
                    }
                }
            }

            // add the TableRow to the TableLayout
            table.addView(row, rowParams);
        }
        Map<Integer, List<Integer>> taskDays = new HashMap<Integer, List<Integer>>();
        for (Task task : game.getTasks()) {
            int start = task.getStartDay();
            int end = task.getEndDay();
            int taskInd = game.getTasks().indexOf(task);
            taskDays.putIfAbsent(taskInd, new ArrayList<>(Collections.nCopies(35, 0)));
            for (int i = start - 1; i < end; i++) {
                Objects.requireNonNull(taskDays.get(taskInd)).set(i, 1);
            }
        }
        for (int task = 0; task < ROWS_NUM - 1; task++) {
            List<TextView> taskRow = taskViews.get(task);
            boolean inTask = false;
            List<Integer> days = taskDays.get(task);
            assert days != null;
            assert taskRow != null;
            for (int day = 0; day < 35; day++) {
                TextView dayTextView = taskRow.get(day);
                if (day % 7 == 0) {
                    if (days.get(day) == 1) {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.red_cell_left_border, null));
                        if (!inTask) {
                            inTask = true;
                            dayTextView.setTag("start");
                        }
                    } else {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cell_left_border, null));
                        if (inTask) {
                            inTask = false;
                            dayTextView.setTag("end");
                        }
                    }
                } else if ((day + 1) % 7 == 0) {
                    if (days.get(day) == 1) {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.red_cell_right_border, null));
                        if (!inTask) {
                            inTask = true;
                            dayTextView.setTag("start");
                        }
                    } else {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cell_right_border, null));
                        if (inTask) {
                            inTask = false;
                            dayTextView.setTag("end");
                        }
                    }
                } else {
                    if (days.get(day) == 1) {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.red_cell, null));
                        if (!inTask) {
                            inTask = true;
                            dayTextView.setTag("start");
                        }
                    } else {
                        dayTextView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cell_bottom_border, null));
                        if (inTask) {
                            inTask = false;
                            dayTextView.setTag("end");
                        }
                    }
                }
            }
        }
    }

    private String generateText(int row, int column, Game game) {
        String result = "";
        if (row == 0) {
            result = headers.get(column);
        } else if (column == 0) {
            result = game.getTasks().get(row - 1).getTaskName();
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    public void startGame(View view) {
        if (!gameStarted) {
            View timeLine = findViewById(R.id.timeLine);
            for (int i = 0; i < ROWS_NUM - 1; i++) {
                List<TextView> cells = taskViews.get(i);
                boolean started = false;
                boolean ended = false;
                List<Float> ends = new ArrayList<>();
                assert cells != null;
                for (TextView cell : cells) {
                    String tag = (String) cell.getTag();
                    Rect rect = locateView(cell);
                    float touchX = rect.left;
                    float touchY = rect.top + (Math.abs(rect.top - rect.bottom) / 2);
                    if (started && !ended) {
                        ends.add((float) rect.left);
                    }
                    if (tag != null) {
                        taskCoordinates.putIfAbsent(i, new Coordinates());
                        if (tag.equals("start")) {
                            Objects.requireNonNull(taskCoordinates.get(i)).setStartX(touchX);
                            Objects.requireNonNull(taskCoordinates.get(i)).setStartY(touchY);
                            started = true;
                        }
                        if (tag.equals("end")) {
                            Objects.requireNonNull(taskCoordinates.get(i)).setEndX(touchX);
                            Objects.requireNonNull(taskCoordinates.get(i)).setEndY(touchY);
                            ended = true;
                        }
                        vlineCoordinates.add(touchX);
                    }
                }
                Objects.requireNonNull(taskCoordinates.get(i)).setDaysEnds(ends);
            }
            vlineCoordinates.add(END_TIME_COORD);
            Rect rect = locateView(timeLine);
            float currentCoord = rect.left;
            ObjectAnimator prevAnimator = null;
            int counter = 0;
            for (Float x : vlineCoordinates) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(timeLine, "X", currentCoord, x);
                objectAnimator.setDuration(Math.round(140 * (x - currentCoord) / 20));
                objectAnimator.setInterpolator(null);
                if (prevAnimator != null) {
                    as.play(prevAnimator).before(objectAnimator);
                }
                for (Integer t : taskCoordinates.keySet()) {
                    Coordinates tc = taskCoordinates.get(t);
                    if (Math.round(Objects.requireNonNull(tc).getStartX()) == Math.round(currentCoord)) {
                        Float prevEnd = tc.getStartX();
                        ValueAnimator prevValueAnimator = null;
                        List<Animator> animators = new ArrayList<>();
                        for (Float end : tc.getDaysEnds()) {
                            if (end.equals(tc.getStartX())) {
                                continue;
                            }
                            ArrowLayout mArrowLayout = getLayout(counter);
                            mArrowLayout.setMainActivity(this);
                            mArrowLayout.mPointFrom.x = Math.round(tc.getStartX());
                            Point pointFrom = new Point();

                            pointFrom.x = Math.round(prevEnd);
                            pointFrom.y = Math.round(tc.getStartY() - 675);
                            Point pointTo = new Point();
                            pointTo.x = Math.round(end);
                            pointTo.y = Math.round(tc.getEndY() - 675);

                            Staff staff = game.getStaffByTaskId(game.getTasks().get(t).getId());
                            int staffAssigned = 0;
                            for (Task task : game.getAssigns().keySet()) {
                                if (Objects.requireNonNull(game.getAssigns().get(task)).getName().equals(staff.getName())) {
                                    Coordinates coordinates = taskCoordinates.get(task.getId());
                                    if (Objects.requireNonNull(coordinates).getDaysEnds().contains(prevEnd)
                                            && coordinates.getDaysEnds().contains(end)) {
                                        staffAssigned++;
                                    }
                                }
                            }
                            Speed speed = staff != null ? staff.getSpeed() : Speed.AVERAGE;
                            int duration = getDuration(speed, end - prevEnd, staffAssigned);

                            ValueAnimator valueAnimator = mArrowLayout.createArrowAnimator(pointFrom, pointTo, duration);

                            if (prevValueAnimator == null) {
                                as.play(objectAnimator).with(valueAnimator);
                            } else {
                                as.play(prevValueAnimator).before(valueAnimator);
                            }
                            animators.add(valueAnimator);
                            prevValueAnimator = valueAnimator;
                            prevEnd = end;
                            tasksPlanned++;
                        }
                        counter++;
                        taskData.putIfAbsent(t, new Pair<>(game.getStaffByTaskId(t), animators));

                    }
                }
                currentCoord = x;
                prevAnimator = objectAnimator;

            }
            Objects.requireNonNull(prevAnimator).addListener(new AnimatorListenerAdapter() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onAnimationEnd(Animator animation) {
                    as.cancel();
                    TextView budgetTV = findViewById(R.id.budget);
                    if (game.getBudget() < spentBudget) {
                        budgetTV.setText("Over budget: $" + spentBudget / 1000 + "k");
                        budgetTV.setTextColor(Color.RED);
                        budgetTV.setTypeface(null, Typeface.BOLD);
                    } else {
                        budgetTV.setText("Under budget: $" + spentBudget / 1000 + "k");
                        budgetTV.setTextColor(Color.parseColor("#29ad00"));
                        budgetTV.setTypeface(null, Typeface.BOLD);
                    }
                    TextView timeTV = findViewById(R.id.time);
                    if (tasksFinished.get() < tasksPlanned) {
                        timeTV.setText("Out of time :(");
                        timeTV.setTextColor(Color.RED);
                        timeTV.setTypeface(null, Typeface.BOLD);
                    } else {
                        timeTV.setText("On time :)");
                        timeTV.setTextColor(Color.parseColor("#29ad00"));
                        timeTV.setTypeface(null, Typeface.BOLD);
                    }
                    Button play = findViewById(R.id.button_play);
                    play.setText("Play");
                    play.setEnabled(false);

                }
            });
            Button play = findViewById(R.id.button_play);
            gameStarted = true;
            play.setText("Pause");
            as.start();
            for (Spinner s : spinners) {
                s.setEnabled(false);
            }
        } else {

            Button play = findViewById(R.id.button_play);
            if (play.getText() == "Play") {
                for (Spinner s : spinners) {
                    s.setEnabled(false);
                }
                play.setText("Pause");
                as.resume();
                List<Animator> animators = as.getChildAnimations();
                for (Animator animator : animators) {
                    if (animator.isStarted() && animator.isRunning()) {
                        for (Integer tI : taskData.keySet()) {
                            Pair<Staff, List<Animator>> tD = taskData.get(tI);
                            boolean changed = false;
                            Staff newStaff = null;
                            assert tD != null;
                            for (Animator a : tD.second) {
                                if (a == animator || changed) {
                                    if (newStaff == null && !tD.first.getName().equals(game.getStaffByTaskId(tI).getName())) {
                                        newStaff = game.getStaffByTaskId(tI);
                                        Speed speed = newStaff != null ? newStaff.getSpeed() : Speed.AVERAGE;
                                        int duration = getDuration(speed, 36.0f, 1);
                                        a.setDuration(duration);
                                        changed = true;
                                    } else if (newStaff != null) {
                                        Speed speed = newStaff.getSpeed();
                                        int duration = getDuration(speed, 10.0f, 1);
                                        a.setDuration(duration);
                                    }
                                }
                            }
                            if (changed && newStaff != null) {

                                taskData.put(tI, new Pair<>(newStaff, tD.second));
                            }
                        }
                    }
                }
            } else {
                play.setText("Play");
                for (Spinner s : spinners) {
                    s.setEnabled(true);
                }
                as.pause();
            }
        }
    }

    private int getDuration(Speed speed, Float distance, int timesAssigned) {
        switch (speed) {
            case FAST:
                return Math.round(140 * distance / 24 * timesAssigned);
            case SLOW:
                return Math.round(140 * distance / 16 * timesAssigned);
            case AVERAGE:
                return Math.round(140 * distance / 20 * timesAssigned);
        }
        throw new IllegalArgumentException("Invalid argument: " + speed.toString());
    }

    private ArrowLayout getLayout(int counter) {
        if (counter == 0) {
            return (ArrowLayout) findViewById(R.id.arrow_layout_1);
        }
        if (counter == 1) {
            return (ArrowLayout) findViewById(R.id.arrow_layout_2);
        }
        if (counter == 2) {
            return (ArrowLayout) findViewById(R.id.arrow_layout_3);
        }
        if (counter == 3) {
            return (ArrowLayout) findViewById(R.id.arrow_layout_4);
        }
        if (counter == 4) {
            return (ArrowLayout) findViewById(R.id.arrow_layout_5);
        }
        throw new IllegalArgumentException("counter is invalid: " + counter);
    }

    public Rect locateView(View view) {
        Rect loc = new Rect();
        int[] location = new int[2];
        if (view == null) {
            return loc;
        }
        view.getLocationOnScreen(location);

        loc.left = location[0];
        loc.top = location[1];
        loc.right = loc.left + view.getWidth();
        loc.bottom = loc.top + view.getHeight();
        return loc;
    }

    public void restartGame(View view) {
        this.recreate();
    }
}
