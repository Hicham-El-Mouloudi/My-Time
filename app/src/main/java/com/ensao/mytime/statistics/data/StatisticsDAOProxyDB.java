package com.ensao.mytime.statistics.data;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsSleepSession;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsStudySession;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsWakeSession;
import com.ensao.mytime.Activityfeature.Repos.StatisticsSleepSessionRepo;
import com.ensao.mytime.Activityfeature.Repos.StatisticsStudySessionRepo;
import com.ensao.mytime.Activityfeature.Repos.StatisticsWakeSessionRepo;
import com.ensao.mytime.statistics.model.DayData;
import com.ensao.mytime.statistics.model.WeekData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StatisticsDAO implementation that fetches real data from the Room database.
 * Uses StatisticsSleepSessionRepo and StatisticsWakeSessionRepo async methods
 * directly.
 * No redundant Executor - relies on the repos' internal threading.
 */
public class StatisticsDAOProxyDB implements StatisticsDAO {

    private final StatisticsSleepSessionRepo sleepSessionRepo;
    private final StatisticsWakeSessionRepo wakeSessionRepo;
    private final StatisticsStudySessionRepo studySessionRepo;
    private final Activity activity;

    /**
     * @param application The application context for repo initialization
     * @param activity    The activity for UI thread callbacks (can be null if not
     *                    needed)
     */
    public StatisticsDAOProxyDB(Application application, Activity activity) {
        this.sleepSessionRepo = new StatisticsSleepSessionRepo(application);
        this.wakeSessionRepo = new StatisticsWakeSessionRepo(application);
        this.studySessionRepo = new StatisticsStudySessionRepo(application);
        this.activity = activity;
    }

    @Override
    public void getDays(int month, int year, StatisticsCallback<List<DayData>> callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Date startDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        sleepSessionRepo.getInRange(startDate, endDate, activity,
                CallbackAdapter.from(sleepSessions -> {
                    // Then get all wake sessions for the month
                    wakeSessionRepo.getInRange(startDate, endDate, activity,
                            CallbackAdapter.from(wakeSessions -> {
                                // Then get all study sessions for the month
                                studySessionRepo.getInRange(startDate, endDate, activity,
                                        CallbackAdapter.from(studySessions -> {
                                            // Build DayData list from all
                                            List<DayData> days = new ArrayList<>();
                                            Calendar dayCalendar = Calendar.getInstance();
                                            dayCalendar.set(Calendar.MONTH, month);
                                            dayCalendar.set(Calendar.YEAR, year);

                                            for (int i = 1; i <= maxDays; i++) {
                                                dayCalendar.set(Calendar.DAY_OF_MONTH, i);
                                                LocalDate localDate = dayCalendar.getTime().toInstant()
                                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                                                Date dayDate = dayCalendar.getTime();

                                                DayData dayData = buildDayDataFromSessions(
                                                        localDate, dayDate, sleepSessions, wakeSessions, studySessions);
                                                days.add(dayData);
                                            }

                                            if (callback != null) {
                                                callback.onComplete(days);
                                            }
                                        }));
                            }));
                }));
    }

    @Override
    public void getDayData(LocalDate date, StatisticsCallback<DayData> callback) {
        Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Get sleep session for this date
        sleepSessionRepo.getByDate(javaDate, activity,
                CallbackAdapter.from(sleepSession -> {
                    // Get wake session for this date
                    wakeSessionRepo.getByDate(javaDate, activity,
                            CallbackAdapter.from(wakeSession -> {
                                // Get study session for this date
                                studySessionRepo.getByDate(javaDate, activity,
                                        CallbackAdapter.from(studySession -> {
                                            DayData data = buildDayData(date, sleepSession, wakeSession, studySession);

                                            // Get last 7 days for variance calculation
                                            LocalDate startDate = date.minusDays(6);
                                            Date startJavaDate = Date
                                                    .from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                                            wakeSessionRepo.getInRange(startJavaDate, javaDate, activity,
                                                    CallbackAdapter.from(wakeSessions -> {
                                                        data.setWakeVariance(calculateWakeVariance(date, wakeSessions));
                                                        data.setAverageWakeTime(calculateAverageWakeTime(wakeSessions));

                                                        if (callback != null) {
                                                            callback.onComplete(data);
                                                        }
                                                    }));
                                        }));
                            }));
                }));
    }

