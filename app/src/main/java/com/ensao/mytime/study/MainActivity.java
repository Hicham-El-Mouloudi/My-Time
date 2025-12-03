package com.ensao.mytime.study;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.ensao.mytime.calendar.CalendarFragment;
import com.ensao.mytime.study.service.PomodoroService;
import com.ensao.mytime.study.fragments.StudySessionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PomodoroService.LocalBinder binder = (PomodoroService.LocalBinder) service;
            pomodoroService = binder.getService();
            isServiceBound = true;

            // Transférer la référence du service au fragment StudySession si actif
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Démarrer et lier le service Pomodoro
        startAndBindPomodoroService();

        setupNavigation();

        // Charger le fragment Study par défaut au démarrage
        if (savedInstanceState == null) {
            loadFragment(new StudySessionFragment());
            // Sélectionner l'item "Study" dans la navigation
            bottomNavigationView.setSelectedItemId(R.id.navigation_study);
        }
    }

    private void startAndBindPomodoroService() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);

        // Démarrer le service en avant-plan
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Lier le service pour la communication
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                selectedFragment = new StudySessionFragment();
            } else if (itemId == R.id.navigation_study) {
                selectedFragment = new StudySessionFragment();
            } else if (itemId == R.id.navigation_sleep) {
                selectedFragment = new StudySessionFragment();
            } else if (itemId == R.id.navigation_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.navigation_alarm) {
                selectedFragment = new StudySessionFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);

                // Transférer la référence du service au nouveau fragment
                transferServiceToFragment(selectedFragment);

                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void transferServiceToCurrentFragment() {
        // Transférer le service au fragment actuellement visible
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            transferServiceToFragment(currentFragment);
        }
    }

    private void transferServiceToFragment(Fragment fragment) {
        if (fragment instanceof StudySessionFragment && isServiceBound) {
            // Transférer la référence du service au fragment StudySession
            ((StudySessionFragment) fragment).setPomodoroService(pomodoroService, isServiceBound);
        }
        // Ajouter d'autres types de fragments si nécessaire
    }

    // Méthode pour que les fragments puissent accéder au service
    public PomodoroService getPomodoroService() {
        return pomodoroService;
    }

    public boolean isServiceBound() {
        return isServiceBound;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Délier le service quand l'activité est détruite
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}