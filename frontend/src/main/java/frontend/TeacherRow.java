package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TeacherRow {

    private final StringProperty teacherName;
    private final StringProperty email;

    public TeacherRow(String name, String email) {
        this.teacherName = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    public StringProperty teacherNameProperty() {
        return teacherName;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getTeacherName() {
        return teacherName.get();
    }

    public String getEmail() {
        return email.get();
    }
}
