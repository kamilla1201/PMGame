package com.example.pmgame;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int COLUMNS_NUM = 7;
    private static final int ROWS_NUM = 6;
    private static final int WEEKS_NUM = 5;
    private static final String EMPTY_STRING = "";
    private List<String> headers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Set No Title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        headers = new ArrayList<String>();
        headers.add("Project start");
        for (int i = 1; i <= WEEKS_NUM; i++) {
            headers.add("Week " + i);
        }
        headers.add("Assign");
        assert headers.size() == COLUMNS_NUM;
        createTable();

    }

    private void createTable() {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableLayout table = (TableLayout) findViewById(R.id.gameTableLayout);
        for (int r = 0; r < ROWS_NUM; r++) {
            // create a new TableRow
            TableRow row = new TableRow(this);

            // count the counter up by one
            //counter++;
            for (int c = 0; c < COLUMNS_NUM; c++) {
                // create a new TextView
                TextView t = new TextView(this);
                // set the text to "text xx"
                t.setText(generateText(r, c));
                //t.setLayoutParams(new TableRow.LayoutParams(c));

                //t.setMaxWidth(80);

                // add the TextView and the CheckBox to the new TableRow
                row.addView(t);
                if (c < COLUMNS_NUM - 1) {
                    t.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.border, null));
                }
            }
            // add the TableRow to the TableLayout
            table.addView(row, rowParams);
        }
    }

    private String generateText(int row, int column) {
        String result = "";
        if (row == 0) {
            result = headers.get(column);
        } else if (row == ROWS_NUM - 1) {
            if (column == 0) {
                result = "Project complete";
            }
        } else if (column == 0) {
            result = "Generated task " + row;
        }
        return result;
    }
}
