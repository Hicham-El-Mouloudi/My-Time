package com.ensao.mytime.games.sudoku;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.ensao.mytime.R;

public class SudoKuMainActivity extends AppCompatActivity {

    private SudokuView sudokuView;
    private SudokuBoard board;
    private SudokuGenerator generator;

    private TextView timerText;
    private TextView difficultyText;
    private TextView hintCountText;
    private ImageButton btnUndo;
    private ImageButton btnRedo;
    private ImageButton btnHint;
    private ImageButton btnThemeToggle;

    private SharedPreferences prefs;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime;
    private long elapsedTime = 0;
    private boolean isTimerRunning = false;

    private SudokuGenerator.Difficulty currentDifficulty = SudokuGenerator.Difficulty.MEDIUM;
    private int hintsRemaining = 3;

    private List<Move> moveHistory;
    private List<Move> redoHistory;

    private static class Move {
        int row, col, oldValue, newValue;

        Move(int row, int col, int oldValue, int newValue) {
            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    // Alarm integration
    private int alarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.games_sudoku_main);

        // Initialize views
        sudokuView = findViewById(R.id.sudokuView);
        timerText = findViewById(R.id.timerText);
        difficultyText = findViewById(R.id.difficultyText);
        hintCountText = findViewById(R.id.hintCountText);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnHint = findViewById(R.id.btnHint);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);

