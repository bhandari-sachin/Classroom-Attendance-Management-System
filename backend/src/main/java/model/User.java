package model;

/**
 * Represents a user within the academic system.
 * A user can be an admin, teacher, or student, depending on their role.
 * Contains authentication and identification details.
 */
public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final UserRole userType;
    private final String studentCode; // null if not a student

    /**
     * Constructs a User with all required details.
     * @param id the unique identifier of the user
     * @param email the user's email address
     * @param passwordHash the hashed password for authentication
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param userType the role of the user (e.g., ADMIN, TEACHER, STUDENT)
     * @param studentCode the student code if the user is a student, otherwise null
     */
    public User(Long id, String email, String passwordHash,
                String firstName, String lastName,
                UserRole userType, String studentCode) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.studentCode = studentCode;
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserRole getUserType() {
        return userType;
    }

    public String getStudentCode() {
        return studentCode;
    }

    // --- Utility Methods ---

    /**
     * Returns the user's full name.
     * @return concatenation of first and last name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * Checks if the user is a student.
     * @return true if the user has a student code, false otherwise
     */
    public boolean isStudent() {
        return studentCode != null && !studentCode.isEmpty();
    }

    /**
     * Checks if the user is a teacher.
     * @return true if the user's role is TEACHER
     */
    public boolean isTeacher() {
        return userType == UserRole.TEACHER;
    }

    /**
     * Checks if the user is an admin.
     * @return true if the user's role is ADMIN
     */
    public boolean isAdmin() {
        return userType == UserRole.ADMIN;
    }

    /**
     * Returns a readable summary of the user record.
     * @return formatted string with user details
     */
    @Override
    public String toString() {
        return String.format(
                "User[id=%d, name=%s %s, email=%s, role=%s, studentCode=%s]",
                id, firstName, lastName, email, userType, studentCode
        );
    }
}
