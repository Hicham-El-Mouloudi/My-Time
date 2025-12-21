package com.ensao.mytime.study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensao.mytime.R;
import com.ensao.mytime.study.adapter.SubjectAdapter;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.service.PomodoroService;
import com.ensao.mytime.study.viewmodel.StudyViewModel;
import java.util.ArrayList;

public class StudySessionFragment extends Fragment {

    private StudyViewModel studyViewModel;

    // Éléments UI - adaptés à votre layout
    private TextView tvTimer;
    private Button btnStart, btnPause, btnStop;
    private Button btn25Min, btn45Min, btnCustom;
    private EditText etSubjectName;
    private Button btnAddSubject;
    private RecyclerView rvSubjects;

    // AJOUT: Adapter pour les subjects
    private SubjectAdapter subjectAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_session, container, false);

        // Initialiser le ViewModel
        studyViewModel = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);

        // Initialiser les vues
        initViews(view);

        // Configurer la RecyclerView - DOIT ÊTRE AVANT l'observation!
        setupRecyclerView();

        // Observer les LiveData
        observeViewModel();

        // Configurer les boutons
        setupButtonListeners();

        return view;
    }

    private void initViews(View view) {
        // Timer et contrôles principaux
        tvTimer = view.findViewById(R.id.tv_timer);
        btnStart = view.findViewById(R.id.btn_start);
        btnPause = view.findViewById(R.id.btn_pause);
        btnStop = view.findViewById(R.id.btn_stop);

        // Boutons de durée
        btn25Min = view.findViewById(R.id.btn_25_min);
        btn45Min = view.findViewById(R.id.btn_45_min);
        btnCustom = view.findViewById(R.id.btn_custom);

        // Gestion des matières
        etSubjectName = view.findViewById(R.id.et_subject_name);
        btnAddSubject = view.findViewById(R.id.btn_add_subject);
        rvSubjects = view.findViewById(R.id.rv_subjects);

        // Initialiser l'état des boutons (idle state)
        updateButtonVisibility("stopped");
    }

    private void setupRecyclerView() {
        // CORRECTION: Initialiser l'adapter avec une liste vide
        subjectAdapter = new SubjectAdapter(new ArrayList<>(), new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onSubjectChecked(Subject subject, boolean isChecked) {
                // Mettre à jour le sujet quand la checkbox est cochée/décochée
                studyViewModel.updateSubject(subject);
                Log.d("SUBJECT_DEBUG", "Subject checked: " + subject.getName() + " - " + isChecked);
            }

            @Override
            public void onSubjectDeleted(Subject subject) {
                // Supprimer le sujet
                studyViewModel.deleteSubject(subject);
                Log.d("SUBJECT_DEBUG", "Subject deleted: " + subject.getName());
            }
        });

        // Configurer la RecyclerView
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSubjects.setAdapter(subjectAdapter); // IMPORTANT: Assigner l'adapter

        Log.d("RECYCLER_DEBUG", "RecyclerView configurée avec l'adapter");
    }

    private void observeViewModel() {
        // Observer le temps restant
        studyViewModel.getCurrentTime().observe(getViewLifecycleOwner(), timeInSeconds -> {
            if (timeInSeconds != null && tvTimer != null) {
                updateTimerDisplay(timeInSeconds);
            }
        });

        // Observer l'état textuel du timer for button visibility and color
        studyViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) {
                // Update button visibility based on state
                updateButtonVisibility(state);

                // Update duration buttons state
                boolean isRunning = "running".equals(state) || "paused".equals(state);
                updateDurationButtonsState(isRunning);

                // Update timer text color
                if (tvTimer != null) {
                    switch (state) {
                        case "running":
                            tvTimer.setTextColor(getResources().getColor(R.color.aurora_primary));
                            break;
                        case "paused":
                            tvTimer.setTextColor(getResources().getColor(R.color.medium_gray));
                            break;
                        case "stopped":
                            tvTimer.setTextColor(getResources().getColor(R.color.aurora_primary));
                            break;
                    }
                }
            }
        });

        // CORRECTION: Observer la liste des matières - CETTE PARTIE ÉTAIT MANQUANTE!
        studyViewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            Log.d("SUBJECT_DEBUG", "Subjects observés: " + (subjects != null ? subjects.size() : "null"));

            if (subjects != null) {
                // Mettre à jour l'adapter avec la nouvelle liste
                subjectAdapter.setSubjects(subjects);

                // Debug: Afficher chaque subject
                for (Subject subject : subjects) {
                    Log.d("SUBJECT_DEBUG", "Subject: " + subject.getName());
                }
            } else {
                Log.d("SUBJECT_DEBUG", "La liste de subjects est null");
            }
        });
    }

    // Ajoutez cette méthode dans StudySessionFragment.java
    private void showCustomDurationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());

        builder.setTitle("Durée personnalisée");

        // Créer une vue avec un NumberPicker
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_duration, null);
        android.widget.NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);

        // Configurer le NumberPicker
        numberPicker.setMinValue(1); // 1 minute minimum
        numberPicker.setMaxValue(120); // 120 minutes maximum
        numberPicker.setValue(30); // Valeur par défaut
        numberPicker.setWrapSelectorWheel(false);

        // Afficher les valeurs avec "min"
        numberPicker.setFormatter(value -> value + " min");

        builder.setView(dialogView)
                .setPositiveButton("Démarrer", (dialog, which) -> {
                    int selectedMinutes = numberPicker.getValue();

                    // Définir la durée personnalisée
                    studyViewModel.setTimerDuration(selectedMinutes);

                    // Mettre à jour l'affichage du timer
                    tvTimer.setText(String.format("%02d:00", selectedMinutes));

                    // Mettre à jour l'apparence du bouton
                    updateDurationButtonSelection(btnCustom);

                    // Changer le texte du bouton pour afficher la durée choisie
                    btnCustom.setText(selectedMinutes + " min");
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    dialog.dismiss();
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupButtonListeners() {
        // Contrôles du timer
        btnStart.setOnClickListener(v -> {
            // Check if we're resuming from pause
            String currentState = studyViewModel.getTimerState().getValue();
            if ("paused".equals(currentState)) {
                studyViewModel.resumeTimer();
                Log.d("TIMER", "btn resume");
            } else {
                studyViewModel.startTimer();
                Log.d("TIMER", "btn start");
            }
        });

        btnPause.setOnClickListener(v -> {
            studyViewModel.pauseTimer();
            Log.d("TIMER", "btn pause");
        });

        btnStop.setOnClickListener(v -> {
            studyViewModel.stopTimer();
            Log.d("TIMER", "btn stop");
        });

        // Boutons de durée
        btn25Min.setOnClickListener(v -> {
            studyViewModel.setTimerDuration(25);
            tvTimer.setText("25:00");
            updateDurationButtonSelection(btn25Min);
        });

        btn45Min.setOnClickListener(v -> {
            studyViewModel.setTimerDuration(45);
            tvTimer.setText("45:00");
            updateDurationButtonSelection(btn45Min);
        });

        btnCustom.setOnClickListener(v -> {
            // Ouvrir le dialog pour choisir la durée
            showCustomDurationDialog();
        });

        // Ajout de matière
        btnAddSubject.setOnClickListener(v -> {
            String subjectName = etSubjectName.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                Log.d("SUBJECT_DEBUG", "Tentative d'ajout: " + subjectName);
                studyViewModel.insertSubject(subjectName);
                etSubjectName.setText("");
                hideKeyboard();
            } else {
                Log.d("SUBJECT_DEBUG", "Nom de subject vide");
            }
        });
    }

    private void updateTimerDisplay(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        if (tvTimer != null) {
            tvTimer.setText(timeString);
        }
    }

    /**
     * Update button visibility based on timer state.
     * - stopped: Show only Start button
     * - running: Show Pause and Stop buttons
     * - paused: Show Resume (Start button) and Stop buttons
     */
    private void updateButtonVisibility(String state) {
        if (btnStart == null || btnPause == null || btnStop == null)
            return;

        switch (state) {
            case "running":
                // Running: Show Pause and Stop, hide Start
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                break;
            case "paused":
                // Paused: Show Resume (Start button) and Stop, hide Pause
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setText("Resume");
                btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_mint));
                btnPause.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                break;
            case "stopped":
            default:
                // Stopped/Idle: Show only Start, hide Pause and Stop
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setText("Start");
                btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_mint));
                btnPause.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                break;
        }
    }

    // updateButtonAppearance is no longer needed as visibility is now managed by
    // updateButtonVisibility

    private void updateDurationButtonsState(boolean isTimerRunning) {
        boolean enabled = !isTimerRunning;
        btn25Min.setEnabled(enabled);
        btn45Min.setEnabled(enabled);
        btnCustom.setEnabled(enabled);

        // Update colors based on enabled state
        if (enabled) {
            btn25Min.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_blue));
            btn45Min.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_dark_teal));
            btnCustom.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_gold));
        } else {
            btn25Min.setBackgroundTintList(getResources().getColorStateList(R.color.button_disabled));
            btn45Min.setBackgroundTintList(getResources().getColorStateList(R.color.button_disabled));
            btnCustom.setBackgroundTintList(getResources().getColorStateList(R.color.button_disabled));
        }
    }

    private void updateDurationButtonSelection(Button selectedButton) {
        // Reset all buttons to their default colors
        btn25Min.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_blue));
        btn45Min.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_dark_teal));
        btnCustom.setBackgroundTintList(getResources().getColorStateList(R.color.selector_pomodoro_gold));

        // Highlight the selected button with a slightly different shade
        if (selectedButton == btn25Min) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.pomodoro_dark_teal));
        } else if (selectedButton == btn45Min) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.pomodoro_blue));
        }
        // Custom button keeps its gold color
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSubjectName != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvTimer = null;
        btnStart = null;
        btnPause = null;
        btnStop = null;
        btn25Min = null;
        btn45Min = null;
        btnCustom = null;
        etSubjectName = null;
        btnAddSubject = null;
        rvSubjects = null;
        subjectAdapter = null; // Nettoyer l'adapter
    }
}