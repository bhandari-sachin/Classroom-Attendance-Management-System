package frontend;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

    private final LongProperty studentId = new SimpleLongProperty();
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty excuseReason = new SimpleStringProperty("");

    public StudentRow(long studentId, String studentName, String email, String status) {
        this.studentId.set(studentId);
        this.studentName.set(studentName);
        this.email.set(email);
        this.status.set(status);
    }

    public long getStudentId() {
        return studentId.get();
    }

    public String getStudentName() {
        return studentName.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getExcuseReason() {
        return excuseReason.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public void setExcuseReason(String value) {
        excuseReason.set(value);
    }

    public LongProperty studentIdProperty() {
        return studentId;
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty excuseReasonProperty() {
        return excuseReason;
    }
}