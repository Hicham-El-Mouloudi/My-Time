package com.ensao.mytime.statistics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsSleepSession;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsWakeSession;
import com.ensao.mytime.Activityfeature.Repos.StatisticsSleepSessionRepo;
import com.ensao.mytime.Activityfeature.Repos.StatisticsWakeSessionRepo;
import com.ensao.mytime.home.AlarmScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for saving sleep and wake statistics to the database.
 * Centralizes the logic for calculating and persisting statistics data.
 */
public class StatisticsHelper {

    private static final String TAG = "StatisticsHelper";

    /**
     * Saves sleep session statistics when the user wakes up.
     * Should be called from WakeUpReceiver.
     *
     * @param context Application context
     */
    public static void saveSleepStatistics(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

        long sleepTimeMillis = prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, 0);
        long wakeUpTimeMillis = prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, 0);

        if (sleepTimeMillis == 0 || wakeUpTimeMillis == 0) {
            Log.w(TAG, "Sleep or wake time not set, cannot save sleep statistics");
            return;
        }

        // Calculate sleep duration in hours
        long durationMillis = wakeUpTimeMillis - sleepTimeMillis;
        if (durationMillis < 0) {
            // Handle overnight sleep (wake time is next day)
            durationMillis += 24 * 60 * 60 * 1000;
        }
        float sleepDurationHours = durationMillis / (1000f * 60f * 60f);

        // Create the statistics entry
        Date today = normalizeToStartOfDay(new Date());

        StatisticsSleepSession session = new StatisticsSleepSession(
                today,
                sleepDurationHours, // sleepDuration
                85, // sleepEfficiency (default estimate)
                sleepDurationHours, // timeInBed (same as duration initially)
                15, // sleepLatency (default 15 min to fall asleep)
                0, // wakeDuringSleep (default 0)
                true // hasSleep
        );

        // Save to database
        Application app = (Application) context.getApplicationContext();
        StatisticsSleepSessionRepo repo = new StatisticsSleepSessionRepo(app);
        repo.insert(session, id -> {
            Log.d(TAG, "Sleep statistics saved with id: " + id +
                    ", duration: " + sleepDurationHours + "h");
        });
    }

    /**
     * Saves wake session statistics when the user completes waking up (solves
     * puzzle).
     * Should be called from puzzle activities when puzzle is solved.
     *
     * @param context Application context
     */
    public static void saveWakeStatistics(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

        long firstAlarmTimeMillis = prefs.getLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, 0);
        int ringCount = prefs.getInt(AlarmScheduler.KEY_RING_COUNT, 0);
        long expectedWakeTimeMillis = prefs.getLong(AlarmScheduler.KEY_EXPECTED_WAKE_TIME, 0);

        if (firstAlarmTimeMillis == 0) {
            Log.w(TAG, "First alarm time not set, cannot save wake statistics");
            return;
        }

        long now = System.currentTimeMillis();

        // Calculate wake latency (time from first alarm to now, in minutes)
        int wakeLatencyMinutes = (int) ((now - firstAlarmTimeMillis) / (1000 * 60));

        // Calculate time variability (difference from expected wake time, in minutes)
        float timeVariabilityMinutes = 0;
        if (expectedWakeTimeMillis > 0) {
            timeVariabilityMinutes = (now - expectedWakeTimeMillis) / (1000f * 60f);
        }

        // Format times as HH:mm
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String firstAlarmStr = timeFormat.format(new Date(firstAlarmTimeMillis));
        String lastOffStr = timeFormat.format(new Date(now));

        // Calculate wake duration in minutes
        float wakeDurationMinutes = (now - firstAlarmTimeMillis) / (1000f * 60f);

        // Create the statistics entry
        Date today = normalizeToStartOfDay(new Date());

        StatisticsWakeSession session = new StatisticsWakeSession(
                today,
                wakeLatencyMinutes, // wakeLatency
                ringCount, // ringCount
                timeVariabilityMinutes, // timeVariability
                firstAlarmStr, // firstAlarm
                lastOffStr, // lastOff
                wakeDurationMinutes, // wakeDuration
                true // hasWake
        );

        // Save to database
        Application app = (Application) context.getApplicationContext();
        StatisticsWakeSessionRepo repo = new StatisticsWakeSessionRepo(app);
        repo.insert(session, id -> {
            Log.d(TAG, "Wake statistics saved with id: " + id +
                    ", wakeLatency: " + wakeLatencyMinutes + "min, ringCount: " + ringCount);
        });

        // Reset tracking data for next session
        resetWakeSessionTracking(context);
    }

    /**
     * Records the first alarm ring time if not already recorded.
     * Should be called from RingtoneService when alarm starts.
     *
     * @param context          Application context
     * @param expectedWakeTime The scheduled wake-up time for variability
     *                         calculation
     */
    public static void recordFirstAlarmRing(Context context, long expectedWakeTime) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        long existingFirstAlarm = prefs.getLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, 0);

        if (existingFirstAlarm == 0) {
            // First ring of this wake session
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, System.currentTimeMillis());
            editor.putInt(AlarmScheduler.KEY_RING_COUNT, 1);
            if (expectedWakeTime > 0) {
                editor.putLong(AlarmScheduler.KEY_EXPECTED_WAKE_TIME, expectedWakeTime);
            }
            editor.apply();
            Log.d(TAG, "First alarm ring recorded");
        }
    }

    /**
     * Increments the ring count for the current wake session.
     * Should be called from RingtoneService for subsequent alarm rings.
     *
     * @param context Application context
     */
    public static void incrementRingCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        int currentCount = prefs.getInt(AlarmScheduler.KEY_RING_COUNT, 0);
        prefs.edit().putInt(AlarmScheduler.KEY_RING_COUNT, currentCount + 1).apply();
        Log.d(TAG, "Ring count incremented to: " + (currentCount + 1));
    }

    /**
     * Resets wake session tracking data for the next session.
     *
     * @param context Application context
     */
    public static void resetWakeSessionTracking(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(AlarmScheduler.KEY_FIRST_ALARM_TIME)
                .remove(AlarmScheduler.KEY_RING_COUNT)
                .remove(AlarmScheduler.KEY_EXPECTED_WAKE_TIME)
                .apply();
        Log.d(TAG, "Wake session tracking data reset");
    }

    /**
     * Normalizes a date to the start of the day (midnight).
     *
     * @param date The date to normalize
     * @return Date at midnight of the same day
     */
    public static Date normalizeToStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
