package service;

import config.AttendanceSQL;
import model.Attendance;
import dto.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ReportService {
    private AttendanceSQL attendanceSQL = new AttendanceSQL();
    // CSV reports
    // attendance for student
    public void exportStudentReport(Long studentId, OutputStream outputStream) throws IOException {
        List<Attendance> attendances = attendanceSQL.findByStudentId(studentId);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write("Session ID, Status, Marked By\n");
            for (Attendance attendance : attendances) {
                writer.write(attendance.getSessionId() + ", " + attendance.getStatus() + ", " + attendance.getMarkedBy() + "\n");
            }
        }
    }

    // attendance for class
    public void exportClassReport(Long classId, OutputStream outputStream) throws IOException {
        List<Attendance> attendances = attendanceSQL.findByClassId(classId);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write("Student ID, Session ID, Status, Marked By\n");
            for (Attendance attendance : attendances) {
                writer.write(attendance.getStudentId() + ", " + attendance.getSessionId() + ", " + attendance.getStatus() + ", " + attendance.getMarkedBy() + "\n");
            }
        }
    }

    // attendance for all sessions for admin implemented later

    // PDF reports
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
