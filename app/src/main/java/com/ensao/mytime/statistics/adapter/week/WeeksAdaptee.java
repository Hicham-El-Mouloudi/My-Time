package com.ensao.mytime.statistics.adapter.week;

import com.ensao.mytime.statistics.data.StatisticsDAO;
import com.ensao.mytime.statistics.model.WeekData;

import java.util.List;
import java.lang.UnsupportedOperationException;

public class WeeksAdaptee {
    private StatisticsDAO statisticsDAO;

    public WeeksAdaptee(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public WeekData getWeekDataForWeekWithIndex(int index) throws UnsupportedOperationException {
        return statisticsDAO.getWeekData(index);
    }

    /*
     * @Brief : Get a number of weeks data starting from a specific week index
     * 
     * @Note : The week index is the index of the week starting from 0 (the current
     * week) to -n (n weeks before the current week)
     */
    public List<WeekData> getWeeksDataFromWeekWithIndex(int index, int numberOfWeeks)
            throws UnsupportedOperationException {
        return statisticsDAO.getWeeksData(index, numberOfWeeks);
    }

}
