package model;

/**
 * Represents the source or method used to mark attendance.
 * Each value indicates who or what recorded the attendance.
 */
public enum MarkedBy {

    /** Attendance marked automatically via QR code scan. */
    QR,

    /** Attendance marked manually by a teacher. */
    TEACHER,

    /** Attendance marked manually by a student. */
    STUDENT;

    /**
     * Returns a human-readable label for the marking source.
     * @return formatted string representing the marking source
     */
    public String getLabel() {
        return switch (this) {
            case QR -> "QR Code";
            case TEACHER -> "Teacher";
            case STUDENT -> "Student";
        };
    }

    /**
     * Checks if the attendance was marked automatically via QR code.
     * @return true if marked by QR, false otherwise
     */
    public boolean isQR() {
        return this == QR;
    }

    /**
     * Checks if the attendance was marked by a teacher.
     * @return true if marked by a teacher, false otherwise
     */
    public boolean isTeacher() {
        return this == TEACHER;
    }

    /**
     * Checks if the attendance was marked by a student.
     * @return true if marked by a student, false otherwise
     */
    public boolean isStudent() {
        return this == STUDENT;
    }
}
