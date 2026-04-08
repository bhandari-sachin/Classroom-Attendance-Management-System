package util;

import config.LocalizationSQL;
import dto.AttendanceReportRow;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting attendance reports to CSV format.
 */
public class CSVReportExporter {

    public static void studentYearReport(OutputStream outputStream, int year,
                                         List<StudentClassReportRow> rows, String lang) throws Exception {
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        try (BufferedWriter writer = createWriter(outputStream)) {
            writeStudentYearHeader(writer, labels, year, rows);
            writeStudentYearRows(writer, labels, rows);
        }
    }

    public static void teacherClassReport(OutputStream outputStream, int year,
                                          List<TeacherStudentReportRow> rows, String lang) throws Exception {
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        try (BufferedWriter writer = createWriter(outputStream)) {
            writeTeacherClassHeader(writer, labels, year, rows);
            writeTeacherClassRows(writer, labels, rows);
        }
    }

    public static void adminAllStudentsReport(OutputStream outputStream,
                                              List<AttendanceReportRow> rows, String lang) throws Exception {
        Map<String, String> labels = LocalizationSQL.getLabels(lang);
        try (BufferedWriter writer = createWriter(outputStream)) {
            writeAdminHeader(writer, labels);
            writeAdminRows(writer, labels, rows);
        }
    }

    private static BufferedWriter createWriter(OutputStream outputStream) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write('\uFEFF'); // UTF-8 BOM
        return writer;
    }

    private static void writeStudentYearHeader(BufferedWriter writer, Map<String, String> labels,
                                               int year, List<StudentClassReportRow> rows) throws Exception {
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];
        writer.write(labels.get("reports.export.studentTitle") + " " + year);
        writer.newLine();

        if (rows != null && !rows.isEmpty()) {
            writer.write(labels.get("signup.studentcode.label") + "," + escape(rows.get(0).getStudentCode()));
            writer.newLine();
        }

        writer.newLine();
        writer.write(String.join(",", labels.get("common.table.column.class"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                rateHeader));
        writer.newLine();
    }

    private static void writeStudentYearRows(BufferedWriter writer, Map<String, String> labels,
                                             List<StudentClassReportRow> rows) throws Exception {
        if (rows != null) {
            for (StudentClassReportRow row : rows) {
                writer.write(String.join(",", escape(row.getClassName()),
                        String.valueOf(row.getPresent()),
                        String.valueOf(row.getAbsent()),
                        String.valueOf(row.getExcused()),
                        String.format("%.2f%%", row.getRate())));
                writer.newLine();
            }
        }
    }

    private static void writeTeacherClassHeader(BufferedWriter writer, Map<String, String> labels,
                                                int year, List<TeacherStudentReportRow> rows) throws Exception {
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];
        writer.write(labels.get("reports.export.teacherTitle"));
        writer.newLine();

        if (rows != null && !rows.isEmpty()) {
            writer.write(labels.get("common.table.column.class") + "," + escape(rows.get(0).getClassName()));
            writer.newLine();
            writer.write(labels.get("signup.role.teacher") + "," + escape(rows.get(0).getTeacherName()));
            writer.newLine();
            writer.write(labels.get("reports.export.dateRange") + "," + year);
            writer.newLine();
        }

        writer.newLine();
        writer.write(String.join(",", labels.get("signup.role.student"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                labels.get("admin.reports.stats.total"),
                rateHeader));
        writer.newLine();
    }

    private static void writeTeacherClassRows(BufferedWriter writer, Map<String, String> labels,
                                              List<TeacherStudentReportRow> rows) throws Exception {
        if (rows != null) {
            for (TeacherStudentReportRow row : rows) {
                writer.write(String.join(",", escape(row.getStudentName()),
                        String.valueOf(row.getPresent()),
                        String.valueOf(row.getAbsent()),
                        String.valueOf(row.getExcused()),
                        String.valueOf(row.getTotal()),
                        String.format("%.2f%%", row.getRate())));
                writer.newLine();
            }
        }
    }

    private static void writeAdminHeader(BufferedWriter writer, Map<String, String> labels) throws Exception {
        String rateHeader = labels.get("teacher.reports.stats.rate").split(":")[0];
        writer.write(labels.get("reports.export.adminTitle"));
        writer.newLine();
        writer.newLine();

        writer.write(String.join(",", labels.get("signup.role.student"),
                labels.get("common.attendance.present"),
                labels.get("common.attendance.absent"),
                labels.get("common.attendance.excused"),
                rateHeader));
        writer.newLine();
    }

    private static void writeAdminRows(BufferedWriter writer, Map<String, String> labels,
                                       List<AttendanceReportRow> rows) throws Exception {
        if (rows != null) {
            for (AttendanceReportRow row : rows) {
                writer.write(String.join(",", escape(row.getStudentName()),
                        String.valueOf(row.getPresent()),
                        String.valueOf(row.getAbsent()),
                        String.valueOf(row.getExcused()),
                        String.format("%.2f%%", row.getRate())));
                writer.newLine();
            }
        }
    }

    /**
     * Escapes a string for safe CSV output.
     * @param value the input string
     * @return escaped string wrapped in quotes
     */
    private static String escape(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
