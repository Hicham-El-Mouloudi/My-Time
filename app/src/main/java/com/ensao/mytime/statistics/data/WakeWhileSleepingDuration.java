package com.ensao.mytime.statistics.data;

/**
 * Represents a period of wakefulness during sleep.
 * Contains the start and end time of each wake episode.
 */
public class WakeWhileSleepingDuration {

    private String startTime; // HH:mm format
    private String endTime; // HH:mm format

    public WakeWhileSleepingDuration() {
    }

    public WakeWhileSleepingDuration(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
