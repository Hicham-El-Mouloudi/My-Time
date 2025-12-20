package com.ensao.mytime.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.ensao.mytime.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get alarm data from intent
        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        long alarmTime = intent.getLongExtra("ALARM_TIME", 0);
        String ringtoneUri = intent.getStringExtra("ALARM_RINGTONE");
        int autoSnoozeCount = intent.getIntExtra("AUTO_SNOOZE_COUNT", 0);

        Intent serviceIntent = new Intent(context, RingtoneService.class);
        serviceIntent.putExtra("ALARM_ID", alarmId);
        serviceIntent.putExtra("ALARM_TIME", alarmTime);
        serviceIntent.putExtra("ALARM_RINGTONE", ringtoneUri);
        serviceIntent.putExtra("AUTO_SNOOZE_COUNT", autoSnoozeCount);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
