package com.ensao.mytime.Activityfeature;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for detecting phone usage during sleep using UsageStatsManager.
 * Requires PACKAGE_USAGE_STATS permission (granted via
 * Settings.ACTION_USAGE_ACCESS_SETTINGS).
 */
public class UsageStatsHelper {

    private static final String TAG = "UsageStatsHelper";

    /**
     * Retrieves wake segments (phone usage periods) during a sleep session.
     * Uses UsageStatsManager.queryEvents() to detect MOVE_TO_FOREGROUND and
     * MOVE_TO_BACKGROUND events.
     *
     * @param context    Application context
     * @param sleepStart Start time of sleep (epoch milliseconds)
     * @param sleepEnd   End time of sleep (epoch milliseconds)
     * @return JSON string: {"wake_sessions": [{"start": long, "end": long}, ...]}
     */
    public static String getWakeSegmentsJson(Context context, long sleepStart, long sleepEnd) {
        JSONObject result = new JSONObject();
        JSONArray wakeSessions = new JSONArray();

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context
                    .getSystemService(Context.USAGE_STATS_SERVICE);

            if (usageStatsManager == null) {
                return createEmptyResult();
            }

            UsageEvents usageEvents = usageStatsManager.queryEvents(sleepStart, sleepEnd);
            if (usageEvents == null) {
                return createEmptyResult();
            }

            UsageEvents.Event event = new UsageEvents.Event();
            long currentWakeStart = -1;

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);

                // MOVE_TO_FOREGROUND indicates app brought to front (phone usage started)
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    if (currentWakeStart == -1) {
                        currentWakeStart = event.getTimeStamp();
                    }
                }
                // MOVE_TO_BACKGROUND indicates app sent to background (phone usage may have
                // ended)
                else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    if (currentWakeStart != -1) {
                        // Create wake segment
                        JSONObject segment = new JSONObject();
                        segment.put("start", currentWakeStart);
                        segment.put("end", event.getTimeStamp());
                        wakeSessions.put(segment);
                        currentWakeStart = -1;
                    }
                }
            }

            // Handle case where wake session extends to sleep end
            if (currentWakeStart != -1) {
                JSONObject segment = new JSONObject();
                segment.put("start", currentWakeStart);
                segment.put("end", sleepEnd);
                wakeSessions.put(segment);
            }

            // Merge overlapping or adjacent segments
            wakeSessions = mergeAdjacentSegments(wakeSessions);

            result.put("wake_sessions", wakeSessions);
        } catch (JSONException e) {
            e.printStackTrace();
            return createEmptyResult();
        }

        return result.toString();
    }

    /**
     * Merges adjacent or overlapping wake segments to consolidate usage periods.
     */
    private static JSONArray mergeAdjacentSegments(JSONArray segments) throws JSONException {
        if (segments.length() <= 1) {
            return segments;
        }

        JSONArray merged = new JSONArray();
        JSONObject current = segments.getJSONObject(0);

        for (int i = 1; i < segments.length(); i++) {
            JSONObject next = segments.getJSONObject(i);
            long currentEnd = current.getLong("end");
            long nextStart = next.getLong("start");

            // Merge if segments overlap or are within 60 seconds of each other
            if (nextStart <= currentEnd + 60000) {
                // Extend current segment
                current.put("end", Math.max(currentEnd, next.getLong("end")));
            } else {
                merged.put(current);
                current = next;
            }
        }
        merged.put(current);

        return merged;
    }

    /**
     * Creates an empty result JSON.
     */
    private static String createEmptyResult() {
        try {
            JSONObject result = new JSONObject();
            result.put("wake_sessions", new JSONArray());
            return result.toString();
        } catch (JSONException e) {
            return "{\"wake_sessions\":[]}";
        }
    }

    /**
     * Calculates total wake duration in minutes from the wake segments JSON.
     */
    public static int getTotalWakeDurationMinutes(String wakeSegmentsJson) {
        try {
            JSONObject json = new JSONObject(wakeSegmentsJson);
            JSONArray sessions = json.getJSONArray("wake_sessions");
            long totalMillis = 0;

            for (int i = 0; i < sessions.length(); i++) {
                JSONObject segment = sessions.getJSONObject(i);
                long start = segment.getLong("start");
                long end = segment.getLong("end");
                totalMillis += (end - start);
            }

            return (int) (totalMillis / 60000); // Convert to minutes
        } catch (JSONException e) {
            return 0;
        }
    }
}
