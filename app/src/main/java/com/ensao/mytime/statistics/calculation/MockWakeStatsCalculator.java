package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.model.DayData;
import java.util.List;

public class MockWakeStatsCalculator extends AbstractWakeStatsCalculator {

    @Override
    protected int getWakeLatency(DayData dayData) {
        return dayData.getWakeLatency();
    }

    @Override
    protected int getRingCount(DayData dayData) {
        return dayData.getRingCount();
    }

    @Override
    protected float getTimeVariability(DayData dayData) {
        return dayData.getTimeVariability();
    }

    @Override
    protected String getFirstAlarm(DayData dayData) {
        return dayData.getFirstAlarm();
    }

    @Override
    protected String getLastOff(DayData dayData) {
        return dayData.getLastOff();
    }

    @Override
    protected float getWakeDuration(DayData dayData) {
        return dayData.getWakeDuration();
    }

    @Override
    protected List<Float> getWakeVarianceLast7Days(DayData dayData) {
        return dayData.getWakeVariance();
    }

    @Override
    protected String getAverageWakeTime(DayData dayData) {
        return dayData.getAverageWakeTime();
    }
}
