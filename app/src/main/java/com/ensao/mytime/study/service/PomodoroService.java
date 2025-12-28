package com.ensao.mytime.study.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class PomodoroService extends Service {
    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 1;

    // === CONSTANTES ===
    // Cycle : 25 min travail + 5 min pause = 30 min total
    private static final long WORK_BLOCK = 25 * 60 * 1000;
    private static final long BREAK_BLOCK = 5 * 60 * 1000;
    private static final long CYCLE_DURATION = WORK_BLOCK + BREAK_BLOCK;

    private PomodoroListener listener;

    // === ÉTAT DU SERVICE ===
    private CountDownTimer activeTimer;
    private PowerManager.WakeLock wakeLock; // INDISPENSABLE : Empêche le CPU de dormir

    private long initialTotalDuration; // Durée totale ajustée (avec pauses)
    private long timeRemainingGlobal; // Temps global restant

    private boolean isWorkSession = true; // État actuel
    private boolean isTimerRunning = false;
    private boolean isPausedByUser = false;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PomodoroService getService() {
            return PomodoroService.this;
        }
    }

    private long totalTime = 25 * 60 * 1000;
    private int pauseCount = 0;

    public interface PomodoroListener {
        void onTimerTick(long remainingTime);

        void onTimerFinished();

        void onTimerStarted();

        void onTimerPaused();

        void onTimerStopped();

        default void onModeChanged(boolean isWorkMode) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setPomodoroListener(PomodoroListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Initialisation du WakeLock pour garder le service actif même écran éteint
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyTime:PomodoroWakelock");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification("Prêt", "En attente"));
        return START_STICKY;
    }

    // ==========================================
    // LOGIQUE "ROBUSTE" (Calcul Mathématique)
    // ==========================================

    public void startTimer(long pureWorkDuration) {
        stopTimer(); // Reset propre

        // --- CORRECTION : Calcul de la durée étendue (incluant les pauses) ---
        // Ex: Si l'utilisateur veut 50 min de travail pur.
        // 50 / 25 = 2 cycles complets.
        // Chaque cycle complet = 30 min (25T + 5P).
        // Donc Total = 2 * 30 = 60 min.

        long fullCycles = pureWorkDuration / WORK_BLOCK;
        long partialWork = pureWorkDuration % WORK_BLOCK;

        long adjustedTotalDuration = (fullCycles * CYCLE_DURATION) + partialWork;

        Log.d("POMODORO",
                "Demande: " + pureWorkDuration + "ms -> Ajusté à: " + adjustedTotalDuration + "ms (avec pauses)");

        this.initialTotalDuration = adjustedTotalDuration;
        this.timeRemainingGlobal = adjustedTotalDuration;
        this.isPausedByUser = false;
        this.isWorkSession = true; // On commence toujours par travailler

        startGlobalCountdown(adjustedTotalDuration);
    }

    private void startGlobalCountdown(long duration) {
        cancelCurrentTimer();
        acquireWakeLock(); // Empêcher le CPU de dormir

        isTimerRunning = true;

        if (listener != null) {
            listener.onTimerStarted();
            // Force une mise à jour immédiate de l'état pour l'UI
            updateSessionState();
        }

        activeTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingGlobal = millisUntilFinished;
                updateSessionState(); // Recalculer l'état (Travail/Pause) à chaque seconde
            }

            @Override
            public void onFinish() {
                finishAll();
            }
        }.start();
        Log.d("TIMER_DEBUG", "Timer démarré (fresh): " + duration + "ms");
        pauseCount = 0;
    }

    /**
     * C'est ici que la magie opère. Au lieu de changer de timer,
     * on calcule où on en est dans le cycle de 30 minutes.
     */
    private void updateSessionState() {
        // Temps écoulé depuis le début absolu
        long timeElapsed = initialTotalDuration - timeRemainingGlobal;

        // Position dans le cycle actuel de 30 min (25+5)
        long timeInCycle = timeElapsed % CYCLE_DURATION;

        boolean newSessionState;
        long displayTime;

        if (timeInCycle < WORK_BLOCK) {
            // === MODE TRAVAIL (0 à 25 min du cycle) ===
            newSessionState = true;

            // Temps restant avant la pause
            displayTime = WORK_BLOCK - timeInCycle;
            // === MODE TRAVAIL (0 à 25 min du cycle) ===
            newSessionState = true;

            // Temps restant avant la pause
            displayTime = WORK_BLOCK - timeInCycle;

            // Cas particulier : Si c'est la toute fin globale (ex: reste 10 min total)
            // On ne doit pas afficher 25 min, mais le vrai temps restant
            if (timeRemainingGlobal < displayTime) {
                displayTime = timeRemainingGlobal;
            }

        } else {
            // === MODE PAUSE (25 à 30 min du cycle) ===
            newSessionState = false;

            // Temps restant avant la fin de la pause
            displayTime = CYCLE_DURATION - timeInCycle;
        }

        // --- DÉTECTION DU CHANGEMENT D'ÉTAT (Travail <-> Pause) ---
        // Cette logique garantit que le son joue dans les DEUX sens
        if (this.isWorkSession != newSessionState) {

            // SONNERIE : On joue le son AVANT de changer la variable pour être sûr
            Log.d("POMODORO", "Changement d'état détecté ! Sonnerie activée.");
            playNotificationSound();

            this.isWorkSession = newSessionState;

            if (listener != null)
                listener.onModeChanged(isWorkSession);
        }

        // Mise à jour UI
        if (listener != null) {
            listener.onTimerTick(displayTime);
        }
        updateNotification(displayTime);
    }

    // ==========================================
    // USER CONTROLS
    // ==========================================
    /**
     * Helper method to create and start a new CountDownTimer.
     * This centralizes the timer creation logic.
     */
    private String currentSubject;

    public void setCurrentSubject(String subject) {
        this.currentSubject = subject;
    }

    public void pauseTimer() {
        if (isTimerRunning) {
            cancelCurrentTimer();
            releaseWakeLock(); // On relâche le CPU quand c'est l'utilisateur qui met pause
            isTimerRunning = false;
            isPausedByUser = true;
            if (listener != null)
                listener.onTimerPaused();
            // On garde la notification à jour
            updateNotification(getTimeLeftForDisplay());
            pauseCount++;
        }
    }

    public void resumeTimer() {
        if (!isTimerRunning && isPausedByUser) {
            isPausedByUser = false;
            // On reprend simplement le timer global là où il s'était arrêté
            startGlobalCountdown(timeRemainingGlobal);
        }
    }

    public void stopTimer() {
        cancelCurrentTimer();
        releaseWakeLock();

        isTimerRunning = false;
        isPausedByUser = false;
        timeRemainingGlobal = 0;
        isWorkSession = true;

        pauseCount = 0;

        if (listener != null) {
            listener.onTimerStopped();
            listener.onModeChanged(true);
        }
        updateNotification(0);
    }

    private void finishAll() {
        playNotificationSound(); // Sonnerie finale
        releaseWakeLock();
        isTimerRunning = false;

        // We use initialTotalDuration instead of totalTime
        int durationMinutes = (int) Math.ceil(initialTotalDuration / (1000.0 * 60.0));

        Log.d("PomodoroService", "Saving stats: " + durationMinutes + "min, subject: " + currentSubject);

        // Check if StatisticsHelper is imported. If not, import
        // com.ensao.mytime.statistics.StatisticsHelper;
        try {
            com.ensao.mytime.statistics.StatisticsHelper.updateStudyStatistics(
                    getApplicationContext(),
                    durationMinutes,
                    currentSubject,
                    pauseCount);
        } catch (Exception e) {
            Log.e("PomodoroService", "Error saving statistics", e);
        }

        if (listener != null)
            listener.onTimerFinished();
        updateNotification(0);
        stopForeground(true);
        pauseCount = 0; // Reset pause count
    }

    private void cancelCurrentTimer() {
        if (activeTimer != null) {
            activeTimer.cancel();
            activeTimer = null;
        }
    }

    // ==========================================
    // GESTION DU WAKELOCK (Important pour veille)
    // ==========================================
    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            // On met un timeout de sécurité (ex: 4 heures) pour ne pas vider la batterie si
            // bug
            wakeLock.acquire(4 * 60 * 60 * 1000L);
            Log.d("POMODORO", "WakeLock acquired");
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d("POMODORO", "WakeLock released");
        }
    }

    // ==========================================
    // AUDIO & NOTIF CORRIGÉ (Arrêt automatique)
    // ==========================================

    private void playNotificationSound() {
        try {
            // 1. Essayer le son d'ALARME (Prioritaire, sonne fort)
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            // 2. Fallback sur NOTIFICATION
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            // 3. Fallback sur RINGTONE
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }

            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);

            if (r != null) {
                // Configuration audio
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r.setLooping(false); // Important : Ne pas boucler
                    r.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }

                r.play();

                // --- CORRECTION CRUCIALE : Arrêter le son après 5 secondes ---
                // Cela empêche le son de tourner à l'infini sur certains téléphones
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (r.isPlaying()) {
                                r.stop();
                                Log.d("POMODORO", "Son arrêté automatiquement.");
                            }
                        } catch (Exception e) {
                            // Ignorer si déjà arrêté
                        }
                    }
                }, 5000); // 5000 ms = 5 secondes
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper pour recalculer le temps d'affichage sans faire avancer le timer
    // Utile pour updateNotification quand on est en pause
    private long getTimeLeftForDisplay() {
        if (activeTimer == null && !isTimerRunning && !isPausedByUser)
            return 0;

        long timeElapsed = initialTotalDuration - timeRemainingGlobal;
        long timeInCycle = timeElapsed % CYCLE_DURATION;
        if (timeInCycle < WORK_BLOCK) {
            long display = WORK_BLOCK - timeInCycle;
            return (timeRemainingGlobal < display) ? timeRemainingGlobal : display;
        } else {
            return CYCLE_DURATION - timeInCycle;
        }
    }

    public long getTimeLeft() {
        return timeRemainingGlobal;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public boolean isTimerPaused() {
        return isPausedByUser;
    }

    public boolean isWorkSession() {
        return isWorkSession;
    }

    // ==========================================
    // GETTERS & NOTIFICATIONS
    // ==========================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Pomodoro Timer", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String title, String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification(long displayTime) {
        String title = isWorkSession ? "Concentration" : "Pause Détente ";
        if (isPausedByUser)
            title += " (Pause)";

        int min = (int) (displayTime / 1000) / 60;
        int sec = (int) (displayTime / 1000) % 60;
        String content = String.format("%02d:%02d", min, sec);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(title, content));
        }
    }

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();
    }
}
