package dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;

public class ClassAttendanceSummaryTest {

    private void setIntField(ClassAttendanceSummary s, String fieldName, int value) throws Exception {
        Field f = ClassAttendanceSummary.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(s, value);
    }

    @Test
    void getRate_zeroTotal_returnsZero() throws Exception {
        ClassAttendanceSummary s = new ClassAttendanceSummary();
        setIntField(s, "present", 0);
        setIntField(s, "absent", 0);
        setIntField(s, "excused", 0);

        Assertions.assertEquals(0.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_allPresent_returns100() throws Exception {
        ClassAttendanceSummary s = new ClassAttendanceSummary();
        setIntField(s, "present", 7);
        setIntField(s, "absent", 0);
        setIntField(s, "excused", 0);

        Assertions.assertEquals(100.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_fractional_calculation() throws Exception {
        ClassAttendanceSummary s = new ClassAttendanceSummary();
        setIntField(s, "present", 2);
        setIntField(s, "absent", 1);
        setIntField(s, "excused", 0);

        double expected = (2 * 100.0) / 3;
        Assertions.assertEquals(expected, s.getRate(), 1e-9);
    }

    @Test
    void getRate_handlesLargeNumbers_withoutOverflow() throws Exception {
        ClassAttendanceSummary s = new ClassAttendanceSummary();
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

