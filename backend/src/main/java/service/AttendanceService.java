package service;

import dto.AttendanceStats;
import model.*;
import config.SessionSQL;
import config.AttendanceSQL;
import dto.AttendanceView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AttendanceService {
    private final AttendanceSQL attendanceSQL;
    private final SessionSQL sessionSQL;

    public AttendanceService(AttendanceSQL attendanceSQL, SessionSQL sessionSQL) {
        this.attendanceSQL = attendanceSQL;
        this.sessionSQL = sessionSQL;
    }

    public void markPresent(Long studentId, Long sessionId) {
        if (attendanceSQL.exists(studentId, sessionId)) {
            attendanceSQL.updateStatus(
                studentId,
                sessionId,
                AttendanceStatus.PRESENT,
                MarkedBy.TEACHER
           );
        } else {
            attendanceSQL.save(new Attendance(
                    studentId,
                    sessionId,
                    AttendanceStatus.PRESENT,
                    MarkedBy.TEACHER
            ));
        }
    }

    public void markAbsent(Long studentId, Long sessionId) {
        if (attendanceSQL.exists(studentId, sessionId)) {
            attendanceSQL.updateStatus(
                    studentId,
                    sessionId,
                    AttendanceStatus.ABSENT,
                    MarkedBy.TEACHER
            );
        } else {
            attendanceSQL.save(new Attendance(
                    studentId,
                    sessionId,
                    AttendanceStatus.ABSENT,
                    MarkedBy.TEACHER
            ));
        }
    }

    public void markExcused(Long studentId, Long sessionId) {
        if (attendanceSQL.exists(studentId, sessionId)) {
            attendanceSQL.updateStatus(
                    studentId,
                    sessionId,
                    AttendanceStatus.EXCUSED,
                    MarkedBy.TEACHER
            );
        } else {
            attendanceSQL.save(new Attendance(
                    studentId,
                    sessionId,
                    AttendanceStatus.EXCUSED,
                    MarkedBy.TEACHER
            ));
        }
    }

    public boolean submitAttendanceCode(Long studentId, Long sessionId, String code) throws SQLException {

        Session session = sessionSQL.findById(sessionId);
        String correctCode = attendanceSQL.getSessionCode(sessionId);
        if (session == null) {
            return false;
        }

        if (!"ACTIVE".equals(session.getStatus())) {
            return false;
        }

        if (!code.equals(correctCode)) {
            return false;
        }

        if (attendanceSQL.exists(studentId, sessionId)) {
            attendanceSQL.updateStatus(
                    studentId,
                    sessionId,
                    AttendanceStatus.PRESENT,
                    MarkedBy.QR
            );
        } else {
            attendanceSQL.save(new Attendance(
                    studentId,
                    sessionId,
                    AttendanceStatus.PRESENT,
                    MarkedBy.QR
            ));
        }
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

    public AttendanceStats getOverallStats() {
        return attendanceSQL.getOverallStats();
    }

    public AttendanceStats getStatsForClass(Long classId) {
        return attendanceSQL.getStatsForClass(classId);
    }

    public AttendanceStats getStatsForStudent(Long studentId) {
        return attendanceSQL.getStatsForStudent(studentId);
    }

    public AttendanceStats getStatsThisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        return attendanceSQL.getStatsByDateRange(start, end);
    }

    public AttendanceStats getStatsLastMonth() {
        LocalDate now = LocalDate.now().minusMonths(1);
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        return attendanceSQL.getStatsByDateRange(start, end);
    }

    public AttendanceStats getStatsThisYear() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfYear(1);
        LocalDate end = now.withDayOfYear(now.lengthOfYear());

        return attendanceSQL.getStatsByDateRange(start, end);
    }

}
