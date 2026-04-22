package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherStudentReportRowTest {

    @Test
    void testConstructorAndGetters() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "Math", "Mr. Smith", "John Doe", 10, 2, 1, 83.33
        );

        assertEquals("Math", row.getClassName());
        assertEquals("Mr. Smith", row.getTeacherName());
        assertEquals("John Doe", row.getStudentName());
        assertEquals(10, row.getPresent());
        assertEquals(2, row.getAbsent());
        assertEquals(1, row.getExcused());
        assertEquals(83.33, row.getRate());
    }

    @Test
    void testGetTotal_NormalCase() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "Science", "Ms. Lee", "Alice", 8, 1, 1, 80.0
        );

        assertEquals(10, row.getTotal());
    }

    @Test
    void testGetTotal_ZeroValues() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "History", "Mr. Brown", "Bob", 0, 0, 0, 0.0
        );

        assertEquals(0, row.getTotal());
    }

    @Test
    void testGetTotal_WithNegativeValues() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "Physics", "Dr. White", "Charlie", -1, -2, -3, -50.0
        );

        assertEquals(-6, row.getTotal());
    }

    @Test
    void testNullValues() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                null, null, null, 5, 2, 1, 62.5
        );

        assertNull(row.getClassName());
        assertNull(row.getTeacherName());
        assertNull(row.getStudentName());
    }

    @Test
    void testValuesRemainConsistent() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "English", "Mrs. Green", "David", 6, 3, 1, 60.0
        );

        assertAll(
                () -> assertEquals("English", row.getClassName()),
                () -> assertEquals("Mrs. Green", row.getTeacherName()),
                () -> assertEquals("David", row.getStudentName()),
                () -> assertEquals(6, row.getPresent()),
                () -> assertEquals(3, row.getAbsent()),
                () -> assertEquals(1, row.getExcused()),
                () -> assertEquals(60.0, row.getRate()),
                () -> assertEquals(10, row.getTotal())
        );
    }
}