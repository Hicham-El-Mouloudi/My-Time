package com.ensao.mytime.calendar;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.study.model.DailyActivity;

import java.util.ArrayList;
import java.util.List;

public class DayDetailActivity extends AppCompatActivity
        implements AddActivityDialog.OnActivityAddedListener,
        DayActivitiesAdapter.OnActivityClickListener {

    private TextView dateTextView;
    private RecyclerView activitiesRecyclerView;
    private Button addActivityButton;
    private DayActivitiesAdapter adapter;
    private List<DailyActivity> activities = new ArrayList<>();
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        dateTextView = findViewById(R.id.dateTextView);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        addActivityButton = findViewById(R.id.addActivityButton);

        selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        if (selectedDate != null) {
            dateTextView.setText("Activités du " + formatDate(selectedDate));
        }

        adapter = new DayActivitiesAdapter(activities, this);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activitiesRecyclerView.setAdapter(adapter);

        loadActivitiesForDate(selectedDate);

        addActivityButton.setOnClickListener(v -> {
            AddActivityDialog dialog = AddActivityDialog.newInstance(selectedDate, this);
            dialog.show(getSupportFragmentManager(), "AddActivityDialog");
        });
    }

    @Override
    public void onActivityAdded(DailyActivity activity) {
        activities.add(activity);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Activité ajoutée", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityEdit(int position, DailyActivity activity) {
        Toast.makeText(this, "Modifier: " + activity.getDescription(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityDelete(int position, DailyActivity activity) {
        activities.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "Activité supprimée", Toast.LENGTH_SHORT).show();
    }

    private String formatDate(String dateString) {
        try {
            String[] parts = dateString.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    private void loadActivitiesForDate(String date) {
        if (date != null && date.equals("2024-01-15")) {
            activities.add(new DailyActivity(date, "09:00", "Révision Mathématiques"));
            activities.add(new DailyActivity(date, "11:00", "Cours de Physique"));
        }
        adapter.notifyDataSetChanged();
    }
}
