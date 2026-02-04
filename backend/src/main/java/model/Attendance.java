package model;

public class Attendance {
    private Long attendanceId;
    private final Long studentId;
    private final Long sessionId;
    private AttendanceStatus status;
    private String remarks;

    public Attendance(Long studentId, Long sessionId, AttendanceStatus status) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }
    public Long getSessionId() {
        return sessionId;
    }
    public AttendanceStatus getStatus() {
        return status;
    }
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
