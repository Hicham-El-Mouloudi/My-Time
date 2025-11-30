package com.ensao.mytime.Activityfeature;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ensao.mytime.Activityfeature.Busniss.*;
import com.ensao.mytime.Activityfeature.DataAccess.*;


@Database(entities = {userActivity.class, RepetitionKind.class,CourseContent.class,Course.class, Category.class,ActivityHistory.class},version = 1)
public abstract class ActivityRoomDB extends RoomDatabase {

    private static volatile ActivityRoomDB db;


    //entities ===========================================================================

    public abstract RepetitionKindDAO m_repetitionKindDao();
    public abstract userActivityDAO m_userActivityDAO();
    public abstract CourseDAO m_CourseDAO();
    public abstract CourseContentDAO m_CourseContentDAO();
    public abstract CategoryDAO m_CategoryDAO();
    public abstract ActivityHistoryDAO m_ActivityHistoryDAO();

    //====================================================================================





    public static synchronized ActivityRoomDB getInstance(Context context){
        if(db==null){

            db= Room.databaseBuilder(context.getApplicationContext(),ActivityRoomDB.class,"activity_db")
                    .build();



        }

        return db;
    }



    private static RoomDatabase.Callback callback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            //here we initialise the Tables with known values
            //like repetition kinds
            //and main activity categories
            //but we can use the room generated insert directly
            // we need to do it as an asynchronous operation


        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };






    //this approch is deprecated
    // but am too lazy to use the executorService with handler ('~')
    private static class populateDbAsync extends AsyncTask<Void,Void,Void>{

        private RepetitionKindDAO _repetitionKindDao;
        private CategoryDAO _categoryDao;




        public populateDbAsync(ActivityRoomDB db){

            _repetitionKindDao=db.m_repetitionKindDao();
            _categoryDao =db.m_CategoryDAO();
        }



        @Override
        protected Void doInBackground(Void... voids) {

            //populate the repetition kinds table

            int EachDayID = _repetitionKindDao.Insert(new RepetitionKind("eachday"));
            int EachMonthID = _repetitionKindDao.Insert(new RepetitionKind("eachmonth"));
            int EachDWeekID = _repetitionKindDao.Insert(new RepetitionKind("eachweek"));
            int OneTimeID = _repetitionKindDao.Insert(new RepetitionKind("onetime"));

            //if we wanted we can add some default categories here

            _categoryDao.Insert(new Category("the dafault category","default",OneTimeID));


            return null;
        }
    }

}
