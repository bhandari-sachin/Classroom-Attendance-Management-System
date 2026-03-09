package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReportRow {

    private final StringProperty student = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public ReportRow(String student, String date, String status) {
        this.student.set(student);
        this.date.set(date);
        this.status.set(status);
    }

    public StringProperty studentProperty() {
        return student;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty statusProperty() {
        return status;
    }
}