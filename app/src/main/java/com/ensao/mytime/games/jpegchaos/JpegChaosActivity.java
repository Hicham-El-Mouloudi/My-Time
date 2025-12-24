package com.ensao.mytime.games.jpegchaos;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.Puzzleable;
import com.ensao.mytime.statistics.StatisticsHelper;

import java.io.IOException;
import java.io.InputStream;

public class JpegChaosActivity extends AppCompatActivity implements Puzzleable {
    public final String TAG = "JpegChaos";
    private final int COLS = 3;
    private final int ROWS = 5;
    private PuzzleGrid grid;
    private Bitmap masterBitmap;
    private ImageView hintBtn;
    private ImageView moreBtn;
    private PuzzleView puzzleView;

    // Settings state
    private boolean isSoundEnabled = true;
    private boolean isMusicEnabled = false;
    private TextView tvHintCount;
    private int hintCount = 2;

    // Audio
    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int soundDrag, soundDrop, soundUnite, soundWin, soundHint;
    private int soundPopup, soundReset, soundSoundOn, soundSoundOff, soundMusicOff;
    private boolean soundsLoaded = false;

    // Alarm integration
    private int alarmId = -1;
    private boolean puzzleActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.games_jpegchaos_main);

        puzzleView = findViewById(R.id.puzzleView);
        moreBtn = findViewById(R.id.btn_menu_left);
        hintBtn = findViewById(R.id.btn_menu_right);
        tvHintCount = findViewById(R.id.tv_hint_count);

        if (!org.opencv.android.OpenCVLoader.initDebug()) {
            android.util.Log.e(TAG, "OpenCV initialization failed!");
        } else {
            android.util.Log.d(TAG, "OpenCV initialized successfully");
        }

        // Setup more button click listener
        moreBtn.setOnClickListener(v -> {
            playSound(soundPopup);
            showMoreOptionsDialog();
        });

        // Setup hint button click listener
        hintBtn.setOnClickListener(v -> {
            if (hintCount > 0) {
                if (puzzleView.showHint()) {
                    hintCount--;
                    updateHintCount();
                }
            }
        });
        updateHintCount();

        // Setup win listener
        puzzleView.setOnWinListener(() -> {
            playSound(soundWin);
            showWinDialog();
        });

        // Setup sound listener
        puzzleView.setOnSoundListener(new PuzzleView.OnSoundListener() {
            @Override
            public void onDrag() {
                playSound(soundDrag);
            }

            @Override
            public void onDrop() {
                playSound(soundDrop);
            }

            @Override
            public void onUnite() {
                playSound(soundUnite);
            }

            @Override
            public void onHint() {
                playSound(soundHint);
            }
        });

        initAudio();
        loadLevel();

        // Get alarm ID if launched from alarm
        alarmId = getIntent().getIntExtra("ALARM_ID", -1);
        if (alarmId != -1) {
            onPuzzleModeActivated(alarmId);
        }
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

    private void initAudio() {
        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sounds
        soundDrag = soundPool.load(this, R.raw.drag_sound, 1);
        soundDrop = soundPool.load(this, R.raw.drop_sound, 1);
        soundUnite = soundPool.load(this, R.raw.unite_sound, 1);
        soundWin = soundPool.load(this, R.raw.win_sound, 1);
        soundHint = soundPool.load(this, R.raw.hint_sound, 1);
        soundPopup = soundPool.load(this, R.raw.pop_up_sound, 1);
        soundReset = soundPool.load(this, R.raw.reset_btn_sound, 1);
        soundSoundOn = soundPool.load(this, R.raw.sound_on, 1);
        soundSoundOff = soundPool.load(this, R.raw.sound_off, 1);
        soundMusicOff = soundPool.load(this, R.raw.music_off_sound, 1);

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundsLoaded = true;
        });

        // Initialize MediaPlayer
        startMusic();
    }

    private void startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.game_music_loop);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
            }
        }
        if (isMusicEnabled && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void playSound(int soundId) {
        if (isSoundEnabled && soundsLoaded) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMusicEnabled) {
            startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (masterBitmap != null && !masterBitmap.isRecycled()) {
            masterBitmap.recycle();
        }
    }

    private void updateHintCount() {
        if (tvHintCount != null) {
            tvHintCount.setText(String.valueOf(hintCount));
            if (hintCount == 0) {
                hintBtn.setAlpha(0.5f);
            }
        }
    }

    private void showMoreOptionsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.games_jpegchaos_more_options_dialog);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout btnReset = dialog.findViewById(R.id.btn_reset);
        LinearLayout btnToggleSound = dialog.findViewById(R.id.btn_toggle_sound);
        LinearLayout btnToggleMusic = dialog.findViewById(R.id.btn_toggle_music);
        ImageView iconSound = dialog.findViewById(R.id.icon_sound);
        ImageView iconMusic = dialog.findViewById(R.id.icon_music);

        // Set initial icons based on state
        updateSoundIcon(iconSound);
        updateMusicIcon(iconMusic);

        // Reset button
        btnReset.setOnClickListener(v -> {
            playSound(soundReset);
            resetPuzzle();
            dialog.dismiss();
        });

        // Toggle sound button
        btnToggleSound.setOnClickListener(v -> {
            isSoundEnabled = !isSoundEnabled;
            playSound(isSoundEnabled ? soundSoundOn : soundSoundOff);
            updateSoundIcon(iconSound);
        });

        // Toggle music button
        btnToggleMusic.setOnClickListener(v -> {
            isMusicEnabled = !isMusicEnabled;
            if (!isMusicEnabled)
                playSound(soundMusicOff);
            updateMusicIcon(iconMusic);
            if (isMusicEnabled)
                startMusic();
            else
                stopMusic();
        });

        dialog.show();
    }

    private void updateSoundIcon(ImageView icon) {
        if (isSoundEnabled) {
            icon.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            icon.setColorFilter(0xFF4CAF50); // Green
        } else {
            icon.setImageResource(android.R.drawable.ic_lock_silent_mode);
            icon.setColorFilter(0xFF999999); // Gray
        }
    }

    private void updateMusicIcon(ImageView icon) {
        if (isMusicEnabled) {
            icon.setImageResource(android.R.drawable.ic_media_play);
            icon.setColorFilter(0xFF2196F3); // Blue
        } else {
            icon.setImageResource(android.R.drawable.ic_media_pause);
            icon.setColorFilter(0xFF999999); // Gray
        }
    }

    private void resetPuzzle() {
        loadLevel();
    }

    private void showWinDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.games_jpegchaos_win_dialog);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);

        android.widget.Button btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            disableAlarmIfNeeded();
            finish();
        });

        dialog.show();
    }

    private void disableAlarmIfNeeded() {
        if (alarmId != -1) {
            // Broadcast puzzle completed first
            onPuzzleSolved();

            // Save wake statistics when puzzle is solved
            StatisticsHelper.saveWakeStatistics(this);

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

    private void loadLevel() {
        // Remove manual recycling to let GC handle it safely
        // preventing race conditions with GPU rendering

        try {
            String selectedImageFileName = null;
            String[] imageFiles = getAssets().list("images");
            if (imageFiles == null)
                imageFiles = new String[0];

            // Filter for valid image files
            java.util.List<String> validImages = new java.util.ArrayList<>();
            for (String file : imageFiles) {
                String lower = file.toLowerCase();
                // Check if file starts with 'img_' as requested
                if (!lower.startsWith("img_"))
                    continue;

                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                        || lower.endsWith(".webp")) {
                    validImages.add(file);
                }
            }

            if (validImages.isEmpty()) {
                android.util.Log.e(TAG, "No valid images found in assets/images folder.");
                return;
            }

            // Select a random image
            java.util.Random random = new java.util.Random();
            selectedImageFileName = validImages.get(random.nextInt(validImages.size()));
            android.util.Log.i(TAG, "Selected random image: " + selectedImageFileName);

            // Load the master bitmap
            masterBitmap = loadScaledBitmap(selectedImageFileName);

            if (masterBitmap == null) {
                android.util.Log.e(TAG, "Failed to decode bitmap: " + selectedImageFileName);
                return;
            }

            android.util.Log.i(TAG, "Loaded bitmap: " + masterBitmap.getWidth() + "x" + masterBitmap.getHeight());

            // Initialize the grid
            grid = new PuzzleGrid(ROWS, COLS);
            grid.shuffle();

            // Pass data to view
            puzzleView.setData(grid, masterBitmap);

        } catch (IOException e) {
            android.util.Log.e(TAG, "Error loading image from assets.", e);
        }
    }

    private Bitmap loadScaledBitmap(String filename) throws IOException {
        InputStream is = null;
        Bitmap bitmap = null;

        // 1. Load Source Bitmap
        try {
            is = new java.io.BufferedInputStream(getAssets().open("images/" + filename));
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to load bitmap stream", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (bitmap == null) {
            android.util.Log.e(TAG, "BitmapFactory returned null for: " + filename);
            return null;
        }

        // 2. Convert to OpenCV Mat
        org.opencv.core.Mat srcMat = new org.opencv.core.Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, srcMat);
        bitmap.recycle(); // Release original bitmap immediately

        // 3. Resize Logic (Strict 720x1080 target)
        int targetWidth = 720;
        int targetHeight = 1080;

        int srcW = srcMat.cols();
        int srcH = srcMat.rows();

        // Calculate scale to COVER the target area
        double scaleW = (double) targetWidth / srcW;
        double scaleH = (double) targetHeight / srcH;
        double scale = Math.max(scaleW, scaleH);

        int scaledW = (int) Math.ceil(srcW * scale);
        int scaledH = (int) Math.ceil(srcH * scale);

        org.opencv.core.Mat resizedMat = new org.opencv.core.Mat();
        org.opencv.imgproc.Imgproc.resize(srcMat, resizedMat, new org.opencv.core.Size(scaledW, scaledH));
        srcMat.release(); // Release source

        // 4. Center Crop Logic
        int x = (scaledW - targetWidth) / 2;
        int y = (scaledH - targetHeight) / 2;

        // Safety clamp (though math should be safe)
        x = Math.max(0, x);
        y = Math.max(0, y);
        if (x + targetWidth > scaledW)
            x = 0;
        if (y + targetHeight > scaledH)
            y = 0;

        org.opencv.core.Rect roi = new org.opencv.core.Rect(x, y, targetWidth, targetHeight);
        org.opencv.core.Mat croppedMat = new org.opencv.core.Mat(resizedMat, roi);

        // 5. Output Final Bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(croppedMat, resultBitmap);

        // Ensure opaque (prevents alpha blending artifacts) and pre-upload to GPU
        resultBitmap.setHasAlpha(false);
        resultBitmap.prepareToDraw();

        // Cleanup
        resizedMat.release();
        croppedMat.release(); // Releases the ROI header

        android.util.Log.i(TAG, "Loaded strict bitmap: " + resultBitmap.getWidth() + "x" + resultBitmap.getHeight());
        return resultBitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width close to the requested height and width.
            while ((height / inSampleSize) > reqHeight
                    || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}