package util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import config.LocalizationSQL;
import dto.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class PDFReportExporter {

    private static Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static Font header = new Font(Font.HELVETICA, 12, Font.BOLD);

    // Student report for all classes in a year
    public static void studentYearReport(
            String file,
            int year,
            List<StudentClassReportRow> rows,
            String lang
    ) throws Exception {

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph(labels.get("reports.export.studentTitle")+ " " + year, title));
        doc.add(new Paragraph(labels.get("signup.studentcode.label")+ ": " + rows.get(0).getStudentCode()));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, labels.get("common.table.column.class"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), rate);

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
            int year,
            List<TeacherStudentReportRow> rows,
            String lang
    ) throws Exception {

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // fetch school name when implemented
        Paragraph school = new Paragraph(labels.get("reports.export.schoolName"), header);
        school.setAlignment(Element.ALIGN_LEFT);
        doc.add(school);

        Paragraph reportTitle = new Paragraph(labels.get("reports.export.teacherTitle"), title);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(reportTitle);

        doc.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(40);

        infoTable.addCell(labels.get("common.table.column.class")+":");
        infoTable.addCell(rows.get(0).getClassName());

        infoTable.addCell(labels.get("signup.role.teacher")+":");
        infoTable.addCell(rows.get(0).getTeacherName());

        infoTable.addCell(labels.get("reports.export.dateRange"));
        infoTable.addCell(String.valueOf(year));

        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        addHeader(table, labels.get("signup.role.student"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), labels.get("admin.reports.stats.total"), rate);

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
            List<AttendanceReportRow> rows,
            String lang
    ) throws Exception {

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        doc.add(new Paragraph(labels.get("reports.export.adminTitle"), title));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, labels.get("signup.role.student"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), rate);

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