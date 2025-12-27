package com.ensao.mytime.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateBarAdapter extends RecyclerView.Adapter<DateBarAdapter.DateViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(Date date, String formattedDate);
    }

    private List<Date> dates;
    private int selectedPosition = -1;
    private int todayPosition = -1;
    private OnDateSelectedListener listener;
    private SimpleDateFormat dayNameFormat;
    private SimpleDateFormat dayNumberFormat;
    private SimpleDateFormat monthFormat;
    private SimpleDateFormat fullDateFormat;

    public DateBarAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
        this.dates = new ArrayList<>();
        this.dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        this.dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());
        this.monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        this.fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        generateDates();
    }

    private void generateDates() {
        dates.clear();
        Calendar calendar = Calendar.getInstance();
        
        // Go back 30 days
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        
        // Get today's date for comparison
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        // Generate 61 days (30 before + today + 30 after)
        for (int i = 0; i < 61; i++) {
            Date date = calendar.getTime();
            dates.add(date);
            
            // Check if this is today
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            dateCalendar.set(Calendar.MINUTE, 0);
            dateCalendar.set(Calendar.SECOND, 0);
            dateCalendar.set(Calendar.MILLISECOND, 0);
            
            if (dateCalendar.getTimeInMillis() == today.getTimeInMillis()) {
                todayPosition = i;
                selectedPosition = i; // Select today by default
            }
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public int getTodayPosition() {
        return todayPosition;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
        notifyItemChanged(selectedPosition);
    }

    public Date getSelectedDate() {
        if (selectedPosition >= 0 && selectedPosition < dates.size()) {
            return dates.get(selectedPosition);
        }
        return new Date();
    }

    public String getFormattedSelectedDate() {
        return fullDateFormat.format(getSelectedDate());
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_bar, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dates.get(position);
        
        holder.tvDayName.setText(dayNameFormat.format(date));
        holder.tvDayNumber.setText(dayNumberFormat.format(date));
        holder.tvMonth.setText(monthFormat.format(date));
        
        boolean isSelected = position == selectedPosition;
        boolean isToday = position == todayPosition;
        
        holder.container.setSelected(isSelected);
        
        // Apply styling based on selection and today status
        if (isSelected) {
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.tvMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.container.setBackgroundResource(R.drawable.date_item_background);
        } else if (isToday) {
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.aurora_primary));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.aurora_primary));
            holder.tvMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.aurora_primary));
            holder.container.setBackgroundResource(R.drawable.date_item_background_today);
        } else {
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_gray));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_gray));
            holder.container.setBackgroundResource(android.R.color.transparent);
        }
        
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                setSelectedPosition(adapterPosition);
                if (listener != null) {
                    listener.onDateSelected(dates.get(adapterPosition), fullDateFormat.format(dates.get(adapterPosition)));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView tvDayName;
        TextView tvDayNumber;
        TextView tvMonth;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.dateItemContainer);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
}
