package com.ensao.mytime.statistics.data;

import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.model.WeekData;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Mock implementation of StatisticsDAO that generates random data.
 * Uses async callbacks for consistency with the interface pattern.
 */
public class StatisticsDAOProxy implements StatisticsDAO {

    private final Executor executor;

    public StatisticsDAOProxy() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void getDays(int month, int year, StatisticsCallback<List<DayData>> callback) {
        executor.execute(() -> {
            List<DayData> days = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 1; i <= maxDays; i++) {
                calendar.set(Calendar.DAY_OF_MONTH, i);
                LocalDate date = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                // Use generateDayData to populate all statistics fields
                days.add(generateDayData(date));
            }

            if (callback != null) {
                callback.onComplete(days);
            }
        });
    }

    @Override
    public void getDayData(LocalDate date, StatisticsCallback<DayData> callback) {
        executor.execute(() -> {
            DayData data = generateDayData(date);
            if (callback != null) {
                callback.onComplete(data);
            }
        });
    }

    @Override
    public void getWeekData(int index, StatisticsCallback<WeekData> callback) throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less !");
        }

        executor.execute(() -> {
            WeekData weekData = new WeekData(index);
            List<DayData> daysOfWeek = new ArrayList<>();
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                LocalDate dayDate = getDateOf(index, dayOfWeek);
                daysOfWeek.add(generateDayData(dayDate));
            }
            weekData.setDays(daysOfWeek);

            if (callback != null) {
                callback.onComplete(weekData);
            }
        });
    }

    @Override
    public void getWeeksData(int index, int numberOfWeeks, StatisticsCallback<List<WeekData>> callback)
            throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less !");
        }

        executor.execute(() -> {
            List<WeekData> weeksData = new ArrayList<>();
            for (int i = 0; i > (-1 * numberOfWeeks); i--) {
                WeekData weekData = new WeekData(i);
                List<DayData> daysOfWeek = new ArrayList<>();
                for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                    LocalDate dayDate = getDateOf(i, dayOfWeek);
                    daysOfWeek.add(generateDayData(dayDate));
                }
                weekData.setDays(daysOfWeek);
                weeksData.add(weekData);
            }

            if (callback != null) {
                callback.onComplete(weeksData);
            }
        });
    }

    /**
     * Generate mock day data for a given date (synchronous internal method)
     */
    private DayData generateDayData(LocalDate date) {
        // Use the date as a seed for deterministic randomness
        Random random = new Random(date.toEpochDay());

        boolean hasSleep = random.nextFloat() > 0.1; // 90% chance of sleep data
        boolean hasWake = random.nextFloat() > 0.1; // 90% chance of wake data

        DayData data = new DayData(date, hasSleep, hasWake);

        // Sleep Data
        float sleepDuration = 6.0f + random.nextFloat() * 3.0f; // 6 to 9 hours
        data.setSleepDuration((float) (Math.round(sleepDuration * 10.0) / 10.0));
        data.setSleepEfficiency(70 + random.nextInt(30)); // 70% to 99%
        data.setTimeInBed(sleepDuration + 0.5f + random.nextFloat());
        data.setSleepLatency(5 + random.nextInt(25)); // 5 to 30 min
        data.setWakeDuringSleep(random.nextInt(40)); // 0 to 40 min

        // Wake Data
        data.setWakeLatency(random.nextInt(15)); // 0 to 15 min
        data.setRingCount(1 + random.nextInt(5)); // 1 to 6 rings
        data.setTimeVariability(random.nextFloat() * 5.0f);

        int hour = 6 + random.nextInt(2);
        int minute = random.nextInt(60);
        data.setFirstAlarm(String.format("%02d:%02d", hour, minute));

        minute += 5 + random.nextInt(20);
        if (minute >= 60) {
            hour++;
            minute -= 60;
        }
        data.setLastOff(String.format("%02d:%02d", hour, minute));

        data.setWakeDuration(10.0f + random.nextFloat() * 20.0f);
        data.setAverageWakeTime(String.format("%02d:%02d", 6 + random.nextInt(2), random.nextInt(60)));

        List<Float> variance = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            variance.add(random.nextFloat() * 15.0f); // 0 to 15 min variance
        }
        data.setWakeVariance(variance);

        // Study Data
        boolean hasStudy = random.nextFloat() > 0.2; // 80% chance of study data
        data.setHasStudy(hasStudy);

        if (hasStudy) {
            data.setTotalFocusTime(30 + random.nextInt(150)); // 30 to 180 min
            data.setStreakCount(1 + random.nextInt(14)); // 1 to 14 days
            data.setPauseCount(random.nextFloat() * 5.0f); // 0 to 5 avg pauses

            // Subject distribution mock
            String[] subjects = { "Maths", "Physique", "Informatique", "Langues", "Histoire" };
            Map<String, Integer> distribution = new HashMap<>();
            int numSubjects = 1 + random.nextInt(4); // 1 to 4 subjects
            for (int i = 0; i < numSubjects; i++) {
                distribution.put(subjects[random.nextInt(subjects.length)], 15 + random.nextInt(60));
            }
            data.setSubjectDistribution(distribution);

            // Task completion
            int total = 3 + random.nextInt(8); // 3 to 10 tasks
            int completed = random.nextInt(total + 1);
            data.setTotalTasksCount(total);
            data.setCompletedTasksCount(completed);

            // Weekly subjects studied
            List<Integer> weeklySubjects = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weeklySubjects.add(random.nextInt(5)); // 0 to 4 subjects per day
            }
            data.setWeeklySubjectsStudied(weeklySubjects);
        } else {
            data.setTotalFocusTime(0);
            data.setStreakCount(0);
            data.setPauseCount(0);
            data.setSubjectDistribution(new HashMap<>());
            data.setTotalTasksCount(0);
            data.setCompletedTasksCount(0);
            data.setWeeklySubjectsStudied(new ArrayList<>());
        }

        return data;
    }
}
