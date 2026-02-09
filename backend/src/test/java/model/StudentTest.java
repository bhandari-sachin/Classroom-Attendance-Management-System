package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class StudentTest {

    // Contract:
    // - Inputs: studentId (Long), name (String), email (String)
    // - Outputs: getters return same values passed to constructor
    // - Error modes: none expected; constructor accepts nulls

    @Test
    void constructorAndGetters_returnValues() {
        Long id = 101L;
        String firstName = "Jane";
        String lastName = "Doe";
        String name = "Jane Doe";
        String email = "jane.doe@example.com";
        Long studentNumber = 1234567L;

        Student s = new Student(id, firstName, lastName, email, studentNumber);

        Assertions.assertEquals(id, s.getStudentId());
        Assertions.assertEquals(name, s.getName());
        Assertions.assertEquals(email, s.getEmail());
        Assertions.assertEquals(studentNumber, s.getStudentNumber());
    }

    @Test
    void constructor_allowsNullValues() {
        Student s = new Student(null, null, null, null, null);
        Assertions.assertNull(s.getStudentId());
        Assertions.assertNull(s.getFirstName());
        Assertions.assertNull(s.getLastName());
        Assertions.assertNull(s.getEmail());
    }

    @Test
    void instancesAreIndependent() {
        Student a = new Student(1L, "A", "B", "a@example.com", 7654321L);
        Student b = new Student(2L, "C", "D", "b@example.com", 7654322L);

        Assertions.assertNotEquals(a.getStudentId(), b.getStudentId());
        Assertions.assertNotEquals(a.getName(), b.getName());
        Assertions.assertNotEquals(a.getEmail(), b.getEmail());
    }
}

