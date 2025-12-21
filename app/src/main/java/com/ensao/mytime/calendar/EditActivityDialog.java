package com.ensao.mytime.calendar;

import android.app.Dialog;
import android.os.Bundle;
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

public class EditActivityDialog extends DialogFragment {

    public interface OnActivityEditedListener {
        void onActivityEdited(int position, DailyActivity updatedActivity);
    }

    private OnActivityEditedListener listener;
    private DailyActivity activityToEdit;
    private int position;
    private TimePicker timePicker;
    private EditText descriptionEditText;

    public static EditActivityDialog newInstance(int position, DailyActivity activity, OnActivityEditedListener listener) {
        EditActivityDialog dialog = new EditActivityDialog();
        dialog.position = position;
        dialog.activityToEdit = activity;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_activity);

        timePicker = dialog.findViewById(R.id.timePicker);
        descriptionEditText = dialog.findViewById(R.id.descriptionEditText);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Configure TimePicker in 24h mode
        timePicker.setIs24HourView(true);

        // Pre-fill with existing values
        if (activityToEdit != null) {
            descriptionEditText.setText(activityToEdit.getDescription());
            
            // Parse and set existing time
            String[] timeParts = activityToEdit.getTime().split(":");
            if (timeParts.length == 2) {
                try {
                    timePicker.setHour(Integer.parseInt(timeParts[0]));
                    timePicker.setMinute(Integer.parseInt(timeParts[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        // Save button
        btnSave.setOnClickListener(v -> saveActivity());

        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Set dialog size
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        return dialog;
    }

    private void saveActivity() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        String description = descriptionEditText.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer une description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create updated activity (preserving the ID)
        DailyActivity updatedActivity = new DailyActivity(
                activityToEdit.getId(),
                activityToEdit.getDate(),
                time,
                description
        );

        if (listener != null) {
            listener.onActivityEdited(position, updatedActivity);
        }

        dismiss();
    }
}
