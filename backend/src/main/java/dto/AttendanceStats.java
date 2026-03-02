package dto;

public class AttendanceStats {

    private int presentCount;
    private int absentCount;
    private int excusedCount;
    private int totalRecords;

    public AttendanceStats(int presentCount, int absentCount, int excusedCount, int totalRecords) {
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.excusedCount = excusedCount;
        this.totalRecords = totalRecords;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public int getExcusedCount() {
        return excusedCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public double getAttendanceRate() {
        if (totalRecords == 0) return 0;
        return (presentCount * 100.0) / totalRecords;
    }
}
