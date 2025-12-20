package com.ensao.mytime.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFullScreenUI extends AppCompatActivity {

    private TextView counterText;
    private TextView alarmTimeText;
    private View swipeButton;
    private View swipeBtnContainer;

    // Swipe Logic Vars
    private float dX = 0f;
    private float initialX = 0f;
    private boolean isSwiping = false;
    private static final float SWIPE_THRESHOLD_DP = 100f; // Distance to trigger action
    private float swipeThresholdPx;

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
        swipeButton = findViewById(R.id.swipe_button);
        swipeBtnContainer = findViewById(R.id.swipe_btn_container);

        // Calculate px threshold
        swipeThresholdPx = SWIPE_THRESHOLD_DP * getResources().getDisplayMetrics().density;

        // Get alarm data from intent
        int alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        long alarmTime = getIntent().getLongExtra("ALARM_TIME", System.currentTimeMillis());
        // String alarmLabel = getIntent().getStringExtra("ALARM_LABEL");

        // Format and display time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(new Date(alarmTime));
        alarmTimeText.setText(formattedTime);

        // Start Countdown
        startCountdown();

        // Setup Swipe Listener
        setupSwipeListener(alarmId);

        // Start Pulse Animation
        startPulseAnimation();

        // Register Receiver to finish activity when service stops (e.g. dismissed from
        // notification)
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
        android.view.animation.ScaleAnimation pulse = new android.view.animation.ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(1000);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        swipeButton.startAnimation(pulse);
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
                // Service handles auto-snooze/stop, activity will be finished or updated
                // separately?
                // Actually, duplicate logic: Activity is just UI. Service does the heavy
                // lifting.
                // We could finish() here or wait for user.
            }
        }.start();
    }

    private void setupSwipeListener(int alarmId) {
        swipeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.clearAnimation(); // Stop pulsing when touched
                    isSwiping = true;
                    initialX = v.getX();
                    dX = v.getX() - event.getRawX();
                    return true;

                case android.view.MotionEvent.ACTION_MOVE:
                    if (isSwiping) {
                        float newX = event.getRawX() + dX;
                        // Limit bounds (simple clamp within container width/2 approx)
                        // This logic centers around 0 relative to parent...
                        // Actually CardView is in FrameLayout, using layout_gravity="center".
                        // We need to move it relative to its initial centered position.

                        // Let's rely on translationX
                        float translationX = event.getRawX() + dX - initialX;

                        // Limit translation
                        if (translationX > swipeThresholdPx * 1.5f)
                            translationX = swipeThresholdPx * 1.5f;
                        if (translationX < -swipeThresholdPx * 1.5f)
                            translationX = -swipeThresholdPx * 1.5f;

                        v.setTranslationX(translationX);
                    }
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                    isSwiping = false;
                    float currentTranslationX = v.getTranslationX();

                    if (currentTranslationX > swipeThresholdPx) {
                        // Swipe Right -> Snooze
                        performSnooze(alarmId);
                    } else if (currentTranslationX < -swipeThresholdPx) {
                        // Swipe Left -> Dismiss
                        performDismiss();
                    } else {
                        // Reset
                        v.animate().translationX(0f).setDuration(300).start();
                    }
                    return true;
            }
            return false;
        });
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