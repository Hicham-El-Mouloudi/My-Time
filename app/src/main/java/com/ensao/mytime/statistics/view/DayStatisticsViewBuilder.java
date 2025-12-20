package com.ensao.mytime.statistics.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.view.addons.SleepStructureAddOn;
import com.ensao.mytime.statistics.view.addons.SleepStyleAddOn;
import com.ensao.mytime.statistics.view.addons.WakeStructureAddOn;
import com.ensao.mytime.statistics.view.addons.WakeStyleAddOn;

import java.util.Map;

public class DayStatisticsViewBuilder {

    private final Context context;
    private final ViewGroup parent; // To use as root for inflation if valid layout params needed
    private DayData day;

    private int sleepLayoutId;
    private int wakeLayoutId;
    private int unavailableSleepLayoutId;
    private int unavailableWakeLayoutId;
    private int unavailableDataLayoutId;

    private Map<String, Object> sleepStats;
    private Map<String, Object> wakeStats;

    private boolean preferSleep = true; // Default

    // Constructor
    public DayStatisticsViewBuilder(Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
    }

    public DayStatisticsViewBuilder forDay(DayData day) {
        this.day = day;
        return this;
    }

    public DayStatisticsViewBuilder setAvailableSleepDataLayout(int layoutId) {
        this.sleepLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder setUnavailableSleepDataLayout(int layoutId) {
        this.unavailableSleepLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder setAvailableWakeDataLayout(int layoutId) {
        this.wakeLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder setUnavailableWakeDataLayout(int layoutId) {
        this.unavailableWakeLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder setUnavailableDataLayout(int layoutId) {
        this.unavailableDataLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder useSleepStats(Map<String, Object> stats) {
        this.sleepStats = stats;
        return this;
    }

    public DayStatisticsViewBuilder useWakeStats(Map<String, Object> stats) {
        this.wakeStats = stats;
        return this;
    }

    public DayStatisticsViewBuilder prioritizeDataAvailability(boolean preferSleep) {
        this.preferSleep = preferSleep;
        return this;
    }

    public View build() {
        if (day == null) {
            if (unavailableDataLayoutId != 0) {
                return LayoutInflater.from(context).inflate(unavailableDataLayoutId, parent, false);
            }
            return new View(context);
        }

        // Determine which tab to show initially
        boolean showSleep = preferSleep;
        boolean hasSleep = (day.hasSleep() && sleepStats != null);
        boolean hasWake = (day.hasWake() && wakeStats != null);

        if (preferSleep) {
            if (!hasSleep && hasWake) {
                showSleep = false;
            }
        } else {
            if (!hasWake && hasSleep) {
                showSleep = true;
            }
        }

        View mainView = LayoutInflater.from(context).inflate(R.layout.layout_statistics_content, parent, false);

        // Setup Logic
        setupTabsAndContent(mainView, showSleep);

        return mainView;
    }

    private void setupTabsAndContent(View mainView, boolean initialSleep) {
        Button btnSleep = mainView.findViewById(R.id.btn_toggle_sommeil);
        Button btnWake = mainView.findViewById(R.id.btn_toggle_reveil);
        LinearLayout statsContainer = mainView.findViewById(R.id.dynamic_stats_container);
        View pbQuality = mainView.findViewById(R.id.qualityChart);
        View pbQualityBackground = mainView.findViewById(R.id.qualityChartBackground);
        // We want to change content of the ImageView background
        android.widget.ImageView headerBackground = mainView.findViewById(R.id.header_background_image);

        // Click listeners
        btnSleep.setOnClickListener(
                v -> updateTab(true, btnSleep, btnWake, statsContainer, pbQuality, pbQualityBackground,
                        headerBackground));
        btnWake.setOnClickListener(
                v -> updateTab(false, btnSleep, btnWake, statsContainer, pbQuality, pbQualityBackground,
                        headerBackground));

        // Initial state
        updateTab(initialSleep, btnSleep, btnWake, statsContainer, pbQuality, pbQualityBackground, headerBackground);
    }

    private void updateTab(boolean isSleep, Button btnSleep, Button btnWake, LinearLayout container, View pbQuality,
            View pbQualityBackground, android.widget.ImageView headerBackground) {
        container.removeAllViews();

        // Tab Styling
        int primaryColor = context.getResources().getColor(R.color.primary_color);
        int whiteColor = context.getResources().getColor(R.color.white);
        int inactiveColor = context.getResources().getColor(R.color.text_secondary); // or gray

        if (isSleep) {
            btnSleep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnSleep.setTextColor(whiteColor);
            btnWake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(whiteColor));
            btnWake.setTextColor(primaryColor);

            if (headerBackground != null) {
                headerBackground.setImageResource(R.drawable.sleepillustration);
            }
        } else {
            // Wake
            btnWake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnWake.setTextColor(whiteColor);
            btnSleep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(whiteColor));
            btnSleep.setTextColor(primaryColor);

            if (headerBackground != null) {
                headerBackground.setImageResource(R.drawable.wakeillustration);
            }
        }

        // Content Generation
        if (isSleep) {
            if (day.hasSleep() && sleepStats != null) {
                // Layout
                View sleepView = LayoutInflater.from(context).inflate(sleepLayoutId, container, false);
                container.addView(sleepView);

                // Decorators
                new SleepStructureAddOn(sleepView, sleepStats); // Logic in constructor
                new SleepStyleAddOn(sleepView); // Logic in constructor

                updatePieChart(pbQuality, pbQualityBackground, day.getSleepEfficiency(), false);

            } else {
                if (unavailableSleepLayoutId != 0) {
                    View emptyView = LayoutInflater.from(context).inflate(unavailableSleepLayoutId, container, false);
                    container.addView(emptyView);
                }
                updatePieChart(pbQuality, pbQualityBackground, 0, true);
            }
        } else {
            // Wake
            if (day.hasWake() && wakeStats != null) {
                View wakeView = LayoutInflater.from(context).inflate(wakeLayoutId, container, false);
                container.addView(wakeView);

                new WakeStructureAddOn(wakeView, wakeStats);
                new WakeStyleAddOn(wakeView);

                updatePieChart(pbQuality, pbQualityBackground, day.getWakeEfficiency(), false);
            } else {
                if (unavailableWakeLayoutId != 0) {
                    View emptyView = LayoutInflater.from(context).inflate(unavailableWakeLayoutId, container, false);
                    container.addView(emptyView);
                }
                updatePieChart(pbQuality, pbQualityBackground, 0, true);
            }
        }
    }

    // Helper for PieChart - borrowing from original logic or create a helper
    // decorator
    private void updatePieChart(View qualityView, View backgroundView, int percentage, boolean isDisabled) {

        StatsViewGenerator.setupQualityPieArcChart(qualityView, backgroundView, percentage, isDisabled);
    }
}
