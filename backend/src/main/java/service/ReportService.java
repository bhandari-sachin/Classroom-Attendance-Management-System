package service;

import config.AttendanceSQL;
import dto.*;

import java.time.Year;
import java.util.List;

public class ReportService {
    private final AttendanceSQL attendanceSQL;
    public ReportService(AttendanceSQL attendanceSQL) {
        this.attendanceSQL = attendanceSQL;
    }

    public List<StudentClassReportRow> studentYearReport(Long studentId, int year) {
        return attendanceSQL.getStudentYearlyReport(studentId, year);
    }

    public List<TeacherStudentReportRow> teacherClassReport(Long teacherId, Long classId, int year) {
        return attendanceSQL.getTeacherClassReport(teacherId, classId, year);
    }

    public List<AttendanceReportRow> getAllStudents(){
        return attendanceSQL.getAllStudentsStats();
    }

    // implement more filters in future
}
