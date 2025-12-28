package com.ensao.mytime.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ensao.mytime.statistics.StatisticsHelper;

public abstract class BasePuzzleActivity extends AppCompatActivity implements Puzzleable {

    protected int alarmId = -1;
    protected boolean puzzleActive = false;

    // Receiver for puzzle reset requests (from fallback alarm) and finish requests
    // (from snooze)
    private final BroadcastReceiver puzzleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            android.util.Log.d("BasePuzzleActivity", "Received action: " + action);
            if (Puzzleable.ACTION_RESET_PUZZLE.equals(action)) {
                // Reset the puzzle
                onPuzzleReset();

                // Signal that we handled it (so alarm doesn't ring)
                setResultCode(android.app.Activity.RESULT_OK);
            } else if (Puzzleable.ACTION_FINISH_PUZZLE.equals(action)) {
                // Time's up or user snoozed from alarm UI - close game
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get alarm ID if launched from alarm
        alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        android.util.Log.d("BasePuzzleActivity", "onCreate - alarmId from intent: " + alarmId);

        // Log SharedPreferences state to verify tracking data exists
        android.content.SharedPreferences prefs = getSharedPreferences(
                com.ensao.mytime.home.AlarmScheduler.PREFS_NAME, MODE_PRIVATE);
        long firstAlarmTime = prefs.getLong(com.ensao.mytime.home.AlarmScheduler.KEY_FIRST_ALARM_TIME, 0);
        int ringCount = prefs.getInt(com.ensao.mytime.home.AlarmScheduler.KEY_RING_COUNT, 0);
        android.util.Log.d("BasePuzzleActivity", "SharedPrefs state - firstAlarmTime: " + firstAlarmTime +
                ", ringCount: " + ringCount);

        if (alarmId != -1) {
            android.util.Log.d("BasePuzzleActivity", "Puzzle registered for ALARM_ID: " + alarmId);
            onPuzzleModeActivated(alarmId);
            registerPuzzleReceiver();
        } else {
            android.util.Log.w("BasePuzzleActivity", "alarmId is -1, puzzle NOT registered for alarm mode!");
        }
    }

    private void registerPuzzleReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Puzzleable.ACTION_RESET_PUZZLE);
        filter.addAction(Puzzleable.ACTION_FINISH_PUZZLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(puzzleReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(puzzleReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d("BasePuzzleActivity", "onDestroy called");
        // Unregister receiver
        if (alarmId != -1) {
            try {
                unregisterReceiver(puzzleReceiver);
            } catch (Exception e) {
                // Ignore if not registered
            }
        }
    }

    @Override
    public void onPuzzleModeActivated(int alarmId) {
        this.alarmId = alarmId;
        this.puzzleActive = true;
    }

    @Override
    public boolean isPuzzleActive() {
        return puzzleActive;
    }

    @Override
    public int getAssociatedAlarmId() {
        return alarmId;
    }

    @Override
    public void onPuzzleSolved() {
        puzzleActive = false;
        // Broadcast puzzle completed to stop alarm and let service know
        Intent puzzleCompleteIntent = new Intent(Puzzleable.ACTION_PUZZLE_COMPLETED);
        puzzleCompleteIntent.putExtra(Puzzleable.EXTRA_ALARM_ID, alarmId);
        sendBroadcast(puzzleCompleteIntent);

        // Cancel the scheduled fallout alarm
        AlarmScheduler.cancelFalloutAlarm(this, alarmId);
    }

    /**
     * Called when the puzzle needs to be reset (e.g. from fall-back alarm trigger).
     * Subclasses should implement this to restart/reset the game implementation.
     */
    protected abstract void onPuzzleReset();

    /**
     * Helper method to handle puzzle completion flow:
     * 1. Notify listeners (onPuzzleSolved)
     * 2. Save stats
     * 3. Update DB (disable if non-repeating)
     * 4. Finish activity
     */
    protected void completePuzzleSession() {
        android.util.Log.d("BasePuzzleActivity", "completePuzzleSession called, alarmId=" + alarmId);
        if (alarmId != -1) {
            // Broadcast puzzle completed first
            onPuzzleSolved();

            // Save wake statistics when puzzle is solved
            android.util.Log.d("BasePuzzleActivity", "Calling StatisticsHelper.saveWakeStatistics");
            StatisticsHelper.saveWakeStatistics(this);

            new Thread(() -> {
                com.ensao.mytime.alarm.database.AlarmRepository repository = new com.ensao.mytime.alarm.database.AlarmRepository(
                        getApplication());
                com.ensao.mytime.alarm.database.Alarm alarm = repository.getAlarmByIdSync(alarmId);
                if (alarm != null && alarm.getDaysOfWeek() == 0) {
                    alarm.setEnabled(false);
                    repository.update(alarm);
                }
                // Finish activity after disabling alarm
                runOnUiThread(this::finish);
            }).start();
        } else {
            // Not in alarm mode, just finish or handle otherwise
            // For now, if called here, assumption is we want to exit
            finish();
        }
    }
}
