package dto;

public class AttendanceStats {
    private int presentCount;
    private int absentCount;
    private int excusedCount;
    private int totalDays;

    public AttendanceStats() {}

    public AttendanceStats(int presentCount, int absentCount, int excusedCount, int totalDays) {
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.excusedCount = excusedCount;
        this.totalDays = totalDays;
    }

    public int getPresentCount() { return presentCount; }
    public int getAbsentCount() { return absentCount; }
    public int getExcusedCount() { return excusedCount; }
    public int getTotalDays() { return totalDays; }

    public double getAttendanceRate() {
        if (totalDays == 0) return 0;
        return (presentCount * 100.0) / totalDays;
    }
}