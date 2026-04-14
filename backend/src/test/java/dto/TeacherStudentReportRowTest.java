package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherStudentReportRowTest {

    @Test
    void testConstructorAndGetters() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "Math",
                "Mr. Smith",
                "John Doe",
                8,
                1,
                1,
                80.0
        );

        assertEquals("Math", row.getClassName());
        assertEquals("Mr. Smith", row.getTeacherName());
        assertEquals("John Doe", row.getStudentName());
        assertEquals(8, row.getPresent());
        assertEquals(1, row.getAbsent());
        assertEquals(1, row.getExcused());
        assertEquals(80.0, row.getRate(), 0.0001);
    }

    @Test
    void testGetTotalCalculation() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "Science",
                "Mrs. Taylor",
                "Alice",
                5,
                2,
                3,
                70.0
        );

        assertEquals(10, row.getTotal());
    }

    @Test
    void testGetTotalWithZeroValues() {
        TeacherStudentReportRow row = new TeacherStudentReportRow(
                "History",
                "Mr. Brown",
                "Bob",
                0,
                0,
                0,
                0.0
        );

        assertEquals(0, row.getTotal());
    }
}