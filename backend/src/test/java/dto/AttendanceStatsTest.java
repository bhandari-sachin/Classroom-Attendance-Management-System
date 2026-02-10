package dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;

public class AttendanceStatsTest {

    private void setIntField(AttendanceStats stats, String fieldName, int value) throws Exception {
        Field f = AttendanceStats.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(stats, value);
    }

    @Test
    void getAttendanceRate_zeroTotal_returnsZero() throws Exception {
        AttendanceStats stats = new AttendanceStats();
        setIntField(stats, "totalRecords", 0);
        setIntField(stats, "presentCount", 10);
        // When totalRecords is zero, rate should be 0 (avoid division by zero)
        Assertions.assertEquals(0.0, stats.getAttendanceRate(), 1e-9);
    }

    @Test
    void getAttendanceRate_calculatesPercentageCorrectly() throws Exception {
        AttendanceStats stats = new AttendanceStats();
        setIntField(stats, "totalRecords", 200);
        setIntField(stats, "presentCount", 50);
        Assertions.assertEquals(25.0, stats.getAttendanceRate(), 1e-9);
    }

    @Test
    void getAttendanceRate_handlesNonIntegerResult() throws Exception {
        AttendanceStats stats = new AttendanceStats();
        setIntField(stats, "totalRecords", 3);
        setIntField(stats, "presentCount", 2);
        double expected = (2 * 100.0) / 3;
        Assertions.assertEquals(expected, stats.getAttendanceRate(), 1e-9);
    }
}

