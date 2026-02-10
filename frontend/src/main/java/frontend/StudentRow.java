package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

    private final StringProperty studentName;
    private final StringProperty email;

    public StudentRow(String name, String email) {
        this.studentName = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getStudentName() {
        return studentName.get();
    }

    public String getEmail() {
        return email.get();
    }
}
