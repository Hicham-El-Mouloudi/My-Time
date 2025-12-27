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
import com.ensao.mytime.statistics.view.addons.StudyStructureAddOn;
import com.ensao.mytime.statistics.view.addons.StudyStyleAddOn;
import com.ensao.mytime.statistics.view.addons.WakeStructureAddOn;
import com.ensao.mytime.statistics.view.addons.WakeStyleAddOn;

import java.util.Map;

import android.graphics.Color;

public class DayStatisticsViewBuilder {

    // Tab type enum
    private static final int TAB_SLEEP = 0;
    private static final int TAB_WAKE = 1;
    private static final int TAB_STUDY = 2;

    private final Context context;
    private final ViewGroup parent;
    private DayData day;

    private int sleepLayoutId;
    private int wakeLayoutId;
    private int studyLayoutId;
    private int unavailableSleepLayoutId;
    private int unavailableWakeLayoutId;
    private int unavailableStudyLayoutId;
    private int unavailableDataLayoutId;

    private Map<String, Object> sleepStats;
    private Map<String, Object> wakeStats;
    private Map<String, Object> studyStats;

    private boolean preferSleep = true;

    // Chart colors
    private static final int COLOR_WAKE = Color.parseColor("#90e0ef");
    private static final int COLOR_SLEEP = Color.parseColor("#540b0e");
    private static final int COLOR_STUDY = Color.parseColor("#4CAF50");

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

    public DayStatisticsViewBuilder setAvailableStudyDataLayout(int layoutId) {
        this.studyLayoutId = layoutId;
        return this;
    }

    public DayStatisticsViewBuilder setUnavailableStudyDataLayout(int layoutId) {
        this.unavailableStudyLayoutId = layoutId;
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

    public DayStatisticsViewBuilder useStudyStats(Map<String, Object> stats) {
        this.studyStats = stats;
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

        // Determine initial tab (default to wake like before)
        int initialTab = TAB_WAKE;
        boolean hasSleep = (day.hasSleep() && sleepStats != null);
        boolean hasWake = (day.hasWake() && wakeStats != null);
        boolean hasStudy = (day.hasStudy() && studyStats != null);

        if (preferSleep && hasSleep) {
            initialTab = TAB_SLEEP;
        } else if (hasWake) {
            initialTab = TAB_WAKE;
        } else if (hasSleep) {
            initialTab = TAB_SLEEP;
        } else if (hasStudy) {
            initialTab = TAB_STUDY;
        }

        View mainView = LayoutInflater.from(context).inflate(R.layout.layout_statistics_content, parent, false);
        setupTabsAndContent(mainView, initialTab);
        return mainView;
    }

    private void setupTabsAndContent(View mainView, int initialTab) {
        Button btnSleep = mainView.findViewById(R.id.btn_toggle_sommeil);
        Button btnWake = mainView.findViewById(R.id.btn_toggle_reveil);
        Button btnStudy = mainView.findViewById(R.id.btn_toggle_etudes);
        LinearLayout statsContainer = mainView.findViewById(R.id.dynamic_stats_container);
        View pbQuality = mainView.findViewById(R.id.qualityChart);
        View pbQualityBackground = mainView.findViewById(R.id.qualityChartBackground);
        android.widget.ImageView headerBackground = mainView.findViewById(R.id.header_background_image);

        // Click listeners
        btnSleep.setOnClickListener(v -> updateTab(TAB_SLEEP, btnSleep, btnWake, btnStudy,
                statsContainer, pbQuality, pbQualityBackground, headerBackground));
        btnWake.setOnClickListener(v -> updateTab(TAB_WAKE, btnSleep, btnWake, btnStudy,
                statsContainer, pbQuality, pbQualityBackground, headerBackground));
        btnStudy.setOnClickListener(v -> updateTab(TAB_STUDY, btnSleep, btnWake, btnStudy,
                statsContainer, pbQuality, pbQualityBackground, headerBackground));

        // Initial state
        updateTab(initialTab, btnSleep, btnWake, btnStudy, statsContainer, pbQuality, pbQualityBackground,
                headerBackground);
    }

    private void updateTab(int tabType, Button btnSleep, Button btnWake, Button btnStudy,
            LinearLayout container, View pbQuality, View pbQualityBackground,
            android.widget.ImageView headerBackground) {
        container.removeAllViews();

        int primaryColor = context.getResources().getColor(R.color.primary_color);
        int whiteColor = context.getResources().getColor(R.color.white);
        int transparentColor = android.graphics.Color.TRANSPARENT;

        // Reset all buttons to inactive
        btnSleep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(transparentColor));
        btnSleep.setTextColor(context.getResources().getColor(R.color.text_secondary));
        btnSleep.refreshDrawableState();
        btnWake.setBackgroundTintList(android.content.res.ColorStateList.valueOf(transparentColor));
        btnWake.setTextColor(context.getResources().getColor(R.color.text_secondary));
        btnWake.refreshDrawableState();
        btnStudy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(transparentColor));
        btnStudy.setTextColor(context.getResources().getColor(R.color.text_secondary));
        btnStudy.refreshDrawableState();

