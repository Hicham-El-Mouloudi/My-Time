package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.userActivity;
import com.ensao.mytime.Activityfeature.DataAccess.userActivityDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ActivityRepo {


    private userActivityDAO _userActivityDAO;
    private Executor _executor;


    public  ActivityRepo(Application app){

        _userActivityDAO= ActivityRoomDB.getInstance(app)._userActivityDAO();
        //our async method executor
        _executor= Executors.newSingleThreadExecutor();


    }


    //=============           NOTE : WE CAN T USE THEM DIRECTLY , WE NEED TO EXECUTE THEM INSIDE AN ASYNC METHOD         ==================
    // we gonna use the executor for this
    // executor is a class to manage threads in nut shell
    // also a delegate so that the uidecides what to do with data






    //insert
    public void Insert(userActivity activity, Activity CurrentActivity, CallBackAfterDbOperation<Long> Callback){

        //we will insert and return the id of the new activity
        _executor.execute(()->{
            //insert
            long id = _userActivityDAO.Insert(activity);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(id));


        });

    }


    //get activities at the date X
    public void GetActivities(Date date, Activity CurrentActivity, CallBackAfterDbOperation<List<userActivity>> Callback){
        _executor.execute(()->{
            //get list
            List<userActivity> list  = _userActivityDAO.getActivities(date);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(list));


        });

    }

    //get activities in date range (for calendar)
    public void GetActivitiesInRange(long startOfDay, long endOfDay, Activity CurrentActivity, CallBackAfterDbOperation<List<userActivity>> Callback){
        _executor.execute(()->{
            List<userActivity> list = _userActivityDAO.getActivitiesInRange(startOfDay, endOfDay);
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(list));
        });
    }



}
