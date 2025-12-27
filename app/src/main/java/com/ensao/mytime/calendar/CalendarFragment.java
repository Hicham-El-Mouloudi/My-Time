package com.ensao.mytime.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.Activityfeature.Busniss.userActivity;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;
import com.ensao.mytime.Activityfeature.Repos.ActivityRepo;
import com.ensao.mytime.Activityfeature.Repos.CategoryRepo;
import com.ensao.mytime.study.model.DailyActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment
        implements AddActivityDialog.OnActivityAddedListener,
        DayActivitiesAdapter.OnActivityClickListener,
        EditActivityDialog.OnActivityEditedListener {

    private static final String TAG = "CalendarFragment";

    private RecyclerView dateBarRecyclerView;
    private RecyclerView activitiesRecyclerView;
    private TextView tvCurrentMonth;
    private TextView tvSelectedDateHeader;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddActivity;
    private ImageButton btnToday;

    private DateBarAdapter dateBarAdapter;
    private DayActivitiesAdapter activitiesAdapter;
    private List<DailyActivity> activities = new ArrayList<>();

    private ActivityRepo activityRepo;
    private CategoryRepo categoryRepo;
    private long defaultCategoryId = -1;
    private String selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        Log.d(TAG, "=== CalendarFragment onCreateView ===");

        // Initialize repositories
        activityRepo = new ActivityRepo(requireActivity().getApplication());
        categoryRepo = new CategoryRepo(requireActivity().getApplication());

        // Initialize views
        dateBarRecyclerView = view.findViewById(R.id.dateBarRecyclerView);
        activitiesRecyclerView = view.findViewById(R.id.activitiesRecyclerView);
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvSelectedDateHeader = view.findViewById(R.id.tvSelectedDateHeader);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        fabAddActivity = view.findViewById(R.id.fabAddActivity);
        btnToday = view.findViewById(R.id.btnToday);

        setupDateBar();
        setupActivitiesList();
        setupFab();
        setupTodayButton();
        loadDefaultCategory();

        return view;
    }

    private void setupDateBar() {
        dateBarAdapter = new DateBarAdapter((date, formattedDate) -> {
            selectedDate = formattedDate;
            Log.d(TAG, "Date selected: " + formattedDate);
            updateMonthHeader(date);
            updateSelectedDateHeader(date);
            loadActivitiesForDate(formattedDate);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        dateBarRecyclerView.setLayoutManager(layoutManager);
        dateBarRecyclerView.setAdapter(dateBarAdapter);

        int todayPosition = dateBarAdapter.getTodayPosition();
        if (todayPosition >= 0) {
            dateBarRecyclerView.scrollToPosition(todayPosition);
            dateBarRecyclerView.post(() -> {
                int offset = dateBarRecyclerView.getWidth() / 2 - 36;
                layoutManager.scrollToPositionWithOffset(todayPosition, offset);
            });
        }

        selectedDate = dateBarAdapter.getFormattedSelectedDate();
        updateMonthHeader(dateBarAdapter.getSelectedDate());
        updateSelectedDateHeader(dateBarAdapter.getSelectedDate());
    }

    private void setupActivitiesList() {
        activitiesAdapter = new DayActivitiesAdapter(activities, this);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activitiesRecyclerView.setAdapter(activitiesAdapter);
    }

    private void setupFab() {
        fabAddActivity.setOnClickListener(v -> {
            if (selectedDate != null) {
                AddActivityDialog dialog = AddActivityDialog.newInstance(selectedDate, this);
                dialog.show(getChildFragmentManager(), "AddActivityDialog");
            }
        });
    }

    private void setupTodayButton() {
        btnToday.setOnClickListener(v -> {
            int todayPosition = dateBarAdapter.getTodayPosition();
            if (todayPosition >= 0) {
                dateBarAdapter.setSelectedPosition(todayPosition);
                LinearLayoutManager layoutManager = (LinearLayoutManager) dateBarRecyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int offset = dateBarRecyclerView.getWidth() / 2 - 36;
                    layoutManager.scrollToPositionWithOffset(todayPosition, offset);
                }
                selectedDate = dateBarAdapter.getFormattedSelectedDate();
                updateMonthHeader(dateBarAdapter.getSelectedDate());
                updateSelectedDateHeader(dateBarAdapter.getSelectedDate());
                loadActivitiesForDate(selectedDate);
            }
        });
    }

    private void loadDefaultCategory() {
        Log.d(TAG, "Loading default category...");
        categoryRepo.GetOrCreateDefaultCategory(requireActivity(), category -> {
            if (category != null) {
                defaultCategoryId = category.getId();
                Log.d(TAG, "Default category loaded: ID=" + defaultCategoryId);

                // Debug: Check what repetition kind the default category has
                categoryRepo.GetCategories(requireActivity(), categories -> {
                    if (categories != null) {
                        for (CategoryDetailedDTO cat : categories) {
                            if (cat.category.getId() == defaultCategoryId) {
                                Log.d(TAG, "Default category repetition: " + cat.RepetitionTitle);
                            }
                        }
                    }
                });
            } else {
                Log.e(TAG, "Failed to load default category!");
            }
            if (selectedDate != null) {
                loadActivitiesForDate(selectedDate);
            }
        });
    }

    private void updateMonthHeader(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCurrentMonth.setText(monthFormat.format(date));
    }

    private void updateSelectedDateHeader(Date date) {
        SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE d MMMM", Locale.getDefault());
        String formattedDate = fullFormat.format(date);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        tvSelectedDateHeader.setText("Activités - " + formattedDate);
    }

    private void loadActivitiesForDate(String date) {
        if (date == null) return;

        Log.d(TAG, "========================================");
        Log.d(TAG, "Loading activities for date: " + date);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date queryDate = sdf.parse(date);
            Log.d(TAG, "Query date timestamp: " + queryDate.getTime());

            // Use GetActivities which uses the smart recurring query
            activityRepo.GetActivities(queryDate, requireActivity(), dbActivities -> {
                Log.d(TAG, "Received " + (dbActivities != null ? dbActivities.size() : 0) + " activities from DB");

                if (dbActivities != null && !dbActivities.isEmpty()) {
                    for (userActivity ua : dbActivities) {
                        Log.d(TAG, "  - Activity: '" + ua.getTitle() + "'" +
                                ", ID=" + ua.getId() +
                                ", CategoryID=" + ua.getCategoryID() +
                                ", StartDate=" + ua.getStartDate() +
                                ", IsActive=" + ua.getIsActive());
                    }
                } else {
                    Log.d(TAG, "  No activities returned by query!");
                }

                List<DailyActivity> loadedActivities = new ArrayList<>();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                if (dbActivities != null) {
                    for (userActivity ua : dbActivities) {
                        String startTime = ua.getStartDate() != null ? timeFormat.format(ua.getStartDate()) : "00:00";
                        String endTime = ua.getEndDate() != null ? timeFormat.format(ua.getEndDate()) : startTime;
                        loadedActivities.add(new DailyActivity(
                                ua.getId(),
                                date,
                                startTime,
                                endTime,
                                ua.getTitle(),
                                ua.getDescription(),
                                ua.getCategoryID()
                        ));
                    }
                }

                activities.clear();
                activities.addAll(loadedActivities);
                activitiesAdapter.notifyDataSetChanged();
                updateEmptyState();

                Log.d(TAG, "Activities list updated. Size: " + activities.size());
                Log.d(TAG, "========================================");
            });

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing date: " + date, e);
        }
    }

    private void updateEmptyState() {
        if (activities.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            activitiesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            activitiesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityAdded(DailyActivity activity) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "onActivityAdded called");
        Log.d(TAG, "Activity title: " + activity.getTitle());
        Log.d(TAG, "Activity categoryId from dialog: " + activity.getCategoryId());

        // Use provided categoryId, or fall back to default
        long finalCategoryId = activity.getCategoryId() > 0 ? activity.getCategoryId() : defaultCategoryId;
        Log.d(TAG, "Final categoryId to use: " + finalCategoryId);
        Log.d(TAG, "Default categoryId: " + defaultCategoryId);

        if (finalCategoryId == -1) {
            Log.e(TAG, "CategoryID is -1! Cannot add activity.");
            Toast.makeText(getContext(), "Chargement en cours, veuillez réessayer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug: Check what category this is
        categoryRepo.GetCategories(requireActivity(), categories -> {
            if (categories != null) {
                for (CategoryDetailedDTO cat : categories) {
                    if (cat.category.getId() == finalCategoryId) {
                        Log.d(TAG, "Using category: " + cat.category.getTitle() +
                                ", Repetition: " + cat.RepetitionTitle);
                    }
                }
            }
        });

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date activityDate = sdf.parse(selectedDate);

            // Parse start time
            String[] startTimeParts = activity.getTime().split(":");
            int startHour = Integer.parseInt(startTimeParts[0]);
            int startMinute = Integer.parseInt(startTimeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activityDate);
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startDateTime = calendar.getTime();

            // Parse end time
            String[] endTimeParts = activity.getEndTime().split(":");
            int endHour = Integer.parseInt(endTimeParts[0]);
            int endMinute = Integer.parseInt(endTimeParts[1]);

            calendar.set(Calendar.HOUR_OF_DAY, endHour);
            calendar.set(Calendar.MINUTE, endMinute);
            Date endDateTime = calendar.getTime();

            Log.d(TAG, "Creating userActivity object...");
            Log.d(TAG, "  StartDate: " + startDateTime + " (" + startDateTime.getTime() + ")");
            Log.d(TAG, "  EndDate: " + endDateTime + " (" + endDateTime.getTime() + ")");
            Log.d(TAG, "  CategoryID: " + finalCategoryId);

            // Create a single activity - the query will handle showing it on recurring dates
            userActivity newActivity = new userActivity();
            newActivity.setTitle(activity.getTitle());
            newActivity.setDescription(activity.getDescription());
            newActivity.setCategoryID(finalCategoryId);
            newActivity.setIsActive(true);
            newActivity.setStartDate(startDateTime);
            newActivity.setEndDate(endDateTime);
            newActivity.setCreatedAt(new Date());
            newActivity.setCourseID(null);

            activityRepo.Insert(newActivity, requireActivity(), insertedId -> {
                Log.d(TAG, "Activity inserted! ID: " + insertedId);

                if (insertedId > 0) {
                    // Reload activities to show the new one
                    Log.d(TAG, "Reloading activities for current date: " + selectedDate);
                    loadActivitiesForDate(selectedDate);
                    Toast.makeText(getContext(), "Activité ajoutée", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to insert activity!");
                    Toast.makeText(getContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "========================================");
            });

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "Error adding activity", e);
            Toast.makeText(getContext(), "Erreur lors de l'ajout de l'activité", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityEdit(int position, DailyActivity activity) {
        EditActivityDialog dialog = EditActivityDialog.newInstance(position, activity, activity.getCategoryId(), this);
        dialog.show(getChildFragmentManager(), "EditActivityDialog");
    }

    @Override
    public void onActivityEdited(int position, DailyActivity updatedActivity) {
        if (updatedActivity.getId() <= 0) {
            Toast.makeText(getContext(), "Erreur: ID d'activité invalide", Toast.LENGTH_SHORT).show();
            return;
        }



        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date activityDate = sdf.parse(updatedActivity.getDate());

            String[] startTimeParts = updatedActivity.getTime().split(":");
            int startHour = Integer.parseInt(startTimeParts[0]);
            int startMinute = Integer.parseInt(startTimeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activityDate);
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startTimestamp = calendar.getTimeInMillis();

            String[] endTimeParts = updatedActivity.getEndTime().split(":");
            int endHour = Integer.parseInt(endTimeParts[0]);
            int endMinute = Integer.parseInt(endTimeParts[1]);

            calendar.set(Calendar.HOUR_OF_DAY, endHour);
            calendar.set(Calendar.MINUTE, endMinute);
            long endTimestamp = calendar.getTimeInMillis();

            activityRepo.Update(
                    updatedActivity.getId(),
                    updatedActivity.getTitle(),
                    updatedActivity.getDescription(),
                    startTimestamp,
                    endTimestamp,
                    updatedActivity.getCategoryId(),
                    requireActivity(),
                    success -> {
                        if (success) {
                            // Reload activities to reflect the update
                            loadActivitiesForDate(selectedDate);
                            Toast.makeText(getContext(), "Activité modifiée", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityDelete(int position, DailyActivity activity) {
        if (activity.getId() > 0) {
            activityRepo.Delete(activity.getId(), requireActivity(), success -> {
                if (success) {
                    // Reload activities to reflect the deletion
                    loadActivitiesForDate(selectedDate);
                    Toast.makeText(getContext(), "Activité supprimée", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            activities.remove(position);
            activitiesAdapter.notifyItemRemoved(position);
            updateEmptyState();
        }
    }
}