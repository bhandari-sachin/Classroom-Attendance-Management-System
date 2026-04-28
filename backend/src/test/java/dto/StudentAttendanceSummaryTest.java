package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentAttendanceSummaryTest {

    @Test
    void getRate_zeroTotal_returnsZero() {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        s.setPresent(0);
        s.setAbsent(0);
        s.setExcused(0);

        assertEquals(0.0, s.getRate());
    }

    @Test
    void getRate_allPresent_returns100() {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        s.setPresent(10);
        s.setAbsent(0);
        s.setExcused(0);

        assertEquals(100.0, s.getRate());
    }

    @Test
    void getRate_fractionalCalculation() {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        s.setPresent(3);
        s.setAbsent(2);
        s.setExcused(1);

        double expected = (3 * 100.0) / 6;
        assertEquals(expected, s.getRate());
    }

    @Test
    void getRate_handlesLargeNumbers() {
        StudentAttendanceSummary s = new StudentAttendanceSummary();

        int present = Integer.MAX_VALUE / 2;
        int absent = Integer.MAX_VALUE / 4;
        int excused = Integer.MAX_VALUE / 4;

        s.setPresent(present);
        s.setAbsent(absent);
        s.setExcused(excused);

        double expected = (present * 100.0) / (present + absent + excused);

        assertEquals(expected, s.getRate());
    }

    // ========================
    // Bonus: constructor test
    // ========================

    @Test
    void constructor_setsFieldsCorrectly() {
        StudentAttendanceSummary s =
                new StudentAttendanceSummary(1L, "A", "B", 5, 3, 2);

        assertEquals(1L, s.getStudentId());
        assertEquals("A", s.getFirstName());
        assertEquals("B", s.getLastName());
        assertEquals(5, s.getPresent());
        assertEquals(3, s.getAbsent());
        assertEquals(2, s.getExcused());
    }

    // ========================
    // Bonus: setters test
    // ========================

    @Test
    void setters_updateFields() {
        StudentAttendanceSummary s = new StudentAttendanceSummary();

        s.setStudentId(2L);
        s.setFirstName("John");
        s.setLastName("Doe");
        s.setPresent(1);
        s.setAbsent(2);
        s.setExcused(3);

        assertEquals(2L, s.getStudentId());
        assertEquals("John", s.getFirstName());
        assertEquals("Doe", s.getLastName());
        assertEquals(1, s.getPresent());
        assertEquals(2, s.getAbsent());
        assertEquals(3, s.getExcused());
    }
}