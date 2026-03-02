package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class StudentTest {

    @Test
    void constructorAndGetters_returnValues() {
        Long id = 101L;
        String firstName = "Jane";
        String lastName = "Doe";
        String email = "jane.doe@example.com";
        String studentNumber = "1234567L";

        Student s = new Student(id, firstName, lastName, email, studentNumber);

        Assertions.assertEquals(id, s.getStudentId());
        Assertions.assertEquals(firstName, s.getFirstName());
        Assertions.assertEquals(lastName, s.getLastName());
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
        Assertions.assertNull(s.getStudentNumber());
    }

    @Test
    void instancesAreIndependent() {
        Student a = new Student(1L, "A", "B", "a@example.com", "7654321L");
        Student b = new Student(2L, "C", "D", "b@example.com", "7654322L");

        Assertions.assertNotEquals(a.getStudentId(), b.getStudentId());
        Assertions.assertNotEquals(a.getFirstName(), b.getFirstName());
        Assertions.assertNotEquals(a.getEmail(), b.getEmail());
    }
}