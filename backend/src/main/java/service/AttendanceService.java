package service;

import model.Attendance;
import model.AttendanceStatus;
import config.AttendanceSQL;

import java.util.List;

public class AttendanceService {
    private final AttendanceSQL attendanceSQL;

    public AttendanceService(AttendanceSQL attendanceSQL) {
        this.attendanceSQL = attendanceSQL;
    }

    public void markPresent(Long studentId, Long sessionId) {
        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.PRESENT
        );
    }

    public void markAbsent(Long studentId, Long sessionId, String reason) {
        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.ABSENT
        );
    }

    public void markExcused(Long studentId, Long sessionId, String reason) {
        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.EXCUSED
        );
    }

    public boolean submitAttendanceCode(Long studentId, Long sessionId, String code) {

        if (sessionId == null) {
            return false; // invalid code
        }

        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.PRESENT
        );
        return true;
    }
    public List<Attendance> getAttendanceForStudent(Long studentId) {
        return attendanceSQL.findByStudentId(studentId);
    }

    public List<Attendance> getAttendanceForClass(Long classId) {
        return attendanceSQL.findByClassId(classId);
    }
}
