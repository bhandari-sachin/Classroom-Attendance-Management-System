package dto;

public class StudentClassReportRow {

    private String className;
    private String studentCode;
    private int present;
    private int absent;
    private int excused;
    private double rate;

    public StudentClassReportRow(String className, String studentCode, int present, int absent, int excused, double rate) {
        this.className = className;
        this.studentCode = studentCode;
        this.present = present;
        this.absent = absent;
        this.excused = excused;
        this.rate = rate;
    }

    public String getClassName() { return className; }
    public String getStudentCode() { return studentCode; }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }
    public int getExcused() { return excused; }
    public double getRate() { return rate; }
}