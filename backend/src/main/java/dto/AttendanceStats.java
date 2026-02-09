package dto;

public class AttendanceStats {

    private int presentCount;
    private int absentCount;
    private int excusedCount;
    private int totalRecords;

    public double getAttendanceRate() {
        if (totalRecords == 0) return 0;
        return (presentCount * 100.0) / totalRecords;
    }
}

