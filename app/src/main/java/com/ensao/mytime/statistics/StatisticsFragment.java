package com.ensao.mytime.statistics;

import java.time.LocalDate;
import java.util.*;
import java.text.*;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.adapter.OnDayClickListener;
import com.ensao.mytime.statistics.adapter.calendar.CalendarDaysAdaptee;
import com.ensao.mytime.statistics.adapter.calendar.CalendarDaysAdapter;
import com.ensao.mytime.statistics.adapter.week.WeeksAdaptee;
import com.ensao.mytime.statistics.adapter.week.WeeksAdapter;
import com.ensao.mytime.statistics.calculation.MockSleepStatsCalculator;
import com.ensao.mytime.statistics.calculation.MockWakeStatsCalculator;
import com.ensao.mytime.statistics.data.StatisticsDAOProxy;
import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import com.github.mikephil.charting.charts.PieChart;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Map;

public class StatisticsFragment extends Fragment implements OnDayClickListener {

    private RecyclerView rvWeeks;
    private FrameLayout contentContainer;
    private ImageView btnCalendar;
    private TextView tvSelectedDay;

    private StatisticsDAOProxy daoProxy;

    private WeeksAdaptee weeksAdaptee;
    private WeeksAdapter weeksAdapter;
    private CalendarDaysAdaptee calendarAdaptee;
    private CalendarDaysAdapter calendarAdapter;
    private Dialog calendarDialog;

    private MockSleepStatsCalculator sleepCalculator;
    private MockWakeStatsCalculator wakeCalculator;
    private StatsViewGenerator viewGenerator;

    private DayData currentDay;
    private boolean isSleepTabSelected = true; // Default to Sleep

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialize Components
        daoProxy = new StatisticsDAOProxy();
        calendarDialog = new Dialog(getContext());
        calendarAdaptee = new CalendarDaysAdaptee(daoProxy);
        calendarAdapter = new CalendarDaysAdapter(calendarAdaptee, day -> {
            onDayClick(day); // Update the day
            calendarDialog.dismiss(); // Close the dialog
        });
        weeksAdaptee = new WeeksAdaptee(daoProxy);
        weeksAdapter = new WeeksAdapter(weeksAdaptee, this);
        sleepCalculator = new MockSleepStatsCalculator();
        wakeCalculator = new MockWakeStatsCalculator();
        // viewGenerator = new StatsViewGenerator();

        // Bind Views
        rvWeeks = view.findViewById(R.id.rv_weeks);
        contentContainer = view.findViewById(R.id.content_of_the_day);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        tvSelectedDay = view.findViewById(R.id.tv_selected_day);

        // Setup the Weeks horizontal scroll bar
        setupWeeksHorizontalScrollBar();

        // Setup Calendar Button
        btnCalendar.setOnClickListener(v -> showCalendarDialog());

        // Initialize the day content from the calendar adapter
        // @Note : we can also use the WeekAdapter to initialize the day content
        calendarAdapter.initializeDayContentOfListener();

        return view;
    }

    private void setupWeeksHorizontalScrollBar() {
        rvWeeks.setAdapter(weeksAdapter);
        PagerSnapHelper linearSnapHelper = new PagerSnapHelper();
        linearSnapHelper.attachToRecyclerView(rvWeeks);
        // Updating the weeks count inside the weeks adapter
        rvWeeks.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!v.canScrollHorizontally(-1)) {
                    weeksAdapter.extendWeeksCount();
                }
            }
        });
    }

    @Override
    public void onDayClick(DayData day) {
        this.currentDay = day;
        weeksAdapter.setSelectedDay(day);
        updateSelectedDayText(day);
        updateContent(day);
    }

    private void updateSelectedDayText(DayData day) {
        if (day == null || tvSelectedDay == null)
            return;
        java.time.LocalDate date = day.getDate();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);

        if (date.equals(today)) {
            tvSelectedDay.setText("Today");
        } else if (date.equals(yesterday)) {
            tvSelectedDay.setText("Yesterday");
        } else {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d");
            tvSelectedDay.setText(date.format(formatter));
        }
    }

    private void updateContent(DayData day) {
        contentContainer.removeAllViews();

        // Use Builder
        com.ensao.mytime.statistics.view.DayStatisticsViewBuilder builder = new com.ensao.mytime.statistics.view.DayStatisticsViewBuilder(
                getContext(), contentContainer);

        Map<String, Object> sleepStats = (day != null && day.hasSleep()) ? sleepCalculator.calculateSleepStats(day)
                : null;
        Map<String, Object> wakeStats = (day != null && day.hasWake()) ? wakeCalculator.calculateWakeStats(day) : null;

        View contentView = builder
                .forDay(day)
                .setAvailableSleepDataLayout(R.layout.layout_sleep_stats)
                .setUnavailableSleepDataLayout(R.layout.layout_statistics_empty) // Or specific empty layout
                .setAvailableWakeDataLayout(R.layout.layout_wake_stats)
                .setUnavailableWakeDataLayout(R.layout.layout_statistics_empty)
                .setUnavailableDataLayout(R.layout.layout_statistics_empty)
                .useSleepStats(sleepStats)
                .useWakeStats(wakeStats)
                .prioritizeDataAvailability(isSleepTabSelected) // Maintains state? Or builder determines priority?
                // The user said: "prioritizeDataAvailability ... returns view will have by
                // default an activated tab ... if hasSleep=true ... activated on tab Sleep"
                // This implies the builder decides the INITIAL tab.
                // But I have state 'isSleepTabSelected' in Fragment.
                // If I want to persist user selection when they change days, I should pass my
                // current selection.
                // But if the user wants the logic "Prioritize available data", maybe I should
                // let builder decide and update my state?
                // For now, I'll pass 'isSleepTabSelected' as the preference.
                .prioritizeDataAvailability(isSleepTabSelected)
                .build();

        contentContainer.addView(contentView);
    }

    private void showCalendarDialog() {
        calendarDialog.setContentView(R.layout.dialog_calendar);
        calendarDialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView rvCalendar = calendarDialog.findViewById(R.id.rv_calendar_days);
        TextView tvMonthYear = calendarDialog.findViewById(R.id.tv_month_year);
        ImageButton btnPrev = calendarDialog.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = calendarDialog.findViewById(R.id.btn_next_month);

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));

        rvCalendar.setAdapter(calendarAdapter);

        // Function to update grid
        Runnable updateCurrentDateTextView = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                tvMonthYear.setText(sdf.format(calendarAdapter.getCalendar().getTime()));
            }
        };

        // Initialize the month and year text view
        updateCurrentDateTextView.run();

        // Navigation Listeners
        btnPrev.setOnClickListener(v -> {
            calendarAdapter.previousMonth();
            updateCurrentDateTextView.run();
        });

        btnNext.setOnClickListener(v -> {
            calendarAdapter.nextMonth();
            updateCurrentDateTextView.run();
        });

        calendarDialog.show();
    }
}