    @Override
    public void getWeekData(int index, StatisticsCallback<WeekData> callback) throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less!");
        }

        WeekData weekData = new WeekData(index);
        List<DayData> daysOfWeek = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);

        // Create array to maintain order
        DayData[] orderedDays = new DayData[7];

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            LocalDate dayDate = getDateOf(index, dayOfWeek);
            int dayIndex = dayOfWeek.getValue() - 1;

            getDayData(dayDate, dayData -> {
                orderedDays[dayIndex] = dayData;

                if (completedCount.incrementAndGet() == 7) {
                    // All days loaded
                    for (DayData day : orderedDays) {
                        daysOfWeek.add(day);
                    }
                    weekData.setDays(daysOfWeek);

                    if (callback != null) {
                        callback.onComplete(weekData);
                    }
                }
            });
        }
    }

    @Override
    public void getWeeksData(int index, int numberOfWeeks, StatisticsCallback<List<WeekData>> callback)
            throws UnsupportedOperationException {
        if (index > 0) {
            throw new UnsupportedOperationException("StatisticsDAO : the index of a week must be 0 or less!");
        }

        List<WeekData> weeksData = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);
        WeekData[] orderedWeeks = new WeekData[numberOfWeeks];

        for (int i = 0; i > (-1 * numberOfWeeks); i--) {
            int weekIndex = i;
            int arrayIndex = Math.abs(i);

            getWeekData(weekIndex, weekData -> {
                orderedWeeks[arrayIndex] = weekData;

                if (completedCount.incrementAndGet() == numberOfWeeks) {
                    // All weeks loaded
                    for (WeekData week : orderedWeeks) {
                        weeksData.add(week);
                    }

                    if (callback != null) {
                        callback.onComplete(weeksData);
                    }
                }
            });
        }
    }

    /**
     * Build DayData from session lists (used when we already have batch data)
     */
    private DayData buildDayDataFromSessions(LocalDate date, Date javaDate,
            List<StatisticsSleepSession> sleepSessions,
            List<StatisticsWakeSession> wakeSessions,
            List<StatisticsStudySession> studySessions) {
        StatisticsSleepSession sleepSession = null;
        StatisticsWakeSession wakeSession = null;
        StatisticsStudySession studySession = null;

        // Find matching sessions
        for (StatisticsSleepSession s : sleepSessions) {
            if (isSameDay(s.getDate(), javaDate)) {
                sleepSession = s;
                break;
            }
        }
        for (StatisticsWakeSession w : wakeSessions) {
            if (isSameDay(w.getDate(), javaDate)) {
                wakeSession = w;
                break;
            }
        }
        if (studySessions != null) {
            for (StatisticsStudySession s : studySessions) {
                if (isSameDay(s.getDate(), javaDate)) {
                    studySession = s;
                    break;
                }
            }
        }

        return buildDayData(date, sleepSession, wakeSession, studySession);
    }

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null)
            return false;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Build DayData from individual sessions
     */
    private DayData buildDayData(LocalDate date, StatisticsSleepSession sleepSession,
            StatisticsWakeSession wakeSession, StatisticsStudySession studySession) {
        boolean hasSleep = sleepSession != null && sleepSession.isHasSleep();
        boolean hasWake = wakeSession != null && wakeSession.isHasWake();
        // Since study session might exist with 0 focus time if just initialized, check
        // focus time or hasStudy flag
        boolean hasStudy = studySession != null && studySession.isHasStudy();

        DayData data = new DayData(date, hasSleep, hasWake);

        if (sleepSession != null) {
            data.setSleepDuration(sleepSession.getSleepDuration());
            data.setSleepEfficiency(sleepSession.getSleepEfficiency());
            data.setTimeInBed(sleepSession.getTimeInBed());
            data.setSleepLatency(sleepSession.getSleepLatency());
            data.setWakeDuringSleep(sleepSession.getWakeDuringSleep());
            // Parse JSON string to List<WakeWhileSleepingDuration>
            String json = sleepSession.getWakeDuringSleepDistributionJSON();
            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<WakeWhileSleepingDuration>>() {
                }.getType();
                List<WakeWhileSleepingDuration> distribution = gson.fromJson(json, listType);
                data.setWakeDuringSleepDistribution(distribution);
            }
        }

        if (wakeSession != null) {
            data.setRingCount(wakeSession.getRingCount());
            data.setTimeVariability(wakeSession.getTimeVariability());
            data.setFirstAlarm(wakeSession.getFirstAlarm());
            data.setLastOff(wakeSession.getLastOff());
            data.setWakeDuration(wakeSession.getWakeDuration());
        }

        if (studySession != null) {
            data.setHasStudy(hasStudy);
            data.setTotalFocusTime(studySession.getTotalFocusTime());
            data.setStreakCount(studySession.getStreakCount());

            // Calculate Mean Pauses
            float meanPauses = 0;
            if (studySession.getSessionsCount() > 0) {
                meanPauses = studySession.getPauseCount() / (float) studySession.getSessionsCount();
            }
            data.setPauseCount(meanPauses);

            data.setCompletedTasksCount(studySession.getCompletedTasksCount());
            data.setTotalTasksCount(studySession.getTotalTasksCount());

            String json = studySession.getSubjectDistribution();
            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                Type mapType = new TypeToken<Map<String, Integer>>() {
                }.getType();
                Map<String, Integer> distribution = gson.fromJson(json, mapType);
                data.setSubjectDistribution(distribution);
            }
        } else {
            data.setHasStudy(false);
        }

        return data;
    }

    private List<Float> calculateWakeVariance(LocalDate date, List<StatisticsWakeSession> wakeSessions) {
        List<Float> variance = new ArrayList<>();
        LocalDate startDate = date.minusDays(6);

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            Date currentJavaDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            float varianceValue = 0.0f;
            for (StatisticsWakeSession session : wakeSessions) {
                if (isSameDay(session.getDate(), currentJavaDate)) {
                    varianceValue = session.getTimeVariability();
                    break;
                }
            }
            variance.add(varianceValue);
        }

        return variance;
    }

    private String calculateAverageWakeTime(List<StatisticsWakeSession> wakeSessions) {
        if (wakeSessions == null || wakeSessions.isEmpty()) {
            return "00:00";
        }

        int totalMinutes = 0;
        int count = 0;

        for (StatisticsWakeSession session : wakeSessions) {
            String firstAlarm = session.getFirstAlarm();
            if (firstAlarm != null && firstAlarm.contains(":")) {
                try {
                    String[] parts = firstAlarm.split(":");
                    int hours = Integer.parseInt(parts[0]);
                    int minutes = Integer.parseInt(parts[1]);
                    totalMinutes += hours * 60 + minutes;
                    count++;
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }

        if (count == 0) {
            return "00:00";
        }

        int averageMinutes = totalMinutes / count;
        int avgHours = averageMinutes / 60;
        int avgMins = averageMinutes % 60;

        return String.format("%02d:%02d", avgHours, avgMins);
    }
}
