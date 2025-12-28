package com.ensao.mytime.statistics.view.addons;

import android.view.View;
import android.widget.TextView;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.data.WakeWhileSleepingDuration;
import com.ensao.mytime.statistics.view.SleepTimelineChartHelper;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import com.github.mikephil.charting.charts.HorizontalBarChart;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SleepStructureAddOn extends StatsViewGenerator {

    private enum SleepStatus {
        MAUVAIS(R.string.stats_status_bad),
        MOYEN(R.string.stats_status_medium),
        BON(R.string.stats_status_good);

        private final int labelResId;

        SleepStatus(int labelResId) {
            this.labelResId = labelResId;
        }

        public int getLabelResId() {
            return labelResId;
        }
    }

    public SleepStructureAddOn(View view, Map<String, Object> stats) {
        // Find Views
        TextView tvDuration = view.findViewById(R.id.tv_sleep_duration_value);
        TextView tvTimeInBed = view.findViewById(R.id.tv_time_in_bed_value);
        TextView tvDurationStatus = view.findViewById(R.id.tv_sleep_duration_status);
        TextView tvSleepLatency = view.findViewById(R.id.tv_sleep_latency_value);
        TextView tvWakeDuringSleep = view.findViewById(R.id.tv_wake_during_sleep_value);

        // Populate Data
        if (tvDuration != null) {
            Object durationObj = stats.get("sleepDuration");
            float duration = 0;
            if (durationObj instanceof Number) {
                duration = ((Number) durationObj).floatValue();
            }
            if (duration < 1.0f) {
                // Display in minutes if less than 1 hour
                int minutes = Math.round(duration * 60);
                tvDuration.setText(String.format(Locale.getDefault(), "%d min", minutes));
            } else {
                tvDuration.setText(String.format(Locale.getDefault(), "%.1f h", duration));
            }

            // Set Status
            if (tvDurationStatus != null) {
                SleepStatus status = getSleepStatus(duration);
                tvDurationStatus.setText(tvDurationStatus.getContext().getString(status.getLabelResId()));
            }
        }

        if (tvTimeInBed != null) {
            Object timeInBedObj = stats.get("timeInBed");
            float timeInBed = 0;
            if (timeInBedObj instanceof Number) {
                timeInBed = ((Number) timeInBedObj).floatValue();
            }
            if (timeInBed < 1.0f) {
                int minutes = Math.round(timeInBed * 60);
                tvTimeInBed.setText(String.format(Locale.getDefault(), "%d min", minutes));
            } else {
                tvTimeInBed.setText(String.format(Locale.getDefault(), "%.1f h", timeInBed));
            }
        }

        if (tvSleepLatency != null) {
            Object sleepLatencyObj = stats.get("sleepLatency");
            int sleepLatency = 0;
            if (sleepLatencyObj instanceof Number) {
                sleepLatency = ((Number) sleepLatencyObj).intValue();
            }
            tvSleepLatency.setText(String.format(Locale.getDefault(), "%d min", sleepLatency));
        }

        if (tvWakeDuringSleep != null) {
            Object wakeDuringObj = stats.get("wakeDuringSleep");
            int wakeDuringSleep = 0;
            if (wakeDuringObj instanceof Number) {
                wakeDuringSleep = ((Number) wakeDuringObj).intValue();
            }
            tvWakeDuringSleep.setText(String.format(Locale.getDefault(), "%d min", wakeDuringSleep));
        }

        // Setup Sleep Timeline Chart
        HorizontalBarChart chart = view.findViewById(R.id.chart_sleep_timeline);
        if (chart != null) {
            // Get bed time and wake time from stats
            Object bedTimeObj = stats.get("bedTime");
            Object wakeTimeObj = stats.get("wakeTime");
            Object wakeDistributionObj = stats.get("wakeDuringSleepDistribution");

            long bedTime = (bedTimeObj instanceof Number) ? ((Number) bedTimeObj).longValue()
                    : System.currentTimeMillis() - 8 * 3600000;
            long wakeTime = (wakeTimeObj instanceof Number) ? ((Number) wakeTimeObj).longValue()
                    : System.currentTimeMillis();

            @SuppressWarnings("unchecked")
            List<WakeWhileSleepingDuration> wakeDistribution = (wakeDistributionObj instanceof List)
                    ? (List<WakeWhileSleepingDuration>) wakeDistributionObj
                    : null;

            SleepTimelineChartHelper chartHelper = new SleepTimelineChartHelper(chart);
            chartHelper.setupChart(bedTime, wakeTime, wakeDistribution);
        }
    }

    private SleepStatus getSleepStatus(float duration) {
        if (duration < 6.0f) {
            return SleepStatus.MAUVAIS;
        } else if (duration <= 7.0f) {
            return SleepStatus.MOYEN;
        } else {
            return SleepStatus.BON;
        }
    }
}
