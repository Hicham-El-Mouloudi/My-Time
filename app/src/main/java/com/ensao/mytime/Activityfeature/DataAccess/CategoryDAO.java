package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;

import java.util.List;

@Dao
public interface CategoryDAO {
    @Insert
    long Insert(Category record);


    @Query("select c.* , r.Title as RepetitionTitle from category as c inner join RepetitionKind as r " +
            "on c.RepetitionKindID=r.id ")
    List<CategoryDetailedDTO> GetCategories();

    // Get default category for calendar activities
    @Query("SELECT * FROM Category WHERE Title = 'default' LIMIT 1")
    Category getDefaultCategory();







}
