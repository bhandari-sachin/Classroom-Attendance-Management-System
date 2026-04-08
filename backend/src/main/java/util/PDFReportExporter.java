package util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import config.LocalizationSQL;
import dto.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class PDFReportExporter {

    private static Font getFont(String lang, int size, int style) {
        try {
            String path;

            switch (lang) {
                case "ar":
                    path = "fonts/NotoNaskhArabic-Regular.ttf";
                    break;
                case "am":
                    path = "fonts/NotoSansEthiopic-Regular.ttf";
                    break;
                default:
                    path = "fonts/NotoSans-Regular.ttf";
            }

            InputStream is = PDFReportExporter.class
                    .getClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                throw new RuntimeException("Font not found: " + path);
            }

            byte[] fontBytes = is.readAllBytes();

            BaseFont bf = BaseFont.createFont(
                    path,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null
            );

            return new Font(bf, size, style);

        } catch (Exception e) {
            throw new RuntimeException("Font loading failed", e);
        }
    }

    // Student report for all classes in a year
    public static void studentYearReport(
            String file,
            int year,
            List<StudentClassReportRow> rows,
            String lang
    ) throws Exception {

        boolean isRTL = "ar".equals(lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        if (isRTL) {
            writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }

        doc.add(new Paragraph(labels.get("reports.export.studentTitle")+ " " + year, title));
        doc.add(new Paragraph(
                labels.get("signup.studentcode.label")+ ": " + rows.get(0).getStudentCode(),
                normal
        ));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        if (isRTL) {
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }
        addHeader(table, header, lang, labels.get("common.table.column.class"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), rate);

        for (StudentClassReportRow r : rows) {
            table.addCell(new Phrase(r.getClassName(), normal));
            table.addCell(new Phrase(String.valueOf(r.getPresent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getAbsent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getExcused()), normal));
            table.addCell(new Phrase(String.format("%.2f%%", r.getRate()), normal));
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

        boolean isRTL = "ar".equals(lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));

        doc.open();
        if (isRTL) {
            writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }

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

        infoTable.addCell(new Phrase(labels.get("common.table.column.class")+":", normal));
        infoTable.addCell(new Phrase(rows.get(0).getClassName(), normal));

        infoTable.addCell(new Phrase(labels.get("signup.role.teacher")+":", normal));
        infoTable.addCell(new Phrase(rows.get(0).getTeacherName(), normal));

        infoTable.addCell(new Phrase(labels.get("reports.export.dateRange"), normal));
        infoTable.addCell(new Phrase(String.valueOf(year), normal));

        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        addHeader(table, header, lang, labels.get("signup.role.student"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), labels.get("admin.reports.stats.total"), rate);

        for (TeacherStudentReportRow r : rows) {
            table.addCell(new Phrase(r.getStudentName(), normal));
            table.addCell(new Phrase(String.valueOf(r.getPresent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getAbsent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getExcused()), normal));
            table.addCell(new Phrase(String.valueOf(r.getTotal()), normal));
            table.addCell(new Phrase(String.format("%.2f%%", r.getRate()), normal));
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

        boolean isRTL = "ar".equals(lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);

        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));

        doc.open();
        if (isRTL) {
            writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }

        doc.add(new Paragraph(labels.get("reports.export.adminTitle"), title));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, header, lang, labels.get("signup.role.student"), labels.get("common.attendance.present"), labels.get("common.attendance.absent"), labels.get("common.attendance.excused"), rate);

        for (AttendanceReportRow r : rows) {
            table.addCell(new Phrase(r.getStudentName(), normal));
            table.addCell(new Phrase(String.valueOf(r.getPresent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getAbsent()), normal));
            table.addCell(new Phrase(String.valueOf(r.getExcused()), normal));
            table.addCell(new Phrase(String.format("%.2f%%", r.getRate()), normal));
        }

        doc.add(table);
        doc.close();
    }

    private static void addHeader(PdfPTable table, Font header, String lang, String... titles) {
        boolean isRTL = "ar".equals(lang);
        for (String t : titles) {

            PdfPCell cell = new PdfPCell(new Phrase(t, header));

            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);

            if (isRTL) {
                cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            }
            table.addCell(cell);
        }
    }

}