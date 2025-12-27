package com.ensao.mytime.statistics.view.addons;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.ensao.mytime.R;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WakeStructureAddOn extends StatsViewGenerator {

    public WakeStructureAddOn(View view, Map<String, Object> stats) {
        // Find Views
        TextView tvLatency = view.findViewById(R.id.tv_wake_latency_value);
        TextView tvRingCount = view.findViewById(R.id.tv_ring_count_value);
        TextView tvTimeVariability = view.findViewById(R.id.tv_time_variability_value);
        TextView tvFirstAlarm = view.findViewById(R.id.tv_first_alarm_value);
        TextView tvLastOff = view.findViewById(R.id.tv_last_off_value);
        TextView tvWakeDuration = view.findViewById(R.id.tv_wake_duration_value);
        TextView tvAverageWake = view.findViewById(R.id.tv_average_wake_time_value);
        LineChart chart = view.findViewById(R.id.chart_wake_weekly);

        // Populate Text with proper type casting
        if (tvLatency != null) {
            Object latencyObj = stats.get("wakeLatency");
            float latency = latencyObj instanceof Number ? ((Number) latencyObj).floatValue() : 0;
            tvLatency.setText(String.format(Locale.getDefault(), "%.1f min", latency));
        }

        if (tvRingCount != null)
            tvRingCount.setText(String.valueOf(stats.get("ringCount")));

        if (tvTimeVariability != null) {
            Object variabilityObj = stats.get("timeVariability");
            float variability = variabilityObj instanceof Number ? ((Number) variabilityObj).floatValue() : 0;
            tvTimeVariability.setText(String.format(Locale.getDefault(), "%.1f min", variability));
        }

        if (tvFirstAlarm != null)
            tvFirstAlarm.setText((String) stats.get("firstAlarm"));

        if (tvLastOff != null)
            tvLastOff.setText((String) stats.get("lastOff"));

        if (tvWakeDuration != null) {
            Object durationObj = stats.get("wakeDuration");
            float duration = durationObj instanceof Number ? ((Number) durationObj).floatValue() : 0;
            tvWakeDuration.setText(String.format(Locale.getDefault(), "%.1f min", duration));
        }

        if (tvAverageWake != null)
            tvAverageWake.setText((String) stats.get("averageWakeTime"));

        // Populate Chart
        if (chart != null) {
            setupChart(chart, stats);
        }
    }

    private void setupChart(LineChart chart, Map<String, Object> stats) {
        List<Float> varianceData = (List<Float>) stats.get("last7DaysGraph");

        if (varianceData == null || varianceData.isEmpty()) {
            chart.setNoDataText("Pas de données disponibles");
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        int count = Math.min(varianceData.size(), 7);
        float sum = 0;
        int validCount = 0;

        for (int i = 0; i < count; i++) {
            Float val = varianceData.get(i);
            if (val != null) {
                entries.add(new Entry(i, val));
                sum += val;
                validCount++;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heure de réveil");
        dataSet.setColor(Color.parseColor("#00BCD4"));
        dataSet.setCircleColor(Color.parseColor("#00BCD4"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        int chartTextColor = chart.getContext().getResources().getColor(R.color.chart_text_color);
        xAxis.setTextColor(chartTextColor);
        final String[] days = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
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
        leftAxis.setGranularity(0.5f);
        leftAxis.setTextColor(chartTextColor);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int h = (int) value;
                int m = (int) ((value - h) * 60);
                return String.format(Locale.getDefault(), "%02d:%02d", h, Math.abs(m));
            }
        });

        if (validCount > 0) {
            float average = sum / validCount;
            LimitLine avgLine = new LimitLine(average, "Moyenne");
            avgLine.setLineWidth(1f);
            avgLine.setLineColor(Color.GRAY);
            avgLine.enableDashedLine(10f, 10f, 0f);
            avgLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            avgLine.setTextSize(10f);
            avgLine.setTextColor(chartTextColor);
            leftAxis.removeAllLimitLines(); // Clear previous
            leftAxis.addLimitLine(avgLine);
        }

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.invalidate();
    }
}
