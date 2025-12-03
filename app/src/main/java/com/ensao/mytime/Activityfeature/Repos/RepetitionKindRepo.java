package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.RepetitionKind;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;
import com.ensao.mytime.Activityfeature.DataAccess.RepetitionKindDAO;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RepetitionKindRepo {


    private Executor _executor;
    private RepetitionKindDAO _repetitionKindDAO;


    public RepetitionKindRepo(Application app) {

        _repetitionKindDAO= ActivityRoomDB.getInstance(app)._repetitionKindDao();

        //our async method executor
        _executor= Executors.newSingleThreadExecutor();


    }







    //get list
    public void GetKinds(Activity CurrentActivity, CallBackAfterDbOperation<List<RepetitionKind>> Callback){
        _executor.execute(()->{
            //get list
            List<RepetitionKind> list  = _repetitionKindDAO.getRepetitionKinds();
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(list));


        });

    }






}
