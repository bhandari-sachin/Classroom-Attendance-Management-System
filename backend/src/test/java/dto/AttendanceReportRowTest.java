package dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceReportRowTest {

    @Test
    void testConstructorAndGetters() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "John", "Doe", 8, 2, 1, 11
        );

        assertEquals(1L, row.getId());
        assertEquals("John", row.getFirstName());
        assertEquals("Doe", row.getLastName());
        assertEquals(8, row.getPresent());
        assertEquals(2, row.getAbsent());
        assertEquals(1, row.getExcused());
        assertEquals(11, row.getTotal());
    }

    @Test
    void testGetStudentName() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "Jane", "Smith", 5, 3, 2, 10
        );

        assertEquals("Jane Smith", row.getStudentName());
    }

    @Test
    void testGetRate_NormalCase() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "Alice", "Brown", 8, 2, 0, 10
        );

        assertEquals(80.0, row.getRate());
    }

    @Test
    void testGetRate_ZeroTotal() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "Bob", "White", 0, 0, 0, 0
        );

        assertEquals(0.0, row.getRate());
    }

    @Test
    void testGetRate_DecimalResult() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "Chris", "Green", 7, 3, 0, 9
        );

        assertEquals((7 * 100.0) / 9, row.getRate());
    }

    @Test
    void testImmutability() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L, "Test", "User", 1, 1, 1, 3
        );

        // No setters exist, so just verify values remain constant
        assertAll(
                () -> assertEquals("Test", row.getFirstName()),
                () -> assertEquals("User", row.getLastName())
        );
    }
}