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
        viewGenerator = new StatsViewGenerator();

        // Bind Views
        rvWeeks = view.findViewById(R.id.rv_weeks);
        contentContainer = view.findViewById(R.id.content_of_the_day);
        btnCalendar = view.findViewById(R.id.btn_calendar);

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
        updateContent(day);
    }

    private void updateContent(DayData day) {
        contentContainer.removeAllViews();

        if (day == null || (!day.hasSleep() && !day.hasWake())) {
            // Show Empty State
            getLayoutInflater().inflate(R.layout.layout_statistics_empty, contentContainer, true);
        } else {
            // Show Content State
            View contentView = getLayoutInflater().inflate(R.layout.layout_statistics_content, contentContainer, true);
            setupContentLogic(contentView, day);
        }
    }

    private void setupContentLogic(View view, DayData day) {
        Button btnSleep = view.findViewById(R.id.btn_toggle_sommeil);
        Button btnWake = view.findViewById(R.id.btn_toggle_reveil);
        LinearLayout statsContainer = view.findViewById(R.id.dynamic_stats_container);
        ProgressBar pbQuality = view.findViewById(R.id.pb_quality);
        TextView tvQuality = view.findViewById(R.id.tv_quality_percentage);

        // Set initial state
        updateTabSelection(btnSleep, btnWake, statsContainer, pbQuality, tvQuality, day);

        btnSleep.setOnClickListener(v -> {
            isSleepTabSelected = true;
            updateTabSelection(btnSleep, btnWake, statsContainer, pbQuality, tvQuality, day);
        });

        btnWake.setOnClickListener(v -> {
            isSleepTabSelected = false;
            updateTabSelection(btnSleep, btnWake, statsContainer, pbQuality, tvQuality, day);
        });
    }

    private void updateTabSelection(Button btnSleep, Button btnWake, LinearLayout container, ProgressBar pbQuality,
            TextView tvQuality, DayData day) {
        container.removeAllViews();
        Context context = getContext();
        if (context == null)
            return;

        // Define Colors
        int primaryColor = getResources().getColor(R.color.primary_color);
        int whiteColor = getResources().getColor(R.color.white);

        if (isSleepTabSelected) {
            // Sleep Active
            btnSleep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnSleep.setTextColor(whiteColor);

            btnWake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(whiteColor));
            btnWake.setTextColor(primaryColor);

            if (day != null && day.hasSleep()) {
                Map<String, Object> stats = sleepCalculator.calculateSleepStats(day);
                container.addView(viewGenerator.generateSleepView(context, stats));
            }
        } else {
            // Wake Active
            btnWake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnWake.setTextColor(whiteColor);

            btnSleep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(whiteColor));
            btnSleep.setTextColor(primaryColor);

            if (day != null && day.hasWake()) {
                Map<String, Object> stats = wakeCalculator.calculateWakeStats(day);
                container.addView(viewGenerator.generateWakeView(context, stats));
            }
        }

        // Update Quality
        if (day != null) {
            int quality = isSleepTabSelected ? day.getSleepEfficiency() : 75; // Example logic
            if (pbQuality != null)
                pbQuality.setProgress(quality);
            if (tvQuality != null)
                tvQuality.setText(quality + "%");
        }
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
