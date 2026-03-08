package service;

import config.AttendanceSQL;
import model.Attendance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ReportService {
    private final AttendanceSQL attendanceSQL = new AttendanceSQL();

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
}