package com.ensao.mytime.statistics.adapter.week;

import android.os.Handler;
import android.os.Looper;
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
import java.util.HashMap;
import java.util.Map;

public class WeeksAdapter extends RecyclerView.Adapter<WeeksAdapter.WeekViewHolder> {
    private WeeksAdaptee weeksAdaptee;
    private OnDayClickListener listenner;
    private int weeksCount;
    private DayData selectedDay;
    private Map<Integer, WeekData> cachedWeeks;
    private Handler mainHandler;

    public WeeksAdapter(WeeksAdaptee weeksAdaptee, OnDayClickListener listenner) {
        this.weeksAdaptee = weeksAdaptee;
        this.listenner = listenner;
        this.weeksCount = 2;
        this.selectedDay = null;
        this.cachedWeeks = new HashMap<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setSelectedDay(DayData day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public DayData getSelectedDay() {
        return selectedDay;
    }

    @NonNull
    @Override
    public WeeksAdapter.WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeksAdapter.WeekViewHolder holder, int position) {
        int weekIndex = -1 * position;

        // Check if we have cached data
        if (cachedWeeks.containsKey(weekIndex)) {
            holder.bind(cachedWeeks.get(weekIndex), listenner, selectedDay);
        } else {
            // Show loading state or empty state initially
            holder.showLoading();

            // Fetch data asynchronously
            weeksAdaptee.getWeekDataForWeekWithIndex(weekIndex, weekData -> {
                cachedWeeks.put(weekIndex, weekData);
                mainHandler.post(() -> {
                    // Only update if still the same position
                    if (holder.getAdapterPosition() == position) {
                        holder.bind(weekData, listenner, selectedDay);
                    }
                });
            });
        }
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

        public void showLoading() {
            // Optionally show a loading indicator or clear the views
            for (DayOfWeek day : DayOfWeek.values()) {
                TextView dayNumberView = (TextView) getDayNumberTextViewOf(day);
                if (dayNumberView != null) {
                    dayNumberView.setText("-");
                }
                View sleepIndicator = getDayIndicatorSleepOf(day);
                if (sleepIndicator != null) {
                    sleepIndicator.setVisibility(View.GONE);
                }
                View wakeIndicator = getDayIndicatorWakeOf(day);
                if (wakeIndicator != null) {
                    wakeIndicator.setVisibility(View.GONE);
                }
                View studyIndicator = getDayIndicatorStudyOf(day);
                if (studyIndicator != null) {
                    studyIndicator.setVisibility(View.GONE);
                }
            }
        }

        public void bind(WeekData weekData, OnDayClickListener listener, DayData selectedDay) {
            DayData dayData = null;
            java.time.LocalDate today = java.time.LocalDate.now();
            for (DayOfWeek day : DayOfWeek.values()) {
                dayData = weekData.getDays().get(day.getValue() - 1); // -1 because the week days starts from 1
                boolean isFuture = dayData.getDate().isAfter(today);
                boolean isSelected = (selectedDay != null && dayData.getDate().equals(selectedDay.getDate()));

                // Setting day number
                setDayNumberTextViewOf(day, dayData.getDate().getDayOfMonth());
                // Setting sleep indicator
                setDayIndicatorSleepOf(day, dayData.hasSleep());
                // Setting wake indicator
                setDayIndicatorWakeOf(day, dayData.hasWake());
                // Setting study indicator
                setDayIndicatorStudyOf(day, dayData.hasStudy());
                // Apply future/selected styling
                applyDayStyling(day, isFuture, isSelected);
                // Setting day container view click listener
                if (!isFuture) {
                    setDayContainerViewClickListenerOf(day, dayData, listener);
                } else {
                    disableDayClickOf(day);
                }
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

        private View getDayIndicatorStudyOf(DayOfWeek day) {
            View dayIndicatorStudyView = null;
            switch (day) {
                case MONDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_monday_indicator_study);
                    break;
                case TUESDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_tuesday_indicator_study);
                    break;
                case WEDNESDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_wednesday_indicator_study);
                    break;
                case THURSDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_thursday_indicator_study);
                    break;
                case FRIDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_friday_indicator_study);
                    break;
                case SATURDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_saturday_indicator_study);
                    break;
                case SUNDAY:
                    dayIndicatorStudyView = itemView.findViewById(R.id.iv_sunday_indicator_study);
                    break;
            }
            return dayIndicatorStudyView;
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

        private void setDayIndicatorStudyOf(DayOfWeek day, boolean isStudy) {
            View dayIndicatorStudyView = getDayIndicatorStudyOf(day);
            if (dayIndicatorStudyView != null) {
                dayIndicatorStudyView.setVisibility(isStudy ? View.VISIBLE : View.GONE);
            }
        }

        private void setDayContainerViewClickListenerOf(DayOfWeek day, DayData dayData,
                OnDayClickListener listener) {
            View dayLayoutView = getDayContainerViewOf(day);
            if (dayLayoutView != null) {
                dayLayoutView.setClickable(true);
                dayLayoutView.setOnClickListener(v -> listener.onDayClick(dayData));
            }
        }

        private void disableDayClickOf(DayOfWeek day) {
            View dayLayoutView = getDayContainerViewOf(day);
            if (dayLayoutView != null) {
                dayLayoutView.setOnClickListener(null);
                dayLayoutView.setClickable(false);
            }
        }

        private void applyDayStyling(DayOfWeek day, boolean isFuture, boolean isSelected) {
            TextView dayNumberView = (TextView) getDayNumberTextViewOf(day);
            if (dayNumberView == null)
                return;

            int grayColor = dayNumberView.getContext().getResources().getColor(R.color.text_secondary);
            int primaryColor = dayNumberView.getContext().getResources().getColor(R.color.text_primary);
            int whiteColor = dayNumberView.getContext().getResources().getColor(R.color.white);

            if (isFuture) {
                // Gray out future days
                dayNumberView.setTextColor(grayColor);
                dayNumberView.setAlpha(0.5f);
                dayNumberView.setBackgroundResource(0); // Remove background
                dayNumberView.setBackgroundTintList(null);
            } else if (isSelected) {
                // Highlight selected day with oval background
                dayNumberView.setTextColor(whiteColor);
                dayNumberView.setAlpha(1.0f);
                dayNumberView.setBackgroundResource(R.drawable.bg_day_selected);
                dayNumberView.setBackgroundTintList(null); // Clear tint so actual color shows
            } else {
                // Normal styling
                dayNumberView.setTextColor(primaryColor);
                dayNumberView.setAlpha(1.0f);
                dayNumberView.setBackgroundResource(0); // Remove background
                dayNumberView.setBackgroundTintList(null);
            }
        }
    }
}
