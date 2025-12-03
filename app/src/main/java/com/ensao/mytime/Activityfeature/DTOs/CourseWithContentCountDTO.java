package com.ensao.mytime.Activityfeature.DTOs;

import androidx.room.Embedded;

import com.ensao.mytime.Activityfeature.Busniss.Course;

public class CourseWithContentCountDTO {

    @Embedded
    public Course course;

    public int ContentCount;


}
