package com.ensao.mytime.statistics.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.util.Map;

public class StatsViewGenerator {

    public View generateSleepView(Context context, Map<String, Object> stats) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Diagnostic du sommeil
        container.addView(createSectionTitle(context, "Diagnostic du sommeil"));
        container.addView(createCard(context, "Durée du sommeil", stats.get("sleepDuration") + " h", "Bon"));
        container.addView(createCard(context, "Efficacité du sommeil", stats.get("sleepEfficiency") + " %", "Bon"));

        // Durée de sommeil nette
        container.addView(createSectionTitle(context, "Durée de sommeil nette"));
        container.addView(createCard(context, "Temps passé au lit", stats.get("timeInBed") + " h", null));
        container.addView(createCard(context, "Latence", stats.get("sleepLatency") + " min", null));
        container.addView(createCard(context, "Réveil pendant sommeil", stats.get("wakeDuringSleep") + " min", null));

        return container;
    }

    public View generateWakeView(Context context, Map<String, Object> stats) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Wake Stats
        container.addView(createCard(context, "Latence", stats.get("wakeLatency") + " min", null));
        container.addView(createCard(context, "Nombre de Sonneries", String.valueOf(stats.get("ringCount")), null));
        container.addView(createCard(context, "Variabilité du temps", stats.get("timeVariability") + " min", null));

        // Horizontal Cards
        LinearLayout horizontalContainer = new LinearLayout(context);
        horizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
        horizontalContainer.setWeightSum(2);

        View firstAlarm = createCard(context, "Premier Alarme", (String) stats.get("firstAlarm"), null);
        ((LinearLayout.LayoutParams) firstAlarm.getLayoutParams()).weight = 1;
        horizontalContainer.addView(firstAlarm);

        View lastOff = createCard(context, "Derniere Extinction", (String) stats.get("lastOff"), null);
        ((LinearLayout.LayoutParams) lastOff.getLayoutParams()).weight = 1;
        horizontalContainer.addView(lastOff);

        container.addView(horizontalContainer);

        container.addView(createCard(context, "Durée du réveil", stats.get("wakeDuration") + " min", null));

        // Graph Placeholder
        container.addView(createSectionTitle(context, "Variance (Last 7 Days)"));

        // Simple Bar Graph
        LinearLayout graphContainer = new LinearLayout(context);
        graphContainer.setOrientation(LinearLayout.HORIZONTAL);
        graphContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300)); // Fixed height
        graphContainer.setGravity(Gravity.BOTTOM);
        graphContainer.setPadding(16, 16, 16, 16);
        graphContainer.setBackgroundColor(Color.parseColor("#F5F7FA")); // Light background

        java.util.List<Float> varianceData = (java.util.List<Float>) stats.get("last7DaysGraph");
        if (varianceData != null) {
            float max = 0;
            for (float v : varianceData)
                max = Math.max(max, v);
            if (max == 0)
                max = 1; // Avoid divide by zero

            for (float val : varianceData) {
                View bar = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1;
                params.setMargins(4, 0, 4, 0);

                // Calculate height percentage
                int heightPercent = (int) ((val / max) * 100);
                params.height = 0; // Reset for weight, but we need to set height dynamically via layout params or
                                   // weight?
                // Actually, for a bottom gravity linear layout, we can set height.
                params.height = (int) ((val / max) * 250); // Scale to container height approx
                params.width = 0; // using weight

                // Better approach for bars in LinearLayout with Gravity.BOTTOM:
                // Use a container for each bar to control height?
                // Let's just set the height directly.
                params = new LinearLayout.LayoutParams(0, (int) ((val / max) * 200)); // Max 200px height
                params.weight = 1;
                params.setMargins(8, 0, 8, 0);

                bar.setLayoutParams(params);
                bar.setBackgroundColor(Color.parseColor("#4A90E2")); // Primary Color
                graphContainer.addView(bar);
            }
        }
        container.addView(graphContainer);

        container.addView(createCard(context, "Heure moyenne au réveil", (String) stats.get("averageWakeTime"), null));

        return container;
    }

    private View createCard(Context context, String title, String value, String status) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16, 16, 16);
        card.setLayoutParams(params);
        card.setRadius(16);
        card.setCardElevation(8);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 32, 32, 32);
        content.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.GRAY);
        content.addView(tvTitle);

        TextView tvValue = new TextView(context);
        tvValue.setText(value);
        tvValue.setTextSize(18);
        tvValue.setTextColor(Color.BLACK);
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(tvValue);

        if (status != null) {
            TextView tvStatus = new TextView(context);
            tvStatus.setText(status);
            tvStatus.setTextColor(Color.GREEN); // Example color
            content.addView(tvStatus);
        }

        card.addView(content);
        return card;
    }

    private View createSectionTitle(Context context, String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setPadding(32, 32, 32, 16);
        tv.setTextSize(16);
        tv.setTextColor(Color.DKGRAY);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }
}
