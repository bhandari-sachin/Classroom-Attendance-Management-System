package service;

import dto.AttendanceStats;
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
    public void markByCode(Long studentId, String code) {

        Long sessionId = attendanceSQL.findSessionIdByCode(code);
        if (sessionId == null) {
            throw new IllegalArgumentException("Invalid attendance code");
        }

        if (attendanceSQL.exists(studentId, sessionId)) {
            throw new IllegalArgumentException("Attendance already marked");
        }

        Attendance a = new Attendance(
                studentId,
                sessionId,
                AttendanceStatus.PRESENT,
                MarkedBy.STUDENT
        );

        attendanceSQL.save(a);
    }

    public boolean submitAttendanceCode(Long studentId, Long sessionId, String code) {

        String correctCode = attendanceSQL.getSessionCode(sessionId);
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
    public dto.AttendanceStats getOverallStats() {
        return attendanceSQL.getOverallStats();
    }
    public AttendanceStats getStudentStats(long studentId) {
        return attendanceSQL.getStudentStats(studentId);
    }


    public dto.AttendanceStats getStudentStats(Long studentId) {
        return attendanceSQL.getStudentStats(studentId);
    }

    public List<dto.AttendanceView> getStudentAttendanceViews(Long studentId) {
        return attendanceSQL.getStudentAttendanceViews(studentId);
    }

}
