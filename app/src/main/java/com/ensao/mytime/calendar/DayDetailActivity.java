package com.ensao.mytime.calendar;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.Activityfeature.Busniss.userActivity;
import com.ensao.mytime.Activityfeature.Repos.ActivityRepo;
import com.ensao.mytime.Activityfeature.Repos.CategoryRepo;
import com.ensao.mytime.study.model.DailyActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayDetailActivity extends AppCompatActivity
        implements AddActivityDialog.OnActivityAddedListener,
        DayActivitiesAdapter.OnActivityClickListener {

    private TextView dateTextView;
    private RecyclerView activitiesRecyclerView;
    private Button addActivityButton;
    private DayActivitiesAdapter adapter;
    private List<DailyActivity> activities = new ArrayList<>();
    private String selectedDate;
    
    // Repository references (following proper architecture)
    private ActivityRepo activityRepo;
    private CategoryRepo categoryRepo;
    private long defaultCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        // Initialize repositories
        activityRepo = new ActivityRepo(getApplication());
        categoryRepo = new CategoryRepo(getApplication());

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

        // Load default category and activities using repos
        loadDefaultCategoryAndActivities();

        addActivityButton.setOnClickListener(v -> {
            AddActivityDialog dialog = AddActivityDialog.newInstance(selectedDate, this);
            dialog.show(getSupportFragmentManager(), "AddActivityDialog");
        });
    }

    private void loadDefaultCategoryAndActivities() {
        // Use CategoryRepo to get or create default category
        categoryRepo.GetOrCreateDefaultCategory(this, category -> {
            if (category != null) {
                defaultCategoryId = category.getId();
            }
            // After loading category, load activities
            loadActivitiesForDate(selectedDate);
        });
    }

    @Override
    public void onActivityAdded(DailyActivity activity) {
        // Check if default category is loaded
        if (defaultCategoryId == -1) {
            Toast.makeText(this, "Chargement en cours, veuillez réessayer", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Parse date from selectedDate (format: yyyy-MM-dd)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date activityDate = sdf.parse(selectedDate);
            
            // Parse time and combine with date using Calendar
            String[] timeParts = activity.getTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Combine date and time
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(activityDate);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
            calendar.set(java.util.Calendar.MINUTE, minute);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            Date combinedDateTime = calendar.getTime();
            
            // Create userActivity
            userActivity newActivity = new userActivity();
            newActivity.setTitle(activity.getDescription());
            newActivity.setDescription(activity.getDescription());
            newActivity.setCategoryID(defaultCategoryId);
            newActivity.setIsActive(true);
            newActivity.setStartDate(combinedDateTime);
            newActivity.setEndDate(combinedDateTime);
            newActivity.setCreatedAt(new Date());
            newActivity.setCourseID(null);  // No course association for calendar activities
            
            // Insert using ActivityRepo
            activityRepo.Insert(newActivity, this, insertedId -> {
                if (insertedId > 0) {
                    activities.add(activity);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Activité ajoutée", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'ajout de l'activité", Toast.LENGTH_SHORT).show();
        }
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
        if (date == null) return;
        
        try {
            // Parse the date string to Date object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date queryDate = sdf.parse(date);
            
            // Calculate start and end of day in milliseconds
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(queryDate);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();
            
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
            long endOfDay = calendar.getTimeInMillis();
            
            // Use ActivityRepo to get activities in range
            activityRepo.GetActivitiesInRange(startOfDay, endOfDay, this, dbActivities -> {
                // Convert to DailyActivity format for adapter
                List<DailyActivity> loadedActivities = new ArrayList<>();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                
                for (userActivity ua : dbActivities) {
                    String time = ua.getStartDate() != null ? timeFormat.format(ua.getStartDate()) : "00:00";
                    loadedActivities.add(new DailyActivity(date, time, ua.getTitle()));
                }
                
                // Update UI (already on main thread due to repo callback)
                activities.clear();
                activities.addAll(loadedActivities);
                adapter.notifyDataSetChanged();
            });
            
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
