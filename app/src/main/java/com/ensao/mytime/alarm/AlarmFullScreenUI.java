package com.ensao.mytime.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFullScreenUI extends AppCompatActivity {

    private TextView counterText;
    private TextView alarmTimeText;
    private View btnSnooze;
    private View btnStop;

    private CountDownTimer countDownTimer;

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

        counterText = findViewById(R.id.counter_text);
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

        // Start Countdown
        startCountdown();

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

    private void startCountdown() {
        long durationMillis = AlarmConfig.RING_DURATION_SECONDS * 1000L;
        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                counterText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                counterText.setText("0");
            }
        }.start();
    }

    private void performSnooze(int alarmId) {
        if (countDownTimer != null)
            countDownTimer.cancel();

        Intent serviceIntent = new Intent(this, RingtoneService.class);
        stopService(serviceIntent); // Stop ringing

        // Schedule Snooze
        long triggerTime = System.currentTimeMillis() + (AlarmConfig.SNOOZE_DELAY_SECONDS * 1000L);
        AlarmScheduler.scheduleSnooze(this, alarmId, triggerTime, 0);

        finish();
    }

    private void performDismiss() {
        if (countDownTimer != null)
            countDownTimer.cancel();

        Intent serviceIntent = new Intent(this, RingtoneService.class);
        stopService(serviceIntent); // Stop ringing

        // Update database: Turn off if not repeating
        int alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        if (alarmId != -1) {
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

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        try {
            unregisterReceiver(finishReceiver);
        } catch (Exception e) {
            // Receiver might not be registered or already unregistered
        }
    }
}