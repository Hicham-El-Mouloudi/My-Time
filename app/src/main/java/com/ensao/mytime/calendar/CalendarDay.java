package com.ensao.mytime.calendar;

public class CalendarDay {
    private int day;
    private boolean currentMonth;
    private boolean today;

    private int month;

    private int year;

    public CalendarDay(int day,int month ,int year , boolean currentMonth, boolean today) {
        this.day = day;
        this.currentMonth = currentMonth;
        this.today = today;
        this.year = year;
        this.month = month;
    }

    public int getDay() { return day; }
    public boolean isCurrentMonth() { return currentMonth; }
    public boolean isToday() { return today; }

    public int getYear() { return year;}

    public int getMonth() { return month;}
}