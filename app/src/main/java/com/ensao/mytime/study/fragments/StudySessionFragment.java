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
import com.example.studysession.viewmodel.StudyViewModel;
import java.util.ArrayList;

public class StudySessionFragment extends Fragment {

    private StudyViewModel studyViewModel;
    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;

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

        // Initialiser l'état des boutons
        updateButtonStates(false);
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

        // Observer l'état du timer
        studyViewModel.getIsTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            if (isRunning != null) {
                updateButtonStates(isRunning);
                updateDurationButtonsState(isRunning);
            }
        });

        // Observer l'état textuel du timer
        studyViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && tvTimer != null) {
                switch (state) {
                    case "running":
                        tvTimer.setTextColor(getResources().getColor(R.color.navy_blue));
                        break;
                    case "paused":
                        tvTimer.setTextColor(getResources().getColor(R.color.medium_gray));
                        break;
                    case "stopped":
                        tvTimer.setTextColor(getResources().getColor(R.color.navy_blue));
                        break;
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
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

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
            studyViewModel.startTimer();
            Log.d("","btn start");
            updateButtonAppearance(true);
            updateButtonStates(true);

        });

        btnPause.setOnClickListener(v -> {
            studyViewModel.pauseTimer();
            updateButtonAppearance(false);
            updateButtonStates(false);

        });

        btnStop.setOnClickListener(v -> {
            studyViewModel.stopTimer();
            updateButtonAppearance(false);
            updateDurationButtonsState(false);
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

    private void updateButtonStates(boolean isTimerRunning) {
        if (btnStart != null) {
            btnStart.setEnabled(!isTimerRunning);
        }
        if (btnPause != null) {
            btnPause.setEnabled(isTimerRunning);
        }
        if (btnStop != null) {
            btnStop.setEnabled(true);
        }
    }

    private void updateButtonAppearance(boolean isTimerRunning) {
        if (isTimerRunning) {
            btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.medium_gray));
            btnPause.setBackgroundTintList(getResources().getColorStateList(R.color.navy_blue));
        } else {
            btnStart.setBackgroundTintList(getResources().getColorStateList(R.color.navy_blue));
            btnPause.setBackgroundTintList(getResources().getColorStateList(R.color.medium_gray));
        }
    }

    private void updateDurationButtonsState(boolean isTimerRunning) {
        boolean enabled = !isTimerRunning;
        btn25Min.setEnabled(enabled);
        btn45Min.setEnabled(enabled);
        btnCustom.setEnabled(enabled);

        int color = enabled ? R.color.navy_blue : R.color.medium_gray;
        btn25Min.setBackgroundTintList(getResources().getColorStateList(color));
        btn45Min.setBackgroundTintList(getResources().getColorStateList(color));
        btnCustom.setBackgroundTintList(getResources().getColorStateList(color));
    }

    private void updateDurationButtonSelection(Button selectedButton) {
        // Réinitialiser tous les boutons
        btn25Min.setBackgroundTintList(getResources().getColorStateList(R.color.navy_blue));
        btn45Min.setBackgroundTintList(getResources().getColorStateList(R.color.navy_blue));
        btnCustom.setBackgroundTintList(getResources().getColorStateList(R.color.navy_blue));

        // Si c'est un bouton prédéfini, mettre à jour son apparence
        if (selectedButton == btn25Min || selectedButton == btn45Min) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue));
        }
        // Si c'est le bouton personnalisé, il garde sa couleur normale
        // mais son texte sera mis à jour dans showCustomDurationDialog()
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSubjectName != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void setPomodoroService(PomodoroService service, boolean bound) {
        this.pomodoroService = service;
        this.isServiceBound = bound;

        if (isServiceBound && pomodoroService != null) {
            Log.d("SERVICE_DEBUG", "Service Pomodoro lié au fragment");
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