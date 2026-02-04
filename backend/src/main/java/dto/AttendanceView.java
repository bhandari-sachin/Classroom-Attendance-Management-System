package dto;

import java.time.LocalDate;

public class AttendanceView {

    private Long studentId;
    private String firstName;
    private String lastName;
    private LocalDate sessionDate;
    private String status;

    public AttendanceView(Long studentId, String firstName, String lastName
                          , LocalDate sessionDate,
                          String status) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessionDate = sessionDate;
        this.status = status;
    }

    public String getFullName() {
        return firstName+" "+lastName;
    }

    public String getStatus() {
        return status;
    }
}

