package frontend.dto;

import java.util.List;

/**
 * DTO representing admin users summary + list.
 */
public class AdminUsersResponseDto {
    public int students;
    public int teachers;
    public int admins;
    public List<AdminUserDto> users;
}