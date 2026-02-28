
import frontend.StudentRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentRowTest {

    @Test
    void testConstructorInitialValues() {
        StudentRow student = new StudentRow(
                "Alice",
                "alice@school.com",
                "Present"
        );

        assertEquals("Alice", student.getStudentName());
        assertEquals("alice@school.com", student.getEmail());
        assertEquals("Present", student.getStatus());

        // Default excuse reason should be empty string
        assertEquals("", student.getExcuseReason());
    }

    @Test
    void testSettersAndGetters() {
        StudentRow student = new StudentRow(
                "John",
                "john@school.com",
                "Absent"
        );

        student.setStudentName("Johnny");
        student.setEmail("johnny@school.com");
        student.setStatus("Present");
        student.setExcuseReason("Sick");

        assertEquals("Johnny", student.getStudentName());
        assertEquals("johnny@school.com", student.getEmail());
        assertEquals("Present", student.getStatus());
        assertEquals("Sick", student.getExcuseReason());
    }

    @Test
    void testExcuseReasonNullBecomesEmptyString() {
        StudentRow student = new StudentRow(
                "Sara",
                "sara@school.com",
                "Absent"
        );

        student.setExcuseReason(null);

        assertEquals("", student.getExcuseReason());
    }

    @Test
    void testPropertiesNotNull() {
        StudentRow student = new StudentRow(
                "Mike",
                "mike@school.com",
                "Present"
        );

        assertNotNull(student.studentNameProperty());
        assertNotNull(student.emailProperty());
        assertNotNull(student.statusProperty());
        assertNotNull(student.excuseReasonProperty());
    }

    @Test
    void testPropertyUpdateReflectsInGetter() {
        StudentRow student = new StudentRow(
                "Emma",
                "emma@school.com",
                "Absent"
        );

        student.studentNameProperty().set("Emily");
        student.statusProperty().set("Present");

        assertEquals("Emily", student.getStudentName());
        assertEquals("Present", student.getStatus());
    }
}