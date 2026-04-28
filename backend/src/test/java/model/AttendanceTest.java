package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AttendanceTest {

    @Test
    void defaultConstructor_andSettersAndGetters() {
        Attendance a = new Attendance();

        LocalDateTime now = LocalDateTime.now();

        a.setId(1L);
        a.setStudentId(10L);
        a.setSessionId(20L);
        a.setStatus(AttendanceStatus.PRESENT);
        a.setMarkedBy(MarkedBy.TEACHER);
        a.setMarkedAt(now);

        assertEquals(1L, a.getId());
        assertEquals(10L, a.getStudentId());
        assertEquals(20L, a.getSessionId());
        assertEquals(AttendanceStatus.PRESENT, a.getStatus());
        assertEquals(MarkedBy.TEACHER, a.getMarkedBy());
        assertEquals(now, a.getMarkedAt());
    }

    @Test
    void parameterizedConstructor_setsFieldsCorrectly() {
        Attendance a = new Attendance(
                5L,
                6L,
                AttendanceStatus.ABSENT,
                MarkedBy.QR
        );

        assertEquals(5L, a.getStudentId());
        assertEquals(6L, a.getSessionId());
        assertEquals(AttendanceStatus.ABSENT, a.getStatus());
        assertEquals(MarkedBy.QR, a.getMarkedBy());

        // fields not set in constructor
        assertNull(a.getId());
        assertNull(a.getMarkedAt());
    }

    @Test
    void setStatus_updatesStatus() {
        Attendance attendance = new Attendance(1L, 2L, AttendanceStatus.ABSENT, MarkedBy.TEACHER);
        assertEquals(AttendanceStatus.ABSENT, attendance.getStatus());

        attendance.setStatus(AttendanceStatus.PRESENT);
        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
    }

    @Test
    void setters_acceptNullValues() {
        Attendance a = new Attendance();

        a.setId(null);
        a.setStudentId(null);
        a.setSessionId(null);
        a.setStatus(null);
        a.setMarkedBy(null);
        a.setMarkedAt(null);

        assertNull(a.getId());
        assertNull(a.getStudentId());
        assertNull(a.getSessionId());
        assertNull(a.getStatus());
        assertNull(a.getMarkedBy());
        assertNull(a.getMarkedAt());
    }

    @Test
    void setters_overwriteValues() {
        Attendance a = new Attendance();

        a.setStudentId(1L);
        a.setStudentId(2L);

        a.setStatus(AttendanceStatus.PRESENT);
        a.setStatus(AttendanceStatus.EXCUSED);

        assertEquals(2L, a.getStudentId());
        assertEquals(AttendanceStatus.EXCUSED, a.getStatus());
    }
}
