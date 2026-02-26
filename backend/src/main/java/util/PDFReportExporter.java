package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import dto.*;

import java.io.FileOutputStream;
import java.util.List;

public class PDFReportExporter {

    private static Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static Font header = new Font(Font.HELVETICA, 12, Font.BOLD);

    // Student report for all classes in a year
    public static void studentYearReport(
            String file,
            Long studentId,
            int year,
            List<StudentClassReportRow> rows
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph("Student Attendance Report - " + year, title));
        doc.add(new Paragraph("Student ID: " + studentId));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, "Class", "Present", "Absent", "Excused", "Rate");

        for (StudentClassReportRow r : rows) {
            table.addCell(r.getClassName());
            table.addCell(String.valueOf(r.getPresent()));
            table.addCell(String.valueOf(r.getAbsent()));
            table.addCell(String.valueOf(r.getExcused()));
            table.addCell(String.format("%.2f%%", r.getRate()));
        }

        doc.add(table);
        doc.close();
    }

    // Teacher report for all students in a class
    public static void teacherClassReport(
            String file,
            Long classId,
            List<TeacherStudentReportRow> rows
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph("Teacher Class Attendance Report", title));
        doc.add(new Paragraph("Class ID: " + classId));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        addHeader(table, "Student", "Present", "Absent", "Excused", "Total", "Rate");

        for (TeacherStudentReportRow r : rows) {
            table.addCell(r.getStudentName());
            table.addCell(String.valueOf(r.getPresent()));
            table.addCell(String.valueOf(r.getAbsent()));
            table.addCell(String.valueOf(r.getExcused()));
            table.addCell(String.valueOf(r.getTotal()));
            table.addCell(String.format("%.2f%%", r.getRate()));
        }

        doc.add(table);
        doc.close();
    }

    // Admin report for all students in the school
    public static void adminAllStudentsReport(
            String file,
            List<AttendanceReportRow> rows
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph("School Attendance Summary", title));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, "Student", "Present", "Absent", "Excused", "Rate");

        for (AttendanceReportRow r : rows) {
            table.addCell(r.getStudentName());
            table.addCell(String.valueOf(r.getPresent()));
            table.addCell(String.valueOf(r.getAbsent()));
            table.addCell(String.valueOf(r.getExcused()));
            table.addCell(String.format("%.2f%%", r.getRate()));
        }

        doc.add(table);
        doc.close();
    }

    private static void addHeader(PdfPTable table, String... titles) {
        for (String t : titles) {
            PdfPCell cell = new PdfPCell(new Phrase(t, header));
            table.addCell(cell);
        }
    }
}