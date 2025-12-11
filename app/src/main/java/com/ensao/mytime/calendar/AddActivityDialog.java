package com.ensao.mytime.calendar;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ensao.mytime.R;
import com.ensao.mytime.study.model.DailyActivity;

import java.util.Locale;

public class AddActivityDialog extends DialogFragment {

    public interface OnActivityAddedListener {
        void onActivityAdded(DailyActivity activity);
    }

    private OnActivityAddedListener listener;
    private String selectedDate;
    private TimePicker timePicker;
    private EditText descriptionEditText;

    // Constructeur
    public static AddActivityDialog newInstance(String date, OnActivityAddedListener listener) {
        AddActivityDialog dialog = new AddActivityDialog();
        dialog.selectedDate = date;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Créer un Dialog personnalisé
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_activity);

        // Récupérer les vues
        timePicker = dialog.findViewById(R.id.timePicker);
        descriptionEditText = dialog.findViewById(R.id.descriptionEditText);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Configurer le TimePicker en mode 24h
        timePicker.setIs24HourView(true);

        // Bouton Sauvegarder
        btnSave.setOnClickListener(v -> saveActivity());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> dismiss());

        // Définir la taille du dialog
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Utiliser cette méthode si vous préférez un DialogFragment standard
        View view = inflater.inflate(R.layout.dialog_add_activity, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        timePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> saveActivity());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveActivity() {
        // Récupérer l'heure
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Formater l'heure (HH:MM)
        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        // Récupérer la description
        String description = descriptionEditText.getText().toString().trim();

        // Validation
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer une description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer la nouvelle activité
        DailyActivity newActivity = new DailyActivity(selectedDate, time, description);

        // Notifier le listener
        if (listener != null) {
            listener.onActivityAdded(newActivity);
        }

        // Fermer le dialog
        dismiss();

        Toast.makeText(getContext(), "Activité ajoutée avec succès", Toast.LENGTH_SHORT).show();
    }
}
