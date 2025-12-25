package com.ensao.mytime.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ensao.mytime.alarm.database.Alarm;

import java.util.Calendar;

public class AlarmScheduler {

    public static void scheduleAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.getId());
        intent.putExtra("ALARM_TIME", alarm.getTimeInMillis());
        intent.putExtra("ALARM_RINGTONE", alarm.getRingtoneUri());
        // Label removed

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            long triggerTime = calculateNextAlarmTime(alarm);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent);
            }
        }
    }

    private static long calculateNextAlarmTime(Alarm alarm) {
        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(alarm.getTimeInMillis());

        // Normalize alarmTime to today/next occurrence based on time only
        Calendar nextAlarm = Calendar.getInstance();
        nextAlarm.set(Calendar.HOUR_OF_DAY, alarmTime.get(Calendar.HOUR_OF_DAY));
        nextAlarm.set(Calendar.MINUTE, alarmTime.get(Calendar.MINUTE));
        nextAlarm.set(Calendar.SECOND, 0);
        nextAlarm.set(Calendar.MILLISECOND, 0);

        int daysOfWeek = alarm.getDaysOfWeek();

        if (daysOfWeek == 0) {
            // One-time alarm
            if (nextAlarm.before(now)) {
                nextAlarm.add(Calendar.DAY_OF_YEAR, 1);
            }
            return nextAlarm.getTimeInMillis();
        } else {
            // Repeating alarm
            // Find the next day that matches the bitmask, starting from today (if time
            // hasn't passed) or tomorrow
            while (true) {
                if (nextAlarm.before(now)) {
                    nextAlarm.add(Calendar.DAY_OF_YEAR, 1);
                    continue; // Check if this new day is enabled
                }

                // Check if this day is in the bitmask
                // Calendar.SUNDAY=1 ... SATURDAY=7
                // Our bitmask: Sun=1 (1<<0) ... Sat=64 (1<<6)
                // Map Calendar day to bit index: index = calendarDay - 1
                int dayOfWeek = nextAlarm.get(Calendar.DAY_OF_WEEK); // 1-7
                int bitIndex = dayOfWeek - 1; // 0-6

                if ((daysOfWeek & (1 << bitIndex)) != 0) {
                    return nextAlarm.getTimeInMillis();
                }

                // Try next day
                nextAlarm.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }

    public static void cancelAlarm(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleSnooze(Context context, int alarmId, long triggerTimeInMillis, int snoozeCount) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarmId);
        intent.putExtra("ALARM_TIME", triggerTimeInMillis);
        intent.putExtra("AUTO_SNOOZE_COUNT", snoozeCount);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeInMillis,
                            pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeInMillis,
                            pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMillis,
                        pendingIntent);
            }
        }
    }

    public static void scheduleFalloutAlarm(Context context, int alarmId, long triggerTimeInMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Puzzleable.ACTION_FALLOUT_TRIGGERED);
        intent.putExtra("ALARM_ID", alarmId);
        intent.putExtra("ALARM_TIME", triggerTimeInMillis);

        // Use a derived ID for fallout to avoid conflict with main alarm
        // Using bitwise complement to ensure unique but deterministic ID
        int falloutId = ~alarmId;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                falloutId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeInMillis,
                            pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeInMillis,
                            pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMillis,
                        pendingIntent);
            }
        }
    }

    public static void cancelFalloutAlarm(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Puzzleable.ACTION_FALLOUT_TRIGGERED);

        int falloutId = ~alarmId;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                falloutId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
