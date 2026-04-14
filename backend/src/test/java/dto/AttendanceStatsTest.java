package dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatsTest {

    @Test
    void testGettersAndConstructor() {
        AttendanceStats stats = new AttendanceStats(8, 2, 1, 10);

        assertEquals(8, stats.getPresentCount());
        assertEquals(2, stats.getAbsentCount());
        assertEquals(1, stats.getExcusedCount());
        assertEquals(10, stats.getTotalDays());
    }

    @Test
    void testAttendanceRateCalculation() {
        AttendanceStats stats = new AttendanceStats(8, 2, 0, 10);

        double expectedRate = 80.0;
        assertEquals(expectedRate, stats.getAttendanceRate(), 0.0001);
    }

    @Test
    void testAttendanceRateWhenTotalDaysIsZero() {
        AttendanceStats stats = new AttendanceStats(5, 3, 2, 0);

        assertEquals(0.0, stats.getAttendanceRate(), 0.0001);
    }
}