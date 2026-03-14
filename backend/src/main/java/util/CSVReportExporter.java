package util;

import dto.AttendanceReportRow;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVReportExporter {

    public static void studentYearReport(
            String file,
            int year,
            List<StudentClassReportRow> rows
    ) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("Student Attendance Report - " + year);
            writer.newLine();

            if (rows != null && !rows.isEmpty()) {
                writer.write("Student Code," + escape(rows.get(0).getStudentCode()));
                writer.newLine();
            }

            writer.newLine();
            writer.write("Class,Present,Absent,Excused,Rate");
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
            String file,
            List<TeacherStudentReportRow> rows
    ) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("Class Attendance Summary");
            writer.newLine();

            if (rows != null && !rows.isEmpty()) {
                writer.write("Class," + escape(rows.get(0).getClassName()));
                writer.newLine();

                writer.write("Teacher," + escape(rows.get(0).getTeacherName()));
                writer.newLine();

                writer.write("Date Range,year");
                writer.newLine();
            }

            writer.newLine();
            writer.write("Student,Present,Absent,Excused,Total,Rate");
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
            String file,
            List<AttendanceReportRow> rows
    ) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("School Attendance Summary");
            writer.newLine();
            writer.newLine();

            writer.write("Student,Present,Absent,Excused,Rate");
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
