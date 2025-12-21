package com.ensao.mytime.study.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.os.CountDownTimer;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class PomodoroService extends Service {
    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 1;
    private PomodoroListener listener;

    // Variables pour le minuteur
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 minutes par défaut
    private boolean isTimerRunning = false;
    private boolean ispaused = false;

    private long totalTime = 25 * 60 * 1000;

    // Interface pour la communication avec le ViewModel
    public interface PomodoroListener {
        void onTimerTick(long remainingTime);
        void onTimerFinished();
        void onTimerStarted();
        void onTimerStopped();
    }

    // Binder pour lier le service
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PomodoroService getService() {
            return PomodoroService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Méthode pour définir le listener
    public void setPomodoroListener(PomodoroListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    // === MÉTHODES POUR LE MINUTEUR ===

    public void startTimer(long duration) {
        // ARRÊTER le timer existant s'il est en cours
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.totalTime = duration;
        if(!ispaused)
            this.timeLeftInMillis = duration;
        ispaused=false;
        this.isTimerRunning = true;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                if (listener != null) {
                    listener.onTimerTick(millisUntilFinished);
                }
                Log.d("  ",millisUntilFinished+"  ");
                updateNotification(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                if (listener != null) {
                    listener.onTimerFinished();
                }
                updateNotification(0);
            }
        }.start();

        if (listener != null) {
            listener.onTimerStarted();
        }

        Log.d("TIMER_DEBUG", "Timer démarré: " + duration + "ms");
    }


    public void pauseTimer() {
        if (countDownTimer != null && isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
            ispaused=true;
            Log.d("TIMER_DEBUG", "Timer mis en pause. Temps restant: " + timeLeftInMillis + "ms");
        }
    }

    public void resumeTimer() {
        if (!isTimerRunning && timeLeftInMillis > 0) {
            // Redémarrer avec le temps restant
            startTimer(timeLeftInMillis);
            Log.d("TIMER_DEBUG", "Timer repris: " + timeLeftInMillis + "ms");
        } else {
            Log.d("TIMER_DEBUG", "Impossible de reprendre - état: running=" + isTimerRunning + ", timeLeft=" + timeLeftInMillis);
        }
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
        timeLeftInMillis = totalTime;

        if (listener != null) {
            listener.onTimerStopped();
        }

        updateNotification(totalTime);
        Log.d("TIMER_DEBUG", "Timer arrêté et réinitialisé");
    }

    // Méthodes pour obtenir l'état du timer
    public long getTimeLeft() {
        return timeLeftInMillis;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public void setTimerDuration(long durationInMillis) {
        this.totalTime = durationInMillis;
        if (!isTimerRunning) {
            this.timeLeftInMillis = durationInMillis;
            // Mettre à jour l'affichage même si le timer ne tourne pas
            if (listener != null) {
                listener.onTimerTick(durationInMillis);
            }
        }
    }

    // === MÉTHODES DE NOTIFICATION ===

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Pomodoro timer notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pomodoro Timer")
                .setContentText("Prêt à démarrer")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification(long millisUntilFinished) {
        String contentText;

        if (millisUntilFinished > 0) {
            int minutes = (int) (millisUntilFinished / 1000) / 60;
            int seconds = (int) (millisUntilFinished / 1000) % 60;
            contentText = String.format("Temps restant: %02d:%02d", minutes, seconds);
        } else {
            contentText = "Timer terminé!";
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pomodoro Timer")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .build();

        // Mettre à jour la notification
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        // Arrêter le timer si running
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Notifier l'arrêt
        if (listener != null) {
            listener.onTimerStopped();
        }

        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }
}