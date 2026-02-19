package model;

public class User {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Long studentCode;

    public User(Long id, String email,
                String firstName, String lastName,
                UserRole role, Long studentCode) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.studentCode = studentCode;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public Long getStudentCode() { return studentCode; }
}
