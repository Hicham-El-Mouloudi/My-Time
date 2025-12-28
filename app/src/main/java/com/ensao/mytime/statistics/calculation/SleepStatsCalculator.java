package com.ensao.mytime.statistics.calculation;

import com.ensao.mytime.statistics.data.WakeWhileSleepingDuration;
import com.ensao.mytime.statistics.model.DayData;

import java.time.ZoneId;
import java.util.List;

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

    @Override
    protected long getBedTime(DayData dayData) {
        // Calculate bed time from date and timeInBed (approximate: date at 22:00)
        if (dayData.getDate() != null) {
            return dayData.getDate().atTime(22, 0)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return System.currentTimeMillis() - (long) (dayData.getTimeInBed() * 3600000);
    }

    @Override
    protected long getWakeTime(DayData dayData) {
        // Calculate wake time from bedTime + timeInBed
        long bedTime = getBedTime(dayData);
        return bedTime + (long) (dayData.getTimeInBed() * 3600000);
    }

    @Override
    protected List<WakeWhileSleepingDuration> getWakeDuringSleepDistribution(DayData dayData) {
        return dayData.getWakeDuringSleepDistribution();
    }
}
