package config;

import model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentSQLTest {

    private StudentSQL studentSQL;

    @BeforeEach
    void setup() {
        studentSQL = mock(StudentSQL.class);
    }

    @Test
    void findById_returnsNull_whenUserIsNotStudent() {

        when(studentSQL.findById(50L)).thenReturn(null);

        Student s = studentSQL.findById(50L);

        assertNull(s);
    }

    @Test
    void findById_returnsNull_whenNotFound() {

        when(studentSQL.findById(99999999L)).thenReturn(null);

        Student s = studentSQL.findById(99999999L);

        assertNull(s);
    }

    @Test
    void findByClassId_returnsEmpty_whenNoEnrollments() {

        when(studentSQL.findByClassId(123L)).thenReturn(List.of());

        List<Student> students = studentSQL.findByClassId(123L);

        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    @Test
    void findByClassId_returnsMockedStudents() {

        Student s1 = mock(Student.class);
        Student s2 = mock(Student.class);

        when(studentSQL.findByClassId(10L)).thenReturn(List.of(s1, s2));

        List<Student> list = studentSQL.findByClassId(10L);

        assertNotNull(list);
        assertEquals(2, list.size());
    }
}