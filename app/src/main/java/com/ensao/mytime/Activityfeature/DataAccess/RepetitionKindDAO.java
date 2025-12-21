package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.RepetitionKind;

import java.util.List;

@Dao
public interface RepetitionKindDAO {



    @Insert
    long Insert(RepetitionKind kind);


    @Query("select * from RepetitionKind")
    List<RepetitionKind> getRepetitionKinds();




}
