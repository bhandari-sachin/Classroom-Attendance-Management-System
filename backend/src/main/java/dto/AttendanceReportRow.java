package dto;

public record AttendanceReportRow(Long id, String firstName, String lastName, int present, int absent, int excused,
                                  int total) {

    public String getStudentName() {
        return firstName + " " + lastName;
    }

    public double getRate() {
        return total == 0 ? 0 : (present * 100.0) / total;
    }
}