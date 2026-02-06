package service;

import model.*;
import config.AttendanceSQL;
import dto.AttendanceView;

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
                AttendanceStatus.PRESENT,
                MarkedBy.TEACHER
        );
        attendanceSQL.save(attendance);
    }

    public void markAbsent(Long studentId, Long sessionId, String reason) {
        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.ABSENT,
                MarkedBy.TEACHER
        );
        attendanceSQL.save(attendance);
    }

    public void markExcused(Long studentId, Long sessionId, String reason) {
        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.EXCUSED,
                MarkedBy.TEACHER
        );
        attendanceSQL.save(attendance);
    }

    public boolean submitAttendanceCode(Long studentId, Long sessionId, String code) {

        String correctCode = attendanceSQL.getSessionCode(sessionId);
        if (!code.equals(correctCode)) {
            return false;
        }

        Attendance attendance = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.PRESENT,
                MarkedBy.QR
        );
        attendanceSQL.save(attendance);
        return true;
    }

    public List<Attendance> getAttendanceForStudent(Long studentId) {
        return attendanceSQL.findByStudentId(studentId);
    }

    public List<Attendance> getAttendanceForClass(Long classId) {
        return attendanceSQL.findByClassId(classId);
    }

    public List<AttendanceView> filterAttendance(
            Long classId,
            String searchTerm
    ) {
        return attendanceSQL.filterAttendanceByStudent(classId, searchTerm);
    }

}
