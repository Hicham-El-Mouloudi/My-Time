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

        void onTimerPaused();

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

    /**
     * Start a fresh timer with the given duration.
     * This resets all state and starts counting down from the beginning.
     */
    public void startTimer(long duration) {
        // Cancel any existing timer first
        cancelCurrentTimer();

        // Reset all state for fresh start
        this.totalTime = duration;
        this.timeLeftInMillis = duration;
        this.ispaused = false;

        // Create and start the countdown
        createAndStartCountdown(duration);

        if (listener != null) {
            listener.onTimerStarted();
        }

        Log.d("TIMER_DEBUG", "Timer démarré (fresh): " + duration + "ms");
    }

    public void pauseTimer() {
        if (countDownTimer != null && isTimerRunning) {
            countDownTimer.cancel();
            countDownTimer = null;
            isTimerRunning = false;
            ispaused = true;

            // Notify listener about pause state
            if (listener != null) {
                listener.onTimerPaused();
            }

            Log.d("TIMER_DEBUG", "Timer mis en pause. Temps restant: " + timeLeftInMillis + "ms");
        }
    }

    public void resumeTimer() {
        // Relaxed condition: if timer is not running and we have time left, we can
        // resume
        if (!isTimerRunning && timeLeftInMillis > 0) {
            // Resume from paused state - don't reset totalTime
            this.ispaused = false;

            // Create and start countdown with remaining time
            createAndStartCountdown(timeLeftInMillis);

            if (listener != null) {
                listener.onTimerStarted();
            }

            Log.d("TIMER_DEBUG", "Timer repris: " + timeLeftInMillis + "ms");
        } else {
            Log.d("TIMER_DEBUG",
                    "Impossible de reprendre - état: running=" + isTimerRunning + ", timeLeft=" + timeLeftInMillis);
        }
    }

    /**
     * Helper method to cancel the current timer if it exists.
     */
    private void cancelCurrentTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * Helper method to create and start a new CountDownTimer.
     * This centralizes the timer creation logic.
     */
    private void createAndStartCountdown(long durationMillis) {
        this.isTimerRunning = true;

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Only update if still running (prevents ghost ticks after cancel)
                if (isTimerRunning) {
                    timeLeftInMillis = millisUntilFinished;
                    if (listener != null) {
                        listener.onTimerTick(millisUntilFinished);
                    }
                    Log.d("TIMER_DEBUG", "Tick: " + millisUntilFinished + "ms");
                    updateNotification(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                ispaused = false;
                if (listener != null) {
                    listener.onTimerFinished();
                }
                updateNotification(0);
            }
        }.start();
    }

    public void stopTimer() {
        cancelCurrentTimer();

        isTimerRunning = false;
        ispaused = false; // Reset paused state on stop
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

    public boolean isTimerPaused() {
        return ispaused;
    }

    public void setTimerDuration(long durationInMillis) {
        this.totalTime = durationInMillis;
        if (!isTimerRunning) {
            this.timeLeftInMillis = durationInMillis;
            this.ispaused = false;
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
                    NotificationManager.IMPORTANCE_LOW);
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
        cancelCurrentTimer();

        // Notifier l'arrêt
        if (listener != null) {
            listener.onTimerStopped();
        }

        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }
}