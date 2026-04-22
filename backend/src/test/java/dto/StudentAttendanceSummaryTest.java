package dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;

class StudentAttendanceSummaryTest {

    private void setIntField(StudentAttendanceSummary s, String fieldName, int value) throws Exception {
        Field f = StudentAttendanceSummary.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(s, value);
    }

    @Test
    void getRate_zeroTotal_returnsZero() throws Exception {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        setIntField(s, "present", 0);
        setIntField(s, "absent", 0);
        setIntField(s, "excused", 0);

        Assertions.assertEquals(0.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_allPresent_returns100() throws Exception {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        setIntField(s, "present", 10);
        setIntField(s, "absent", 0);
        setIntField(s, "excused", 0);

        Assertions.assertEquals(100.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_fractionalCalculation() throws Exception {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        setIntField(s, "present", 3);
        setIntField(s, "absent", 2);
        setIntField(s, "excused", 1);

        double expected = (3 * 100.0) / (3 + 2 + 1);
        Assertions.assertEquals(expected, s.getRate(), 1e-9);
    }

    @Test
    void getRate_handlesLargeNumbers_withoutOverflow() throws Exception {
        StudentAttendanceSummary s = new StudentAttendanceSummary();
        int present = Integer.MAX_VALUE / 2;
        int absent = Integer.MAX_VALUE / 4;
        int excused = Integer.MAX_VALUE / 4;
        setIntField(s, "present", present);
        setIntField(s, "absent", absent);
        setIntField(s, "excused", excused);

        double total = (double) present + absent + excused;
        double expected = (present * 100.0) / total;
        Assertions.assertEquals(expected, s.getRate(), 1e-9);
    }
}