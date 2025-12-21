package com.ensao.mytime.statistics.view;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

/**
 * Abstract base class for statistics view generators and decorators.
 */
public abstract class StatsViewGenerator {

    public static void setupQualityPieArcChart(View qualityView, View backgroundView, int pourcentage,
            boolean isDisabled, int progressColor) {
        PieChart qualityPieArcChart = (PieChart) qualityView;
        PieChart backgroundPieChart = (PieChart) backgroundView;

        qualityPieArcChart.clear();
        backgroundPieChart.clear();

        // --- BACKGROUND CHART SETUP (Static Gray Arc) ---
        setupPieChartAppearance(backgroundPieChart);
        backgroundPieChart.setRotationAngle(180f);

        ArrayList<PieEntry> bgEntries = new ArrayList<>();
        bgEntries.add(new PieEntry(100f, "")); // Full arc

        PieDataSet bgDataSet = new PieDataSet(bgEntries, "");
        bgDataSet.setSliceSpace(0f);
        bgDataSet.setSelectionShift(0f);
        bgDataSet.setColors(new int[] { Color.parseColor("#7b7b7bff") }); // Gray color

        PieData bgData = new PieData(bgDataSet);
        bgData.setDrawValues(false);
        backgroundPieChart.setData(bgData);
        backgroundPieChart.invalidate(); // No animation for background

        // --- FOREGROUND CHART SETUP (Animated Progress) ---
        setupPieChartAppearance(qualityPieArcChart);
        qualityPieArcChart.setRotationAngle(180f);

        // 2. Prepare Data (Value and "The Rest")
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (!isDisabled) {
            entries.add(new PieEntry(pourcentage, "")); // The Progress
            entries.add(new PieEntry(100f - pourcentage, "")); // The Rest (Transparent)

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(0f);

            // 3. Colors (Use provided progressColor, TRANSPARENT for remainder)
            dataSet.setColors(new int[] {
                    progressColor, // Progress color (wake = #00b4d8, sleep = #540b0e)
                    Color.TRANSPARENT // Remainder is transparent to show background
            });

            PieData data = new PieData(dataSet);
            data.setDrawValues(false); // Hide numbers on the slice itself

            qualityPieArcChart.setData(data);

            // 4. Center Text (The "97%")
            qualityPieArcChart.setCenterText("Qualité\n" + pourcentage + "%");
            qualityPieArcChart.setCenterTextSize(24f);
            qualityPieArcChart.setCenterTextColor(Color.BLACK);

            // 5. Animation (The "Filling" effect)
            qualityPieArcChart.animateY(1400, Easing.EaseInOutQuad);
        } else {
            // Disabled State
            entries.add(new PieEntry(0f, ""));
            entries.add(new PieEntry(100f, ""));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(0f);
            dataSet.setSelectionShift(0f);

            dataSet.setColors(new int[] {
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
            });

            PieData data = new PieData(dataSet);
            data.setDrawValues(false);

            qualityPieArcChart.setData(data);

            // 4. Center Text
            qualityPieArcChart.setCenterText("Qualité\n?");
            qualityPieArcChart.setCenterTextSize(24f);
            qualityPieArcChart.setCenterTextColor(Color.GRAY);
            // No animation needed for disabled state effectively
            qualityPieArcChart.invalidate();
        }
    }

    private static void setupPieChartAppearance(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setHoleRadius(85f);
        chart.setTransparentCircleRadius(0f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setMaxAngle(180f);
        chart.setTouchEnabled(false);
    }
}
