package com.ensao.mytime.statistics.view;

import android.graphics.Color;

import com.ensao.mytime.statistics.data.WakeWhileSleepingDuration;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to setup a HorizontalBarChart as a Gantt-style sleep timeline.
 */
public class SleepTimelineChartHelper {

    private static final int COLOR_SLEEP = Color.parseColor("#0077b6");
    private static final int COLOR_WAKE = Color.parseColor("#90e0ef");

    private final HorizontalBarChart chart;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public SleepTimelineChartHelper(HorizontalBarChart chart) {
        this.chart = chart;
    }

    public void setupChart(long bedTime, long wakeTime, List<WakeWhileSleepingDuration> wakeIntervals) {
        if (chart == null)
            return;

        // Calculate session duration in minutes
        float sessionDurationMinutes = (wakeTime - bedTime) / 60000f;
        if (sessionDurationMinutes <= 0) {
            sessionDurationMinutes = 480f; // Default 8 hours
        }

        // Convert wake intervals to minute offsets
        List<long[]> wakeMinuteIntervals = convertToMinuteIntervals(bedTime, wakeIntervals);
        Collections.sort(wakeMinuteIntervals, Comparator.comparingLong(a -> a[0]));

        // Build stacked values
        List<Float> stackValues = new ArrayList<>();
        List<Integer> stackColors = new ArrayList<>();

        long currentPos = 0;
        for (long[] wakeInterval : wakeMinuteIntervals) {
            long wakeStart = wakeInterval[0];
            long wakeEnd = wakeInterval[1];

            if (wakeStart > currentPos) {
                stackValues.add((float) (wakeStart - currentPos));
                stackColors.add(COLOR_SLEEP);
            }

            if (wakeEnd > wakeStart) {
                stackValues.add((float) (wakeEnd - wakeStart));
                stackColors.add(COLOR_WAKE);
            }

            currentPos = wakeEnd;
        }

        if (currentPos < sessionDurationMinutes) {
            stackValues.add(sessionDurationMinutes - currentPos);
            stackColors.add(COLOR_SLEEP);
        }

        if (stackValues.isEmpty()) {
            stackValues.add(sessionDurationMinutes);
            stackColors.add(COLOR_SLEEP);
        }

        // Convert to arrays
        float[] stackValuesArray = new float[stackValues.size()];
        int[] stackColorsArray = new int[stackColors.size()];
        for (int i = 0; i < stackValues.size(); i++) {
            stackValuesArray[i] = stackValues.get(i);
            stackColorsArray[i] = stackColors.get(i);
        }

        // Create bar entry
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, stackValuesArray));

        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setColors(stackColorsArray);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        // Set data first
        chart.setData(barData);

        // Chart settings
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setFitBars(true);
        chart.setExtraTopOffset(40f);
        chart.setExtraLeftOffset(15f);
        chart.setExtraRightOffset(15f);
        chart.setExtraBottomOffset(20f);

        // Detect current UI mode (Day/Night)
        int currentNightMode = chart.getContext().getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        int textColor = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE
                : Color.BLACK;

        // Custom Legend - use dynamic text color
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(textColor);
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setFormSize(14f);
        legend.setFormToTextSpace(8f);
        legend.setXEntrySpace(30f);
        legend.setYOffset(5f);

        // Create custom legend entries with explicit constructor
        LegendEntry sleepLegend = new LegendEntry("Sleep", Legend.LegendForm.SQUARE, 14f, 2f, null, COLOR_SLEEP);
        LegendEntry awakeLegend = new LegendEntry("Awake", Legend.LegendForm.SQUARE, 14f, 2f, null, COLOR_WAKE);
        legend.setCustom(new LegendEntry[] { sleepLegend, awakeLegend });

        // X-axis (row labels - hidden)
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        // Left Y-axis (time axis - shows HH:mm)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisLineColor(Color.parseColor("#888888"));
        leftAxis.setTextColor(textColor);
        leftAxis.setTextSize(12f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(sessionDurationMinutes);
        leftAxis.setLabelCount(5, true);
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                long timeMillis = bedTime + (long) (value * 60000);
                return timeFormat.format(new Date(timeMillis));
            }
        });

        // Right Y-axis (disabled)
        chart.getAxisRight().setEnabled(false);

        chart.invalidate();
    }

    private List<long[]> convertToMinuteIntervals(long bedTime, List<WakeWhileSleepingDuration> wakeIntervals) {
        List<long[]> result = new ArrayList<>();
        if (wakeIntervals == null)
            return result;

        for (WakeWhileSleepingDuration wake : wakeIntervals) {
            try {
                long startMinutes = parseTimeToMinutes(bedTime, wake.getStartTime());
                long endMinutes = parseTimeToMinutes(bedTime, wake.getEndTime());
                if (startMinutes >= 0 && endMinutes > startMinutes) {
                    result.add(new long[] { startMinutes, endMinutes });
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private long parseTimeToMinutes(long bedTime, String timeStr) {
        if (timeStr == null || !timeStr.contains(":"))
            return -1;
        try {
            Date parsed = timeFormat.parse(timeStr);
            if (parsed == null)
                return -1;

            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.setTimeInMillis(bedTime);

            java.util.Calendar parsedCal = java.util.Calendar.getInstance();
            parsedCal.setTime(parsed);

            timeCal.set(java.util.Calendar.HOUR_OF_DAY, parsedCal.get(java.util.Calendar.HOUR_OF_DAY));
            timeCal.set(java.util.Calendar.MINUTE, parsedCal.get(java.util.Calendar.MINUTE));

            if (timeCal.getTimeInMillis() < bedTime) {
                timeCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }

            return (timeCal.getTimeInMillis() - bedTime) / 60000;
        } catch (Exception e) {
            return -1;
        }
    }
}
