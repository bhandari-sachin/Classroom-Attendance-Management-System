package service;

import model.CourseClass;
import model.Student;

import java.util.List;

public class ClassService {

    private final ClassSQL classSQL;

    public ClassService(ClassSQL classSQL) {
        this.classSQL = classSQL;
    }

    public void createClass(CourseClass c) {
        classSQL.createClass(c);
    }

    public  void updateClass(CourseClass c) {
        classSQL.updateClass(c);
    }

    public void deleteClass(Long classId) {
        classSQL.deleteClass(classId);
    }

    public int getEnrollmentCount(Long classId) {
        return classSQL.getEnrollmentCount(classId);
    }
    public void enrollStudent(Long studentId, Long classId) {
        if (classSQL.isStudentEnrolled(studentId, classId)) {
            throw new IllegalStateException("Student is already enrolled in this class.");
        }
        int enrollmentCount = classSQL.getEnrollmentCount(classId);
        int capacity = classSQL.getClassCapacity(classId);
        if (enrollmentCount >= capacity) {
            throw new IllegalStateException("Class is at full capacity.");
        }
        classSQL.enrollStudent(studentId, classId);
    }

    public List<CourseClass> getAllClasses() {
        return classSQL.findAll();
    }

    public List<CourseClass> getClassesByTeacher(Long teacherId) {
        return classSQL.findByTeacherId(teacherId);
    }

    public List<Student> getStudentsInClass(Long classId) {
        return classSQL.getStudentsInClass(classId);
    }

    public List<CourseClass> getClassesForStudent(Long studentId) {
        return classSQL.findByStudentId(studentId);
    }
}
