package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassDtoTest {

    @Test
    void getId() {
        ClassDto dto = new ClassDto(1L, "Databases", "CS101", "teacher@test.com", 25);

        assertEquals(1L, dto.getId());
    }

    @Test
    void getName() {
        ClassDto dto = new ClassDto(1L, "Databases", "CS101", "teacher@test.com", 25);

        assertEquals("Databases", dto.getName());
    }

    @Test
    void getClassCode() {
        ClassDto dto = new ClassDto(1L, "Databases", "CS101", "teacher@test.com", 25);

        assertEquals("CS101", dto.getClassCode());
    }

    @Test
    void getTeacherEmail() {
        ClassDto dto = new ClassDto(1L, "Databases", "CS101", "teacher@test.com", 25);

        assertEquals("teacher@test.com", dto.getTeacherEmail());
    }

    @Test
    void getStudentCount() {
        ClassDto dto = new ClassDto(1L, "Databases", "CS101", "teacher@test.com", 25);

        assertEquals(25, dto.getStudentCount());
    }

    @Test
    void defaultConstructor_fieldsAreNull() {
        ClassDto dto = new ClassDto();

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getClassCode());
        assertNull(dto.getTeacherEmail());
        assertNull(dto.getStudentCount());
    }
}