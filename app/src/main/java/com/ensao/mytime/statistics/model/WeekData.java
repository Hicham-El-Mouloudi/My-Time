package com.ensao.mytime.statistics.model;

import java.util.ArrayList;
import java.util.List;

/*
 * WeekData class
 * @Brief: This class represents a week data
 * @Note : weeks indexes are incremental in the direction from current week to previous weeks
 */
public class WeekData {
    // 0 is the current week, -1 is the previous week
    private int weekIndex; // week number from current week
    private List<DayData> days; // list of 7 days

    public WeekData(int weekIndex, List<DayData> days) {
        this.weekIndex = weekIndex;
        this.days = days;
    }

    public WeekData(int weekIndex) {
        this.weekIndex = weekIndex;
        this.days = new ArrayList<>();
    }

    public int getweekIndex() {
        return weekIndex;
    }

    public void setweekIndex(int weekIndex) {
        this.weekIndex = weekIndex;
    }

    public List<DayData> getDays() {
        return days;
    }

    public void setDays(List<DayData> days) {
        this.days = days;
    }

}
