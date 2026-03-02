package model;

public class Attendance {
    private Long attendanceId;
    private final Long studentId;
    private final Long sessionId;
    private AttendanceStatus status;
    private MarkedBy markedBy;
    private String remarks;

    public Attendance(Long studentId, Long sessionId, AttendanceStatus status, MarkedBy markedBy) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
        this.markedBy = markedBy;
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
    public MarkedBy getMarkedBy() {
        return markedBy;
    }
    public String getRemarks() {
        return remarks;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}