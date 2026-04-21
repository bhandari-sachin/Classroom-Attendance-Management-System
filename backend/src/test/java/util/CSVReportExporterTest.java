package util;

import config.LocalizationSQL;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;
import dto.AttendanceReportRow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CSVReportExporterTest {

    private Map<String, String> mockLabels() {
        Map<String, String> map = new java.util.HashMap<>();

        map.put("reports.export.studentTitle", "Student Report");
        map.put("reports.export.teacherTitle", "Teacher Report");
        map.put("reports.export.adminTitle", "Admin Report");
        map.put("signup.studentcode.label", "Student Code");
        map.put("common.table.column.class", "Class");
        map.put("signup.role.student", "Student");
        map.put("signup.role.teacher", "Teacher");
        map.put("common.attendance.present", "Present");
        map.put("common.attendance.absent", "Absent");
        map.put("common.attendance.excused", "Excused");
        map.put("teacher.reports.stats.rate", "Rate:");

        return map;
    }

    // Student Report Tests
    @Test
    void studentReportValidDataShouldGenerateCsv() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        StudentClassReportRow row = mock(StudentClassReportRow.class);
        when(row.getStudentCode()).thenReturn("S123");
        when(row.getClassName()).thenReturn("Math");
        when(row.getPresent()).thenReturn(10);
        when(row.getAbsent()).thenReturn(2);
        when(row.getExcused()).thenReturn(1);
        when(row.getRate()).thenReturn(76.92);

        try (var mocked = mockStatic(LocalizationSQL.class)) {
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockLabels());

            CSVReportExporter.studentYearReport(os, 2025, List.of(row), "en");
        }

        String output = os.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Student Report"));
        assertTrue(output.contains("S123"));
        assertTrue(output.contains("Math"));
        assertTrue(output.contains("76.92%"));
    }

    @Test
    void studentReportSpecialCharactersShouldBeEscaped() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        StudentClassReportRow row = mock(StudentClassReportRow.class);
        when(row.getStudentCode()).thenReturn("S,123");
        when(row.getClassName()).thenReturn("Math");

        try (var mocked = mockStatic(LocalizationSQL.class)) {
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockLabels());

            CSVReportExporter.studentYearReport(os, 2025, List.of(row), "en");
        }

        String output = os.toString(StandardCharsets.UTF_8);

        // CSV escaping check
        assertTrue(output.contains("\"S,123\""));
    }

    // Teacher Report Tests
    @Test
    void teacherReportValidDataShouldGenerateCsv() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        TeacherStudentReportRow row = mock(TeacherStudentReportRow.class);
        when(row.getClassName()).thenReturn("Math");
        when(row.getTeacherName()).thenReturn("Mr Smith");
        when(row.getStudentName()).thenReturn("John");
        when(row.getPresent()).thenReturn(10);
        when(row.getAbsent()).thenReturn(2);
        when(row.getExcused()).thenReturn(1);
        when(row.getTotal()).thenReturn(13);
        when(row.getRate()).thenReturn(76.92);

        try (var mocked = mockStatic(LocalizationSQL.class)) {
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockLabels());

            CSVReportExporter.teacherClassReport(os, 2025, List.of(row), "en");
        }

        String output = os.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Teacher Report"));
        assertTrue(output.contains("John"));
        assertTrue(output.contains("76.92%"));
    }

    // Admin Report Tests
    @Test
    void adminReportValidDataShouldGenerateCsv() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        AttendanceReportRow row = mock(AttendanceReportRow.class);
        when(row.getStudentName()).thenReturn("John");
        when(row.getPresent()).thenReturn(10);
        when(row.getAbsent()).thenReturn(2);
        when(row.getExcused()).thenReturn(1);
        when(row.getRate()).thenReturn(76.92);

        try (var mocked = mockStatic(LocalizationSQL.class)) {
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockLabels());

            CSVReportExporter.adminAllStudentsReport(os, List.of(row), "en");
        }

        String output = os.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Admin Report"));
        assertTrue(output.contains("John"));
        assertTrue(output.contains("76.92%"));
    }
}