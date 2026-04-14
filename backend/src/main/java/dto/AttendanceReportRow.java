package dto;

public class AttendanceReportRow {

    public Long id;
    public String firstName;
    public String lastName;
    public int present;
    public int absent;
    public int excused;
    public int total;

    public AttendanceReportRow(Long id, String firstName, String lastName, int present, int absent, int excused, int total){
        this.id=id; this.firstName = firstName; this.lastName = lastName;
        this.present = present; this.absent = absent; this.excused = excused; this.total = total;
    }

    public String getStudentName() {
        return firstName + " " + lastName;
    }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }
    public int getExcused() { return excused; }
    public double getRate() {
        return total == 0 ? 0 : (present * 100.0) / total;
    }
}