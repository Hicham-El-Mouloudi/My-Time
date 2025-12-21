package com.ensao.mytime.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.SharedPreferences;

import com.ensao.mytime.home.model.InvocationData;
import com.ensao.mytime.sleep.service.SleepPreparationReceiver;
import com.ensao.mytime.sleep.receiver.WakeUpReceiver;

import java.util.Calendar;

public class AlarmScheduler {

    // --- CODES DE REQUÊTE ---
    private static final int REQUEST_CODE_MORNING = 10;
    private static final int REQUEST_CODE_EVENING = 20;
    public static final int REQUEST_CODE_SLEEP_PREPARATION = 400;
    private static final int WAKE_UP_REQUEST_CODE = 500;

    // --- CENTRALISATION DES CLÉS DE SAUVEGARDE ---
    public static final String PREFS_NAME = "SleepSessionPrefs";

    // Clés pour le temps
    public static final String KEY_PREP_START_TIME = "PreparationStartTime";
    public static final String KEY_PREP_END_TIME = "PreparationEndTime";
    public static final String KEY_WAKE_UP_TIME = "wake_up_time";
    public static final String KEY_SLEEP_TIME = "sleep_time";

    // Clés pour l'état et les réglages
    public static final String KEY_IS_SESSION_ACTIVE = "is_session_active";

    // Clés pour les applications
    public static final String KEY_BLOCKED_APPS_SET = "BLOCKED_APPS_SET";
    public static final String KEY_BLOCKED_APPS_LIST = "BLOCKED_APPS_LIST";
    public static final String KEY_BLOCKED_APP_PACKAGES = "BlockedAppPackages";

    // Clés pour les statistiques de réveil
    public static final String KEY_FIRST_ALARM_TIME = "first_alarm_time";
    public static final String KEY_RING_COUNT = "ring_count";
    public static final String KEY_EXPECTED_WAKE_TIME = "expected_wake_time";

    // --- MÉTHODES POUR L'INVOCATION (MATIN/SOIR) ---
    public static void scheduleNextAlarm(Context context, boolean isMorningAlarm) {
        SharedPreferences prefs = context.getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        int startHour;
        int requestCode;

        if (isMorningAlarm) {
            startHour = prefs.getInt(InvocationData.KEY_MORNING_START_HOUR, InvocationData.DEFAULT_MORNING_START);
            requestCode = REQUEST_CODE_MORNING;
        } else {
            startHour = prefs.getInt(InvocationData.KEY_EVENING_START_HOUR, InvocationData.DEFAULT_EVENING_START);
            requestCode = REQUEST_CODE_EVENING;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_IS_MORNING, isMorningAlarm);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        scheduleSecurely(alarmManager, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelAllAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        int flags = PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE;

        PendingIntent morning = PendingIntent.getBroadcast(context, REQUEST_CODE_MORNING, intent, flags);
        if (morning != null)
            alarmManager.cancel(morning);

        PendingIntent evening = PendingIntent.getBroadcast(context, REQUEST_CODE_EVENING, intent, flags);
        if (evening != null)
            alarmManager.cancel(evening);
    }

    // --- MÉTHODES POUR LA PHASE DE PRÉPARATION ---
    public static void scheduleSleepPreparation(Context context, Calendar preparationTimeCalendar) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SleepPreparationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE_SLEEP_PREPARATION, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            scheduleSecurely(alarmManager, preparationTimeCalendar.getTimeInMillis(), pendingIntent);

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long sleepTime = prefs.getLong(KEY_SLEEP_TIME, 0);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_PREP_START_TIME, preparationTimeCalendar.getTimeInMillis());
            editor.putLong(KEY_PREP_END_TIME, sleepTime);
            editor.apply();
        }
    }

    public static void cancelSleepPreparation(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SleepPreparationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE_SLEEP_PREPARATION, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .remove(KEY_PREP_START_TIME).remove(KEY_PREP_END_TIME).apply();
        }
    }

    // --- MÉTHODES POUR LE RÉVEIL ---
    public static void scheduleWakeUpAlarm(Context context, Calendar wakeUpTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakeUpReceiver.class);
        intent.setAction(WakeUpReceiver.ACTION_WAKE_UP);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WAKE_UP_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        scheduleSecurely(alarmManager, wakeUpTime.getTimeInMillis(), pendingIntent);
    }

    public static void cancelWakeUpAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakeUpReceiver.class);
        intent.setAction(WakeUpReceiver.ACTION_WAKE_UP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WAKE_UP_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }

    // --- UTILITAIRES DE SÉCURITÉ ---
    private static void scheduleSecurely(AlarmManager alarmManager, long triggerTime, PendingIntent pendingIntent) {
        if (alarmManager == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}