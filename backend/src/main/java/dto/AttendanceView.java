package dto;

import java.time.LocalDate;

public class AttendanceView {

    private Long studentNumber;
    private String firstName;
    private String lastName;
    private LocalDate sessionDate;
    private String status;

    public AttendanceView(Long studentNumber, String firstName, String lastName
                          , LocalDate sessionDate,
                          String status) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessionDate = sessionDate;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

