package dto;

public class StudentAttendanceSummary {

    private Long studentId;
    private String firstName;
    private String lastName;
    private int present;
    private int absent;
    private int excused;

    public StudentAttendanceSummary() {
    }

    public StudentAttendanceSummary(Long studentId, String firstName, String lastName, int present, int absent, int excused) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.present = present;
        this.absent = absent;
        this.excused = excused;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPresent() {
        return present;
    }

    public void setPresent(int present) {
        this.present = present;
    }

    public int getAbsent() {
        return absent;
    }

    public void setAbsent(int absent) {
        this.absent = absent;
    }

    public int getExcused() {
        return excused;
    }

    public void setExcused(int excused) {
        this.excused = excused;
    }

    public double getRate() {
        int total = present + absent + excused;
        if (total == 0) return 0;
        return (present * 100.0) / total;
    }
}