package com.ensao.mytime.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.ensao.mytime.home.model.InvocationData;
import java.util.Calendar;

public class AlarmScheduler {

    private static final int REQUEST_CODE_MORNING = 10;
    private static final int REQUEST_CODE_EVENING = 20;

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
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelAllAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent morning = PendingIntent.getBroadcast(
                context, REQUEST_CODE_MORNING, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (morning != null) alarmManager.cancel(morning);

        PendingIntent evening = PendingIntent.getBroadcast(
                context, REQUEST_CODE_EVENING, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (evening != null) alarmManager.cancel(evening);
    }
}