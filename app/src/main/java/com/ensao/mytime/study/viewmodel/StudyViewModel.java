package com.example.studysession.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.repository.StudyRepository;
import com.ensao.mytime.study.service.PomodoroService;
import java.util.List;
import android.util.Log;

public class StudyViewModel extends AndroidViewModel implements PomodoroService.PomodoroListener {

    private StudyRepository repository;
    private MutableLiveData<List<Subject>> allSubjects;

    private MutableLiveData<Integer> currentTime = new MutableLiveData<>();
    private MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>();
    private MutableLiveData<String> timerState = new MutableLiveData<>();

    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;

    private int initialTime = 25 * 60; // 25 minutes en secondes

    public StudyViewModel(Application application) {
        super(application);
        repository = new StudyRepository();
        allSubjects = repository.getAllSubjects();

        currentTime.setValue(initialTime);
        isTimerRunning.setValue(false);
        timerState.setValue("stopped");

        startPomodoroService();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PomodoroService.LocalBinder binder = (PomodoroService.LocalBinder) service;
            pomodoroService = binder.getService();
            pomodoroService.setPomodoroListener(StudyViewModel.this);
            isServiceBound = true;
            updateTimerFromService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            pomodoroService = null;
        }
    };

    private void startPomodoroService() {
        try {
            Intent intent = new Intent(getApplication(), PomodoroService.class);

            // Démarrer le service en avant-plan
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getApplication().startForegroundService(intent);
            } else {
                getApplication().startService(intent);
            }

            // Lier le service
            getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTimerFromService() {
        if (isServiceBound && pomodoroService != null) {
            long timeLeft = pomodoroService.getTimeLeft();
            if (timeLeft > 0) {
                currentTime.setValue((int) (timeLeft / 1000));
                isTimerRunning.setValue(pomodoroService.isTimerRunning());
                timerState.setValue(pomodoroService.isTimerRunning() ? "running" : "paused");
            }
        }
    }

    // Implémentation de l'interface PomodoroListener
    @Override
    public void onTimerTick(long millisUntilFinished) {
        currentTime.postValue((int) (millisUntilFinished / 1000));
    }

    @Override
    public void onTimerFinished() {
        currentTime.postValue(0);
        isTimerRunning.postValue(false);
        timerState.postValue("finished");

        // Optionnel : Redémarrer automatiquement ou afficher une notification
    }

    @Override
    public void onTimerStarted() {
        isTimerRunning.postValue(true);
        timerState.postValue("running");
    }

    @Override
    public void onTimerStopped() {
        currentTime.postValue(initialTime);
        isTimerRunning.postValue(false);
        timerState.postValue("stopped");
    }

    public MutableLiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public MutableLiveData<Integer> getCurrentTime() {
        return currentTime;
    }

    public MutableLiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    public MutableLiveData<String> getTimerState() {
        return timerState;
    }

    public void insertSubject(String subjectName) {
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            Subject subject = new Subject(subjectName.trim());
            repository.insertSubject(subject);
        }
    }

    public void updateSubject(Subject subject) {
        repository.updateSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        repository.deleteSubject(subject);
    }

    // === MÉTHODES DE CONTRÔLE DU TIMER ===

    public void startTimer() {
        if (isServiceBound && pomodoroService != null) {
            // Vérifier l'état actuel du timer
            if (pomodoroService.isTimerRunning()) {
                Log.d("TIMER_DEBUG", "Timer déjà en cours, ignore start");
                return;
            }

            long duration = initialTime * 1000L;
            pomodoroService.startTimer(duration);
            Log.d("TIMER_DEBUG", "Démarrage timer depuis ViewModel: " + duration + "ms");
        } else {
            Log.d("TIMER_DEBUG", "Service non lié, fallback timer");
            startFallbackTimer();
        }
    }

    public void pauseTimer() {
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.pauseTimer();
            Log.d("TIMER_DEBUG", "Pause demandée depuis ViewModel");
        } else {
            isTimerRunning.setValue(false);
            timerState.setValue("paused");
        }
    }

    public void resumeTimer() {
        if (isServiceBound && pomodoroService != null) {
            // Vérifier si on peut reprendre
            if (!pomodoroService.isTimerRunning() && pomodoroService.getTimeLeft() > 0) {
                pomodoroService.resumeTimer();
                Log.d("TIMER_DEBUG", "Reprise demandée depuis ViewModel");
            } else {
                Log.d("TIMER_DEBUG", "Impossible de reprendre - état incompatible");
            }
        } else {
            isTimerRunning.setValue(true);
            timerState.setValue("running");
        }
    }

    public void stopTimer() {
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.stopTimer();
            Log.d("TIMER_DEBUG", "Arrêt demandé depuis ViewModel");
        } else {
            onTimerStopped();
        }
    }

    public void setTimerDuration(int minutes) {
        this.initialTime = minutes * 60;
        if (!isTimerRunning.getValue()) {
            currentTime.setValue(initialTime);

            // Mettre à jour aussi la durée dans le service
            if (isServiceBound && pomodoroService != null) {
                pomodoroService.setTimerDuration(initialTime * 1000L);
            }
        }
    }

    // Méthode fallback si le service n'est pas disponible
    private void startFallbackTimer() {
        isTimerRunning.setValue(true);
        timerState.setValue("running");

        // Timer local de secours
        new android.os.CountDownTimer(initialTime * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                currentTime.postValue((int) (millisUntilFinished / 1000));
            }

            public void onFinish() {
                currentTime.postValue(0);
                isTimerRunning.postValue(false);
                timerState.postValue("finished");
            }
        }.start();
    }

    // Méthodes pour vérifier l'état du service
    public boolean isServiceBound() {
        return isServiceBound;
    }

    public PomodoroService getPomodoroService() {
        return pomodoroService;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Nettoyer les ressources
        if (isServiceBound) {
            getApplication().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}