package util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import dto.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.util.List;

public class PDFReportExporter {

    private static Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static Font header = new Font(Font.HELVETICA, 12, Font.BOLD);

    // Student report for all classes in a year
    public static void studentYearReport(
            String file,
            int year,
            List<StudentClassReportRow> rows
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph("Student Attendance Report - " + year, title));
        doc.add(new Paragraph("Student Code: " + rows.get(0).getStudentCode()));
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
            List<TeacherStudentReportRow> rows
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // fetch school name when implemented
        Paragraph school = new Paragraph("School Name", header);
        school.setAlignment(Element.ALIGN_LEFT);
        doc.add(school);

        Paragraph reportTitle = new Paragraph("Class Attendance Summary", title);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(reportTitle);

        doc.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(40);

        infoTable.addCell("Class:");
        infoTable.addCell(rows.get(0).getClassName());

        infoTable.addCell("Teacher:");
        infoTable.addCell(rows.get(0).getTeacherName());

        infoTable.addCell("Date Range:");
        infoTable.addCell("year"); // to be implemented

        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
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

            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);

            table.addCell(cell);
        }
    }
}