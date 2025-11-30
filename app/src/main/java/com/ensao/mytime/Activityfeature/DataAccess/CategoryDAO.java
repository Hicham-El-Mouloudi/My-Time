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
    int Insert(Category record);


    @Query("select c.* , r.Title as RepetitionTitle from category as c inner join RepetitionKind as r " +
            "on c.RepetitionKindID=r.id ")
    List<CategoryDetailedDTO> GetCategories();







}
