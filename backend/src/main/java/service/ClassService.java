package service;

import config.ClassSQL;
import model.Class;

import java.util.List;

public class ClassService {

    private final ClassSQL classSQL;

    public ClassService(ClassSQL classSQL) {
        this.classSQL = classSQL;
    }

    public List<Class> getAllClasses() {
        return classSQL.findAll();
    }

    public List<Class> getClassesByTeacher(Long teacherId) {
        return classSQL.findByTeacherId(teacherId);
    }
}
