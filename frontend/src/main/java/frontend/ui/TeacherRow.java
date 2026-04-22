package frontend.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TeacherRow {

    private final StringProperty teacherName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    public TeacherRow(String teacherName, String email) {
        this.teacherName.set(teacherName);
        this.email.set(email);
    }

    public String getTeacherName() {
        return teacherName.get();
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty teacherNameProperty() {
        return teacherName;
    }

    public StringProperty emailProperty() {
        return email;
    }
}