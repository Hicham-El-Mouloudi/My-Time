package com.ensao.mytime.statistics.view.addons;

import android.view.View;
import android.widget.TextView;
import com.ensao.mytime.R;
import com.ensao.mytime.statistics.view.StatsViewGenerator;
import java.util.Locale;
import java.util.Map;

public class SleepStructureAddOn extends StatsViewGenerator {

    private enum SleepStatus {
        MAUVAIS("Mauvais"),
        MOYEN("Moyen"),
        BON("Bon");

        private final String label;

        SleepStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public SleepStructureAddOn(View view, Map<String, Object> stats) {
        // Find Views
        TextView tvDuration = view.findViewById(R.id.tv_sleep_duration_value);
        TextView tvTimeInBed = view.findViewById(R.id.tv_time_in_bed_value);
        TextView tvDurationStatus = view.findViewById(R.id.tv_sleep_duration_status);

        // Populate Data
        if (tvDuration != null) {
            Object durationObj = stats.get("sleepDuration");
            float duration = 0;
            if (durationObj instanceof Number) {
                duration = ((Number) durationObj).floatValue();
            }
            tvDuration.setText(String.format(Locale.getDefault(), "%.1f h", duration));

            // Set Status
            if (tvDurationStatus != null) {
                SleepStatus status = getSleepStatus(duration);
                tvDurationStatus.setText(status.getLabel());
            }
        }

        if (tvTimeInBed != null) {
            Object timeInBedObj = stats.get("timeInBed");
            float timeInBed = 0;
            if (timeInBedObj instanceof Number) {
                timeInBed = ((Number) timeInBedObj).floatValue();
            }
            tvTimeInBed.setText(String.format(Locale.getDefault(), "%.1f h", timeInBed));
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
