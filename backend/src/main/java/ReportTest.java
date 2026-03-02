import config.AttendanceSQL;
import service.ReportService;
import util.PDFReportExporter;

public class ReportTest {

    public static void main(String[] args) throws Exception {

        ReportService reportService =
                new ReportService(new AttendanceSQL());

        var student = reportService.studentYearReport(4L, 2026);
        PDFReportExporter.studentYearReport("student_year.pdf", 4L, 2026, student);

        var teacher = reportService.teacherClassReport(2L, 1L);
        PDFReportExporter.teacherClassReport("teacher.pdf", 1L, teacher);

        var admin = reportService.getAllStudents();
        PDFReportExporter.adminAllStudentsReport("admin.pdf", admin);
    }
}