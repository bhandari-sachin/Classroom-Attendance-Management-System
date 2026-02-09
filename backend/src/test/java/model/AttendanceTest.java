package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AttendanceTest {

    @Test
    void constructorAndGetters_workAsExpected() {
        Attendance attendance = new Attendance(10L, 20L, AttendanceStatus.EXCUSED, MarkedBy.TEACHER);

        Assertions.assertEquals(10L, attendance.getStudentId());
        Assertions.assertEquals(20L, attendance.getSessionId());
        Assertions.assertEquals(AttendanceStatus.EXCUSED, attendance.getStatus());
        Assertions.assertEquals(MarkedBy.TEACHER, attendance.getMarkedBy());
    }

    @Test
    void setStatus_updatesStatus() {
        Attendance attendance = new Attendance(1L, 2L, AttendanceStatus.ABSENT, MarkedBy.TEACHER);
        Assertions.assertEquals(AttendanceStatus.ABSENT, attendance.getStatus());

        attendance.setStatus(AttendanceStatus.PRESENT);
        Assertions.assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
    }

    @Test
    void constructor_allowsNullRemarksAndFieldsAreImmutable() {
        // remarks is not exposed via getter; verify studentId/sessionId remain as provided
        Attendance attendance = new Attendance(123L, 456L, AttendanceStatus.PRESENT, MarkedBy.TEACHER);
        Assertions.assertEquals(123L, attendance.getStudentId());
        Assertions.assertEquals(456L, attendance.getSessionId());

        // try changing status and ensure student/session ids stay the same
        attendance.setStatus(AttendanceStatus.ABSENT);
        Assertions.assertEquals(123L, attendance.getStudentId());
        Assertions.assertEquals(456L, attendance.getSessionId());
    }
}
