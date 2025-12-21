package com.ensao.mytime.alarm;

/**
 * Interface for puzzle activities that can be used to dismiss sleep alarms.
 * Activities implementing this interface can communicate with the alarm service
 * to control alarm behavior during puzzle solving.
 */
public interface Puzzleable {
    // Broadcast action constants
    String ACTION_PUZZLE_STARTED = "com.ensao.mytime.ACTION_PUZZLE_STARTED";
    String ACTION_PUZZLE_COMPLETED = "com.ensao.mytime.ACTION_PUZZLE_COMPLETED";
    String EXTRA_ALARM_ID = "ALARM_ID";

    /**
     * Called when the activity starts as a puzzle for an alarm.
     * 
     * @param alarmId The ID of the alarm that triggered this puzzle.
     */
    void onPuzzleModeActivated(int alarmId);

    /**
     * Check if the puzzle is currently being actively solved.
     * When true, auto-snooze alarms should be ignored.
     * 
     * @return true if the user is actively solving the puzzle
     */
    boolean isPuzzleActive();

    /**
     * Get the alarm ID associated with this puzzle session.
     * 
     * @return the alarm ID, or -1 if not in puzzle mode
     */
    int getAssociatedAlarmId();

    /**
     * Broadcast that the puzzle has been completed/solved.
     * This should stop the alarm and deactivate it in the database.
     */
    void onPuzzleSolved();
}
