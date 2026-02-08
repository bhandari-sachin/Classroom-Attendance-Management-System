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
        String name = "Jane Doe";
        String email = "jane.doe@example.com";

        Student s = new Student(id, name, email);

        Assertions.assertEquals(id, s.getStudentId());
        // Note: getter is getFirstName() although field is 'name'
        Assertions.assertEquals(name, s.getFirstName());
        Assertions.assertEquals(email, s.getEmail());
    }

    @Test
    void constructor_allowsNullValues() {
        Student s = new Student(null, null, null);
        Assertions.assertNull(s.getStudentId());
        Assertions.assertNull(s.getFirstName());
        Assertions.assertNull(s.getEmail());
    }

    @Test
    void instancesAreIndependent() {
        Student a = new Student(1L, "A", "a@example.com");
        Student b = new Student(2L, "B", "b@example.com");

        Assertions.assertNotEquals(a.getStudentId(), b.getStudentId());
        Assertions.assertNotEquals(a.getFirstName(), b.getFirstName());
        Assertions.assertNotEquals(a.getEmail(), b.getEmail());
    }
}

