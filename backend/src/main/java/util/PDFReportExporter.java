package util;

import com.lowagie.text.DocumentException;
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

    private static final String PRESENT_LABEL = "common.attendance.present";
    private static final String ABSENT_LABEL = "common.attendance.absent";
    private static final String EXCUSED_LABEL = "common.attendance.excused";
    private static final String DECIMAL_FORMAT = "%.2f%%";

    private static Font getFont(String lang, int size, int style) throws DocumentException {
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
                throw new IllegalArgumentException("Font not found: " + path);
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
            throw new DocumentException(e);
        }
    }

    private static boolean isRTL(String lang) {
        return List.of("ar", "he", "fa").contains(lang);
    }

    private static Document createDocument(String file, boolean isRTL) throws DocumentException, IOException {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        if (isRTL) {
            writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }
        return doc;
    }

    private static class ReportContext {
        Font title;
        Font header;
        Font normal;
        Map<String, String> labels;
        String rate;
    }

    private static ReportContext createContext(String lang) throws DocumentException {
        ReportContext ctx = new ReportContext();
        ctx.title = getFont(lang, 18, Font.BOLD);
        ctx.header = getFont(lang, 12, Font.BOLD);
        ctx.normal = getFont(lang, 12, Font.NORMAL);

        ctx.labels = LocalizationSQL.getLabels(lang);
        String rateLabel = ctx.labels.get("teacher.reports.stats.rate");
        ctx.rate = rateLabel.split(":")[0];

        return ctx;
    }

    private static <T> PdfPTable createTable(
            int columns,
            Font headerFont,
            Font normalFont,
            String lang,
            String[] headers,
            List<T> rows,
            RowMapper<T> mapper
    ) {
        PdfPTable table = new PdfPTable(columns);
        addHeader(table, headerFont, lang, headers);

        for (T row : rows) {
            for (String value : mapper.map(row)) {
                table.addCell(new Phrase(value, normalFont));
            }
        }

        return table;
    }

    private interface RowMapper<T> {
        String[] map(T row);
    }

    // Student report for all classes in a year
    public static void studentYearReport(String file, int year, List<StudentClassReportRow> rows, String lang) throws DocumentException, IOException {
        boolean rtl = isRTL(lang);
        ReportContext ctx = createContext(lang);
        Document doc = createDocument(file, rtl);

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Rows cannot be null or empty");
        }

        doc.add(new Paragraph(ctx.labels.get("reports.export.studentTitle") + " " + year, ctx.title));
        doc.add(new Paragraph(ctx.labels.get("signup.studentcode.label") + ": " + rows.get(0).getStudentCode(), ctx.normal));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = createTable(
                5,
                ctx.header,
                ctx.normal,
                lang,
                new String[]{
                        ctx.labels.get("common.table.column.class"),
                        ctx.labels.get(PRESENT_LABEL),
                        ctx.labels.get(ABSENT_LABEL),
                        ctx.labels.get(EXCUSED_LABEL),
                        ctx.rate
                },
                rows,
                r -> new String[]{
                        r.getClassName(),
                        String.valueOf(r.getPresent()),
                        String.valueOf(r.getAbsent()),
                        String.valueOf(r.getExcused()),
                        String.format(DECIMAL_FORMAT, r.getRate())
                }
        );

        doc.add(table);
        doc.close();
    }

    // Teacher report for all students in a class
    public static void teacherClassReport(String file, int year, List<TeacherStudentReportRow> rows, String lang) throws DocumentException, IOException {
        boolean rtl = isRTL(lang);
        ReportContext ctx = createContext(lang);
        Document doc = createDocument(file, rtl);

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Rows cannot be null or empty");
        }

        // fetch school name when implemented
        Paragraph school = new Paragraph(ctx.labels.get("reports.export.schoolName"), ctx.header);
        school.setAlignment(Element.ALIGN_LEFT);
        doc.add(school);

        Paragraph title = new Paragraph(ctx.labels.get("reports.export.teacherTitle"), ctx.title);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(Chunk.NEWLINE);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(40);

        info.addCell(new Phrase(ctx.labels.get("common.table.column.class") + ":", ctx.normal));
        info.addCell(new Phrase(rows.get(0).getClassName(), ctx.normal));

        info.addCell(new Phrase(ctx.labels.get("signup.role.teacher") + ":", ctx.normal));
        info.addCell(new Phrase(rows.get(0).getTeacherName(), ctx.normal));

        info.addCell(new Phrase(ctx.labels.get("reports.export.dateRange"), ctx.normal));
        info.addCell(new Phrase(String.valueOf(year), ctx.normal));

        doc.add(info);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = createTable(
                6,
                ctx.header,
                ctx.normal,
                lang,
                new String[]{
                        ctx.labels.get("signup.role.student"),
                        ctx.labels.get(PRESENT_LABEL),
                        ctx.labels.get(ABSENT_LABEL),
                        ctx.labels.get(EXCUSED_LABEL),
                        ctx.labels.get("admin.reports.stats.total"),
                        ctx.rate
                },
                rows,
                r -> new String[]{
                        r.getStudentName(),
                        String.valueOf(r.getPresent()),
                        String.valueOf(r.getAbsent()),
                        String.valueOf(r.getExcused()),
                        String.valueOf(r.getTotal()),
                        String.format(DECIMAL_FORMAT, r.getRate())
                }
        );

        doc.add(table);
        doc.close();
    }

    // Admin report for all students in the school
    public static void adminAllStudentsReport(String file, List<AttendanceReportRow> rows, String lang) throws DocumentException, IOException {
        boolean rtl = isRTL(lang);
        ReportContext ctx = createContext(lang);
        Document doc = createDocument(file, rtl);

        doc.add(new Paragraph(ctx.labels.get("reports.export.adminTitle"), ctx.title));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = createTable(
                5,
                ctx.header,
                ctx.normal,
                lang,
                new String[]{
                        ctx.labels.get("signup.role.student"),
                        ctx.labels.get(PRESENT_LABEL),
                        ctx.labels.get(ABSENT_LABEL),
                        ctx.labels.get(EXCUSED_LABEL),
                        ctx.rate
                },
                rows,
                r -> new String[]{
                        r.getStudentName(),
                        String.valueOf(r.getPresent()),
                        String.valueOf(r.getAbsent()),
                        String.valueOf(r.getExcused()),
                        String.format(DECIMAL_FORMAT, r.getRate())
                }
        );

        doc.add(table);
        doc.close();
    }

    private static void addHeader(PdfPTable table, Font header, String lang, String... titles) {
        boolean rtl = isRTL(lang);

        for (String t : titles) {
            PdfPCell cell = new PdfPCell(new Phrase(t, header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);

            if (rtl) {
                cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            }

            table.addCell(cell);
        }
    }
}