        // Initialize preferences
        prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE);

        // Apply saved theme
        int savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        updateThemeIcon();

        // Initialize game components
        board = new SudokuBoard();
        generator = new SudokuGenerator();
        moveHistory = new ArrayList<>();
        redoHistory = new ArrayList<>();

        sudokuView.setBoard(board);

        // Setup timer
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    updateTimerDisplay();
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        // Setup button listeners
        setupNumberButtons();
        setupControlButtons();

        // Setup cell selection listener
        sudokuView.setOnCellSelectedListener((row, col) -> {
            // Cell selected, ready for input
        });

        // Start new game
        startNewGame(currentDifficulty);

        // Get alarm ID if launched from alarm
        alarmId = getIntent().getIntExtra("ALARM_ID", -1);
    }

    private void setupNumberButtons() {
        int[] buttonIds = { R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
                R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9 };

        for (int i = 0; i < buttonIds.length; i++) {
            final int number = i + 1;
            Button button = findViewById(buttonIds[i]);
            button.setOnClickListener(v -> onNumberButtonClicked(number));
        }

        // Erase button
        findViewById(R.id.btnErase).setOnClickListener(v -> onEraseClicked());
    }

    private void setupControlButtons() {
        findViewById(R.id.btnRefresh).setOnClickListener(v -> showDifficultyDialog());
        btnUndo.setOnClickListener(v -> undo());
        btnRedo.setOnClickListener(v -> redo());
        btnHint.setOnClickListener(v -> showHint());
        btnThemeToggle.setOnClickListener(v -> toggleTheme());
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode;

        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        // Save preference
        prefs.edit().putInt("theme_mode", newMode).apply();

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    private void updateThemeIcon() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            btnThemeToggle.setImageResource(R.drawable.ic_light_mode);
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_dark_mode);
        }
    }

    private void onNumberButtonClicked(int number) {
        int row = sudokuView.getSelectedRow();
        int col = sudokuView.getSelectedCol();

        if (row < 0 || col < 0) {
            Toast.makeText(this, "Please select a cell first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (board.isClue(row, col)) {
            Toast.makeText(this, "Cannot modify clue cells", Toast.LENGTH_SHORT).show();
            return;
        }

        // Place number
        int oldValue = board.getValue(row, col);
        if (oldValue != number) {
            moveHistory.add(new Move(row, col, oldValue, number));
            redoHistory.clear(); // Clear redo history on new move
            board.setValue(row, col, number);
            updateButtonStates();

            // Check if puzzle is solved
            if (board.isSolved()) {
                onPuzzleSolved();
            }
        }

        sudokuView.invalidate();
    }

    private void onEraseClicked() {
        int row = sudokuView.getSelectedRow();
        int col = sudokuView.getSelectedCol();

        if (row < 0 || col < 0) {
            Toast.makeText(this, "Please select a cell first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (board.isClue(row, col)) {
            Toast.makeText(this, "Cannot modify clue cells", Toast.LENGTH_SHORT).show();
            return;
        }

        int oldValue = board.getValue(row, col);
        if (oldValue != 0) {
            moveHistory.add(new Move(row, col, oldValue, 0));
            redoHistory.clear(); // Clear redo history on new move
            board.clearCell(row, col);
            updateButtonStates();
            sudokuView.invalidate();
        }
    }

    private void undo() {
        if (moveHistory.isEmpty()) {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
            return;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        redoHistory.add(lastMove);

        board.setValue(lastMove.row, lastMove.col, lastMove.oldValue);
        sudokuView.setSelectedCell(lastMove.row, lastMove.col);
        updateButtonStates();
        sudokuView.invalidate();
    }

    private void redo() {
        if (redoHistory.isEmpty()) {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
            return;
        }

        Move nextMove = redoHistory.remove(redoHistory.size() - 1);
        moveHistory.add(nextMove);

        board.setValue(nextMove.row, nextMove.col, nextMove.newValue);
        sudokuView.setSelectedCell(nextMove.row, nextMove.col);
        updateButtonStates();
        sudokuView.invalidate();
    }

    private void updateButtonStates() {
        // Update undo button state
        btnUndo.setEnabled(!moveHistory.isEmpty());
        btnUndo.setAlpha(moveHistory.isEmpty() ? 0.3f : 1.0f);

        // Update redo button state
        btnRedo.setEnabled(!redoHistory.isEmpty());
        btnRedo.setAlpha(redoHistory.isEmpty() ? 0.3f : 1.0f);
    }

    private void showHint() {
        if (hintsRemaining <= 0) {
            Toast.makeText(this, "No hints remaining!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find an empty cell that we can fill correctly
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board.getValue(i, j) == 0) {
                    int correctValue = board.getSolution()[i][j];

                    // Record move for undo
                    moveHistory.add(new Move(i, j, 0, correctValue));
                    redoHistory.clear();

                    board.setValue(i, j, correctValue);
                    sudokuView.setSelectedCell(i, j);
                    sudokuView.invalidate();

                    hintsRemaining--;
                    updateHintDisplay();

                    if (board.isSolved()) {
                        onPuzzleSolved();
                    }
                    return;
                }
            }
        }

        Toast.makeText(this, R.string.no_hints_available, Toast.LENGTH_SHORT).show();
    }

    private void updateHintDisplay() {
        hintCountText.setText(String.valueOf(hintsRemaining));
    }

    private void showDifficultyDialog() {
        String[] difficulties = { "Easy", "Medium", "Hard" };

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_difficulty)
                .setItems(difficulties, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentDifficulty = SudokuGenerator.Difficulty.EASY;
                            break;
                        case 1:
                            currentDifficulty = SudokuGenerator.Difficulty.MEDIUM;
                            break;
                        case 2:
                            currentDifficulty = SudokuGenerator.Difficulty.HARD;
                            break;
                    }
                    startNewGame(currentDifficulty);
                })
                .show();
    }

    private void startNewGame(SudokuGenerator.Difficulty difficulty) {
        // Generate new puzzle
        SudokuGenerator.PuzzleData puzzleData = generator.generate(difficulty);
        board.setPuzzle(puzzleData.puzzle, puzzleData.solution);

        // Reset game state
        moveHistory.clear();
        redoHistory.clear();
        hintsRemaining = 3;
        updateHintDisplay();
        updateButtonStates();

        // Update difficulty display
        String diffText = "";
        switch (difficulty) {
            case EASY:
                diffText = getString(R.string.difficulty_easy);
                break;
            case MEDIUM:
                diffText = getString(R.string.difficulty_medium);
                break;
            case HARD:
                diffText = getString(R.string.difficulty_hard);
                break;
        }
        difficultyText.setText(diffText);

        // Reset and start timer
        elapsedTime = 0;
        startTime = System.currentTimeMillis();
        isTimerRunning = true;
        timerHandler.post(timerRunnable);

        // Reset view
        sudokuView.setSelectedCell(-1, -1);
        sudokuView.invalidate();
    }

    private void updateTimerDisplay() {
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void onPuzzleSolved() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);

        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        // Disable alarm if non-repeating
        disableAlarmIfNeeded();

        new AlertDialog.Builder(this)
                .setTitle(R.string.victory_title)
                .setMessage(getString(R.string.victory_message, timeStr))
                .setPositiveButton(R.string.new_game, (dialog, which) -> {
                    if (alarmId != -1) {
                        finish(); // Exit if launched from alarm
                    } else {
                        showDifficultyDialog();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void disableAlarmIfNeeded() {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        isTimerRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (elapsedTime > 0 && !board.isSolved()) {
            startTime = System.currentTimeMillis() - elapsedTime;
            isTimerRunning = true;
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}