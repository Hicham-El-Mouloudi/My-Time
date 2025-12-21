package com.ensao.mytime;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

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
import com.ensao.mytime.study.service.PomodoroService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements InvocationFragment.InvocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private AlertDialog blockedDialog = null;
    private BottomNavigationView bottomNavigationView;
    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
            });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PomodoroService.LocalBinder binder = (PomodoroService.LocalBinder) service;
            pomodoroService = binder.getService();
            isServiceBound = true;
            transferServiceToCurrentFragment();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            pomodoroService = null;
        }
    };

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

        startAndBindPomodoroService();
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

        // Check the actual current night mode from configuration
        int currentNightMode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkModeActive = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        boolean shouldBeDark = "dark".equals(theme);

        // Only change the mode if it's actually different from what's currently applied
        if (isDarkModeActive != shouldBeDark) {
            int desiredMode = shouldBeDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(desiredMode);
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

        // Check the actual current night mode from configuration
        int currentNightMode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkModeActive = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        boolean shouldBeDark;
        if (isSessionActive) {
            long now = System.currentTimeMillis();
            long sleepTime = prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, 0);
            long wakeUpTime = prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, 0);

            long startTime = sleepTime - (2 * 60 * 60 * 1000);

            if (wakeUpTime > startTime) {
                shouldBeDark = (now >= startTime && now <= wakeUpTime);
            } else {
                shouldBeDark = (now >= startTime || now <= wakeUpTime);
            }
        } else {
            shouldBeDark = false;
        }

        // Only change the mode if it's different from the current applied mode
        if (isDarkModeActive != shouldBeDark) {
            int desiredMode = shouldBeDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(desiredMode);
        }
    }

    // --- NAVIGATION AND SERVICES (from main + alarm-feature cleanup) ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister preference listener (from alarm-feature)
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        // Unbind service (from main)
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void startAndBindPomodoroService() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = navigateTo(item.getItemId());
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                transferServiceToFragment(selectedFragment);
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

    private void transferServiceToCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainContent);
        if (currentFragment != null) {
            transferServiceToFragment(currentFragment);
        }
    }

    private void transferServiceToFragment(Fragment fragment) {
        if (fragment instanceof StudySessionFragment && isServiceBound) {
            ((StudySessionFragment) fragment).setPomodoroService(pomodoroService, isServiceBound);
        }
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