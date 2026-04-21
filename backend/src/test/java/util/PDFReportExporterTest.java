package util;

import config.LocalizationSQL;
import dto.AttendanceReportRow;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PDFReportExporterTest {

    private Map<String, String> mockLabels() {
        Map<String, String> map = new java.util.HashMap<>();

        map.put("reports.export.studentTitle", "Student Report");
        map.put("reports.export.teacherTitle", "Teacher Report");
        map.put("reports.export.adminTitle", "Admin Report");
        map.put("signup.studentcode.label", "Student Code");
        map.put("common.table.column.class", "Class");
        map.put("common.attendance.present", "Present");
        map.put("common.attendance.absent", "Absent");
        map.put("common.attendance.excused", "Excused");
        map.put("teacher.reports.stats.rate", "Rate:");

        return map;
    }

    // Student Report Tests
    @Test
    void studentYearReportCreatesPdfFile() throws Exception {
        String file = "student_report.pdf";

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

            PDFReportExporter.studentYearReport(
                    file,
                    2025,
                    List.of(row),
                    "en"
            );
        }

        File pdf = new File(file);

        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 100);

        pdf.delete();
    }

    // Teacher Report Tests
    @Test
    void teacherClassReportCreatesPdfFile() throws Exception {
        String file = "teacher_report.pdf";

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

            PDFReportExporter.teacherClassReport(
                    file,
                    2025,
                    List.of(row),
                    "en"
            );
        }

        File pdf = new File(file);

        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 100);

        pdf.delete();
    }

    // Admin Report Tests
    @Test
    void adminAllStudentsReportCreatesPdfFile() throws Exception {
        String file = "admin_report.pdf";

        AttendanceReportRow row = mock(AttendanceReportRow.class);
        when(row.getStudentName()).thenReturn("John");
        when(row.getPresent()).thenReturn(10);
        when(row.getAbsent()).thenReturn(2);
        when(row.getExcused()).thenReturn(1);
        when(row.getRate()).thenReturn(76.92);

        try (var mocked = mockStatic(LocalizationSQL.class)) {
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockLabels());

            PDFReportExporter.adminAllStudentsReport(
                    file,
                    List.of(row),
                    "en"
            );
        }

        File pdf = new File(file);

        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 100);

        pdf.delete();
    }
}