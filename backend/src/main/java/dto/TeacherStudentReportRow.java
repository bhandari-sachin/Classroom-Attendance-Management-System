package dto;

public class TeacherStudentReportRow {

    private String className;
    private String teacherName;
    private String studentName;
    private int present;
    private int absent;
    private int excused;
    private double rate;

    public TeacherStudentReportRow(String className, String teacherName, String studentName, int present, int absent, int excused, double rate) {
        this.className = className;
        this.teacherName = teacherName;
        this.studentName = studentName;
        this.present = present;
        this.absent = absent;
        this.excused = excused;
        this.rate = rate;
    }

    public String getClassName() { return className; }
    public String getTeacherName() { return teacherName; }
    public String getStudentName() { return studentName; }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }
    public int getExcused() { return excused; }
    public double getRate() { return rate; }
    public int getTotal() {
        return present + absent + excused;
    }
}