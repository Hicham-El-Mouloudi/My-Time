package com.ensao.mytime.Activityfeature.Repos;

import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.DataAccess.CourseContentDAO;
import com.ensao.mytime.Activityfeature.DataAccess.CourseDAO;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseContentRepo {


    private Executor _executor;
    private CourseContentDAO _courseContentDAO;


    public CourseContentRepo(Application app) {

        _courseContentDAO = ActivityRoomDB.getInstance(app)._CourseContentDAO();

        //our async method executor
        _executor= Executors.newSingleThreadExecutor();


    }




    //insert



    //get contents of course X



    //



}
