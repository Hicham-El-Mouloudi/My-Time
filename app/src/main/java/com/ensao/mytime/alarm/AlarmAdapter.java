package com.ensao.mytime.alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.Alarm;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarms = new ArrayList<>();
    private Set<Alarm> selectedAlarms = new HashSet<>();
    private boolean isSelectionMode = false;
    private OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onAlarmToggle(Alarm alarm, boolean isEnabled);

        void onAlarmClick(Alarm alarm);

        void onSelectionChanged(int count);
    }

    public AlarmAdapter(OnAlarmActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_alarm_item,
                        parent,
                        false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    public List<Alarm> getSelectedAlarms() {
        return new ArrayList<>(selectedAlarms);
    }

    public void clearSelection() {
        selectedAlarms.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        if (listener != null)
            listener.onSelectionChanged(0);
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView timeText;
        private TextView repetitionText;
        private SwitchCompat alarmSwitch;
        private View cardView;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            // MaterialCardView is the root
            cardView = itemView;
            timeText = itemView.findViewById(R.id.alarm_time);
            repetitionText = itemView.findViewById(R.id.alarm_repetition);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
        }

        public void bind(Alarm alarm) {
            // format time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String formattedTime = timeFormat.format(alarm.getTimeInMillis());
            timeText.setText(formattedTime);

            // set repetition text
            repetitionText.setText(getRepetitionString(alarm.getDaysOfWeek()));

            // set switch
            alarmSwitch.setOnCheckedChangeListener(null);
            alarmSwitch.setChecked(alarm.isEnabled());
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAlarmToggle(alarm, isChecked);
                }
            });

            // Selection Logic
            if (selectedAlarms.contains(alarm)) {
                cardView.setAlpha(0.5f); // Simple visual indicator for selection
            } else {
                cardView.setAlpha(1.0f);
            }

            cardView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    toggleSelection(alarm);
                } else {
                    if (listener != null) {
                        listener.onAlarmClick(alarm);
                    }
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (!isSelectionMode) {
                    isSelectionMode = true;
                    toggleSelection(alarm);
                }
                return true;
            });
        }

        private void toggleSelection(Alarm alarm) {
            if (selectedAlarms.contains(alarm)) {
                selectedAlarms.remove(alarm);
            } else {
                selectedAlarms.add(alarm);
            }
            if (selectedAlarms.isEmpty()) {
                isSelectionMode = false;
            }
            notifyItemChanged(getAdapterPosition());
            if (listener != null) {
                listener.onSelectionChanged(selectedAlarms.size());
            }
        }

        private String getRepetitionString(int daysOfWeek) {
            if (daysOfWeek == 0)
                return "Once";
            if (daysOfWeek == 127)
                return "Daily"; // All days selected (1+2+4+8+16+32+64 = 127)

            StringBuilder sb = new StringBuilder();
            String[] shortWeekdays = new DateFormatSymbols().getShortWeekdays(); // Sun=1
            // Our bitmask: Sun=1 (1<<0), Mon=2 (1<<1), ..., Sat=64 (1<<6)
            // Calendar constants: SUNDAY=1, MONDAY=2, ... SATURDAY=7

            // Map our bits to Calendar days
            int[] calendarDays = {
                    java.util.Calendar.SUNDAY,
                    java.util.Calendar.MONDAY,
                    java.util.Calendar.TUESDAY,
                    java.util.Calendar.WEDNESDAY,
                    java.util.Calendar.THURSDAY,
                    java.util.Calendar.FRIDAY,
                    java.util.Calendar.SATURDAY
            };

            int count = 0;
            for (int i = 0; i < 7; i++) {
                if ((daysOfWeek & (1 << i)) != 0) {
                    if (count > 0)
                        sb.append(", ");
                    sb.append(shortWeekdays[calendarDays[i]]);
                    count++;
                }
            }
            return sb.toString();
        }
    }// alarmViewHolder
}// adapter
