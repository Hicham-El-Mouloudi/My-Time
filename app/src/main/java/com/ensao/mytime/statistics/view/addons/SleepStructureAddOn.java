package com.ensao.mytime.statistics.view.addons;

import android.view.View;
import android.widget.TextView;
import com.ensao.mytime.R;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import java.util.Map;

public class SleepStructureAddOn extends StatsViewGenerator {

    public SleepStructureAddOn(View view, Map<String, Object> stats) {
        // Find Views
        TextView tvDuration = view.findViewById(R.id.tv_sleep_duration_value);
        TextView tvEfficiency = view.findViewById(R.id.tv_sleep_efficiency_value);
        TextView tvTimeInBed = view.findViewById(R.id.tv_time_in_bed_value);
        TextView tvLatency = view.findViewById(R.id.tv_sleep_latency_value);
        TextView tvWakeDuringSleep = view.findViewById(R.id.tv_wake_during_sleep_value);

        TextView tvDurationStatus = view.findViewById(R.id.tv_sleep_duration_status);
        TextView tvEfficiencyStatus = view.findViewById(R.id.tv_sleep_efficiency_status);

        // Populate Data
        if (tvDuration != null)
            tvDuration.setText(stats.get("sleepDuration") + " h");
        if (tvEfficiency != null)
            tvEfficiency.setText(stats.get("sleepEfficiency") + " %");
        if (tvTimeInBed != null)
            tvTimeInBed.setText(stats.get("timeInBed") + " h");
        if (tvLatency != null)
            tvLatency.setText(stats.get("sleepLatency") + " min");
        if (tvWakeDuringSleep != null)
            tvWakeDuringSleep.setText(stats.get("wakeDuringSleep") + " min");

        if (tvDurationStatus != null)
            tvDurationStatus.setText("Bon"); // Logic for status could be more complex
        if (tvEfficiencyStatus != null)
            tvEfficiencyStatus.setText("Bon");
    }
}
