package model;

public class User {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final UserRole userType;
    private final String studentCode;  // null if not student

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

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getName() { return firstName + " " + lastName; }
    public UserRole getUserType() { return userType; }
    public String getStudentCode() { return studentCode; }
}