package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.data.WakeWhileSleepingDuration;
import com.ensao.mytime.statistics.model.DayData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSleepStatsCalculator {

    // Template Method
    public Map<String, Object> calculateSleepStats(DayData dayData) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("sleepDuration", getSleepDuration(dayData));
        stats.put("sleepEfficiency", getSleepEfficiency(dayData));
        stats.put("timeInBed", getTimeInBed(dayData));
        stats.put("sleepLatency", getSleepLatency(dayData));
        stats.put("wakeDuringSleep", getWakeDuringSleep(dayData));
        stats.put("bedTime", getBedTime(dayData));
        stats.put("wakeTime", getWakeTime(dayData));
        stats.put("wakeDuringSleepDistribution", getWakeDuringSleepDistribution(dayData));
        return stats;
    }

    // Abstract Steps (Hooks)
    protected abstract float getSleepDuration(DayData dayData);

    protected abstract int getSleepEfficiency(DayData dayData);

    protected abstract float getTimeInBed(DayData dayData);

    protected abstract int getSleepLatency(DayData dayData);

    protected abstract int getWakeDuringSleep(DayData dayData);

    protected abstract long getBedTime(DayData dayData);

    protected abstract long getWakeTime(DayData dayData);

    protected abstract List<WakeWhileSleepingDuration> getWakeDuringSleepDistribution(DayData dayData);
}
