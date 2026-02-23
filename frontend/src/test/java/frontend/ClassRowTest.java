package frontend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassRowTest {

    @Test
    void testConstructorAndGetters() {
        ClassRow row = new ClassRow(
                "Mathematics",
                "MATH101",
                "Mr. Smith",
                "smith@school.com",
                "Mon 9AM",
                30
        );

        assertEquals("Mathematics", row.getClassName());
        assertEquals("MATH101", row.codeProperty().get());
        assertEquals("Mr. Smith", row.teacherProperty().get());
        assertEquals("smith@school.com", row.emailProperty().get());
        assertEquals("Mon 9AM", row.scheduleProperty().get());
        assertEquals(30, row.studentsProperty().get());
    }

    @Test
    void testPropertyUpdate() {
        ClassRow row = new ClassRow(
                "Science",
                "SCI101",
                "Ms. Johnson",
                "johnson@school.com",
                "Tue 10AM",
                25
        );

        // Update properties
        row.classNameProperty().set("Advanced Science");
        row.studentsProperty().set(40);

        assertEquals("Advanced Science", row.getClassName());
        assertEquals(40, row.studentsProperty().get());
    }

    @Test
    void testPropertiesNotNull() {
        ClassRow row = new ClassRow(
                "History",
                "HIS101",
                "Mr. Brown",
                "brown@school.com",
                "Wed 11AM",
                20
        );

        assertNotNull(row.classNameProperty());
        assertNotNull(row.codeProperty());
        assertNotNull(row.teacherProperty());
        assertNotNull(row.emailProperty());
        assertNotNull(row.scheduleProperty());
        assertNotNull(row.studentsProperty());
    }
}