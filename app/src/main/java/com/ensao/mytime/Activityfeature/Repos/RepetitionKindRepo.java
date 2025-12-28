package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.Busniss.RepetitionKind;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;
import com.ensao.mytime.Activityfeature.DataAccess.RepetitionKindDAO;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RepetitionKindRepo {


    private Executor _executor;
    private RepetitionKindDAO _repetitionKindDAO;
    private com.ensao.mytime.Activityfeature.DataAccess.CategoryDAO _categoryDAO;


    public RepetitionKindRepo(Application app) {

        _repetitionKindDAO= ActivityRoomDB.getInstance(app)._repetitionKindDao();
        _categoryDAO = ActivityRoomDB.getInstance(app)._CategoryDAO();

        //our async method executor
        _executor= Executors.newSingleThreadExecutor();
        
        // Check and seed initial data
        _executor.execute(this::checkAndSeedKinds);
    }
    
    private void checkAndSeedKinds() {
        checkAndInsertKind("eachday");
        checkAndInsertKind("eachmonth");
        checkAndInsertKind("eachweek");
        long OneTimeID = checkAndInsertKind("onetime");

        // Ensure default category exists
        if (_categoryDAO.getDefaultCategory() == null) {
            _categoryDAO.Insert(new Category("the dafault category", "default", OneTimeID));
        }
    }

    private long checkAndInsertKind(String title) {
        RepetitionKind kind = _repetitionKindDAO.getByTitle(title);
        if (kind == null) {
            return _repetitionKindDAO.Insert(new RepetitionKind(title));
        }
        return kind.getId();
    }

    public void Insert(RepetitionKind repetitionKind, Activity CurrentActivity, CallBackAfterDbOperation<Long> Callback){

        //we will insert and return the id of the new kind
        _executor.execute(()->{
            //insert
            long id = _repetitionKindDAO.Insert(repetitionKind);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(id));


        });

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
