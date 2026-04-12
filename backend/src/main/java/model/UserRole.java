package model;

/**
 * Represents the different roles a user can have within the academic system.
 * Each role defines the user's permissions and responsibilities.
 */
public enum UserRole {

    /** Administrative user with full system access and management privileges. */
    ADMIN,

    /** Teacher user responsible for managing classes and marking attendance. */
    TEACHER,

    /** Student user who attends sessions and can view personal attendance reports. */
    STUDENT;

    /**
     * Returns a human-readable label for the user role.
     * @return formatted string representing the role
     */
    public String getLabel() {
        return switch (this) {
            case ADMIN -> "Administrator";
            case TEACHER -> "Teacher";
            case STUDENT -> "Student";
        };
    }

    /**
     * Checks if the role represents an administrator.
     * @return true if ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if the role represents a teacher.
     * @return true if TEACHER, false otherwise
     */
    public boolean isTeacher() {
        return this == TEACHER;
    }

    /**
     * Checks if the role represents a student.
     * @return true if STUDENT, false otherwise
     */
    public boolean isStudent() {
        return this == STUDENT;
    }
}
