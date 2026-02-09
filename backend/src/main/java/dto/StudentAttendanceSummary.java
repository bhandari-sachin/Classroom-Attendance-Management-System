package dto;

public class StudentAttendanceSummary {

    private Long studentId;
    private String firstName;
    private String lastName;
    private int present;
    private int absent;
    private int excused;

    public double getRate() {
        int total = present + absent + excused;
        if (total == 0) return 0;
        return (present * 100.0) / total;
    }
}

