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

    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final String CHANNEL_NAME = "Alarm Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Acquire wake lock to ensure alarm displays even when device is sleeping
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AlarmApp:AlarmWakeLock");
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes

        // Get alarm data from intent
        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        long alarmTime = intent.getLongExtra("ALARM_TIME", 0);
        String alarmLabel = intent.getStringExtra("ALARM_LABEL");

        // Create intent for full screen UI
        Intent fullScreenIntent = new Intent(context, AlarmFullScreenUI.class);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        fullScreenIntent.putExtra("ALARM_ID", alarmId);
        fullScreenIntent.putExtra("ALARM_TIME", alarmTime);
        fullScreenIntent.putExtra("ALARM_LABEL", alarmLabel);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create Notification Channel
        createNotificationChannel(context);

        // Build Notification with Full Screen Intent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
                .setContentTitle("Alarm")
                .setContentText(alarmLabel != null && !alarmLabel.isEmpty() ? alarmLabel : "Alarm is ringing")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(alarmId, builder.build());
        }

        // Release wake lock after a short delay
        wakeLock.release();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Alarm Notifications");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Important for heads-up notification
            channel.setSound(null, null); // We handle sound in the activity or use default

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
