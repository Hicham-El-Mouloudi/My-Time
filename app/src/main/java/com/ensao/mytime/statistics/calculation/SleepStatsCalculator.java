package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.model.DayData;

public class SleepStatsCalculator extends AbstractSleepStatsCalculator {

    @Override
    protected float getSleepDuration(DayData dayData) {
        return dayData.getSleepDuration();
    }

    @Override
    protected int getSleepEfficiency(DayData dayData) {
        return dayData.getSleepEfficiency();
    }

    @Override
    protected float getTimeInBed(DayData dayData) {
        return dayData.getTimeInBed();
    }

    @Override
    protected int getSleepLatency(DayData dayData) {
        return dayData.getSleepLatency();
    }

    @Override
    protected int getWakeDuringSleep(DayData dayData) {
        return dayData.getWakeDuringSleep();
    }
}
