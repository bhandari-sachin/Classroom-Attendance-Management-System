package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceReportRowTest {

    @Test
    void constructor_ShouldSetAllFieldsCorrectly() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "John",
                "Doe",
                8,
                1,
                1,
                10
        );

        assertEquals(1L, row.id);
        assertEquals("John", row.firstName);
        assertEquals("Doe", row.lastName);
        assertEquals(8, row.present);
        assertEquals(1, row.absent);
        assertEquals(1, row.excused);
        assertEquals(10, row.total);
    }

    @Test
    void getStudentName_ShouldReturnFullName() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Jane",
                "Smith",
                5,
                0,
                0,
                5
        );

        assertEquals("Jane Smith", row.getStudentName());
    }

    @Test
    void getPresent_ShouldReturnCorrectValue() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Test",
                "User",
                7,
                2,
                1,
                10
        );

        assertEquals(7, row.getPresent());
    }

    @Test
    void getAbsent_ShouldReturnCorrectValue() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Test",
                "User",
                7,
                2,
                1,
                10
        );

        assertEquals(2, row.getAbsent());
    }

    @Test
    void getExcused_ShouldReturnCorrectValue() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Test",
                "User",
                7,
                2,
                1,
                10
        );

        assertEquals(1, row.getExcused());
    }

    @Test
    void getRate_ShouldReturnCorrectPercentage() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Test",
                "User",
                8,
                1,
                1,
                10
        );

        assertEquals(80.0, row.getRate());
    }

    @Test
    void getRate_ShouldReturnZeroWhenTotalIsZero() {
        AttendanceReportRow row = new AttendanceReportRow(
                1L,
                "Test",
                "User",
                5,
                0,
                0,
                0
        );

        assertEquals(0.0, row.getRate());
    }
}