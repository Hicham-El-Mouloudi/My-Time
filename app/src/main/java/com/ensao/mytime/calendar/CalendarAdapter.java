package com.ensao.mytime.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarDay> calendarDays;
    private OnDateClickListener onDateClickListener;
    // Déclarer clickedDates comme variable d'instance
    private Set<String> clickedDates = new HashSet<>();
    private String currentMonthYear;

    public interface OnDateClickListener {
        void onDateClick(CalendarDay calendarDay);
    }

    public CalendarAdapter(List<CalendarDay> calendarDays, OnDateClickListener listener, String monthYear) {
        this.calendarDays = calendarDays;
        this.onDateClickListener = listener;
        this.currentMonthYear = monthYear;
    }

    // Méthode pour marquer une date comme cliquée
    public void markDateAsClicked(String date) {
        clickedDates.add(date);
        notifyDataSetChanged();
    }

    // Méthode pour vérifier si une date a été cliquée
    private boolean isDateClicked(CalendarDay day) {
        if (!day.isCurrentMonth()) return false;

        String dateKey = currentMonthYear + "-" +
                String.format("%02d", day.getDay());
        return clickedDates.contains(dateKey);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        holder.bind(day, onDateClickListener, this); // Passer 'this' à la méthode bind
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    public void updateCalendarDays(List<CalendarDay> newCalendarDays) {
        this.calendarDays = newCalendarDays;
        notifyDataSetChanged();
    }

    // Ajouter un getter pour clickedDates si nécessaire
    public Set<String> getClickedDates() {
        return clickedDates;
    }

    // Ajouter un getter pour currentMonthYear
    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        private TextView dayText;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
        }

        public void bind(CalendarDay day, OnDateClickListener listener, CalendarAdapter adapter) {
            dayText.setText(String.valueOf(day.getDay()));

            if (day.isCurrentMonth()) {
                dayText.setAlpha(1.0f);
                dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_dark));

                // Vérifier les différents états
                if (day.isToday()) {
                    // Jour actuel - rouge
                    dayText.setBackgroundResource(R.drawable.today_background);
                    dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                }
                else if (adapter.isDateClicked(day)) {
                    // Jour cliqué - navy blue
                    dayText.setBackgroundResource(R.drawable.selected_day_background);
                    dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                }
                else {
                    // Jour normal
                    dayText.setBackgroundResource(R.drawable.day_background);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        // Marquer la date comme cliquée
                        String dateKey = adapter.currentMonthYear + "-" +
                                String.format("%02d", day.getDay());
                        adapter.clickedDates.add(dateKey);
                        adapter.notifyDataSetChanged();

                        // Appeler le listener
                        listener.onDateClick(day);
                    }
                });
            } else {
                // Jours des autres mois
                dayText.setAlpha(0.3f);
                dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.medium_gray));
                dayText.setBackgroundResource(R.drawable.day_background);
                itemView.setOnClickListener(null);
            }
        }
    }
}