package util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import config.LocalizationSQL;
import dto.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting attendance reports to PDF format.
 */
public class PDFReportExporter {

    private static Font getFont(String lang, int size, int style) {
        try {
            String path = switch (lang) {
                case "ar" -> "fonts/NotoNaskhArabic-Regular.ttf";
                case "am" -> "fonts/NotoSansEthiopic-Regular.ttf";
                default -> "fonts/NotoSans-Regular.ttf";
            };

            InputStream is = PDFReportExporter.class.getClassLoader().getResourceAsStream(path);
            if (is == null) throw new RuntimeException("Font not found: " + path);

            byte[] fontBytes = is.readAllBytes();
            BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
            return new Font(bf, size, style);

        } catch (Exception e) {
            throw new RuntimeException("Font loading failed", e);
        }
    }

    public static void studentYearReport(String file, int year,
                                         List<StudentClassReportRow> rows, String lang) throws Exception {
        Document doc = createDocument(file, lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];

        doc.add(new Paragraph(labels.get("reports.export.studentTitle") + " " + year, title));
        doc.add(new Paragraph(labels.get("signup.studentcode.label") + ": " + rows.get(0).getStudentCode(), normal));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        setDirection(table, lang);
        addHeader(table, header, lang,
                labels.get("common.table.column.class"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                rateHeader);

        for (StudentClassReportRow row : rows) {
            addRow(table, normal, row.getClassName(), row.getPresent(), row.getAbsent(), row.getExcused(), row.getRate());
        }

        doc.add(table);
        doc.close();
    }

    public static void teacherClassReport(String file, int year,
                                          List<TeacherStudentReportRow> rows, String lang) throws Exception {
        Document doc = createDocument(file, lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];

        doc.add(new Paragraph(labels.get("reports.export.schoolName"), header));
        doc.add(new Paragraph(labels.get("reports.export.teacherTitle"), title).setAlignment(Element.ALIGN_CENTER));
        doc.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(40);
        infoTable.addCell(new Phrase(labels.get("common.table.column.class") + ":", normal));
        infoTable.addCell(new Phrase(rows.get(0).getClassName(), normal));
        infoTable.addCell(new Phrase(labels.get("signup.role.teacher") + ":", normal));
        infoTable.addCell(new Phrase(rows.get(0).getTeacherName(), normal));
        infoTable.addCell(new Phrase(labels.get("reports.export.dateRange"), normal));
        infoTable.addCell(new Phrase(String.valueOf(year), normal));
        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        addHeader(table, header, lang,
                labels.get("signup.role.student"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                labels.get("admin.reports.stats.total"),
                rateHeader);

        for (TeacherStudentReportRow row : rows) {
            addRow(table, normal, row.getStudentName(), row.getPresent(), row.getAbsent(), row.getExcused(), row.getTotal(), row.getRate());
        }

        doc.add(table);
        doc.close();
    }

    public static void adminAllStudentsReport(String file,
                                              List<AttendanceReportRow> rows, String lang) throws Exception {
        Document doc = createDocument(file, lang);
        Font title = getFont(lang, 18, Font.BOLD);
        Font header = getFont(lang, 12, Font.BOLD);
        Font normal = getFont(lang, 12, Font.NORMAL);
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];

        doc.add(new Paragraph(labels.get("reports.export.adminTitle"), title));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        addHeader(table, header, lang,
                labels.get("signup.role.student"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                rateHeader);

        for (AttendanceReportRow row : rows) {
            addRow(table, normal, row.getStudentName(), row.getPresent(), row.getAbsent(), row.getExcused(), row.getRate());
        }

        doc.add(table);
        doc.close();
    }

    private static Document createDocument(String file, String lang) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();
        if ("ar".equals(lang)) writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        return doc;
    }

    private static void setDirection(PdfPTable table, String lang) {
        if ("ar".equals(lang)) table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
    }

    private static void addHeader(PdfPTable table, Font header, String lang, String... titles) {
        for (String title : titles) {
            PdfPCell cell = new PdfPCell(new Phrase(title, header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            if ("ar".equals(lang)) cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.addCell(cell);
        }
    }

    private static void addRow(PdfPTable table, Font font, Object... values) {
        for (Object value : values) {
            String text = value instanceof Double ? String.format("%.2f%%", value) : String.valueOf(value);
            table.addCell(new Phrase(text, font));
        }
    }
}
