package com.ensao.mytime.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialisation avec les bons IDs de votre layout
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearText);

        currentCalendar = Calendar.getInstance();
        setupCalendar(); // Appel de la méthode modifiée
        setupNavigationButtons(view);

        // Activities are now loaded from database in DayDetailActivity

        return view;
    }

    private void setupCalendar() {
        List<CalendarDay> calendarDays = getCalendarDays();

        // Obtenir le mois/année au format "MM-yyyy" pour le 3ème paramètre
        String monthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault())
                .format(currentCalendar.getTime());

        // MODIFICATION : Utilisation de OnDateClickListener avec ouverture de
        // DayDetailActivity
        calendarAdapter = new CalendarAdapter(calendarDays,
                new CalendarAdapter.OnDateClickListener() {
                    @Override
                    public void onDateClick(CalendarDay day) {
                        // Vérifier si c'est un jour du mois courant
                        if (day.isCurrentMonth()) {
                            // Formater la date comme vous l'avez demandé
                            String selectedDate = day.getYear() + "-" +
                                    String.format("%02d", day.getMonth()) + "-" +
                                    String.format("%02d", day.getDay());

                            // Ouvrir l'activité indépendante DayDetailActivity
                            Intent intent = new Intent(getActivity(), DayDetailActivity.class);
                            intent.putExtra("SELECTED_DATE", selectedDate);
                            startActivity(intent);
                        }
                    }
                },
                monthYear // 3ème paramètre
        );

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        updateMonthYearHeader();
    }

    private void setupNavigationButtons(View view) {
        // Utiliser les bons IDs de votre layout
        ImageButton btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);

        if (btnPreviousMonth != null) {
            btnPreviousMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });
        }
    }

    private void updateCalendar() {
        List<CalendarDay> calendarDays = getCalendarDays();
        if (calendarAdapter != null) {
            // Mettre à jour aussi le mois/année dans l'adapter
            String monthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault())
                    .format(currentCalendar.getTime());

            // Créer un nouvel adapter avec le nouveau mois/année
            calendarAdapter = new CalendarAdapter(calendarDays,
                    new CalendarAdapter.OnDateClickListener() {
                        @Override
                        public void onDateClick(CalendarDay day) {
                            if (day.isCurrentMonth()) {
                                String selectedDate = day.getYear() + "-" +
                                        String.format("%02d", day.getMonth()) + "-" +
                                        String.format("%02d", day.getDay());

                                Intent intent = new Intent(getActivity(), DayDetailActivity.class);
                                intent.putExtra("SELECTED_DATE", selectedDate);
                                startActivity(intent);
                            }
                        }
                    },
                    monthYear);
            calendarRecyclerView.setAdapter(calendarAdapter);
        }
        updateMonthYearHeader();
    }

    private List<CalendarDay> getCalendarDays() {
        List<CalendarDay> calendarDays = new ArrayList<>();

        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int currentMonth = calendar.get(Calendar.MONTH) + 1; // +1 car Calendar commence à 0
        int currentYear = calendar.get(Calendar.YEAR);

        // Obtenir la date d'aujourd'hui pour comparer
        Calendar todayCalendar = Calendar.getInstance();
        int todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = todayCalendar.get(Calendar.MONTH) + 1; // +1 car Calendar commence à 0
        int todayYear = todayCalendar.get(Calendar.YEAR);

        // Déterminer le jour de la semaine du premier jour du mois
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Ajuster pour que lundi soit le premier jour de la semaine
        int emptyDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7;
        if (emptyDays < 0)
            emptyDays += 7;

        // Ajouter les jours vides du mois précédent
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int prevMonthValue = prevMonth.get(Calendar.MONTH) + 1;
        int prevYear = prevMonth.get(Calendar.YEAR);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < emptyDays; i++) {
            int day = daysInPrevMonth - emptyDays + i + 1;
            // Important : 5 paramètres pour CalendarDay
            calendarDays.add(new CalendarDay(day, prevMonthValue, prevYear, false, false));
        }

        // Ajouter les jours du mois courant
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            // Vérifier si c'est aujourd'hui
            boolean isToday = (currentYear == todayYear) &&
                    (currentMonth == todayMonth) &&
                    (day == todayDay);

            // Important : 5 paramètres pour CalendarDay
            calendarDays.add(new CalendarDay(day, currentMonth, currentYear, true, isToday));
        }

        // Ajouter les jours vides du mois suivant pour compléter la grille
        int totalCells = emptyDays + daysInMonth;
        int remainingCells = 42 - totalCells; // 6 lignes de 7 jours

        Calendar nextMonth = (Calendar) calendar.clone();
        nextMonth.add(Calendar.MONTH, 1);
        int nextMonthValue = nextMonth.get(Calendar.MONTH) + 1;
        int nextYear = nextMonth.get(Calendar.YEAR);

        for (int day = 1; day <= remainingCells; day++) {
            // Important : 5 paramètres pour CalendarDay
            calendarDays.add(new CalendarDay(day, nextMonthValue, nextYear, false, false));
        }

        return calendarDays;
    }

    private void updateMonthYearHeader() {
        if (monthYearText != null) {
            String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(currentCalendar.getTime());
            monthYearText.setText(monthYear);
        }
    }
}