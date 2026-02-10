package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataStore {

    private static final ObservableList<StudentRow> students =
            FXCollections.observableArrayList();

    private static final ObservableList<TeacherRow> teachers =
            FXCollections.observableArrayList();

    static {
        // ===== Dummy Students =====
        students.add(new StudentRow("Alice Martin", "alice@school.com", "Present"));
        students.add(new StudentRow("John Smith", "john@school.com", "Absent"));
        students.add(new StudentRow("Sara Lopez", "sara@school.com", "Present"));

        // ===== Dummy Teachers =====
        teachers.add(new TeacherRow("Mr. Johnson", "johnson@school.com"));
        teachers.add(new TeacherRow("Ms. Williams", "williams@school.com"));
    }

    public static ObservableList<StudentRow> getStudents() {
        return students;
    }

    public static ObservableList<TeacherRow> getTeachers() {
        return teachers;
    }
}
