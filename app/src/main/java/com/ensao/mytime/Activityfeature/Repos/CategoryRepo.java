package com.ensao.mytime.Activityfeature.Repos;


import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.Busniss.userActivity;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;
import com.ensao.mytime.Activityfeature.DataAccess.CategoryDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CategoryRepo {



    private CategoryDAO _categoryDAO;
    private Executor _executor;


    public CategoryRepo(Application app) {

        _categoryDAO = ActivityRoomDB.getInstance(app)._CategoryDAO();
        //our async method executor
        _executor= Executors.newSingleThreadExecutor();

    }


    //insert

    public void Insert(Category category, Activity CurrentActivity, CallBackAfterDbOperation<Long> Callback){

        //we will insert and return the id of the new category
        _executor.execute(()->{
            //insert
            long id = _categoryDAO.Insert(category);
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(id));


        });

    }




    //get list

    public void GetCategories( Activity CurrentActivity, CallBackAfterDbOperation<List<CategoryDetailedDTO>> Callback){
        _executor.execute(()->{
            //get list
            List<CategoryDetailedDTO> list  = _categoryDAO.GetCategories();
            //run the delegation on the ui thread
            //to update the ui
            if(Callback!=null)
                CurrentActivity.runOnUiThread(()-> Callback.onComplete(list));


        });

    }






}
