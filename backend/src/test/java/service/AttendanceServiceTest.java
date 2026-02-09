package service;

import config.AttendanceSQL;
import dto.AttendanceView;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AttendanceServiceTest {

    static class StubAttendanceSQL extends AttendanceSQL {
        List<Attendance> saved = new ArrayList<>();
        String sessionCode = null;
        List<Attendance> findByStudentResult = null;
        List<AttendanceView> filterResult = null;

        @Override
        public void save(Attendance attendance) {
            saved.add(attendance);
        }

        @Override
        public String getSessionCode(Long sessionId) {
            return sessionCode;
        }

        @Override
        public List<Attendance> findByStudentId(Long studentId) {
            return findByStudentResult;
        }

        @Override
        public List<AttendanceView> filterAttendanceByStudent(Long classId, String searchTerm) {
            return filterResult;
        }
    }

    @Test
    void markPresent_savesAttendanceWithTeacherPresent() {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        AttendanceService svc = new AttendanceService(stub);

        svc.markPresent(10L, 20L);

        assertEquals(1, stub.saved.size());
        Attendance saved = stub.saved.get(0);
        assertEquals(10L, saved.getStudentId());
        assertEquals(20L, saved.getSessionId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
        assertEquals(MarkedBy.TEACHER, saved.getMarkedBy());
    }

    @Test
    void submitAttendanceCode_success_whenCodeMatches_savesWithQRAndReturnsTrue() {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        stub.sessionCode = "correct-code";
        AttendanceService svc = new AttendanceService(stub);

        boolean result = svc.submitAttendanceCode(7L, 42L, "correct-code");

        assertTrue(result);
        assertEquals(1, stub.saved.size());
        Attendance saved = stub.saved.get(0);
        assertEquals(7L, saved.getStudentId());
        assertEquals(42L, saved.getSessionId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
        assertEquals(MarkedBy.QR, saved.getMarkedBy());
    }

    @Test
    void submitAttendanceCode_failsWhenCodeDoesNotMatch_andDoesNotSave() {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        stub.sessionCode = "server-code";
        AttendanceService svc = new AttendanceService(stub);

        boolean result = svc.submitAttendanceCode(1L, 100L, "bad-code");

        assertFalse(result);
        assertTrue(stub.saved.isEmpty());
    }

    @Test
    void getAttendanceForStudent_delegatesToSql() {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        List<Attendance> expected = Arrays.asList(new Attendance(1L, 2L, AttendanceStatus.PRESENT, MarkedBy.TEACHER));
        stub.findByStudentResult = expected;

        AttendanceService svc = new AttendanceService(stub);
        List<Attendance> res = svc.getAttendanceForStudent(1L);

        assertSame(expected, res);
    }

    @Test
    void filterAttendance_delegatesToSql() {
        StubAttendanceSQL stub = new StubAttendanceSQL();
        List<AttendanceView> expected = Arrays.asList(new AttendanceView(1L, "A", LocalDate.now(), "PRESENT"));
        stub.filterResult = expected;

        AttendanceService svc = new AttendanceService(stub);
        List<AttendanceView> res = svc.filterAttendance(5L, "term");

        assertSame(expected, res);
    }
}
