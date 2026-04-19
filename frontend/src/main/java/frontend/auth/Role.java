package frontend.auth;

/**
 * Represents user roles in the system.
 */
public enum Role {
    ADMIN,
    TEACHER,
    STUDENT;

    /**
     * Converts a string to a Role safely.
     *
     * @param value role string (case-insensitive)
     * @return corresponding Role, or STUDENT as default if invalid
     */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return STUDENT;
        }

        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT; // fallback instead of crash
        }
    }

    /**
     * Checks if the role is admin.
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if the role is teacher.
     */
    public boolean isTeacher() {
        return this == TEACHER;
    }

    /**
     * Checks if the role is student.
     */
    public boolean isStudent() {
        return this == STUDENT;
    }
}