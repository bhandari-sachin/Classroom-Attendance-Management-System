package dto;

public class ClassAttendanceSummary {

    private Long classId;
    private String className;
    private int present;
    private int absent;
    private int excused;

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public int getPresent() {
        return present;
    }

    public int getAbsent() {
        return absent;
    }

    public int getExcused() {
        return excused;
    }

    public double getRate() {
        int total = present + absent + excused;
        if (total == 0) return 0;
        return (present * 100.0) / total;
    }
}

