package com.ensao.mytime.study.database;



import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.repository.SubjectDao;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Subject.class}, version = 1, exportSchema = false)

public abstract class StudyDatabase extends RoomDatabase {

    public abstract SubjectDao subjectDao();

    private static volatile StudyDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static StudyDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (StudyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    StudyDatabase.class,
                                    "study_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}