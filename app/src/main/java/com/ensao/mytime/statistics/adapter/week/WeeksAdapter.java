package com.ensao.mytime.statistics.adapter.week;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.statistics.adapter.OnDayClickListener;
import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.model.WeekData;

import java.time.DayOfWeek;

public class WeeksAdapter extends RecyclerView.Adapter<WeeksAdapter.WeekViewHolder> {
    private WeeksAdaptee weeksAdaptee;
    private OnDayClickListener listenner;
    private int weeksCount;

    public WeeksAdapter(WeeksAdaptee weeksAdaptee, OnDayClickListener listenner) {
        this.weeksAdaptee = weeksAdaptee;
        this.listenner = listenner;
        this.weeksCount = 2;
    }

    @NonNull
    @Override
    public WeeksAdapter.WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeksAdapter.WeekViewHolder holder, int position) {
        // The index must be negative to get the week before the current week
        WeekData weekData = weeksAdaptee.getWeekDataForWeekWithIndex(-1 * position);
        holder.bind(weekData, listenner);
    }

    @Override
    public int getItemCount() {
        return weeksCount;
    }

    /*
     * Extends the weeks count by 1 and notify the adapter that a new item has been
     * added
     */
    public void extendWeeksCount() {
        weeksCount += 1;
        notifyItemInserted(weeksCount - 1);
    }

    /*
     * @Brief: This class is used to hold the views of the week item
     */
    public static class WeekViewHolder extends RecyclerView.ViewHolder {

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(WeekData weekData, OnDayClickListener listener) {
            DayData dayData = null;
            for (DayOfWeek day : DayOfWeek.values()) {
                dayData = weekData.getDays().get(day.getValue() - 1); // -1 because the week days starts from 1
                // Setting day number
                setDayNumberTextViewOf(day, dayData.getDate().getDayOfMonth());
                // Setting sleep indicator
                setDayIndicatorSleepOf(day, dayData.hasSleep());
                // Setting wake indicator
                setDayIndicatorWakeOf(day, dayData.hasWake());
                // Setting day container view click listener
                setDayContainerViewClickListenerOf(day, dayData, listener);
            }
        }

        private View getDayNumberTextViewOf(DayOfWeek day) {
            View dayNumberView = null;
            switch (day) {
                case MONDAY:
                    dayNumberView = itemView.findViewById(R.id.monday_day_number);
                    break;
                case TUESDAY:
                    dayNumberView = itemView.findViewById(R.id.tuesday_day_number);
                    break;
                case WEDNESDAY:
                    dayNumberView = itemView.findViewById(R.id.wednesday_day_number);
                    break;
                case THURSDAY:
                    dayNumberView = itemView.findViewById(R.id.thursday_day_number);
                    break;
                case FRIDAY:
                    dayNumberView = itemView.findViewById(R.id.friday_day_number);
                    break;
                case SATURDAY:
                    dayNumberView = itemView.findViewById(R.id.saturday_day_number);
                    break;
                case SUNDAY:
                    dayNumberView = itemView.findViewById(R.id.sunday_day_number);
                    break;
            }
            return dayNumberView;
        }

        private View getDayIndicatorSleepOf(DayOfWeek day) {
            View dayIndicatorSleepView = null;
            switch (day) {
                case MONDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_monday_indicator_sleep);
                    break;
                case TUESDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_tuesday_indicator_sleep);
                    break;
                case WEDNESDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_wednesday_indicator_sleep);
                    break;
                case THURSDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_thursday_indicator_sleep);
                    break;
                case FRIDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_friday_indicator_sleep);
                    break;
                case SATURDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_saturday_indicator_sleep);
                    break;
                case SUNDAY:
                    dayIndicatorSleepView = itemView.findViewById(R.id.iv_sunday_indicator_sleep);
                    break;
            }
            return dayIndicatorSleepView;
        }

        private View getDayIndicatorWakeOf(DayOfWeek day) {
            View dayIndicatorWakeView = null;
            switch (day) {
                case MONDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_monday_indicator_wake);
                    break;
                case TUESDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_tuesday_indicator_wake);
                    break;
                case WEDNESDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_wednesday_indicator_wake);
                    break;
                case THURSDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_thursday_indicator_wake);
                    break;
                case FRIDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_friday_indicator_wake);
                    break;
                case SATURDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_saturday_indicator_wake);
                    break;
                case SUNDAY:
                    dayIndicatorWakeView = itemView.findViewById(R.id.iv_sunday_indicator_wake);
                    break;
            }
            return dayIndicatorWakeView;
        }

        private View getDayContainerViewOf(DayOfWeek day) {
            View dayLayoutView = null;
            switch (day) {
                case MONDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_monday);
                    break;
                case TUESDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_tuesday);
                    break;
                case WEDNESDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_wednesday);
                    break;
                case THURSDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_thursday);
                    break;
                case FRIDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_friday);
                    break;
                case SATURDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_saturday);
                    break;
                case SUNDAY:
                    dayLayoutView = itemView.findViewById(R.id.ll_sunday);
                    break;
            }
            return dayLayoutView;
        }

        private void setDayNumberTextViewOf(DayOfWeek day, int dayNumber) {
            TextView dayNumberView = (TextView) getDayNumberTextViewOf(day);
            if (dayNumberView != null) {
                dayNumberView.setText(String.valueOf(dayNumber));
            }
        }

        private void setDayIndicatorSleepOf(DayOfWeek day, boolean isSleep) {
            View dayIndicatorSleepView = getDayIndicatorSleepOf(day);
            if (dayIndicatorSleepView != null) {
                dayIndicatorSleepView.setVisibility(isSleep ? View.VISIBLE : View.GONE);
            }
        }

        private void setDayIndicatorWakeOf(DayOfWeek day, boolean isWake) {
            View dayIndicatorWakeView = getDayIndicatorWakeOf(day);
            if (dayIndicatorWakeView != null) {
                dayIndicatorWakeView.setVisibility(isWake ? View.VISIBLE : View.GONE);
            }
        }

        private void setDayContainerViewClickListenerOf(DayOfWeek day, DayData dayData,
                OnDayClickListener listener) {
            View dayLayoutView = getDayContainerViewOf(day);
            if (dayLayoutView != null) {
                dayLayoutView.setOnClickListener(v -> listener.onDayClick(dayData));
            }
        }
    }
}
