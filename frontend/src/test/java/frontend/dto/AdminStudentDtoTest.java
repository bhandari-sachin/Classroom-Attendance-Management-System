package frontend.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminStudentDtoTest {

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        AdminStudentDto dto = new AdminStudentDto();

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getEmail());
        assertNull(dto.getStudentCode());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AdminStudentDto dto = new AdminStudentDto();

        dto.setId(1L);
        dto.setFirstName("Farah");
        dto.setLastName("Elbajta");
        dto.setEmail("farah@example.com");
        dto.setStudentCode("STU123");

        assertEquals(1L, dto.getId());
        assertEquals("Farah", dto.getFirstName());
        assertEquals("Elbajta", dto.getLastName());
        assertEquals("farah@example.com", dto.getEmail());
        assertEquals("STU123", dto.getStudentCode());
    }

    @Test
    void setters_shouldAllowNullValues() {
        AdminStudentDto dto = new AdminStudentDto();

        dto.setId(null);
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setEmail(null);
        dto.setStudentCode(null);

        assertNull(dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getEmail());
        assertNull(dto.getStudentCode());
    }

    @Test
    void id_shouldHandleDifferentValues() {
        AdminStudentDto dto = new AdminStudentDto();

        dto.setId(0L);
        assertEquals(0L, dto.getId());

        dto.setId(999L);
        assertEquals(999L, dto.getId());
    }
}