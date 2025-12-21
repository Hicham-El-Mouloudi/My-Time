package com.ensao.mytime.statistics.adapter.week;

import com.ensao.mytime.statistics.data.StatisticsCallback;
import com.ensao.mytime.statistics.data.StatisticsDAO;
import com.ensao.mytime.statistics.model.WeekData;

import java.util.List;

public class WeeksAdaptee {
    private StatisticsDAO statisticsDAO;

    public WeeksAdaptee(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public void getWeekDataForWeekWithIndex(int index, StatisticsCallback<WeekData> callback)
            throws UnsupportedOperationException {
        statisticsDAO.getWeekData(index, callback);
    }

    /*
     * @Brief : Get a number of weeks data starting from a specific week index
     * 
     * @Note : The week index is the index of the week starting from 0 (the current
     * week) to -n (n weeks before the current week)
     */
    public void getWeeksDataFromWeekWithIndex(int index, int numberOfWeeks, StatisticsCallback<List<WeekData>> callback)
            throws UnsupportedOperationException {
        statisticsDAO.getWeeksData(index, numberOfWeeks, callback);
    }
}
