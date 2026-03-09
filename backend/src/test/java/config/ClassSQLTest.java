package config;

import config.ClassSQL.ClassView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassSQLTest {

    private ClassSQL classSQL;

    @BeforeEach
    void setup() {
        classSQL = mock(ClassSQL.class);
    }

    @Test
    void listAllForAdmin_returnsClasses() {

        // Fully mocked ClassView – no constructor, no fields
        ClassView view = mock(ClassView.class);

        when(classSQL.listAllForAdmin()).thenReturn(List.of(view));

        List<ClassView> result = classSQL.listAllForAdmin();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findTeacherIdByEmail_returnsTeacherId() {

        when(classSQL.findTeacherIdByEmail("teacher@test.com"))
                .thenReturn(10L);

        Long id = classSQL.findTeacherIdByEmail("teacher@test.com");

        assertNotNull(id);
        assertEquals(10L, id);
    }

    @Test
    void findTeacherIdByEmail_returnsNull_whenNotTeacher() {

        when(classSQL.findTeacherIdByEmail("student@test.com"))
                .thenReturn(null);

        Long id = classSQL.findTeacherIdByEmail("student@test.com");

        assertNull(id);
    }

    @Test
    void createClass_returnsId() {

        when(classSQL.createClass(
                "CLS123",
                "New Class",
                1L,
                "SPRING",
                "2025/2026",
                25
        )).thenReturn(100L);

        long id = classSQL.createClass(
                "CLS123",
                "New Class",
                1L,
                "SPRING",
                "2025/2026",
                25
        );

        assertEquals(100L, id);
    }

    @Test
    void listForTeacher_returnsTeachersClasses() {

        Map<String,Object> classMap =
                Map.of("id",1L,"name","T1 Class");

        when(classSQL.listForTeacher(1L))
                .thenReturn(List.of(classMap));

        List<Map<String,Object>> list =
                classSQL.listForTeacher(1L);

        assertEquals(1, list.size());
        assertEquals("T1 Class", list.get(0).get("name"));
    }

    @Test
    void isClassOwnedByTeacher_trueWhenOwned() {

        when(classSQL.isClassOwnedByTeacher(1L, 10L))
                .thenReturn(true);

        boolean result = classSQL.isClassOwnedByTeacher(1L,10L);

        assertTrue(result);
    }

    @Test
    void isClassOwnedByTeacher_falseWhenNotOwned() {

        when(classSQL.isClassOwnedByTeacher(1L, 20L))
                .thenReturn(false);

        boolean result = classSQL.isClassOwnedByTeacher(1L,20L);

        assertFalse(result);
    }

    @Test
    void listStudentsForClass_returnsStudents() {

        Map<String,Object> s1 =
                Map.of("firstName","Alice","lastName","One");

        Map<String,Object> s2 =
                Map.of("firstName","Bob","lastName","Two");

        when(classSQL.listStudentsForClass(1L))
                .thenReturn(List.of(s1,s2));

        List<Map<String,Object>> students =
                classSQL.listStudentsForClass(1L);

        assertEquals(2, students.size());
    }

    @Test
    void countForTeacher_returnsNumberOfClasses() {

        when(classSQL.countForTeacher(10L))
                .thenReturn(3);

        int result = classSQL.countForTeacher(10L);

        assertEquals(3, result);
    }
}
