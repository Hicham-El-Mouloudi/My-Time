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
    /*
     * public StudyRepository() {
     * // Initialiser avec quelques données de test
     * subjectsList.add(new Subject(1, "Mathématiques", false));
     * subjectsList.add(new Subject(2, "Physique", false));
     * subjectsList.add(new Subject(3, "Programmation Java", false));
     * nextId = 4;
     * subjectsLiveData.setValue(new ArrayList<>(subjectsList));
     * }
     */

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
        //subjectsList.removeIf(s -> s.getId() == subject.getId());
        //subjectsLiveData.setValue(new ArrayList<>(subjectsList));
    }

    public Subject getSubjectById(int id) {
        for (Subject s : subjectsList) {
            if (s.getId() == id)
                return s;
        }
        return null;
    }

    public MutableLiveData<List<Subject>> getAllSubjects() {
        return subjectsLiveData;
    }
}