package util;

import config.LocalizationSQL;
import dto.AttendanceReportRow;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CSVReportExporter {

    public static void studentYearReport(
            OutputStream os,
            int year,
            List<StudentClassReportRow> rows,
            String lang
    ) throws Exception {
        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8))) {

            writer.write('\uFEFF');
            writer.write(labels.get("reports.export.studentTitle")+ " " + year);
            writer.newLine();

            if (rows != null && !rows.isEmpty()) {
                writer.write(labels.get("signup.studentcode.label")+ "," + escape(rows.get(0).getStudentCode()));
                writer.newLine();
            }

            writer.newLine();
            writer.write(String.join(",",
                    labels.get("common.table.column.class"),
                    labels.get("common.attendance.present"),
                    labels.get("common.attendance.absent"),
                    labels.get("common.attendance.excused"),
                    rate
            ));
            writer.newLine();

            if (rows != null) {
                for (StudentClassReportRow r : rows) {
                    writer.write(String.join(",",
                            escape(r.getClassName()),
                            String.valueOf(r.getPresent()),
                            String.valueOf(r.getAbsent()),
                            String.valueOf(r.getExcused()),
                            String.format("%.2f%%", r.getRate())
                    ));
                    writer.newLine();
                }
            }
        }
    }

    public static void teacherClassReport(
            OutputStream os,
            int year,
            List<TeacherStudentReportRow> rows,
            String lang
    ) throws Exception {
        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8))) {

            writer.write('\uFEFF');
            writer.write(labels.get("reports.export.teacherTitle"));
            writer.newLine();

            if (rows != null && !rows.isEmpty()) {
                writer.write(labels.get("common.table.column.class")+"," + escape(rows.get(0).getClassName()));
                writer.newLine();

                writer.write(labels.get("signup.role.teacher")+"," + escape(rows.get(0).getTeacherName()));
                writer.newLine();

                writer.write(labels.get("reports.export.dateRange")+"," + year);
                writer.newLine();
            }

            writer.newLine();
            writer.write(String.join(",",
                    labels.get("signup.role.student"),
                    labels.get("common.attendance.present"),
                    labels.get("common.attendance.absent"),
                    labels.get("common.attendance.excused"),
                    labels.get("admin.reports.stats.total"),
                    rate
            ));
            writer.newLine();

            if (rows != null) {
                for (TeacherStudentReportRow r : rows) {
                    writer.write(String.join(",",
                            escape(r.getStudentName()),
                            String.valueOf(r.getPresent()),
                            String.valueOf(r.getAbsent()),
                            String.valueOf(r.getExcused()),
                            String.valueOf(r.getTotal()),
                            String.format("%.2f%%", r.getRate())
                    ));
                    writer.newLine();
                }
            }
        }
    }

    public static void adminAllStudentsReport(
            OutputStream os,
            List<AttendanceReportRow> rows,
            String lang
    ) throws Exception {
        Map<String, String> labels= LocalizationSQL.getLabels(lang);
        String rateLabel = labels.get("teacher.reports.stats.rate");
        String rate = rateLabel.split(":")[0];
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8))) {

            writer.write('\uFEFF');
            writer.write(labels.get("reports.export.adminTitle"));
            writer.newLine();
            writer.newLine();

            writer.write(String.join(",",
                    labels.get("signup.role.student"),
                    labels.get("common.attendance.present"),
                    labels.get("common.attendance.absent"),
                    labels.get("common.attendance.excused"),
                    rate
            ));;
            writer.newLine();

            if (rows != null) {
                for (AttendanceReportRow r : rows) {
                    writer.write(String.join(",",
                            escape(r.getStudentName()),
                            String.valueOf(r.getPresent()),
                            String.valueOf(r.getAbsent()),
                            String.valueOf(r.getExcused()),
                            String.format("%.2f%%", r.getRate())
                    ));
                    writer.newLine();
                }
            }
        }
    }

    private static String escape(String value) {
        if (value == null) return "\"\"";

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
