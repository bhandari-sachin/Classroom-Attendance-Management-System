package service;

import config.AttendanceSQL;
import dto.*;

import java.util.List;

public class ReportService {

    private final AttendanceSQL attendanceSQL;

    public ReportService(AttendanceSQL attendanceSQL) {
        this.attendanceSQL = attendanceSQL;
    }

    public List<StudentClassReportRow> studentYearReport(Long studentId, int year) {
        return attendanceSQL.getStudentYearlyReport(studentId, year);
    }

    public List<TeacherStudentReportRow> teacherClassReport(Long teacherId, Long classId) {
        return attendanceSQL.getTeacherClassReport(teacherId, classId);
    }

    public List<AttendanceReportRow> getAllStudents(){
        return attendanceSQL.getAllStudentsStats();
    }
}