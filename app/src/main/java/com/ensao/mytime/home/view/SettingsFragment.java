package com.ensao.mytime.home.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.R;
import com.ensao.mytime.home.model.InvocationData;
import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private EditText editMorningStart, editMorningEnd, editEveningStart, editEveningEnd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        editMorningStart = view.findViewById(R.id.edit_morning_start);
        editMorningEnd = view.findViewById(R.id.edit_morning_end);
        editEveningStart = view.findViewById(R.id.edit_evening_start);
        editEveningEnd = view.findViewById(R.id.edit_evening_end);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_settings);

        loadSettings();

        btnSave.setOnClickListener(v -> saveSettingsAndScheduleAlarms());
    }

    private void loadSettings() {
        int morningStart = prefs.getInt(InvocationData.KEY_MORNING_START_HOUR, InvocationData.DEFAULT_MORNING_START);
        int morningEnd = prefs.getInt(InvocationData.KEY_MORNING_END_HOUR, InvocationData.DEFAULT_MORNING_END);
        int eveningStart = prefs.getInt(InvocationData.KEY_EVENING_START_HOUR, InvocationData.DEFAULT_EVENING_START);
        int eveningEnd = prefs.getInt(InvocationData.KEY_EVENING_END_HOUR, InvocationData.DEFAULT_EVENING_END);

        editMorningStart.setText(String.valueOf(morningStart));
        editMorningEnd.setText(String.valueOf(morningEnd));
        editEveningStart.setText(String.valueOf(eveningStart));
        editEveningEnd.setText(String.valueOf(eveningEnd));
    }

    private void saveSettingsAndScheduleAlarms() {
        if (getContext() == null) return;

        try {
            int morningStart = Integer.parseInt(editMorningStart.getText().toString());
            int morningEnd = Integer.parseInt(editMorningEnd.getText().toString());
            int eveningStart = Integer.parseInt(editEveningStart.getText().toString());
            int eveningEnd = Integer.parseInt(editEveningEnd.getText().toString());

            if (morningStart < 1 || morningStart > 9 || morningEnd < 1 || morningEnd > 9 ||
                    eveningStart < 16 || eveningStart > 23 || eveningEnd < 16 || eveningEnd > 23 ||
                    morningStart >= morningEnd || eveningStart >= eveningEnd) {

                Toast.makeText(getContext(), "Veuillez entrer une heure valide et assurez-vous que l\'heure de début est inférieure à l\'heure de fin.", Toast.LENGTH_LONG).show();
                return;
            }

            prefs.edit()
                    .putInt(InvocationData.KEY_MORNING_START_HOUR, morningStart)
                    .putInt(InvocationData.KEY_MORNING_END_HOUR, morningEnd)
                    .putInt(InvocationData.KEY_EVENING_START_HOUR, eveningStart)
                    .putInt(InvocationData.KEY_EVENING_END_HOUR, eveningEnd)
                    .apply();

            Toast.makeText(getContext(), "Réglages sauvegardés et alarmes mises à jour !", Toast.LENGTH_SHORT).show();

            AlarmScheduler.cancelAllAlarms(requireContext());
            AlarmScheduler.scheduleNextAlarm(requireContext(), true);
            AlarmScheduler.scheduleNextAlarm(requireContext(), false);

            getParentFragmentManager().popBackStack();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs avec des chiffres.", Toast.LENGTH_SHORT).show();
        }
    }
}