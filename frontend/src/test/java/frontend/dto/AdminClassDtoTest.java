package frontend.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminClassDtoTest {

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        AdminClassDto dto = new AdminClassDto();

        assertNotNull(dto);
        assertEquals(0, dto.getId());
        assertNull(dto.getClassCode());
        assertNull(dto.getName());
        assertNull(dto.getTeacherEmail());
        assertNull(dto.getSemester());
        assertNull(dto.getAcademicYear());
        assertEquals(0, dto.getStudents());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AdminClassDto dto = new AdminClassDto();

        dto.setId(10L);
        dto.setClassCode("TX-123");
        dto.setName("Mathematics");
        dto.setTeacherEmail("teacher@example.com");
        dto.setSemester("Spring");
        dto.setAcademicYear("2025/2026");
        dto.setStudents(25);

        assertEquals(10L, dto.getId());
        assertEquals("TX-123", dto.getClassCode());
        assertEquals("Mathematics", dto.getName());
        assertEquals("teacher@example.com", dto.getTeacherEmail());
        assertEquals("Spring", dto.getSemester());
        assertEquals("2025/2026", dto.getAcademicYear());
        assertEquals(25, dto.getStudents());
    }

    @Test
    void setters_shouldAllowNullValues() {
        AdminClassDto dto = new AdminClassDto();

        dto.setClassCode(null);
        dto.setName(null);
        dto.setTeacherEmail(null);
        dto.setSemester(null);
        dto.setAcademicYear(null);

        assertNull(dto.getClassCode());
        assertNull(dto.getName());
        assertNull(dto.getTeacherEmail());
        assertNull(dto.getSemester());
        assertNull(dto.getAcademicYear());
    }

    @Test
    void students_shouldAllowZeroAndPositiveValues() {
        AdminClassDto dto = new AdminClassDto();

        dto.setStudents(0);
        assertEquals(0, dto.getStudents());

        dto.setStudents(100);
        assertEquals(100, dto.getStudents());
    }
}