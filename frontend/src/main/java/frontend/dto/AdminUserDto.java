package frontend.dto;

/**
 * DTO representing an admin user.
 */
public class AdminUserDto {

    private String name;
    private String email;
    private String role;
    private String enrolled;

    public AdminUserDto() {
        // Needed for Jackson
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(String enrolled) {
        this.enrolled = enrolled;
    }
}