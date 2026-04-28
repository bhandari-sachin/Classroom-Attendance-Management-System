package frontend.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO representing admin users summary + list.
 */
public class AdminUsersResponseDto {

    private int students;
    private int teachers;
    private int admins;
    private List<AdminUserDto> users;

    public AdminUsersResponseDto() {
        // Needed for Jackson
    }

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }

    public int getTeachers() {
        return teachers;
    }

    public void setTeachers(int teachers) {
        this.teachers = teachers;
    }

    public int getAdmins() {
        return admins;
    }

    public void setAdmins(int admins) {
        this.admins = admins;
    }

    public List<AdminUserDto> getUsers() {
        return users == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(users);
    }

    public void setUsers(List<AdminUserDto> users) {
        this.users = (users == null)
                ? new ArrayList<>()
                : new ArrayList<>(users);
    }
}