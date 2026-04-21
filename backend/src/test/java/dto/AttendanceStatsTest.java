package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatsTest {

    @Test
    void testDefaultConstructor() {
        AttendanceStats stats = new AttendanceStats();

        assertEquals(0, stats.getPresentCount());
        assertEquals(0, stats.getAbsentCount());
        assertEquals(0, stats.getExcusedCount());
        assertEquals(0, stats.getTotalDays());
    }

    @Test
    void testParameterizedConstructorAndGetters() {
        AttendanceStats stats = new AttendanceStats(10, 5, 2, 17);

        assertEquals(10, stats.getPresentCount());
        assertEquals(5, stats.getAbsentCount());
        assertEquals(2, stats.getExcusedCount());
        assertEquals(17, stats.getTotalDays());
    }

    @Test
    void testGetAttendanceRate_NormalCase() {
        AttendanceStats stats = new AttendanceStats(8, 2, 0, 10);

        assertEquals(80.0, stats.getAttendanceRate());
    }

    @Test
    void testGetAttendanceRate_ZeroTotalDays() {
        AttendanceStats stats = new AttendanceStats(5, 3, 2, 0);

        assertEquals(0.0, stats.getAttendanceRate());
    }

    @Test
    void testGetAttendanceRate_DecimalResult() {
        AttendanceStats stats = new AttendanceStats(7, 3, 0, 9);

        assertEquals((7 * 100.0) / 9, stats.getAttendanceRate());
    }

    @Test
    void testValuesRemainConsistent() {
        AttendanceStats stats = new AttendanceStats(3, 1, 1, 5);

        assertAll(
                () -> assertEquals(3, stats.getPresentCount()),
                () -> assertEquals(1, stats.getAbsentCount()),
                () -> assertEquals(1, stats.getExcusedCount()),
                () -> assertEquals(5, stats.getTotalDays())
        );
    }
}