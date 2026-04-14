package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassDtoTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        ClassDto dto = new ClassDto(
                1L,
                "Mathematics",
                "MATH101",
                "teacher@example.com",
                30
        );

        assertEquals(1L, dto.getId());
        assertEquals("Mathematics", dto.getName());
        assertEquals("MATH101", dto.getClassCode());
        assertEquals("teacher@example.com", dto.getTeacherEmail());
        assertEquals(30, dto.getStudentCount());
    }

    @Test
    void testNoArgsConstructor() {
        ClassDto dto = new ClassDto();

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getClassCode());
        assertNull(dto.getTeacherEmail());
        assertNull(dto.getStudentCount());
    }
}