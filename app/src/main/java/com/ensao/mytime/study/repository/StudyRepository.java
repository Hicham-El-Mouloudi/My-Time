package com.ensao.mytime.study.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.ensao.mytime.study.database.AppDatabase;
import com.ensao.mytime.study.model.Subject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyRepository {
    private SubjectDao subjectDao;
    private LiveData<List<Subject>> allSubjects;
    private ExecutorService executorService;

    public StudyRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        subjectDao = database.subjectDao();
        allSubjects = subjectDao.getAllSubjects();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insertSubject(Subject subject) {
        executorService.execute(() -> {
            subjectDao.insert(subject);
        });
    }

    public void updateSubject(Subject subject) {
        executorService.execute(() -> {
            subjectDao.update(subject);
        });
    }

    public void deleteSubject(Subject subject) {
        executorService.execute(() -> {
            subjectDao.delete(subject);
        });
    }
}