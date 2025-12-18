package com.ensao.mytime.alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.Alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List <Alarm> alarms= new ArrayList<>();
    private OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onAlarmToggle(Alarm alarm, boolean isEnabled);
        void onAlarmEdit(Alarm alarm);
        void onAlarmDelete(Alarm alarm);

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
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position)
    {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm);
    }
    @Override
    public int getItemCount() {
        return alarms.size();
    }
    public void setAlarms(List<Alarm> alarms){
        this.alarms = alarms;
        notifyDataSetChanged();
    }
    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView timeText;
        private TextView labelText;
        private SwitchCompat alarmSwitch;
        private ImageButton editButton;
        private ImageButton deleteButton;
        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.alarm_time);
            labelText = itemView.findViewById(R.id.alarm_label);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }
        public void bind (Alarm alarm){
            // format time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
            String formattedTime = timeFormat.format(alarm.getTimeInMillis());
            timeText.setText(formattedTime);

            // set label
            if (alarm.getLabel() != null && !alarm.getLabel().isEmpty()){
                labelText.setText(alarm.getLabel());
                labelText.setVisibility(View.VISIBLE);
            } else {
                labelText.setVisibility(View.GONE);
            }
            // set switch
            alarmSwitch.setOnCheckedChangeListener(null);
            alarmSwitch.setChecked(alarm.isEnabled());
            alarmSwitch.setOnCheckedChangeListener((buttonView , isChecked) -> {
                if(listener != null){
                    listener.onAlarmToggle(alarm,isChecked);
                }
            });
            // Edit button
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmEdit(alarm);
                }
            });

            // Delete button
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmDelete(alarm);
                }
            });
        }
    }//alarmViewHolder
}//adapter
