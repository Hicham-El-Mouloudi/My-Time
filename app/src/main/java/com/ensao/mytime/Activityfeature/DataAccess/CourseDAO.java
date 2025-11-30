package com.ensao.mytime.Activityfeature.DataAccess;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.Course;
import com.ensao.mytime.Activityfeature.DTOs.CourseWithContentCountDTO;

import java.util.List;

@Dao
public interface CourseDAO {

    @Insert
    long Insert(Course newCourse);


    @Query("delete from Course where id = :id")
    void Delete(int id);


    //note that because we used inner join , courses with no content will not be returned in this list
    @Query("select c.* , CourseContentCount.NumberOfContent as ContentCount  from Course as c inner  join " +
            "(select CourseID as CID ,  Count(1) as NumberOfContent from CourseContent group by CourseID) as CourseContentCount " +
            "on c.id= CourseContentCount.CID ")
    List<CourseWithContentCountDTO> GetCourses();



}
