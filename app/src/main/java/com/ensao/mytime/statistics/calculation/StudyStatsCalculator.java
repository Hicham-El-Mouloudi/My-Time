package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.model.DayData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyStatsCalculator extends AbstractStudyStatsCalculator {

    @Override
    protected int getTotalFocusTime(DayData dayData) {
        return dayData.getTotalFocusTime();
    }

    @Override
    protected int getStreakCount(DayData dayData) {
        return dayData.getStreakCount();
    }

    @Override
    protected float getPauseCount(DayData dayData) {
        return dayData.getPauseCount();
    }

    @Override
    protected Map<String, Integer> getSubjectDistribution(DayData dayData) {
        Map<String, Integer> dist = dayData.getSubjectDistribution();
        return dist != null ? dist : new HashMap<>();
    }

    @Override
    protected int getCompletedTasksCount(DayData dayData) {
        return dayData.getCompletedTasksCount();
    }

    @Override
    protected int getTotalTasksCount(DayData dayData) {
        return dayData.getTotalTasksCount();
    }

    @Override
    protected int getSubjectsStudiedCount(DayData dayData) {
        Map<String, Integer> dist = dayData.getSubjectDistribution();
        return dist != null ? dist.size() : 0;
    }

    @Override
    protected List<Integer> getWeeklySubjectsStudied(DayData dayData) {
        List<Integer> weekly = dayData.getWeeklySubjectsStudied();
        return weekly != null ? weekly : new ArrayList<>();
    }
}
