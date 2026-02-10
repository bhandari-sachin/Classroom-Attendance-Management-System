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
        students.add(new StudentRow("Alice Martin", "alice@school.com"));
        students.add(new StudentRow("John Smith", "john@school.com"));
        students.add(new StudentRow("Sara Lopez", "sara@school.com"));

        // ===== Dummy Teachers =====
        teachers.add(new TeacherRow("Mr. Johnson", "johnson@school.com"));
        teachers.add(new TeacherRow("Ms. Williams", "williams@school.com"));
    }
            FXCollections.observableArrayList(
                    new StudentRow("User", "user1@example.com", "Present"),
                    new StudentRow("User", "user2@example.com", "Present"),
                    new StudentRow("User", "user3@example.com", "Present")
            );

    public static ObservableList<StudentRow> getStudents() {
        return students;
    }

    public static ObservableList<TeacherRow> getTeachers() {
        return teachers;
    }
}
