package com.ensao.mytime.statistics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsSleepSession;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsWakeSession;
import com.ensao.mytime.Activityfeature.Repos.StatisticsSleepSessionRepo;
import com.ensao.mytime.Activityfeature.Repos.StatisticsWakeSessionRepo;
import com.ensao.mytime.Activityfeature.UsageStatsHelper;
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
        Log.d(TAG, "saveSleepStatistics called");
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

        long sleepTimeMillis = prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, 0);
        long wakeUpTimeMillis = prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, 0);

        Log.d(TAG, "Sleep stats - sleepTime: " + sleepTimeMillis + ", wakeUpTime: " + wakeUpTimeMillis);

        if (sleepTimeMillis == 0 || wakeUpTimeMillis == 0) {
            Log.w(TAG, "Sleep or wake time not set, cannot save sleep statistics. " +
                    "sleepTime=" + sleepTimeMillis + ", wakeUpTime=" + wakeUpTimeMillis);
            return;
        }

        // Calculate sleep duration in hours
        long durationMillis = wakeUpTimeMillis - sleepTimeMillis;
        if (durationMillis < 0) {
            // Handle overnight sleep (wake time is next day)
            durationMillis += 24 * 60 * 60 * 1000;
        }
        float sleepDurationHours = durationMillis / (1000f * 60f * 60f);

        // Get wake during sleep data using UsageStatsManager
        long actualSleepStart = sleepTimeMillis;
        long actualSleepEnd = System.currentTimeMillis(); // Current time is actual wake time

        // Adjust for overnight if needed
        if (actualSleepEnd < actualSleepStart) {
            actualSleepEnd += 24 * 60 * 60 * 1000;
        }

        String wakeSegmentsJson = UsageStatsHelper.getWakeSegmentsJson(context, actualSleepStart, actualSleepEnd);
        int wakeDuringSleepMinutes = UsageStatsHelper.getTotalWakeDurationMinutes(wakeSegmentsJson);

        // Calculate sleep efficiency: (actual sleep time / time in bed) * 100
        // Calculate sleep efficiency: (actual sleep time / time in bed) * 100
        float timeInBedHours = sleepDurationHours;

        // Get configured sleep latency (default 15 min)
        int sleepLatencyMinutes = prefs.getInt("pref_sleep_latency", 15);
        float actualSleepHours = sleepDurationHours - (wakeDuringSleepMinutes / 60f) - (sleepLatencyMinutes / 60f);
        int sleepEfficiency = (int) ((actualSleepHours / timeInBedHours) * 100);
        sleepEfficiency = Math.max(0, Math.min(100, sleepEfficiency)); // Clamp to 0-100

        Log.d(TAG, "Wake during sleep: " + wakeDuringSleepMinutes + " min, efficiency: " + sleepEfficiency + "%");

        // Create the statistics entry
        Date today = normalizeToStartOfDay(new Date());

        StatisticsSleepSession session = new StatisticsSleepSession(
                today,
                sleepDurationHours, // sleepDuration
                sleepEfficiency, // sleepEfficiency (calculated from wake time)
                timeInBedHours, // timeInBed
                sleepLatencyMinutes, // sleepLatency (configured value)
                wakeDuringSleepMinutes, // wakeDuringSleep (actual detected value)
                wakeSegmentsJson, // wakeDuringSleepDistributionJSON
                true // hasSleep
        );

        // Save to database
        Application app = (Application) context.getApplicationContext();
        StatisticsSleepSessionRepo repo = new StatisticsSleepSessionRepo(app);
        repo.insert(session, id -> {
            Log.d(TAG, "Sleep statistics saved with id: " + id +
                    ", duration: " + sleepDurationHours + "h" +
                    ", wakeDuringSleep: " + wakeDuringSleepMinutes + "min");
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
        Log.d(TAG, "saveWakeStatistics called");
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

        long firstAlarmTimeMillis = prefs.getLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, 0);
        int ringCount = prefs.getInt(AlarmScheduler.KEY_RING_COUNT, 0);
        long expectedWakeTimeMillis = prefs.getLong(AlarmScheduler.KEY_EXPECTED_WAKE_TIME, 0);

        Log.d(TAG, "Wake stats - firstAlarmTime: " + firstAlarmTimeMillis +
                ", ringCount: " + ringCount +
                ", expectedWakeTime: " + expectedWakeTimeMillis);

        if (firstAlarmTimeMillis == 0) {
            Log.w(TAG, "First alarm time not set, cannot save wake statistics. " +
                    "This may indicate recordFirstAlarmRing was not called.");
            return;
        }

        long now = System.currentTimeMillis();

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
                ringCount, // ringCount
                timeVariabilityMinutes, // timeVariability
                firstAlarmStr, // firstAlarm
                lastOffStr, // lastOff
                wakeDurationMinutes, // wakeDuration
                true // hasWake
        );

        Log.d(TAG, "Creating StatisticsWakeSession: date=" + today +
                ", ringCount=" + ringCount +
                ", timeVariability=" + timeVariabilityMinutes +
                ", firstAlarm=" + firstAlarmStr +
                ", lastOff=" + lastOffStr +
                ", wakeDuration=" + wakeDurationMinutes + "min");

        // Save to database
        Application app = (Application) context.getApplicationContext();
        StatisticsWakeSessionRepo repo = new StatisticsWakeSessionRepo(app);
        repo.insert(session, id -> {
            Log.d(TAG, "Wake statistics saved with id: " + id +
                    ", wakeDuration: " + wakeDurationMinutes + "min, ringCount: " + ringCount);
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
        Log.d(TAG, "recordFirstAlarmRing called with expectedWakeTime: " + expectedWakeTime);
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        long existingFirstAlarm = prefs.getLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, 0);
        Log.d(TAG, "Existing firstAlarmTime in prefs: " + existingFirstAlarm);

        if (existingFirstAlarm == 0) {
            // First ring of this wake session
            long currentTime = System.currentTimeMillis();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AlarmScheduler.KEY_FIRST_ALARM_TIME, currentTime);
            editor.putInt(AlarmScheduler.KEY_RING_COUNT, 1);
            if (expectedWakeTime > 0) {
                editor.putLong(AlarmScheduler.KEY_EXPECTED_WAKE_TIME, expectedWakeTime);
            }
            editor.apply();
            Log.d(TAG, "First alarm ring recorded at: " + currentTime + ", ringCount set to 1");
        } else {
            Log.d(TAG, "First alarm already recorded, not overwriting");
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
     * Updates study statistics when a Pomodoro session is completed.
     *
     * @param context         Application context
     * @param durationMinutes Duration of the session in minutes
     * @param subjectName     Name of the subject studied (can be null/empty)
     */
    /**
     * Updates study statistics when a Pomodoro session is completed.
     *
     * @param context         Application context
     * @param durationMinutes Duration of the session in minutes
     * @param subjectName     Name of the subject studied (can be null/empty)
     * @param sessionPauses   Number of times paused during the session
     */
    public static void updateStudyStatistics(Context context, int durationMinutes, String subjectName,
            int sessionPauses) {
        Application app = (Application) context.getApplicationContext();
        com.ensao.mytime.Activityfeature.Repos.StatisticsStudySessionRepo repo = new com.ensao.mytime.Activityfeature.Repos.StatisticsStudySessionRepo(
                app);
        Date today = normalizeToStartOfDay(new Date());

        repo.getByDate(today, null, session -> {
            if (session == null) {
                session = new com.ensao.mytime.Activityfeature.Busniss.StatisticsStudySession();
                session.setDate(today);
                session.setTotalFocusTime(0);
                session.setStreakCount(1); // Basic streak logic
                session.setPauseCount(0);
                session.setSessionsCount(0); // Initialize new field
                session.setCompletedTasksCount(0);
                session.setTotalTasksCount(0);
                session.setSubjectsStudiedCount(0);
                session.setHasStudy(true);
                session.setSubjectDistribution("{}");
            }

            // Update Total Focus Time
            session.setTotalFocusTime(session.getTotalFocusTime() + durationMinutes);

            // Update Session count and Pauses
            session.setSessionsCount(session.getSessionsCount() + 1);
            session.setPauseCount(session.getPauseCount() + sessionPauses);
            // Note: We store total pauses here. The DAO/DayData converter will calculate
            // the mean.

            // Update Subject Distribution
            java.util.Map<String, Integer> subjects = new java.util.HashMap<>();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            if (session.getSubjectDistribution() != null && !session.getSubjectDistribution().isEmpty()) {
                java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, Integer>>() {
                }.getType();
                try {
                    subjects = gson.fromJson(session.getSubjectDistribution(), type);
                    if (subjects == null)
                        subjects = new java.util.HashMap<>();
                } catch (Exception e) {
                    subjects = new java.util.HashMap<>();
                }
            }

            if (subjectName != null && !subjectName.trim().isEmpty()) {
                String key = subjectName.trim();
                int currentSubjectTime = 0;
                if (subjects.containsKey(key)) {
                    currentSubjectTime = subjects.get(key);
                }
                subjects.put(key, currentSubjectTime + durationMinutes);
            }

            session.setSubjectDistribution(gson.toJson(subjects));
            session.setSubjectsStudiedCount(subjects.size());

            repo.insert(session,
                    id -> Log.d(TAG, "Study statistics updated: + " + durationMinutes + " min for " + subjectName));
        });
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
