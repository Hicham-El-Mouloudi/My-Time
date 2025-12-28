package com.ensao.mytime.statistics.view.addons;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudyStructureAddOn extends StatsViewGenerator {

    // Study-themed colors
    // private static final int COLOR_STUDY_PRIMARY = Color.parseColor("#4CAF50");
    private static final int COLOR_STUDY_PRIMARY = Color.parseColor("#808BC1");
    private static final int COLOR_COMPLETED = Color.parseColor("#4CAF50");
    private static final int COLOR_NOT_COMPLETED = Color.parseColor("#FF5722");

    public StudyStructureAddOn(View view, Map<String, Object> stats) {
        // Find Views
        TextView tvTotalFocusTime = view.findViewById(R.id.tv_total_focus_time_value);
        TextView tvStreakCount = view.findViewById(R.id.tv_streak_count_value);
        TextView tvPauseCount = view.findViewById(R.id.tv_pause_count_value);
        TextView tvSubjectsCount = view.findViewById(R.id.tv_subjects_count_value);
        TextView tvProductivitySummary = view.findViewById(R.id.tv_productivity_summary);
        PieChart chartSubjectDistribution = view.findViewById(R.id.chart_subject_distribution);
        PieChart chartCompletionDistribution = view.findViewById(R.id.chart_completion_distribution);
        BarChart chartWeeklySubjects = view.findViewById(R.id.chart_weekly_subjects);

        // Populate Total Focus Time
        if (tvTotalFocusTime != null) {
            Object focusTimeObj = stats.get("totalFocusTime");
            int focusTime = focusTimeObj instanceof Number ? ((Number) focusTimeObj).intValue() : 0;
            int hours = focusTime / 60;
            int minutes = focusTime % 60;
            if (hours > 0) {
                tvTotalFocusTime.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
            } else {
                tvTotalFocusTime.setText(String.format(Locale.getDefault(), "%d min", minutes));
            }
        }

        // Populate Streak Count
        if (tvStreakCount != null) {
            Object streakObj = stats.get("streakCount");
            int streak = streakObj instanceof Number ? ((Number) streakObj).intValue() : 0;
            tvStreakCount.setText(String.format(Locale.getDefault(), "%d %s", streak,
                    tvStreakCount.getContext().getString(R.string.stats_days_suffix)));
        }

        // Populate Pause Count
        if (tvPauseCount != null) {
            Object pauseObj = stats.get("pauseCount");
            float pauseCount = pauseObj instanceof Number ? ((Number) pauseObj).floatValue() : 0;
            tvPauseCount.setText(String.format(Locale.getDefault(), "%.1f", pauseCount));
        }

        // Populate Subjects Count
        if (tvSubjectsCount != null) {
            Object subjectsObj = stats.get("subjectsStudiedCount");
            int subjects = subjectsObj instanceof Number ? ((Number) subjectsObj).intValue() : 0;
            tvSubjectsCount.setText(String.valueOf(subjects));
        }

        // Populate Productivity Summary
        if (tvProductivitySummary != null) {
            Object focusTimeObj = stats.get("totalFocusTime");
            int focusTime = focusTimeObj instanceof Number ? ((Number) focusTimeObj).intValue() : 0;
            String summary;
            if (focusTime >= 180) {
                summary = tvProductivitySummary.getContext().getString(R.string.stats_prod_excellent);
            } else if (focusTime >= 90) {
                summary = tvProductivitySummary.getContext().getString(R.string.stats_prod_good_job);
            } else if (focusTime > 0) {
                summary = tvProductivitySummary.getContext().getString(R.string.stats_prod_keep_going);
            } else {
                summary = tvProductivitySummary.getContext().getString(R.string.stats_prod_start);
            }
            tvProductivitySummary.setText(summary);
        }

        // Setup Subject Distribution Pie Chart
        if (chartSubjectDistribution != null) {
            setupSubjectDistributionChart(chartSubjectDistribution, stats);
        }

        // Setup Completion Distribution Pie Chart
        if (chartCompletionDistribution != null) {
            setupCompletionDistributionChart(chartCompletionDistribution, stats);
        }

        // Setup Weekly Progress Bar Chart
        if (chartWeeklySubjects != null) {
            setupWeeklySubjectsChart(chartWeeklySubjects, stats);
        }
    }

    private void setupSubjectDistributionChart(PieChart chart, Map<String, Object> stats) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> distribution = (Map<String, Integer>) stats.get("subjectDistribution");

        if (distribution == null || distribution.isEmpty()) {
            chart.setNoDataText(chart.getContext().getString(R.string.stats_no_data));
            chart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        chart.setData(data);

        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(40f);
        chart.setTransparentCircleRadius(45f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.getLegend().setEnabled(true);
        int chartTextColor = chart.getContext().getResources().getColor(R.color.chart_text_color);
        chart.getLegend().setTextColor(chartTextColor);
        chart.setEntryLabelColor(chartTextColor);
        chart.setTouchEnabled(false);
        // Make the chart bigger within its container by using negative offsets
        chart.setExtraOffsets(-10f, -10f, -10f, -10f);
        chart.animateY(1100);
        chart.invalidate();
    }

    private void setupCompletionDistributionChart(PieChart chart, Map<String, Object> stats) {
        Object completedObj = stats.get("completedTasksCount");
        Object totalObj = stats.get("totalTasksCount");

        int completed = completedObj instanceof Number ? ((Number) completedObj).intValue() : 0;
        int total = totalObj instanceof Number ? ((Number) totalObj).intValue() : 0;
        int notCompleted = Math.max(0, total - completed);

        if (total == 0) {
            chart.setNoDataText(chart.getContext().getString(R.string.stats_chart_no_tasks));
            chart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (completed > 0) {
            entries.add(new PieEntry(completed, chart.getContext().getString(R.string.stats_chart_completed)));
        }
        if (notCompleted > 0) {
            entries.add(new PieEntry(notCompleted, chart.getContext().getString(R.string.stats_chart_in_progress)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[] { COLOR_COMPLETED, COLOR_NOT_COMPLETED });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        chart.setData(data);

        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(40f);
        chart.setTransparentCircleRadius(45f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.getLegend().setEnabled(true);
        int chartTextColor = chart.getContext().getResources().getColor(R.color.chart_text_color);
        chart.getLegend().setTextColor(chartTextColor);
        chart.setEntryLabelColor(chartTextColor);
        chart.setCenterTextColor(chartTextColor);
        chart.setTouchEnabled(false);
        chart.setCenterText(String.format(Locale.getDefault(), "%d%%", total > 0 ? (completed * 100 / total) : 0));
        chart.setCenterTextSize(16f);
        // making the chart a little bigger
        chart.setExtraOffsets(-10f, -10f, -10f, -10f);
        chart.animateY(1100);
        chart.invalidate();
    }

    private void setupWeeklySubjectsChart(BarChart chart, Map<String, Object> stats) {
        @SuppressWarnings("unchecked")
        List<Integer> weeklyData = (List<Integer>) stats.get("weeklySubjectsStudied");

        if (weeklyData == null || weeklyData.isEmpty()) {
            chart.setNoDataText(chart.getContext().getString(R.string.stats_no_data));
            chart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        int count = Math.min(weeklyData.size(), 7);
        for (int i = 0; i < count; i++) {
            Integer val = weeklyData.get(i);
            entries.add(new BarEntry(i, val != null ? val : 0));
        }

        BarDataSet dataSet = new BarDataSet(entries,
                chart.getContext().getString(R.string.stats_chart_subjects_studied));
        dataSet.setColor(COLOR_STUDY_PRIMARY);
        dataSet.setValueTextSize(10f);
        int chartTextColor = chart.getContext().getResources().getColor(R.color.chart_text_color);
        dataSet.setValueTextColor(chartTextColor);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(chartTextColor);
        final String[] days = {
                chart.getContext().getString(R.string.date_mon),
                chart.getContext().getString(R.string.date_tue),
                chart.getContext().getString(R.string.date_wed),
                chart.getContext().getString(R.string.date_thu),
                chart.getContext().getString(R.string.date_fri),
                chart.getContext().getString(R.string.date_sat),
                chart.getContext().getString(R.string.date_sun)
        };
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < days.length) {
                    return days[index];
                }
                return "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(chartTextColor);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
