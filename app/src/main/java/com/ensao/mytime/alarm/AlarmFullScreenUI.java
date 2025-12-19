package com.ensao.mytime.alarm;

import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFullScreenUI extends AppCompatActivity {

    private TextView alarmTimeText;
    private TextView alarmLabelText;
    private Button dismissButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show on lock screen and turn screen on
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
        alarmLabelText = findViewById(R.id.alarm_label_text);
        dismissButton = findViewById(R.id.dismiss_button);

        // Get alarm data from intent
        int alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        long alarmTime = getIntent().getLongExtra("ALARM_TIME", System.currentTimeMillis());
        String alarmLabel = getIntent().getStringExtra("ALARM_LABEL");

        // Format and display time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(new Date(alarmTime));
        alarmTimeText.setText(formattedTime);

        // Display label
        if (alarmLabel != null && !alarmLabel.isEmpty()) {
            alarmLabelText.setText(alarmLabel);
            alarmLabelText.setVisibility(View.VISIBLE);
        } else {
            alarmLabelText.setVisibility(View.GONE);
        }

        // Dismiss button
        dismissButton.setOnClickListener(v -> {
            // Stop the Ringtone Service
            Intent serviceIntent = new Intent(this, RingtoneService.class);
            stopService(serviceIntent);
            finish();
        });
    }
}