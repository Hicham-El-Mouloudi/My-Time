package com.ensao.mytime.statistics.data;

import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.model.WeekData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.*;
import java.time.LocalDate;
import java.util.List;
import java.lang.UnsupportedOperationException;

/*
* @Author : Hicham El Mouloudi
 * @Brief : Statistics Data Access Object
 * @Note : This interface is used to access the statistics data
 */
public interface StatisticsDAO {
    /*
     * @Brief : Get the days data for a specific month and year
     * 
     * @Param : month, year
     * 
     * @Return : List of days data
     */
    List<DayData> getDays(int month, int year);

    /*
     * @Brief : Get the day data for a specific date
     * 
     * @Param : date
     * 
     * @Return : Day data
     */
    DayData getDayData(LocalDate date);

    /*
     * @Brief : Get the week data for a specific week index
     * 
     * @Note : The week index is the index of the week starting from 0 (the current
     * week) to -n (n weeks before the current week)
     * 
     * @Note: The days are ordered from Monday to Sunday
     */
    WeekData getWeekData(int index) throws UnsupportedOperationException;

    /*
     * @Brief : Get a number of weeks data starting from a specific week index
     * 
     * @Note : The week index is the index of the week starting from 0 (the current
     * week) to -n (n weeks before the current week) with n > 0
     * 
     * @Note: The weeks are ordered from past to future
     */
    List<WeekData> getWeeksData(int index, int numberOfWeeks) throws UnsupportedOperationException;

    /*
     * @Brief : Get the date of a specific day of a specific week
     * 
     * @Note : The week index is the index of the week starting from 0 (the current
     * week) to -n (n weeks before the current week) with n > 0
     */
    default LocalDate getDateOf(int weekIndex, DayOfWeek day) throws UnsupportedOperationException {
        if (weekIndex > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less !");
        }
        LocalDate currentWeekMondayDate = LocalDate.now().with(previousOrSame(DayOfWeek.MONDAY));
        return currentWeekMondayDate.minusWeeks(-weekIndex).with(nextOrSame(day));

    }
}
