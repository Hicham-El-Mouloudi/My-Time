package com.ensao.mytime.study.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.repository.StudyRepository;
import com.ensao.mytime.study.service.PomodoroService;

import java.util.List;

public class StudyViewModel extends AndroidViewModel implements PomodoroService.PomodoroListener {

    private StudyRepository repository;
    private LiveData<List<Subject>> allSubjects;

    private MutableLiveData<Integer> currentTime = new MutableLiveData<>();
    private MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>();
    private MutableLiveData<String> timerState = new MutableLiveData<>();

    // Pour savoir si on affiche "Travail" ou "Pause"
    private MutableLiveData<Boolean> isWorkMode = new MutableLiveData<>(true);

    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;

    private int initialTime = 25 * 60; // 25 minutes par défaut en secondes

    public StudyViewModel(Application application) {
        super(application);
        repository = new StudyRepository(application);
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
            if (currentSubject != null) {
                pomodoroService.setCurrentSubject(currentSubject);
            }
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

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getApplication().startForegroundService(intent);
            } else {
                getApplication().startService(intent);
            }

            getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTimerFromService() {
        if (isServiceBound && pomodoroService != null) {
            long timeLeft = pomodoroService.getTimeLeft();

            // Mise à jour du temps
            if (timeLeft > 0) {
                currentTime.setValue((int) (timeLeft / 1000));
            }

            // Mise à jour de l'état (Running/Paused)
            isTimerRunning.setValue(pomodoroService.isTimerRunning());

            String state = "stopped";
            if (pomodoroService.isTimerRunning()) {
                state = "running";
            } else {
                // Vérification sécurisée si la méthode existe, sinon on suppose false
                try {
                    // Si vous avez ajouté isTimerPaused() dans le Service :
                    if (pomodoroService.isTimerPaused()) {
                        state = "paused";
                    }
                } catch (NoSuchMethodError e) {
                    // Fallback si le Service n'est pas encore mis à jour
                    Log.w("ViewModel", "Méthode isTimerPaused manquante dans le service");
                }
            }
            timerState.setValue(state);

            // Mise à jour du mode (Travail/Pause)
            try {
                isWorkMode.setValue(pomodoroService.isWorkSession());
            } catch (NoSuchMethodError e) {
                isWorkMode.setValue(true);
            }
        }
    }
  
  
    private String currentSubject;

    public void setCurrentSubject(String subject) {
        this.currentSubject = subject;
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.setCurrentSubject(subject);
        }
    }

    // === Implémentation de l'interface PomodoroListener ===
    @Override
    public void onTimerTick(long millisUntilFinished) {
        currentTime.postValue((int) (millisUntilFinished / 1000));
    }

    @Override
    public void onTimerFinished() {
        // Attention: Avec le nouveau service "intelligent", onTimerFinished est appelé à la toute fin
        // ou entre les sessions. Ici, on ne reset à 0 que si tout est fini.
        // La gestion fine se fait via l'affichage du temps restant.
        currentTime.postValue(0);
        isTimerRunning.postValue(false);
        timerState.postValue("finished");

        // Note: Statistics saving is now handled by PomodoroService directly

        // Optionnel : Redémarrer automatiquement ou afficher une notification
    }

    @Override
    public void onTimerStarted() {
        isTimerRunning.postValue(true);
        timerState.postValue("running");
    }

    @Override
    public void onTimerPaused() {
        if (fallbackTimer != null) {
            fallbackTimer.cancel();
        }
        isTimerRunning.postValue(false);
        timerState.postValue("paused");
    }

    @Override
    public void onTimerStopped() {
        if (fallbackTimer != null) {
            fallbackTimer.cancel();
        }
        // On remet le temps initial choisi par l'utilisateur
        currentTime.postValue(initialTime);
        isTimerRunning.postValue(false);
        timerState.postValue("stopped");
    }

    @Override
    public void onModeChanged(boolean isWorkMode) {
        this.isWorkMode.postValue(isWorkMode);
    }

    // === Getters pour l'UI ===
    public LiveData<List<Subject>> getAllSubjects() {
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

    public MutableLiveData<Boolean> getIsWorkMode() {
        return isWorkMode;
    }

    // === Base de données ===
    public void insertSubject(String subjectName) {
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            Subject subject = new Subject(subjectName.trim());
            repository.insertSubject(subject);
            com.ensao.mytime.statistics.StatisticsHelper.updateTotalTasks(getApplication(), 1);
        }
    }

    /**
     * Updates the completion status of a subject and updates statistics
     * accordingly.
     * 
     * @param subject     The subject to update
     * @param isCompleted The new completion status
     */
    public void changeSubjectCompletion(Subject subject, boolean isCompleted) {
        if (subject.isCompleted() != isCompleted) {
            com.ensao.mytime.statistics.StatisticsHelper.updateTaskCompletionStats(
                    getApplication(),
                    subject.getCreatedAt(),
                    isCompleted);
            subject.setCompleted(isCompleted);
            repository.updateSubject(subject);
        }
    }

    public void updateSubject(Subject subject) {
        repository.updateSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        repository.deleteSubject(subject);
        // Only decrement total if it wasn't completed (as it was part of workload)
        // OR decrement if user deletes it entirely?
        // Usually if deleted, it's gone from stats? Let's decrement.
        com.ensao.mytime.statistics.StatisticsHelper.updateTotalTasks(getApplication(), -1);
    }

    // === MÉTHODES DE CONTRÔLE DU TIMER ===

    public void startTimer() {
        if (isServiceBound && pomodoroService != null) {
            if (pomodoroService.isTimerRunning()) {
                return;
            }

            // On envoie le temps total (ex: 60 min) au service
            long duration = initialTime * 1000L;
            pomodoroService.startTimer(duration);

        } else {
            startFallbackTimer();
        }
    }

    public void pauseTimer() {
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.pauseTimer();
        } else {
            if (fallbackTimer != null) {
                fallbackTimer.cancel();
            }
            isTimerRunning.setValue(false);
            timerState.setValue("paused");
        }
    }

    public void resumeTimer() {
        if (isServiceBound && pomodoroService != null) {
            // On vérifie simplement si on peut reprendre
            if (!pomodoroService.isTimerRunning()) {
                pomodoroService.resumeTimer();
            }
        } else {
            isTimerRunning.setValue(true);
            timerState.setValue("running");
        }
    }

    public void stopTimer() {
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.stopTimer();
        } else {
            onTimerStopped();
        }
    }

    public void setTimerDuration(int minutes) {
        this.initialTime = minutes * 60;

        // Si le timer est arrêté, on met à jour l'affichage immédiatement
        if (!isTimerRunning.getValue()) {
            currentTime.setValue(initialTime);
        }
        // Note: On n'appelle plus pomodoroService.setTimerDuration() ici car
        // le service recalculera tout au moment du startTimer()
    }

    // === Fallback (Secours si le service plante) ===
    private android.os.CountDownTimer fallbackTimer;

    private void startFallbackTimer() {
        if (fallbackTimer != null) {
            fallbackTimer.cancel();
        }

        isTimerRunning.setValue(true);
        timerState.setValue("running");

        fallbackTimer = new android.os.CountDownTimer(initialTime * 1000L, 1000) {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (isServiceBound) {
            getApplication().unbindService(serviceConnection);
            isServiceBound = false;
        }

        if (fallbackTimer != null) {
            fallbackTimer.cancel();
            fallbackTimer = null;
        }
    }
}
