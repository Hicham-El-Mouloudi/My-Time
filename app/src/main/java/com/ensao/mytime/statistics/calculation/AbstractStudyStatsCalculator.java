package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.model.DayData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractStudyStatsCalculator {

    // Template Method
    public Map<String, Object> calculateStudyStats(DayData dayData) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFocusTime", getTotalFocusTime(dayData));
        stats.put("streakCount", getStreakCount(dayData));
        stats.put("pauseCount", getPauseCount(dayData));
        stats.put("subjectDistribution", getSubjectDistribution(dayData));
        stats.put("completedTasksCount", getCompletedTasksCount(dayData));
        stats.put("totalTasksCount", getTotalTasksCount(dayData));
        stats.put("subjectsStudiedCount", getSubjectsStudiedCount(dayData));
        stats.put("weeklySubjectsStudied", getWeeklySubjectsStudied(dayData));
        return stats;
    }

    // Abstract Steps (Hooks)
    protected abstract int getTotalFocusTime(DayData dayData);

    protected abstract int getStreakCount(DayData dayData);

    protected abstract float getPauseCount(DayData dayData);

    protected abstract Map<String, Integer> getSubjectDistribution(DayData dayData);

    protected abstract int getCompletedTasksCount(DayData dayData);

    protected abstract int getTotalTasksCount(DayData dayData);

    protected abstract int getSubjectsStudiedCount(DayData dayData);

    protected abstract List<Integer> getWeeklySubjectsStudied(DayData dayData);
}
