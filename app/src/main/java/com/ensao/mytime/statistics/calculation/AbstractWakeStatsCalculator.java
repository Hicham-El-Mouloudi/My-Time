package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.model.DayData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractWakeStatsCalculator {

    // Template Method
    public Map<String, Object> calculateWakeStats(DayData dayData) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("wakeLatency", getWakeLatency(dayData));
        stats.put("ringCount", getRingCount(dayData));
        stats.put("timeVariability", getTimeVariability(dayData));
        stats.put("firstAlarm", getFirstAlarm(dayData));
        stats.put("lastOff", getLastOff(dayData));
        stats.put("wakeDuration", getWakeDuration(dayData));
        stats.put("averageWakeTime", getAverageWakeTime(dayData));

        // Concrete method usage
        stats.put("last7DaysGraph", getCurrentWeekWakeTimeStats(getWakeVarianceLast7Days(dayData)));

        return stats;
    }

    // Abstract Steps (Hooks)
    protected abstract int getWakeLatency(DayData dayData);

    protected abstract int getRingCount(DayData dayData);

    protected abstract float getTimeVariability(DayData dayData);

    protected abstract String getFirstAlarm(DayData dayData);

    protected abstract String getLastOff(DayData dayData);

    protected abstract float getWakeDuration(DayData dayData);

    protected abstract List<Float> getWakeVarianceLast7Days(DayData dayData);

    protected abstract String getAverageWakeTime(DayData dayData);

    // Concrete Method
    protected List<Float> getCurrentWeekWakeTimeStats(List<Float> varianceData) {
        // Logic to process data for graph (e.g., normalization or just passing through)
        // For now, just return the data as is, assuming the View will handle rendering
        return varianceData;
    }
}
