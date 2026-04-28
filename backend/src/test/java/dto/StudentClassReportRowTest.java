package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentClassReportRowTest {

    @Test
    void testConstructorAndGetters() {
        StudentClassReportRow row = new StudentClassReportRow(
                "Math", "STU123", 10, 2, 1, 83.33
        );

        assertEquals("Math", row.getClassName());
        assertEquals("STU123", row.getStudentCode());
        assertEquals(10, row.getPresent());
        assertEquals(2, row.getAbsent());
        assertEquals(1, row.getExcused());
        assertEquals(83.33, row.getRate());
    }

    @Test
    void testWithZeroValues() {
        StudentClassReportRow row = new StudentClassReportRow(
                "Science", "STU999", 0, 0, 0, 0.0
        );

        assertEquals(0, row.getPresent());
        assertEquals(0, row.getAbsent());
        assertEquals(0, row.getExcused());
        assertEquals(0.0, row.getRate());
    }

    @Test
    void testWithNegativeValues() {
        StudentClassReportRow row = new StudentClassReportRow(
                "History", "STU456", -1, -2, -3, -10.5
        );

        assertEquals(-1, row.getPresent());
        assertEquals(-2, row.getAbsent());
        assertEquals(-3, row.getExcused());
        assertEquals(-10.5, row.getRate());
    }

    @Test
    void testNullValues() {
        StudentClassReportRow row = new StudentClassReportRow(
                null, null, 5, 1, 0, 83.33
        );

        assertNull(row.getClassName());
        assertNull(row.getStudentCode());
    }

    @Test
    void testValuesRemainConsistent() {
        StudentClassReportRow row = new StudentClassReportRow(
                "English", "STU777", 6, 3, 1, 60.0
        );

        assertAll(
                () -> assertEquals("English", row.getClassName()),
                () -> assertEquals("STU777", row.getStudentCode()),
                () -> assertEquals(6, row.getPresent()),
                () -> assertEquals(3, row.getAbsent()),
                () -> assertEquals(1, row.getExcused()),
                () -> assertEquals(60.0, row.getRate())
        );
    }
}