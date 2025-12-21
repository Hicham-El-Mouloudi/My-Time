package com.ensao.mytime.Activityfeature;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ensao.mytime.Activityfeature.Busniss.*;
import com.ensao.mytime.Activityfeature.DataAccess.*;


@Database(entities = {userActivity.class, RepetitionKind.class,CourseContent.class,Course.class, Category.class,ActivityHistory.class},version = 2)
@TypeConverters({Converters.class})
public abstract class ActivityRoomDB extends RoomDatabase {

    private static volatile ActivityRoomDB Instance;


    //entities ===========================================================================

    public abstract RepetitionKindDAO _repetitionKindDao();
    public abstract userActivityDAO _userActivityDAO();
    public abstract CourseDAO _CourseDAO();
    public abstract CourseContentDAO _CourseContentDAO();
    public abstract CategoryDAO _CategoryDAO();
    public abstract ActivityHistoryDAO _ActivityHistoryDAO();

    //====================================================================================





    public static synchronized ActivityRoomDB getInstance(Context context){
        if(Instance ==null){

            Instance = Room.databaseBuilder(context.getApplicationContext(),ActivityRoomDB.class,"activity_db")
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()  // Allow destructive migration during development
                    .build();



        }

        return Instance;
    }



    private static RoomDatabase.Callback callback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // No initialization here - it would run every time and cause duplicate inserts!
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Initialize tables with known values ONLY on first database creation
            // This runs only once when the database is first created
            new populateDbAsync(Instance).execute();
        }
    };






    //this approch is deprecated
    // but am too lazy to use the executorService with handler ('~')
    private static class populateDbAsync extends AsyncTask<Void,Void,Void>{

        private RepetitionKindDAO _repetitionKindDao;
        private CategoryDAO _categoryDao;




        public populateDbAsync(ActivityRoomDB db){

            _repetitionKindDao=db._repetitionKindDao();
            _categoryDao =db._CategoryDAO();
        }



        @Override
        protected Void doInBackground(Void... voids) {

            //populate the repetition kinds table

            long EachDayID = _repetitionKindDao.Insert(new RepetitionKind("eachday"));
            long EachMonthID = _repetitionKindDao.Insert(new RepetitionKind("eachmonth"));
            long EachDWeekID = _repetitionKindDao.Insert(new RepetitionKind("eachweek"));
            long OneTimeID = _repetitionKindDao.Insert(new RepetitionKind("onetime"));

            //if we wanted we can add some default categories here

            _categoryDao.Insert(new Category("the dafault category","default",OneTimeID));


            return null;
        }
    }

}
