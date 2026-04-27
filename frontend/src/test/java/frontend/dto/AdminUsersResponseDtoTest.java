package frontend.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminUsersResponseDtoTest {

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        AdminUsersResponseDto dto = new AdminUsersResponseDto();

        assertNotNull(dto);
        assertEquals(0, dto.getStudents());
        assertEquals(0, dto.getTeachers());
        assertEquals(0, dto.getAdmins());
        assertNotNull(dto.getUsers());
        assertTrue(dto.getUsers().isEmpty());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AdminUsersResponseDto dto = new AdminUsersResponseDto();

        AdminUserDto user1 = new AdminUserDto();
        user1.setName("Farah");

        AdminUserDto user2 = new AdminUserDto();
        user2.setName("John");

        dto.setStudents(10);
        dto.setTeachers(5);
        dto.setAdmins(2);
        dto.setUsers(List.of(user1, user2));

        assertEquals(10, dto.getStudents());
        assertEquals(5, dto.getTeachers());
        assertEquals(2, dto.getAdmins());

        assertNotNull(dto.getUsers());
        assertEquals(2, dto.getUsers().size());
        assertEquals("Farah", dto.getUsers().get(0).getName());
        assertEquals("John", dto.getUsers().get(1).getName());
    }

    @Test
    void setters_shouldHandleNullUsersList() {
        AdminUsersResponseDto dto = new AdminUsersResponseDto();

        dto.setUsers(null);

        assertNotNull(dto.getUsers());
        assertTrue(dto.getUsers().isEmpty());
    }

    @Test
    void numericFields_shouldHandleDifferentValues() {
        AdminUsersResponseDto dto = new AdminUsersResponseDto();

        dto.setStudents(0);
        dto.setTeachers(0);
        dto.setAdmins(0);

        assertEquals(0, dto.getStudents());
        assertEquals(0, dto.getTeachers());
        assertEquals(0, dto.getAdmins());

        dto.setStudents(100);
        dto.setTeachers(50);
        dto.setAdmins(10);

        assertEquals(100, dto.getStudents());
        assertEquals(50, dto.getTeachers());
        assertEquals(10, dto.getAdmins());
    }
}