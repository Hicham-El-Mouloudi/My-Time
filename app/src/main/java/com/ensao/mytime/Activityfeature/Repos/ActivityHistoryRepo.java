package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.ActivityHistory;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.DataAccess.ActivityHistoryDAO;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ActivityHistoryRepo {





    private Executor _executor;
    private ActivityHistoryDAO _activityHistoryDAO;

    public ActivityHistoryRepo(Application app) {


        _activityHistoryDAO= ActivityRoomDB.getInstance(app)._ActivityHistoryDAO();
        //our async method executor
        _executor= Executors.newSingleThreadExecutor();



    }




    //insert
    public void Insert(ActivityHistory record, Activity CurrentActivity, CallBackAfterDbOperation<Long> Callback){

        //we will insert and return the id of the new History record
        _executor.execute(()->{
            //insert
            long id = _activityHistoryDAO.Insert(record);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(id));


        });

    }




    //other work ....






}
