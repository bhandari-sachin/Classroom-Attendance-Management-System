package dto;

import java.time.LocalDate;

public class AttendanceView {

    private Long studentNumber;
    private String firstName;
    private String lastName;
    private LocalDate sessionDate;
    private String status;

    public AttendanceView() {
    }

    public AttendanceView(Long studentNumber, String firstName, String lastName,
                          LocalDate sessionDate, String status) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessionDate = sessionDate;
        this.status = status;
    }

    public Long getStudentNumber() { return studentNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getSessionDate() { return sessionDate; }
    public String getStatus() { return status; }

    public void setStudentNumber(Long studentNumber) { this.studentNumber = studentNumber; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public void setStatus(String status) { this.status = status; }
}