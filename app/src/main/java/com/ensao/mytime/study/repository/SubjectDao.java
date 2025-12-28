package com.ensao.mytime.study.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.ensao.mytime.study.model.Subject;
import java.util.List;

@Dao
public interface SubjectDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Subject subject);

    // UPDATE
    @Update
    void update(Subject subject);

    // DELETE
    @Delete
    void delete(Subject subject);

    // GET ALL
    @Query("SELECT * FROM subjects ORDER BY id DESC")
    LiveData<List<Subject>> getAllSubjects();

    // Autres méthodes optionnelles
    @Query("SELECT * FROM subjects WHERE id = :id")
    LiveData<Subject> getSubjectById(int id);

    @Query("UPDATE subjects SET is_completed = :completed WHERE id = :id")
    void updateCompletedStatus(int id, boolean completed);

    @Query("DELETE FROM subjects WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM subjects")
    void deleteAll();
}