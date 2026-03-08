package frontend;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

<<<<<<< HEAD
    private Long studentId = null;
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty excuseReason = new SimpleStringProperty("");

    //for dummy data
    public StudentRow(String studentName, String email, String status) {
=======
    private final SimpleLongProperty studentId = new SimpleLongProperty();
    private final SimpleStringProperty studentName = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty status = new SimpleStringProperty();
    private final SimpleStringProperty excuseReason = new SimpleStringProperty("");

    public StudentRow(long studentId, String studentName, String email, String status) {
        this.studentId.set(studentId);
>>>>>>> origin/admin-api
        this.studentName.set(studentName);
        this.email.set(email);
        this.status.set(status);
    }

<<<<<<< HEAD
    //studentId
    public StudentRow(Long studentId, String studentName, String email, String status) {
        this.studentId = studentId;
        this.studentName.set(studentName);
        this.email.set(email);
        this.status.set(status);
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long id) { this.studentId = id; }

    public String getStudentName() { return studentName.get(); }
    public void setStudentName(String v) { studentName.set(v); }
    public StringProperty studentNameProperty() { return studentName; }
=======
    public long getStudentId() {
        return studentId.get();
    }
>>>>>>> origin/admin-api

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty emailProperty() {
        return email;
    }

<<<<<<< HEAD
    public String getExcuseReason() { return excuseReason.get(); }
    public void setExcuseReason(String v) { excuseReason.set(v == null ? "" : v); }
    public StringProperty excuseReasonProperty() { return excuseReason; }
=======
    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public void setExcuseReason(String value) {
        excuseReason.set(value);
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
>>>>>>> origin/admin-api
}