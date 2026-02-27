package dto;

public class TeacherStudentReportRow {

    private String studentName;
    private int present;
    private int absent;
    private int excused;
    private double rate;

    public TeacherStudentReportRow(String studentName, int present, int absent, int excused, double rate) {
        this.studentName = studentName;
        this.present = present;
        this.absent = absent;
        this.excused = excused;
        this.rate = rate;
    }

    public String getStudentName() { return studentName; }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }
    public int getExcused() { return excused; }
    public double getRate() { return rate; }
    public int getTotal() {
        return present + absent + excused;
    }
}