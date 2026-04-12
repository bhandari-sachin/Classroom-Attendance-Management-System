package model;

import java.time.LocalDateTime;

/**
 * Represents an attendance record for a student in a specific session.
 * Contains details about who marked the attendance, when it was marked,
 * and the attendance status.
 */
public class Attendance {

    private Long id;
    private Long studentId;
    private Long sessionId;
    private AttendanceStatus status;
    private MarkedBy markedBy;
    private LocalDateTime markedAt;

    /**
     * Default constructor for frameworks and serialization.
     */
    public Attendance() {
    }

    /**
     * Constructs an Attendance record with essential details.
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @param status the attendance status (e.g., PRESENT, ABSENT)
     * @param markedBy who marked the attendance (e.g., TEACHER, ADMIN)
     */
    public Attendance(Long studentId, Long sessionId, AttendanceStatus status, MarkedBy markedBy) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
        this.markedBy = markedBy;
        this.markedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    // --- Utility Methods ---

    /**
     * Checks if the attendance was marked by a teacher.
     * @return true if marked by a teacher, false otherwise
     */
    public boolean isMarkedByTeacher() {
        return markedBy == MarkedBy.TEACHER;
    }

    /**
     * Returns a readable summary of the attendance record.
     * @return formatted string with student, session, and status info
     */
    @Override
    public String toString() {
        return String.format(
                "Attendance[id=%d, studentId=%d, sessionId=%d, status=%s, markedBy=%s, markedAt=%s]",
                id, studentId, sessionId, status, markedBy, markedAt
        );
    }
}
