package com.ensao.mytime;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.ensao.mytime.alarm.AlarmFragment;
import com.ensao.mytime.calendar.CalendarFragment;
import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.home.HomeFragment;
import com.ensao.mytime.home.view.InvocationFragment;
import com.ensao.mytime.home.view.SettingsFragment;
import com.ensao.mytime.sleep.view.SleepFragment;
import com.ensao.mytime.statistics.StatisticsFragment;
import com.ensao.mytime.study.fragments.StudySessionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements InvocationFragment.InvocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private AlertDialog blockedDialog = null;
    private BottomNavigationView bottomNavigationView;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before view creation (from alarm-feature)
        applyTheme();

        super.onCreate(savedInstanceState);

        // Register preference listener (from alarm-feature)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupNavigation();
        requestNotificationPermission();

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }

        // Check for blocking on launch (from main)
        checkIntentForBlocking(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dynamic night mode based on sleep session (from main)
        checkNightModeDynamic();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkIntentForBlocking(intent);
    }

    // --- THEME HANDLING (from alarm-feature) ---

    private void applyTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "light");
        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("theme".equals(key)) {
            applyTheme();
        }
    }

    // --- BLOCKING LOGIC (from main) ---

    private void checkIntentForBlocking(Intent intent) {
        if (intent != null && intent.getBooleanExtra("is_blocked_mode", false)) {
            showBlockedMessage();
        }
    }

    private void showBlockedMessage() {
        if (blockedDialog != null && blockedDialog.isShowing()) {
            return;
        }

        blockedDialog = new AlertDialog.Builder(this)
                .setTitle("Mode Sommeil Actif 🌙")
                .setMessage("Cette application est bloquée pour vous aider à mieux dormir.")
                .setPositiveButton("Retourner à MyTime", (dialog, which) -> {
                    getIntent().removeExtra("is_blocked_mode");
                    blockedDialog = null;
                })
                .setCancelable(false)
                .show();
    }

    // --- DYNAMIC NIGHT MODE LOGIC (from main) ---

    public void checkNightModeDynamic() {
        SharedPreferences prefs = getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        boolean isSessionActive = prefs.getBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, false);

        if (isSessionActive) {
            long now = System.currentTimeMillis();
            long sleepTime = prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, 0);
            long wakeUpTime = prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, 0);

            long startTime = sleepTime - (2 * 60 * 60 * 1000);

            boolean shouldBeDark;
            if (wakeUpTime > startTime) {
                shouldBeDark = (now >= startTime && now <= wakeUpTime);
            } else {
                shouldBeDark = (now >= startTime || now <= wakeUpTime);
            }

            if (shouldBeDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // --- NAVIGATION AND SERVICES (from main + alarm-feature cleanup) ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister preference listener (from alarm-feature)
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Pomodoro logic moved to StudyViewModel
    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = navigateTo(item.getItemId());
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private Fragment navigateTo(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_study) {
            fragment = new StudySessionFragment();
        } else if (itemId == R.id.navigation_sleep) {
            fragment = new SleepFragment();
        } else if (itemId == R.id.navigation_calendar) {
            fragment = new CalendarFragment();
        } else if (itemId == R.id.navigation_alarm) {
            fragment = new AlarmFragment();
        } else if (itemId == R.id.nav_statistics) {
            fragment = new StatisticsFragment();
        }
        return fragment;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContent, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSettingsButtonClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContent, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAllInvocationsCompleted() {
    }
}