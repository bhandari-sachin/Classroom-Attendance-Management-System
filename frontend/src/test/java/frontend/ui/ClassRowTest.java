package frontend.ui;

import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassRowTest {

    @Test
    void constructorAndGettersShouldReturnCorrectValues() {
        ClassRow row = new ClassRow(
                "Math",
                "M101",
                "Mr. Smith",
                "Mon 10:00",
                "25"
        );

        assertEquals("Math", row.getClassName());
        assertEquals("M101", row.getCode());
        assertEquals("Mr. Smith", row.getTeacher());
        assertEquals("Mon 10:00", row.getSchedule());
        assertEquals("25", row.getStudents());
    }

    @Test
    void propertiesShouldReturnCorrectInitialValues() {
        ClassRow row = new ClassRow(
                "Physics",
                "P202",
                "Ms. Johnson",
                "Tue 14:00",
                "30"
        );

        assertEquals("Physics", row.classNameProperty().get());
        assertEquals("P202", row.codeProperty().get());
        assertEquals("Ms. Johnson", row.teacherProperty().get());
        assertEquals("Tue 14:00", row.scheduleProperty().get());
        assertEquals("30", row.studentsProperty().get());
    }

    @Test
    void updatingPropertyShouldReflectInGetter() {
        ClassRow row = new ClassRow(
                "Biology",
                "B303",
                "Dr. Lee",
                "Wed 09:00",
                "20"
        );

        row.classNameProperty().set("Advanced Biology");
        row.codeProperty().set("B404");
        row.teacherProperty().set("Dr. Brown");
        row.scheduleProperty().set("Thu 11:00");
        row.studentsProperty().set("22");

        assertEquals("Advanced Biology", row.getClassName());
        assertEquals("B404", row.getCode());
        assertEquals("Dr. Brown", row.getTeacher());
        assertEquals("Thu 11:00", row.getSchedule());
        assertEquals("22", row.getStudents());
    }

    @Test
    void propertyObjectsShouldNotBeNull() {
        ClassRow row = new ClassRow("A", "B", "C", "D", "E");

        assertNotNull(row.classNameProperty());
        assertNotNull(row.codeProperty());
        assertNotNull(row.teacherProperty());
        assertNotNull(row.scheduleProperty());
        assertNotNull(row.studentsProperty());
    }

    @Test
    void propertyShouldBeSameReferenceEachTime() {
        ClassRow row = new ClassRow("A", "B", "C", "D", "E");

        StringProperty first = row.classNameProperty();
        StringProperty second = row.classNameProperty();

        assertSame(first, second);
    }
}