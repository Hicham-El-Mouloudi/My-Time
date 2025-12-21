package com.ensao.mytime.statistics.adapter.calendar;

import com.ensao.mytime.statistics.data.StatisticsCallback;
import com.ensao.mytime.statistics.data.StatisticsDAO;
import com.ensao.mytime.statistics.model.DayData;

import java.util.List;

public class CalendarDaysAdaptee {
    private StatisticsDAO statisticsDAO;

    public CalendarDaysAdaptee(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public void getDaysForMonth(int month, int year, StatisticsCallback<List<DayData>> callback) {
        statisticsDAO.getDays(month, year, callback);
    }
}
