package com.ensao.mytime.sleep.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.content.SharedPreferences;

import com.ensao.mytime.R;
import com.ensao.mytime.home.AlarmScheduler;

import java.util.Random;

public class SleepInteractionReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "SleepAdviceChannel";
    private static final int NOTIFICATION_ID_INTERACTION = 600;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Déclenché à chaque fois que l'utilisateur déverrouille le téléphone
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

            SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
            long startTime = prefs.getLong(AlarmScheduler.KEY_PREP_START_TIME, 0);
            long endTime = prefs.getLong(AlarmScheduler.KEY_PREP_END_TIME, 0);
            long currentTime = System.currentTimeMillis();

            if (startTime > 0 && currentTime >= startTime && currentTime <= endTime) {
                // 3. Si la condition est remplie (dans la fenêtre de 2h), on envoie la notification
                sendSleepAdviceNotification(context);
            }
        }
    }

    private void sendSleepAdviceNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        createNotificationChannel(notificationManager);

        String[] advices = context.getResources().getStringArray(R.array.sleep_advice_messages);
        String randomAdvice = advices[new Random().nextInt(advices.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sleep)
                .setContentTitle("Phase de Préparation au Sommeil")
                .setContentText(randomAdvice)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID_INTERACTION, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rappels et Conseils Sommeil",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
    }
}