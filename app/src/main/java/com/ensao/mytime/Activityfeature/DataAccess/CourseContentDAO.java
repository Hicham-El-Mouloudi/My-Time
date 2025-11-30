package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;

import com.ensao.mytime.Activityfeature.Busniss.CourseContent;

@Dao
public interface CourseContentDAO {

    @Insert
    int Insert(CourseContent content);







}
