package com.ensao.mytime.games.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.Puzzleable;

public class MinesweeperGameActivity extends AppCompatActivity
        implements MinesweeperView.OnCellClickListener, Puzzleable {

    private MinesweeperView minesweeperView;
    private TextView tvMineCount;
    private TextView tvTimer;
    private ImageButton btnRestart;
    private ImageButton btnToggleMode;
    private ImageButton btnDarkMode;
    private LinearLayout rootLayout;
    private MinesweeperEngine engine;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private boolean isTimerRunning = false;
    private boolean isFlagMode = false;
    private boolean isDarkMode = true;

    // Alarm integration
    private int alarmId = -1;
    private boolean puzzleActive = false;

    // Default difficulty (can be set from intent later)
    private Difficulty currentDifficulty = Difficulty.BEGINNER;

    // Theme colors
    private static final int DARK_BG = 0xFF1A1A2E;
    private static final int LIGHT_BG = 0xFFF5F5F5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.games_minesweeper_main);

        minesweeperView = findViewById(R.id.minesweeperView);
        tvMineCount = findViewById(R.id.tvMineCount);
        tvTimer = findViewById(R.id.tvTimer);
        btnRestart = findViewById(R.id.btnRestart);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        btnDarkMode = findViewById(R.id.btnDarkMode);
        rootLayout = findViewById(R.id.rootLayout);

        btnRestart.setOnClickListener(v -> showDifficultyDialog());
        btnToggleMode.setOnClickListener(v -> toggleFlagMode());
        btnDarkMode.setOnClickListener(v -> toggleDarkMode());

        minesweeperView.setOnCellClickListener(this);

        // Get alarm ID if launched from alarm
        alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        if (alarmId != -1) {
            onPuzzleModeActivated(alarmId);
        }

        startNewGame();
    }

    // Puzzleable interface implementation
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
    }

    private void showDifficultyDialog() {
        String[] items = new String[] {
                "Beginner (9x9 - 10 Mines)",
                "Intermediate (16x16 - 40 Mines)",
                "Expert (16x30 - 99 Mines)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Difficulty")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentDifficulty = Difficulty.BEGINNER;
                            break;
                        case 1:
                            currentDifficulty = Difficulty.INTERMEDIATE;
                            break;
                        case 2:
                            currentDifficulty = Difficulty.EXPERT;
                            break;
                    }
                    startNewGame();
                })
                .show();
    }

    private void startNewGame() {
        engine = new MinesweeperEngine(currentDifficulty);

        // Set listener for first click to start timer
        engine.setGameStartListener(() -> {
            if (!isTimerRunning) {
                startTimer();
            }
        });

        minesweeperView.setEngine(engine);
        updateFlagCounter();
        resetTimer();
        isFlagMode = false;
        updateFlagModeUI();
        btnRestart.setImageResource(R.drawable.ic_face_smile);
    }

    private void toggleFlagMode() {
        isFlagMode = !isFlagMode;
        minesweeperView.setFlagMode(isFlagMode);
        updateFlagModeUI();
    }

    private void updateFlagModeUI() {
        if (isFlagMode) {
            btnToggleMode.setImageResource(R.drawable.ic_flag);
            btnToggleMode.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_orange_dark, null));
        } else {
            btnToggleMode.setImageResource(R.drawable.ic_shovel);
            btnToggleMode.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_blue_dark, null));
        }
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        if (isDarkMode) {
            btnDarkMode.setImageResource(R.drawable.ic_mode_dark);
            rootLayout.setBackgroundColor(DARK_BG);
            minesweeperView.setDarkMode(true);
        } else {
            btnDarkMode.setImageResource(R.drawable.ic_mode_light);
            rootLayout.setBackgroundColor(LIGHT_BG);
            minesweeperView.setDarkMode(false);
        }
        minesweeperView.invalidate();
    }

    private void updateFlagCounter() {
        int remaining = engine.getRemainingFlags();
        int total = engine.getTotalMines();
        tvMineCount.setText(String.format("%d/%d", remaining, total));
    }

    private void resetTimer() {
        stopTimer();
        tvTimer.setText("000");
        startTime = System.currentTimeMillis();
    }

    private void startTimer() {
        isTimerRunning = true;
        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isTimerRunning)
                return;

            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);

            // Cap at 999 seconds for display
            if (seconds > 999)
                seconds = 999;

            tvTimer.setText(String.format("%03d", seconds));

            timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onCellClick(int row, int col) {
        if (engine.isGameOver())
            return;

        if (isFlagMode) {
            // Flag mode - toggle flag
            engine.toggleFlag(row, col);
            updateFlagCounter();
            minesweeperView.invalidate();
        } else {
            // Reveal mode - reveal cell
            engine.reveal(row, col);
            minesweeperView.invalidate();

            if (engine.isGameOver()) {
                stopTimer();
                if (engine.isGameWon()) {
                    btnRestart.setImageResource(R.drawable.ic_face_win);
                    Toast.makeText(this, "🎉 Congratulations! You won!", Toast.LENGTH_LONG).show();
                    disableAlarmIfNeeded();
                } else {
                    btnRestart.setImageResource(R.drawable.ic_face_dead);
                    Toast.makeText(this, "💥 Game Over! You hit a mine.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void disableAlarmIfNeeded() {
        if (alarmId != -1) {
            // Broadcast puzzle completed first
            onPuzzleSolved();

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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
