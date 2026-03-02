package service;

import config.AttendanceSQL;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    static class StubAttendanceSQL extends AttendanceSQL {

        List<Attendance> studentResult;
        List<Attendance> classResult;

        @Override
        public List<Attendance> findByStudentId(Long studentId) {
            return studentResult;
        }

        @Override
        public List<Attendance> findByClassId(Long classId) {
            return classResult;
        }
    }

    // ---------------------------------------------
    // exportStudentReport
    // ---------------------------------------------

    @Test
    void exportStudentReport_writesCorrectCsv() throws IOException {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        stub.studentResult = Arrays.asList(
                new Attendance(1L, 101L, AttendanceStatus.PRESENT, MarkedBy.TEACHER),
                new Attendance(1L, 102L, AttendanceStatus.ABSENT, MarkedBy.QR)
        );

        ReportService service = new ReportService();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.exportStudentReport(1L, outputStream);

        String result = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Session ID, Status, Marked By"));
        assertTrue(result.contains("101, PRESENT, TEACHER"));
        assertTrue(result.contains("102, ABSENT, QR"));
    }

    // ---------------------------------------------
    // exportClassReport
    // ---------------------------------------------

    @Test
    void exportClassReport_writesCorrectCsv() throws IOException {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        stub.classResult = Arrays.asList(
                new Attendance(1L, 201L, AttendanceStatus.PRESENT, MarkedBy.TEACHER),
                new Attendance(2L, 201L, AttendanceStatus.EXCUSED, MarkedBy.QR)
        );

        ReportService service = new ReportService();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.exportClassReport(10L, outputStream);

        String result = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Student ID, Session ID, Status, Marked By"));
        assertTrue(result.contains("1, 201, PRESENT, TEACHER"));
        assertTrue(result.contains("2, 201, EXCUSED, QR"));
    }
}