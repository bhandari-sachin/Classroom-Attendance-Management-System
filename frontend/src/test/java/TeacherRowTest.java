
import frontend.TeacherRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TeacherRowTest {

    @Test
    void testConstructorAndGetters() {
        TeacherRow teacher = new TeacherRow(
                "Mr. Johnson",
                "johnson@school.com"
        );

        assertEquals("Mr. Johnson", teacher.getTeacherName());
        assertEquals("johnson@school.com", teacher.getEmail());
    }

    @Test
    void testPropertiesNotNull() {
        TeacherRow teacher = new TeacherRow(
                "Ms. Williams",
                "williams@school.com"
        );

        assertNotNull(teacher.teacherNameProperty());
        assertNotNull(teacher.emailProperty());
    }

    @Test
    void testPropertyUpdateReflectsInGetter() {
        TeacherRow teacher = new TeacherRow(
                "Mr. Smith",
                "smith@school.com"
        );

        // Update using property
        teacher.teacherNameProperty().set("Dr. Smith");
        teacher.emailProperty().set("drsmith@school.com");

        assertEquals("Dr. Smith", teacher.getTeacherName());
        assertEquals("drsmith@school.com", teacher.getEmail());
    }
}