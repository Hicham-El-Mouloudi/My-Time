package com.ensao.mytime.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.AlarmRepository;

public class PuzzleTestActivity extends AppCompatActivity {

    private int alarmId;
    private AlarmRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        showOnLockScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_test);

        alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        repository = new AlarmRepository(getApplication());

        Button okButton = findViewById(R.id.puzzle_ok_button);
        okButton.setOnClickListener(v -> {
            // "Puzzle Solved" logic
            disableAlarmIfNeeded();
            Toast.makeText(this, "Great Job! Alarm Stopped.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void disableAlarmIfNeeded() {
        if (alarmId != -1) {
            new Thread(() -> {
                com.ensao.mytime.alarm.database.Alarm alarm = repository.getAlarmByIdSync(alarmId);
                if (alarm != null && alarm.getDaysOfWeek() == 0) {
                    alarm.setEnabled(false);
                    repository.update(alarm);
                }
            }).start();
        }
    }

    private void showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }
}
