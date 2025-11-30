package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;

import com.ensao.mytime.Activityfeature.Busniss.ActivityHistory;

@Dao
public interface ActivityHistoryDAO {

    @Insert
    int Insert(ActivityHistory Historyrecord);


    //we will work on this later
    //we will either have an interface to show history or just use this data for some analytics (to calculate user performance)






}
