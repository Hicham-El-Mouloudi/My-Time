package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.Busniss.Course;
import com.ensao.mytime.Activityfeature.Busniss.userActivity;
import com.ensao.mytime.Activityfeature.DTOs.CourseWithContentCountDTO;
import com.ensao.mytime.Activityfeature.DataAccess.CourseDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseRepo {

    private  Executor  _executor;
    private  CourseDAO  _courseDAO;


    public CourseRepo(Application app) {

        _courseDAO = ActivityRoomDB.getInstance(app)._CourseDAO();

        //our async method executor
        _executor= Executors.newSingleThreadExecutor();


    }




    //insert
    public void Insert(Course course, Activity CurrentActivity, CallBackAfterDbOperation<Long> Callback){

        //we will insert and return the id of the new course
        _executor.execute(()->{
            //insert
            long id = _courseDAO.Insert(course);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(id));


        });

    }

    //get course list

    public void GetCourse(Date date, Activity CurrentActivity, CallBackAfterDbOperation<List<CourseWithContentCountDTO>> Callback){
        _executor.execute(()->{
            //get list
            List<CourseWithContentCountDTO> list  = _courseDAO.GetCourses();
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(list));


        });

    }












}
