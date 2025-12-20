package com.ensao.mytime;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.ensao.mytime.alarm.AlarmFragment;
import com.ensao.mytime.settings.SettingsFragment;
import com.ensao.mytime.sleep.SleepSessionFragment;
import com.ensao.mytime.statistics.StatisticsFragment;
import com.ensao.mytime.study.StudySessionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before view creation
        applyTheme();

        // preparing launch screen
        super.onCreate(savedInstanceState);

        // Register listener
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // enabling full display on the screen
        EdgeToEdge.enable(this);
        // Setting the main view
        setContentView(R.layout.activity_main);
        // Avoiding system insets -> system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // applyingg the bottom inset to the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            navigateTo(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) { // if the activity is created for the first time
            bottomNavigationView.setSelectedItemId(R.id.nav_alarm);
        }
    }

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
            // Recreate activity to apply theme immediately if needed (usually handled by
            // delegate but good practice if not)
            // recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void navigateTo(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.nav_alarm) {
            fragment = new AlarmFragment();
        } else if (itemId == R.id.nav_sleep_session) {
            fragment = new SleepSessionFragment();
        } else if (itemId == R.id.nav_study_session) {
            fragment = new StudySessionFragment();
        } else if (itemId == R.id.nav_statistics) {
            fragment = new StatisticsFragment();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()// mainContent is the id of the frame layout
                    .replace(R.id.mainContent, fragment)
                    .commit();
        }
    }
}