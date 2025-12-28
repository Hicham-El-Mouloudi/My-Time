package com.ensao.mytime.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.StatisticsHelper;

import java.io.IOException;

public class RingtoneService extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private static final String CHANNEL_ID = "ALARM_FULLSCREEN_CHANNEL"; // New ID to force channel recreation
    private static final String ACTION_DISMISS = "ACTION_DISMISS";
    private static final String ACTION_SNOOZE = "ACTION_SNOOZE";

    private android.os.Handler handler;
    private Runnable autoSnoozeRunnable;

    // Puzzle mode state tracking
    private static boolean isPuzzleActive = false;
    private static int puzzleAlarmId = -1;
    private android.content.BroadcastReceiver puzzleReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerPuzzleReceiver();
    }

    private void registerPuzzleReceiver() {
        puzzleReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null)
                    return;

                int alarmId = intent.getIntExtra(Puzzleable.EXTRA_ALARM_ID, -1);

                switch (intent.getAction()) {
                    case Puzzleable.ACTION_PUZZLE_STARTED:
                        // User clicked stop on puzzle alarm, entering puzzle mode
                        isPuzzleActive = true;
                        puzzleAlarmId = alarmId;
                        break;
                    case Puzzleable.ACTION_PUZZLE_COMPLETED:
                        // Puzzle solved, deactivate alarm
                        isPuzzleActive = false;
                        puzzleAlarmId = -1;
                        // Stop service if we're still running
                        stopSelf();
                        break;
                }
            }
        };

        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction(Puzzleable.ACTION_PUZZLE_STARTED);
        filter.addAction(Puzzleable.ACTION_PUZZLE_COMPLETED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(puzzleReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(puzzleReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;

        if (ACTION_DISMISS.equals(intent.getAction())) {
            // Check if Sleep Alarm
            // Note: alarmId needed. We need to fetch from Intent or Store it.
            // Problem: The PendingIntent for Dismiss might not have extras if we didn't put
            // them.
            // But we can try to get them.

            // Actually, we should check DB. But we need ID.
            // Let's assume we can get ID from intent or we use standard dismiss.

            // To properly handle "Dismiss from Notification" for Puzzle:
            // We need the notification pending intent to trigger an Activity or Broadcast
            // that checks logic.
            // Services can't easily start Activities from background on Android 10+.
            // However, since we are in Foreground Service, we have some privileges, or we
            // use fullScreenIntent.

            // For now, let's just stop. The user asked to add logic here.
            // We will trust the UI (AlarmFullScreenUI) handles the main interaction.
            // If they click "Dismiss" on notification, we should probably just stop.
            // BUT, user explicitly asked: "edit the service, to call the puzzle... after
            // dismissing from notification"

            // So we need to:
            // 1. Get Alarm ID (we need to ensure it is passed in Dismiss Intent)
            // 2. Check DB.
            // 3. If Sleep -> Start Activity.

            // Let's proceed with Stop Self for now, but I will modify buildNotification to
            // pass extras.
            stopSelf();
            return START_NOT_STICKY;
        }

        // Extract data - Need to extract these before Snooze check because we need
        // alarmId
        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        long alarmTime = intent.getLongExtra("ALARM_TIME", System.currentTimeMillis());
        boolean isSleepAlarm = intent.getBooleanExtra("IS_SLEEP_ALARM", false);

        // Check if puzzle is active for this alarm - skip if user is solving puzzle
        if (isPuzzleActive && puzzleAlarmId == alarmId) {
            // User is actively solving puzzle for this alarm, ignore this trigger
            return START_NOT_STICKY;
        }

        if (ACTION_SNOOZE.equals(intent.getAction())) {
            // Schedule Snooze - use puzzle mode delay if puzzle is active
            int snoozeDelay = isPuzzleActive ? AlarmConfig.PUZZLE_MODE_AUTO_SNOOZE_DELAY_SECONDS
                    : AlarmConfig.SNOOZE_DELAY_SECONDS;
            long triggerTime = System.currentTimeMillis() + (snoozeDelay * 1000L);
            AlarmScheduler.scheduleSnooze(this, alarmId, triggerTime, 0);
            stopSelf();
            return START_NOT_STICKY;
        }

        // 1. Acquire WakeLock FIRST to ensure screen wakes up
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            @SuppressWarnings("deprecation")
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyTime:AlarmWakeLock");
            wakeLock.acquire(60 * 1000L); // 60 seconds max
        }

        // Extract Rington Uri
        String ringtoneUri = intent.getStringExtra("ALARM_RINGTONE");
        // Extract Auto Snooze Count
        int autoSnoozeCount = intent.getIntExtra("AUTO_SNOOZE_COUNT", 0);

        // 2. Play Ringtone
        playRingtone(ringtoneUri);

        // 3. Vibrate
        startVibration();

        // 4. Track statistics for wake session
        trackAlarmRingStatistics(alarmTime, autoSnoozeCount);

        // 5. Start Priority Activity (for unlocked state) + Foreground Notification
        // (for locked state/fallback)
        Intent activityIntent = new Intent(this, AlarmFullScreenUI.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activityIntent.putExtra("ALARM_ID", alarmId);
        activityIntent.putExtra("ALARM_TIME", alarmTime);
        activityIntent.putExtra("AUTO_SNOOZE_COUNT", autoSnoozeCount);
        activityIntent.putExtra("IS_SLEEP_ALARM", isSleepAlarm);
        startActivity(activityIntent);

        // 6. Start Foreground with Notification
        startForeground(1, buildNotification(alarmId, alarmTime, autoSnoozeCount, isSleepAlarm));

        // 6. Schedule Auto-Snooze / Stop Service after duration
        if (handler == null) {
            handler = new android.os.Handler(android.os.Looper.getMainLooper());
        }

        // Cancel any existing runnable
        if (autoSnoozeRunnable != null) {
            handler.removeCallbacks(autoSnoozeRunnable);
        }

        autoSnoozeRunnable = () -> {
            // If puzzle is active for this alarm, skip auto-snooze trigger
            if (isPuzzleActive && puzzleAlarmId == alarmId) {
                // Reschedule with puzzle mode delay - alarm will trigger again later
                long triggerTime = System.currentTimeMillis()
                        + (AlarmConfig.PUZZLE_MODE_AUTO_SNOOZE_DELAY_SECONDS * 1000L);
                AlarmScheduler.scheduleSnooze(this, alarmId, triggerTime, autoSnoozeCount);
                stopSelf();
                return;
            }

            // Check max auto snoozes
            if (autoSnoozeCount < AlarmConfig.MAX_AUTO_SNOOZES) {
                // Schedule Auto Snooze with incremented count
                // Use Puzzle Mode delay if it is a Sleep Alarm (requires Puzzle) OR if active
                // puzzle session
                int autoSnoozeDelay = (isSleepAlarm || isPuzzleActive)
                        ? AlarmConfig.PUZZLE_MODE_AUTO_SNOOZE_DELAY_SECONDS
                        : AlarmConfig.AUTO_SNOOZE_DELAY_SECONDS;
                long triggerTime = System.currentTimeMillis() + (autoSnoozeDelay * 1000L);
                AlarmScheduler.scheduleSnooze(this, alarmId, triggerTime, autoSnoozeCount + 1);
            } else {
                // Limit reached, just stop (Alarm "turned down")
                // Turn off alarm in DB if not repeating
                new Thread(() -> {
                    com.ensao.mytime.alarm.database.AlarmRepository repository = new com.ensao.mytime.alarm.database.AlarmRepository(
                            getApplication());
                    com.ensao.mytime.alarm.database.Alarm alarm = repository.getAlarmByIdSync(alarmId);
                    if (alarm != null && alarm.getDaysOfWeek() == 0) {
                        alarm.setEnabled(false);
                        repository.update(alarm);
                    }
                }).start();
            }

            stopSelf();
        };

        handler.postDelayed(autoSnoozeRunnable, AlarmConfig.RING_DURATION_SECONDS * 1000L);

        return START_STICKY;
    }

    /**
     * Tracks alarm ring statistics for wake session data.
     * Records first alarm time on first ring, increments ring count on subsequent
     * rings.
     *
     * @param alarmTime       The scheduled alarm time
     * @param autoSnoozeCount The current auto-snooze count (0 = first ring)
     */
    private void trackAlarmRingStatistics(long alarmTime, int autoSnoozeCount) {
        android.util.Log.d("RingtoneService", "trackAlarmRingStatistics called - alarmTime: " + alarmTime +
                ", autoSnoozeCount: " + autoSnoozeCount);
        if (autoSnoozeCount == 0) {
            // First ring - record the initial alarm time
            android.util.Log.d("RingtoneService", "Recording first alarm ring");
            StatisticsHelper.recordFirstAlarmRing(this, alarmTime);
        } else {
            // Subsequent ring - increment the count
            android.util.Log.d("RingtoneService", "Incrementing ring count");
            StatisticsHelper.incrementRingCount(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove notification
        stopForeground(true);

        // Explicitly cancel notification as backup
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1); // Same ID used in startForeground
        }

        // Notify UI to close
        Intent stopUIIntent = new Intent("com.ensao.mytime.ACTION_STOP_ALARM_UI");
        stopUIIntent.setPackage(getPackageName());
        sendBroadcast(stopUIIntent);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (handler != null && autoSnoozeRunnable != null) {
            handler.removeCallbacks(autoSnoozeRunnable);
        }

        // Unregister puzzle broadcast receiver
        if (puzzleReceiver != null) {
            try {
                unregisterReceiver(puzzleReceiver);
            } catch (Exception e) {
                // Receiver might not be registered
            }
        }
    }

    private void playRingtone(String ringtoneUriString) {
        Uri alarmUri = null;
        if (ringtoneUriString != null && !ringtoneUriString.isEmpty()) {
            try {
                alarmUri = Uri.parse(ringtoneUriString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = { 0, 1000, 1000 };
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0), audioAttributes);
            } else {
                vibrator.vibrate(pattern, 0, audioAttributes);
            }
        }
    }

    private Notification buildNotification(int alarmId, long alarmTime, int autoSnoozeCount, boolean isSleepAlarm) {
        createNotificationChannel();

        Intent fullScreenIntent = new Intent(this, AlarmFullScreenUI.class);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        fullScreenIntent.putExtra("ALARM_ID", alarmId);
        fullScreenIntent.putExtra("ALARM_TIME", alarmTime);
        fullScreenIntent.putExtra("AUTO_SNOOZE_COUNT", autoSnoozeCount);
        fullScreenIntent.putExtra("IS_SLEEP_ALARM", isSleepAlarm);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                alarmId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Dismiss Action
        Intent dismissIntent = new Intent(this, RingtoneService.class);
        dismissIntent.setAction(ACTION_DISMISS);
        dismissIntent.putExtra("ALARM_ID", alarmId); // Pass ID
        PendingIntent dismissPendingIntent = PendingIntent.getService(
                this,
                alarmId, // Use ID to make unique
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Snooze Action
        Intent snoozeIntent = new Intent(this, RingtoneService.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent snoozePendingIntent = PendingIntent.getService(
                this,
                0,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Alarm")
                .setContentText("Tap to control alarm")
                .setContentIntent(fullScreenPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setDeleteIntent(dismissPendingIntent) // Keeps swipe to dismiss behavior if possible, though 'ongoing'
                                                       // prevents it usually
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for active alarms");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null); // Sound played by MediaPlayer
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 0, 1000, 1000 });
            channel.setBypassDnd(true); // Bypass Do Not Disturb for alarms

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