        // Set active button
        Button activeBtn = null;
        int bannerResource = R.drawable.sleepillustration;
        switch (tabType) {
            case TAB_SLEEP:
                activeBtn = btnSleep;
                bannerResource = R.drawable.sleepillustration;
                break;
            case TAB_WAKE:
                activeBtn = btnWake;
                bannerResource = R.drawable.wakeillustration;
                break;
            case TAB_STUDY:
                activeBtn = btnStudy;
                bannerResource = R.drawable.studyingillustration;
                break;
        }

        if (activeBtn != null) {
            activeBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            activeBtn.setTextColor(whiteColor);
        }

        if (headerBackground != null) {
            headerBackground.setImageResource(bannerResource);
        }

        // Content Generation based on tab type
        switch (tabType) {
            case TAB_SLEEP:
                renderSleepContent(container, pbQuality, pbQualityBackground);
                break;
            case TAB_WAKE:
                renderWakeContent(container, pbQuality, pbQualityBackground);
                break;
            case TAB_STUDY:
                renderStudyContent(container, pbQuality, pbQualityBackground);
                break;
        }
    }

    private void renderSleepContent(LinearLayout container, View pbQuality, View pbQualityBackground) {
        if (day.hasSleep() && sleepStats != null) {
            View sleepView = LayoutInflater.from(context).inflate(sleepLayoutId, container, false);
            container.addView(sleepView);
            new SleepStructureAddOn(sleepView, sleepStats);
            new SleepStyleAddOn(sleepView);
            int quality = calculateSleepQuality(day.getSleepDuration());
            updatePieChart(pbQuality, pbQualityBackground, quality, false, COLOR_SLEEP);
        } else {
            if (unavailableSleepLayoutId != 0) {
                View emptyView = LayoutInflater.from(context).inflate(unavailableSleepLayoutId, container, false);
                container.addView(emptyView);
            }
            updatePieChart(pbQuality, pbQualityBackground, 0, true, COLOR_SLEEP);
        }
    }

    private void renderWakeContent(LinearLayout container, View pbQuality, View pbQualityBackground) {
        if (day.hasWake() && wakeStats != null) {
            View wakeView = LayoutInflater.from(context).inflate(wakeLayoutId, container, false);
            container.addView(wakeView);
            new WakeStructureAddOn(wakeView, wakeStats);
            new WakeStyleAddOn(wakeView);
            int quality = calculateWakeQuality(day.getWakeLatency());
            updatePieChart(pbQuality, pbQualityBackground, quality, false, COLOR_WAKE);
        } else {
            if (unavailableWakeLayoutId != 0) {
                View emptyView = LayoutInflater.from(context).inflate(unavailableWakeLayoutId, container, false);
                container.addView(emptyView);
            }
            updatePieChart(pbQuality, pbQualityBackground, 0, true, COLOR_WAKE);
        }
    }

    private void renderStudyContent(LinearLayout container, View pbQuality, View pbQualityBackground) {
        if (day.hasStudy() && studyStats != null) {
            View studyView = LayoutInflater.from(context).inflate(studyLayoutId, container, false);
            container.addView(studyView);
            new StudyStructureAddOn(studyView, studyStats);
            new StudyStyleAddOn(studyView);
            int quality = calculateStudyQuality(day.getTotalFocusTime());
            updatePieChart(pbQuality, pbQualityBackground, quality, false, COLOR_STUDY);
        } else {
            if (unavailableStudyLayoutId != 0) {
                View emptyView = LayoutInflater.from(context).inflate(unavailableStudyLayoutId, container, false);
                container.addView(emptyView);
            }
            updatePieChart(pbQuality, pbQualityBackground, 0, true, COLOR_STUDY);
        }
    }

    private int calculateSleepQuality(float hoursSlept) {
        float normalRange = 8.0f;
        float quality = hoursSlept / normalRange;
        if (quality > 1.0f)
            quality = 1.0f;
        return (int) (quality * 100);
    }

    private int calculateWakeQuality(int wakeLatencyMins) {
        float normalThreshold = 10.0f;
        if (wakeLatencyMins <= 0)
            wakeLatencyMins = 1;
        float quality = normalThreshold / (float) wakeLatencyMins;
        if (quality > 1.0f)
            quality = 1.0f;
        return (int) (quality * 100);
    }

    private int calculateStudyQuality(int focusTimeMinutes) {
        // Target: 120 minutes (2 hours) of focused study = 100%
        float target = 120.0f;
        float quality = focusTimeMinutes / target;
        if (quality > 1.0f)
            quality = 1.0f;
        return (int) (quality * 100);
    }

    private void updatePieChart(View qualityView, View backgroundView, int percentage, boolean isDisabled, int color) {
        StatsViewGenerator.setupQualityPieArcChart(qualityView, backgroundView, percentage, isDisabled, color);
    }
}
