package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty excuseReason = new SimpleStringProperty("");

    public StudentRow(String studentName, String email, String status) {
        this.studentName.set(studentName);
        this.email.set(email);
        this.status.set(status);
    }

    public String getStudentName() { return studentName.get(); }
    public void setStudentName(String v) { studentName.set(v); }
    public StringProperty studentNameProperty() { return studentName; }

    public String getEmail() { return email.get(); }
    public void setEmail(String v) { email.set(v); }
    public StringProperty emailProperty() { return email; }

    public String getStatus() { return status.get(); }
    public void setStatus(String v) { status.set(v); }
    public StringProperty statusProperty() { return status; }

    public String getExcuseReason() { return excuseReason.get(); }
    public void setExcuseReason(String v) { excuseReason.set(v == null ? "" : v); }
    public StringProperty excuseReasonProperty() { return excuseReason; }
}
