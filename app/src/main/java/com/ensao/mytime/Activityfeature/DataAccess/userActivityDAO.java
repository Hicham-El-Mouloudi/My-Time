package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.userActivity;

import java.util.Date;
import java.util.List;

@Dao
public interface userActivityDAO {

    @Insert
    long Insert(userActivity Activity);


    @Query("delete from activities where id = :id")
    int Delete(int id);




    @Query("update activities  set IsActive = 0 where id = :id")
    void DesactivateActivity(int id);


    //get Activitys of date
    // Note: Room stores Date as milliseconds (Long), so we divide by 1000 to get Unix timestamp for strftime
    // Using 'unixepoch', 'localtime' to convert to local timezone for proper comparison
    @Query("SELECT a.* FROM activities AS a " +
            "INNER JOIN Category AS c ON a.CategoryID = c.id " +
            "INNER JOIN RepetitionKind AS r ON r.id = c.RepetitionKindID " +
            "WHERE a.IsActive = 1 AND (" +
            "  r.Title = 'eachday' " +
            "  OR (r.Title = 'eachweek' AND CAST(strftime('%w', a.StartDate / 1000, 'unixepoch', 'localtime') AS INTEGER) = CAST(strftime('%w', :date / 1000, 'unixepoch', 'localtime') AS INTEGER)) " +
            "  OR (r.Title = 'eachmonth' AND CAST(strftime('%d', a.StartDate / 1000, 'unixepoch', 'localtime') AS INTEGER) = CAST(strftime('%d', :date / 1000, 'unixepoch', 'localtime') AS INTEGER)) " +
            "  OR (r.Title = 'onetime' AND date(a.StartDate / 1000, 'unixepoch', 'localtime') = date(:date / 1000, 'unixepoch', 'localtime'))" +
            ")")
    List<userActivity> getActivities(Date date);

    // Simpler query - get activities for a specific date range (for debugging or alternative use)
    @Query("SELECT * FROM activities WHERE IsActive = 1 AND StartDate >= :startOfDay AND StartDate < :endOfDay")
    List<userActivity> getActivitiesInRange(long startOfDay, long endOfDay);

    // Get all active activities (for debugging)
    @Query("SELECT * FROM activities WHERE IsActive = 1")
    List<userActivity> getAllActiveActivities();


}
