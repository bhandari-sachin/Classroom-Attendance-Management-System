package frontend.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserDtoTest {

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        AdminUserDto dto = new AdminUserDto();

        assertNotNull(dto);
        assertNull(dto.getName());
        assertNull(dto.getEmail());
        assertNull(dto.getRole());
        assertNull(dto.getEnrolled());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AdminUserDto dto = new AdminUserDto();

        dto.setName("Farah");
        dto.setEmail("farah@example.com");
        dto.setRole("ADMIN");
        dto.setEnrolled("Yes");

        assertEquals("Farah", dto.getName());
        assertEquals("farah@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
        assertEquals("Yes", dto.getEnrolled());
    }

    @Test
    void setters_shouldAllowNullValues() {
        AdminUserDto dto = new AdminUserDto();

        dto.setName(null);
        dto.setEmail(null);
        dto.setRole(null);
        dto.setEnrolled(null);

        assertNull(dto.getName());
        assertNull(dto.getEmail());
        assertNull(dto.getRole());
        assertNull(dto.getEnrolled());
    }

    @Test
    void fields_shouldHandleDifferentValues() {
        AdminUserDto dto = new AdminUserDto();

        dto.setName("John Doe");
        dto.setEmail("john.doe@test.com");
        dto.setRole("TEACHER");
        dto.setEnrolled("No");

        assertEquals("John Doe", dto.getName());
        assertEquals("john.doe@test.com", dto.getEmail());
        assertEquals("TEACHER", dto.getRole());
        assertEquals("No", dto.getEnrolled());
    }
}