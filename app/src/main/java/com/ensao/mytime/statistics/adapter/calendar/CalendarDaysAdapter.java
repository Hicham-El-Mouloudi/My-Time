package com.ensao.mytime.statistics.adapter.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensao.mytime.R;
import com.ensao.mytime.statistics.adapter.OnDayClickListener;
import com.ensao.mytime.statistics.model.DayData;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarDaysAdapter extends RecyclerView.Adapter<CalendarDaysAdapter.DayViewHolder> {

    private CalendarDaysAdaptee adaptee;
    private List<DayData> days;
    private final OnDayClickListener listener;
    private final Calendar calendar;
    private int selectedMonth;
    private int selectedYear;

    public CalendarDaysAdapter(CalendarDaysAdaptee adaptee, OnDayClickListener listener) {
        this.adaptee = adaptee;
        this.listener = listener;
        // Initial load for current month (example)
        calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);
        this.days = adaptee.getDaysForMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    public int getSelectedMonth() {
        return selectedMonth;
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    private void setDays(List<DayData> days) {
        this.days = days;
        this.selectedMonth = calendar.get(Calendar.MONTH);
        this.selectedYear = calendar.get(Calendar.YEAR);
        notifyDataSetChanged();
    }

    public void nextMonth() {
        // Prevent navigation past current month
        Calendar now = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) > now.get(Calendar.YEAR) ||
                (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) >= now.get(Calendar.MONTH))) {
            return; // Already at current month or beyond, don't advance
        }
        calendar.add(Calendar.MONTH, 1);
        setDays(adaptee.getDaysForMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
    }

    public void previousMonth() {
        calendar.add(Calendar.MONTH, -1);
        setDays(adaptee.getDaysForMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
    }

    public void initializeDayContentOfListener() {
        int currentDayIndex = calendar.get(Calendar.DAY_OF_MONTH);
        listener.onDayClick(days.get(currentDayIndex));
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayData day = days.get(position);
        holder.bind(day, listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        ImageView ivSleepIndicator;
        ImageView ivWakeIndicator;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            ivSleepIndicator = itemView.findViewById(R.id.iv_indicator_sleep);
            ivWakeIndicator = itemView.findViewById(R.id.iv_indicator_wake);
        }

        public void bind(final DayData day, final OnDayClickListener listener) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(day.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            tvDayNumber.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            ivSleepIndicator.setVisibility(day.hasSleep() ? View.VISIBLE : View.GONE);
            ivWakeIndicator.setVisibility(day.hasWake() ? View.VISIBLE : View.GONE);

            // Check if this day is in the future
            boolean isFutureDay = day.getDate().isAfter(java.time.LocalDate.now());

            // Styling
            tvDayNumber.setScaleX(1.0f);
            tvDayNumber.setScaleY(1.0f);
            tvDayNumber.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));

            if (isFutureDay) {
                // Gray out future days
                tvDayNumber.setTextColor(tvDayNumber.getContext().getResources().getColor(R.color.text_secondary));
                tvDayNumber.setAlpha(0.5f);
                itemView.setOnClickListener(null); // Disable click
                itemView.setClickable(false);
            } else {
                tvDayNumber.setTextColor(tvDayNumber.getContext().getResources().getColor(R.color.text_primary));
                tvDayNumber.setAlpha(1.0f);
                itemView.setClickable(true);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onDayClick(day);
                    }
                });
            }
        }
    }
}
