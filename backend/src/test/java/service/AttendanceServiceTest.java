package service;

import config.AttendanceSQL;
import dto.AttendanceStats;
import dto.AttendanceView;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceSQL attendanceSQL;

    @InjectMocks
    private AttendanceService attendanceService;

    @Captor
    private ArgumentCaptor<Attendance> attendanceCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Mark present
    @Test
    void markPresentShouldUpdateIfExists() {
        when(attendanceSQL.exists(1L, 10L)).thenReturn(true);

        attendanceService.markPresent(1L, 10L);

        verify(attendanceSQL).updateStatus(
                1L, 10L,
                AttendanceStatus.PRESENT,
                MarkedBy.TEACHER
        );
        verify(attendanceSQL, never()).save(any());
    }

    @Test
    void markPresentShouldSaveCorrectObjectIfNotExists() {
        when(attendanceSQL.exists(1L, 10L)).thenReturn(false);

        attendanceService.markPresent(1L, 10L);

        verify(attendanceSQL).save(attendanceCaptor.capture());

        Attendance saved = attendanceCaptor.getValue();
        assertEquals(1L, saved.getStudentId());
        assertEquals(10L, saved.getSessionId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
        assertEquals(MarkedBy.TEACHER, saved.getMarkedBy());
    }

    // Mark absent
    @Test
    void markAbsentShouldUpdateIfExists() {
        when(attendanceSQL.exists(1L, 10L)).thenReturn(true);

        attendanceService.markAbsent(1L, 10L);

        verify(attendanceSQL).updateStatus(
                1L, 10L,
                AttendanceStatus.ABSENT,
                MarkedBy.TEACHER
        );
    }

    @Test
    void markAbsentShouldSaveCorrectObjectIfNotExists() {
        when(attendanceSQL.exists(1L, 10L)).thenReturn(false);

        attendanceService.markAbsent(1L, 10L);

        verify(attendanceSQL).save(attendanceCaptor.capture());

        Attendance saved = attendanceCaptor.getValue();
        assertEquals(AttendanceStatus.ABSENT, saved.getStatus());
    }

    // Mark excused
    @Test
    void markExcusedShouldSaveCorrectObjectIfNotExists() {
        when(attendanceSQL.exists(1L, 10L)).thenReturn(false);

        attendanceService.markExcused(1L, 10L);

        verify(attendanceSQL).save(attendanceCaptor.capture());

        Attendance saved = attendanceCaptor.getValue();
        assertEquals(AttendanceStatus.EXCUSED, saved.getStatus());
    }

    // Mark by code
    @Test
    void markByCodeShouldThrowIfInvalidCode() {
        when(attendanceSQL.findSessionIdByCode("BAD")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> attendanceService.markByCode(1L, "BAD"));
    }

    @Test
    void markByCodeShouldThrowIfAlreadyExists() {
        when(attendanceSQL.findSessionIdByCode("CODE")).thenReturn(10L);
        when(attendanceSQL.exists(1L, 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> attendanceService.markByCode(1L, "CODE"));
    }

    @Test
    void markByCodeShouldSaveCorrectObject() {
        when(attendanceSQL.findSessionIdByCode("CODE")).thenReturn(10L);
        when(attendanceSQL.exists(1L, 10L)).thenReturn(false);

        attendanceService.markByCode(1L, "CODE");

        verify(attendanceSQL).save(attendanceCaptor.capture());

        Attendance saved = attendanceCaptor.getValue();
        assertEquals(1L, saved.getStudentId());
        assertEquals(10L, saved.getSessionId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
        assertEquals(MarkedBy.QR, saved.getMarkedBy());
    }

    // Manual code submission
    @Test
    void submitAttendanceCodeShouldReturnFalseIfWrongCode() {
        when(attendanceSQL.getSessionCode(10L)).thenReturn("CORRECT");

        boolean result = attendanceService.submitAttendanceCode(1L, 10L, "WRONG");

        assertFalse(result);
        verify(attendanceSQL, never()).save(any());
    }

    @Test
    void submitAttendanceCodeShouldUpdateIfExists() {
        when(attendanceSQL.getSessionCode(10L)).thenReturn("CODE");
        when(attendanceSQL.exists(1L, 10L)).thenReturn(true);

        boolean result = attendanceService.submitAttendanceCode(1L, 10L, "CODE");

        assertTrue(result);

        verify(attendanceSQL).updateStatus(
                1L, 10L,
                AttendanceStatus.PRESENT,
                MarkedBy.QR
        );
    }

    @Test
    void submitAttendanceCodeShouldSaveIfNotExists() {
        when(attendanceSQL.getSessionCode(10L)).thenReturn("CODE");
        when(attendanceSQL.exists(1L, 10L)).thenReturn(false);

        boolean result = attendanceService.submitAttendanceCode(1L, 10L, "CODE");

        assertTrue(result);
        verify(attendanceSQL).save(any(Attendance.class));
    }

    // Data retrieval
    @Test
    void getAttendanceForStudentShouldReturnData() {
        List<Attendance> mockData = List.of(
                new Attendance(1L, 10L, AttendanceStatus.PRESENT, MarkedBy.TEACHER)
        );

        when(attendanceSQL.findByStudentId(1L)).thenReturn(mockData);

        List<Attendance> result = attendanceService.getAttendanceForStudent(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAttendanceForClassShouldReturnData() {
        when(attendanceSQL.findByClassId(5L)).thenReturn(List.of());

        List<Attendance> result = attendanceService.getAttendanceForClass(5L);

        assertNotNull(result);
    }

    @Test
    void filterAttendanceShouldReturnViews() {
        when(attendanceSQL.filterAttendanceByStudent(1L, "john", "en"))
                .thenReturn(List.of());

        List<AttendanceView> result =
                attendanceService.filterAttendance(1L, "john", "en");

        assertNotNull(result);
    }

    @Test
    void getOverallStatsShouldReturnStats() {
        AttendanceStats stats = new AttendanceStats();
        when(attendanceSQL.getOverallStats()).thenReturn(stats);

        AttendanceStats result = attendanceService.getOverallStats();

        assertNotNull(result);
    }

    @Test
    void getStudentStatsShouldReturnStats() {
        AttendanceStats stats = new AttendanceStats();
        when(attendanceSQL.getStudentStats(1L)).thenReturn(stats);

        AttendanceStats result = attendanceService.getStudentStats(1L);

        assertNotNull(result);
    }
}