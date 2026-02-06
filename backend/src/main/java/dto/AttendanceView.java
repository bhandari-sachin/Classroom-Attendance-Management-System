package dto;

import java.time.LocalDate;

public class AttendanceView {

    private Long studentId;
    private String name;
    private LocalDate sessionDate;
    private String status;

    public AttendanceView(Long studentId, String name
                          , LocalDate sessionDate,
                          String status) {
        this.studentId = studentId;
        this.name = name;
        this.sessionDate = sessionDate;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

