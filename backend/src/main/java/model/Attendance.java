package model;

import java.time.LocalDateTime;

public class Attendance {
    private Long id;
    private Long studentId;
    private Long sessionId;
    private AttendanceStatus status;
    private MarkedBy markedBy;
    private LocalDateTime markedAt;

    public Attendance() {
    }

    public Attendance(Long studentId, Long sessionId, AttendanceStatus status, MarkedBy markedBy) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
        this.markedBy = markedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public MarkedBy getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(MarkedBy markedBy) {
        this.markedBy = markedBy;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}