package model;

public class Attendance {
    private final Long studentId;
    private final Long classId;
    private AttendanceStatus status;

    public Attendance(Long studentId, Long classId, AttendanceStatus status) {
        this.studentId = studentId;
        this.classId = classId;
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }
    public Long getClassId() {
        return classId;
    }
    public AttendanceStatus getStatus() {
        return status;
    }
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
