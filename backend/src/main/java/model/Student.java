package model;

/**
 * Represents a student within the academic system.
 * Contains identifying information such as name, email, and student number.
 */
public class Student {

    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private Long studentNumber;

    /**
     * Constructs a Student with all required details.
     * @param studentId the unique identifier of the student
     * @param firstName the student's first name
     * @param lastName the student's last name
     * @param email the student's email address
     * @param studentNumber the student's institutional number
     */
    public Student(Long studentId, String firstName, String lastName, String email, Long studentNumber) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.studentNumber = studentNumber;
    }

    // --- Getters ---

    public Long getStudentId() {
        return studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getStudentNumber() {
        return studentNumber;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Returns the student's full name.
     * @return concatenation of first and last name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    // --- Utility Methods ---

    /**
     * Checks if the student's email is valid (basic format check).
     * @return true if email contains '@' and '.', false otherwise
     */
    public boolean hasValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Returns a readable summary of the student record.
     * @return formatted string with student details
     */
    @Override
    public String toString() {
        return String.format(
                "Student[id=%d, name=%s %s, email=%s, studentNumber=%d]",
                studentId, firstName, lastName, email, studentNumber
        );
    }
}
