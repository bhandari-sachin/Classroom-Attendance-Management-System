package dto;

public class AttendanceReportRow {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final int present;
    private final int absent;
    private final int excused;
    private final int total;

    public AttendanceReportRow(Long id, String firstName, String lastName, int present, int absent, int excused, int total) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.present = present;
        this.absent = absent;
        this.excused = excused;
        this.total = total;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getPresent() { return present; }
    public int getAbsent() { return absent; }
    public int getExcused() { return excused; }
    public int getTotal() { return total; }

    public String getStudentName() {
        return firstName + " " + lastName;
    }

    public double getRate() {
        return total == 0 ? 0 : (present * 100.0) / total;
    }
}