package com.ensao.mytime.alarm;

public enum AlarmRepetition {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String strValue;
    private AlarmRepetition(String strValue) {
        this.strValue = strValue;
    }
    public String getStrValue() {
        return strValue;
    }
}

