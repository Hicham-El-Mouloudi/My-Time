package com.ensao.mytime.home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.ensao.mytime.MainActivity;
import com.ensao.mytime.R;
import com.ensao.mytime.home.model.InvocationData;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_IS_MORNING = "is_morning";
    private static final String CHANNEL_ID = "invocation_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isMorning = intent.getBooleanExtra(EXTRA_IS_MORNING, true);
        String title = isMorning ? InvocationData.MORNING_TITLE : InvocationData.EVENING_TITLE;
        String message = isMorning ? "C'est l'heure de commencer vos invocations du matin." : "C'est l'heure de commencer vos invocations du soir.";

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(isMorning ? 100 : 200, builder.build());

            // Replanifier l'alarme pour le jour suivant
            AlarmScheduler.scheduleNextAlarm(context, isMorning);
        }
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_NAME = "Rappels d'Invocations";
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Notifications pour les heures de début des invocations.");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}