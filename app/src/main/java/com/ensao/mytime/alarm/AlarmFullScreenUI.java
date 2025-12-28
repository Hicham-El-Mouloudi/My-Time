package com.ensao.mytime.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFullScreenUI extends AppCompatActivity {

    private TextView alarmTimeText;
    private View btnSnooze;
    private View btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show on lock screen and turn screen on (WITHOUT dismissing keyguard)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.fragment_alarm_fullscreen_ui);

        alarmTimeText = findViewById(R.id.alarm_time_text);
        btnSnooze = findViewById(R.id.btn_snooze);
        btnStop = findViewById(R.id.btn_stop);

        // Get alarm data from intent
        int alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        long alarmTime = getIntent().getLongExtra("ALARM_TIME", System.currentTimeMillis());

        // Format and display time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(new Date(alarmTime));
        alarmTimeText.setText(formattedTime);

        // Setup Buttons
        btnSnooze.setOnClickListener(v -> performSnooze(alarmId));
        btnStop.setOnClickListener(v -> performDismiss());

        // Start Pulse Animation on Stop Button
        startPulseAnimation();

        // Register Receiver to finish activity when service stops
        android.content.IntentFilter filter = new android.content.IntentFilter("com.ensao.mytime.ACTION_STOP_ALARM_UI");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(finishReceiver, filter);
        }
    }

    private final android.content.BroadcastReceiver finishReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.ensao.mytime.ACTION_STOP_ALARM_UI".equals(intent.getAction())) {
                finish();
            }
        }
    };

    private void startPulseAnimation() {
        // Pulse the Stop button to draw attention
        android.view.animation.ScaleAnimation pulse = new android.view.animation.ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(1000);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        btnStop.startAnimation(pulse);
    }

    private void performSnooze(int alarmId) {
        Intent serviceIntent = new Intent(this, RingtoneService.class);
        stopService(serviceIntent); // Stop ringing

        // Close any active puzzle
        android.util.Log.d("AlarmFullScreenUI", "Sending ACTION_FINISH_PUZZLE broadcast");
        Intent finishIntent = new Intent(Puzzleable.ACTION_FINISH_PUZZLE);
        finishIntent.setPackage(getPackageName());
        sendBroadcast(finishIntent);

        // Schedule Snooze
        long triggerTime = System.currentTimeMillis() + (AlarmConfig.SNOOZE_DELAY_SECONDS * 1000L);
        AlarmScheduler.scheduleSnooze(this, alarmId, triggerTime, 0);

        finish();
    }

    private void performDismiss() {
        // Check if Sleep Alarm
        int alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        if (alarmId != -1) {
            new Thread(() -> {
                com.ensao.mytime.alarm.database.AlarmRepository repository = new com.ensao.mytime.alarm.database.AlarmRepository(
                        getApplication());
                com.ensao.mytime.alarm.database.Alarm alarm = repository.getAlarmByIdSync(alarmId);

                // Stop Ringing (Common for both - user requested ringtone stops on click)
                Intent serviceIntent = new Intent(this, RingtoneService.class);
                stopService(serviceIntent);

                if (alarm != null && alarm.isSleepAlarm()) {
                    // 1. Schedule Fallout Alarm (Auto-Snooze) for +3 mins
                    long falloutTime = System.currentTimeMillis()
                            + (AlarmConfig.PUZZLE_MODE_AUTO_SNOOZE_DELAY_SECONDS * 1000L);
                    AlarmScheduler.scheduleFalloutAlarm(this, alarmId, falloutTime);

                    // 2. Broadcast that puzzle mode is starting
                    Intent puzzleStartIntent = new Intent(Puzzleable.ACTION_PUZZLE_STARTED);
                    puzzleStartIntent.putExtra(Puzzleable.EXTRA_ALARM_ID, alarmId);
                    sendBroadcast(puzzleStartIntent);

                    // 3. Redirect to selected puzzle game
                    String puzzleType = alarm.getPuzzleType();
                    Intent puzzleIntent;
                    switch (puzzleType != null ? puzzleType : "jpegchaos") {
                        case "minesweeper":
                            puzzleIntent = new Intent(this,
                                    com.ensao.mytime.games.minesweeper.MinesweeperGameActivity.class);
                            break;
                        case "sudoku":
                            puzzleIntent = new Intent(this, com.ensao.mytime.games.sudoku.SudoKuMainActivity.class);
                            break;
                        case "jpegchaos":
                        default:
                            puzzleIntent = new Intent(this, com.ensao.mytime.games.jpegchaos.JpegChaosActivity.class);
                            break;
                    }
                    puzzleIntent.putExtra("ALARM_ID", alarmId);
                    puzzleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(puzzleIntent);
                } else {
                    // Normal Dismiss - Turn off if not repeating
                    if (alarm != null && alarm.getDaysOfWeek() == 0) {
                        alarm.setEnabled(false);
                        repository.update(alarm);
                    }
                    // Save wake stats for normal alarms too (if applicable)
                    com.ensao.mytime.statistics.StatisticsHelper.saveWakeStatistics(getApplicationContext());
                }
                finish();
            }).start();
        } else {
            // Fallback if no ID (shouldn't happen)
            Intent serviceIntent = new Intent(this, RingtoneService.class);
            stopService(serviceIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(finishReceiver);
        } catch (Exception e) {
            // Receiver might not be registered or already unregistered
        }
    }
}