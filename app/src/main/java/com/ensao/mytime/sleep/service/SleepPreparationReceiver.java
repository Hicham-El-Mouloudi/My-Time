package com.ensao.mytime.sleep.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ensao.mytime.R;

import java.util.Random;

public class SleepPreparationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "SleepAdviceChannel";
    private static final int NOTIFICATION_ID = 500;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SleepPrep", "Déclenchement de la phase de préparation (2h avant le coucher)");

        // --- 1. Activation du Mode Sombre pour l'app ---
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // --- 2. Envoi du Conseil ---
        sendSleepAdviceNotification(context);
    }

    private void sendSleepAdviceNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        createNotificationChannel(notificationManager);

        // ✅ Récupération depuis strings.xml
        String[] advices = context.getResources().getStringArray(R.array.sleep_advice_messages);
        String randomAdvice = advices[new Random().nextInt(advices.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sleep)
                .setContentTitle("Phase de Préparation") // Titre adapté au Receiver
                .setContentText(randomAdvice)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
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