package com.ensao.mytime.study.repository;

import androidx.lifecycle.MutableLiveData;
import com.ensao.mytime.study.model.Subject;
import java.util.ArrayList;
import java.util.List;

public class StudyRepository {
    private MutableLiveData<List<Subject>> subjectsLiveData = new MutableLiveData<>();
    private List<Subject> subjectsList = new ArrayList<>();
    private int nextId = 1;

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

    // Opérations pour Subject - SIMPLES sans Base de données
    public void insertSubject(Subject subject) {
        subject.setId(nextId++);
        subjectsList.add(subject);
        // Notifier les observateurs que les données ont changé
        subjectsLiveData.setValue(new ArrayList<>(subjectsList));
    }

    public void updateSubject(Subject subject) {
        for (int i = 0; i < subjectsList.size(); i++) {
            if (subjectsList.get(i).getId() == subject.getId()) {
                subjectsList.set(i, subject);
                break;
            }
        }
        subjectsLiveData.setValue(new ArrayList<>(subjectsList));
    }

    public void deleteSubject(Subject subject) {
        subjectsList.removeIf(s -> s.getId() == subject.getId());
        subjectsLiveData.setValue(new ArrayList<>(subjectsList));
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