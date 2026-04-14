package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentClassReportRowTest {

    @Test
    void testConstructorAndGetters() {
        StudentClassReportRow row = new StudentClassReportRow(
                "Math",
                "STU123",
                10,
                2,
                1,
                83.5
        );

        assertEquals("Math", row.getClassName());
        assertEquals("STU123", row.getStudentCode());
        assertEquals(10, row.getPresent());
        assertEquals(2, row.getAbsent());
        assertEquals(1, row.getExcused());
        assertEquals(83.5, row.getRate(), 0.0001);
    }

    @Test
    void testRatePrecision() {
        StudentClassReportRow row = new StudentClassReportRow(
                "Science",
                "STU999",
                9,
                1,
                0,
                90.0
        );

        assertEquals(90.0, row.getRate(), 0.0001);
    }
}