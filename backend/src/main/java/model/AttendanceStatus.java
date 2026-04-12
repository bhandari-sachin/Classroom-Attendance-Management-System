package model;

/**
 * Represents the possible attendance states for a student in a session.
 * Each status indicates whether the student was present, absent, or excused.
 */
public enum AttendanceStatus {

    /** Student attended the session. */
    PRESENT,

    /** Student was absent from the session. */
    ABSENT,

    /** Student was absent but excused for valid reasons. */
    EXCUSED;

    /**
     * Returns a human-readable label for the attendance status.
     * @return a formatted string representing the status
     */
    public String getLabel() {
        return switch (this) {
            case PRESENT -> "Present";
            case ABSENT -> "Absent";
            case EXCUSED -> "Excused";
        };
    }

    /**
     * Checks if the student was present.
     * @return true if status is PRESENT, false otherwise
     */
    public boolean isPresent() {
        return this == PRESENT;
    }

    /**
     * Checks if the student was absent.
     * @return true if status is ABSENT, false otherwise
     */
    public boolean isAbsent() {
        return this == ABSENT;
    }

    /**
     * Checks if the student was excused.
     * @return true if status is EXCUSED, false otherwise
     */
    public boolean isExcused() {
        return this == EXCUSED;
    }
}
