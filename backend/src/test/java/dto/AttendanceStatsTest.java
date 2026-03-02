package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatsTest {

    @Test
    void constructor_ShouldSetAllFieldsCorrectly() {
        AttendanceStats stats = new AttendanceStats(10, 2, 3, 15);

        assertEquals(10, stats.getPresentCount());
        assertEquals(2, stats.getAbsentCount());
        assertEquals(3, stats.getExcusedCount());
        assertEquals(15, stats.getTotalRecords());
    }

    @Test
    void getAttendanceRate_ShouldReturnCorrectPercentage() {
        AttendanceStats stats = new AttendanceStats(8, 1, 1, 10);

        assertEquals(80.0, stats.getAttendanceRate());
    }

    @Test
    void getAttendanceRate_ShouldReturnZeroWhenTotalIsZero() {
        AttendanceStats stats = new AttendanceStats(5, 0, 0, 0);

        assertEquals(0.0, stats.getAttendanceRate());
    }

    @Test
    void getAttendanceRate_ShouldHandleAllAbsent() {
        AttendanceStats stats = new AttendanceStats(0, 5, 0, 5);

        assertEquals(0.0, stats.getAttendanceRate());
    }

    @Test
    void getAttendanceRate_ShouldHandleFullAttendance() {
        AttendanceStats stats = new AttendanceStats(10, 0, 0, 10);

        assertEquals(100.0, stats.getAttendanceRate());
    }
}