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
    int Insert(userActivity Activity);


    @Query("delete from activities where id = :id")
    int Delete(int id);




    @Query("update activities  set IsActive = 0 where id = :id")
    int DesactivateActivity(int id);


    //get Activitys of date
    @Query("select a.* from activities as a inner join Category as c on a.CategoryID= c.id inner join  RepetitionKind as r on r.id=c.RepetitionKindID " +
            "where a.IsActive = 1 and (  r.Title='eachday'  " +
            "or (r.Title = 'eachweek' and  CAST(strftime('%w', a.StartDate) as integer) =  Cast(strftime('%w', :date) as integer)) " +
            "or (r.Title = 'eachmonth' and  CAST(strftime('%d', a.StartDate) as integer) = Cast(strftime('%d', :date) as integer))  " +
            "or (r.Title = 'onetime' and  a.StartDate =  :date)) ")
    List<userActivity> getActivities(Date date);






}
