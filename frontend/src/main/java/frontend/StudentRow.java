package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public StudentRow(String studentName, String status) {
        this.studentName.set(studentName);
        this.status.set(status);
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String newStatus) {
        status.set(newStatus);
    }
}
