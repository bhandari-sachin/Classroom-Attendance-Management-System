package config;

import dto.AttendanceStats;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceSQLTest {

    private AttendanceSQL attendanceSQL;

    @BeforeEach
    void setup() {
        attendanceSQL = Mockito.mock(AttendanceSQL.class);
    }

    @Test
    void exists_returnsTrue_whenAttendanceExists() {

        long studentId = 1;
        long sessionId = 10;

        when(attendanceSQL.exists(studentId, sessionId)).thenReturn(true);

        boolean result = attendanceSQL.exists(studentId, sessionId);

        assertTrue(result);
        verify(attendanceSQL).exists(studentId, sessionId);
    }

    @Test
    void save_attendance_success() {

        Attendance attendance =
                new Attendance(1L, 10L, AttendanceStatus.PRESENT, MarkedBy.QR);

        doNothing().when(attendanceSQL).save(attendance);

        attendanceSQL.save(attendance);

        verify(attendanceSQL, times(1)).save(attendance);
    }

    @Test
    void findByStudentId_returnsList() {

        Attendance a1 =
                new Attendance(1L, 10L, AttendanceStatus.PRESENT, MarkedBy.QR);

        Attendance a2 =
                new Attendance(1L, 11L, AttendanceStatus.ABSENT, MarkedBy.QR);

        when(attendanceSQL.findByStudentId(1L)).thenReturn(List.of(a1, a2));

        List<Attendance> result = attendanceSQL.findByStudentId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getOverallStats_returnsStats() {

        AttendanceStats stats = new AttendanceStats(10,5,3,2);

        when(attendanceSQL.getOverallStats()).thenReturn(stats);

        AttendanceStats result = attendanceSQL.getOverallStats();

        assertNotNull(result);
        assertEquals(2, result.getTotalDays());
    }
}