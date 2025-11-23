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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // preparing launch screen
        super.onCreate(savedInstanceState);
        // enabling full display on the screen
        EdgeToEdge.enable(this);
        // Setting the main view
        setContentView(R.layout.activity_main);
        // Avoiding system insets -> system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            navigateTo(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_alarm);
        }
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
                    .beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .commit();
        }
    }
}