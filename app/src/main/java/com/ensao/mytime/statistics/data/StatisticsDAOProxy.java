package com.ensao.mytime.statistics.data;

import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.model.WeekData;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class StatisticsDAOProxy implements StatisticsDAO {
    
    @Override
    public List<DayData> getDays(int month, int year) {
        List<DayData> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Random random = new Random();

        for (int i = 1; i <= maxDays; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            boolean hasSleep = random.nextBoolean();
            boolean hasWake = random.nextBoolean();
            days.add(new DayData(calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), hasSleep, hasWake));
        }
        return days;
    }

    @Override
    public DayData getDayData(LocalDate date) {
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

        return data;
    }

    @Override
    public WeekData getWeekData(int index) throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less !");
        }
        WeekData weekData = new WeekData(index);
        List<DayData> daysOfWeek = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            LocalDate dayDate = getDateOf(index, dayOfWeek);
            daysOfWeek.add(getDayData(dayDate));
        }
        weekData.setDays(daysOfWeek);
        return weekData;
    }

    @Override
    public List<WeekData> getWeeksData(int index, int numberOfWeeks) throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less !");
        }
        //
        List<WeekData> weeksData = new ArrayList<>();
        for (int i = 0; i > (-1 * numberOfWeeks); i--) {
            weeksData.add(getWeekData(i));
        }
        return weeksData;
    }
}
