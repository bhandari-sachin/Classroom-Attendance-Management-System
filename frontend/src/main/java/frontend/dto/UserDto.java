package frontend.dto;

public class UserDto {
    public Long id;
    public String email;
    public String firstName;
    public String lastName;
    public String role;        // "ADMIN" "TEACHER" "STUDENT"
    public String studentCode; // nullable
}