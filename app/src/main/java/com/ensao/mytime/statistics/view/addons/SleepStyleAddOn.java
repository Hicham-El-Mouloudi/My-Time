package com.ensao.mytime.statistics.view.addons;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.ensao.mytime.R;
import com.ensao.mytime.statistics.view.StatsViewGenerator;

public class SleepStyleAddOn extends StatsViewGenerator {

    public SleepStyleAddOn(View view) {
        // Example styling logic: Change status text color
        TextView tvDurationStatus = view.findViewById(R.id.tv_sleep_duration_status);
        if (tvDurationStatus != null) {
            tvDurationStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        }
    }
}
