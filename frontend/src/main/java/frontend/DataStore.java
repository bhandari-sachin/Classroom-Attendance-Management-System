package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataStore {

    private static final ObservableList<StudentRow> students =
            FXCollections.observableArrayList(
                    new StudentRow("User", "user1@example.com", "Present"),
                    new StudentRow("User", "user2@example.com", "Present"),
                    new StudentRow("User", "user3@example.com", "Present")
            );

    public static ObservableList<StudentRow> getStudents() {
        return students;
    }
}
