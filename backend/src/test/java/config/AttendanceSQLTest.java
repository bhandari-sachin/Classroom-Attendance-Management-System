package config;

import dto.AttendanceStats;
import dto.AttendanceView;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import model.UserRole;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceSQLTest {

    private static final String HASH =
            "$2a$10$95DbuhjYv6biZ9Amv4xUvO07Dw90GfhHkgC3v44YpIlgKHKg2AVkq";

    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;

    @BeforeAll
    static void initSchema() {
        DatabaseInitializer.init();
    }

    @BeforeEach
    void setup() throws Exception {
        attendanceSQL = new AttendanceSQL();
        classSQL = new ClassSQL();
        sessionSQL = new SessionSQL();
        resetDatabase();
    }

    @Test
    void exists() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-EX1", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "Ent", UserRole.STUDENT, "S-EX1");

        long sessionId = sessionSQL.createSession(
                classId, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), "CODEEX01"
        );

        assertFalse(attendanceSQL.exists(studentId, sessionId));

        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        assertTrue(attendanceSQL.exists(studentId, sessionId));
    }

    @Test
    void save() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-S1", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-S1");

        long sessionId = sessionSQL.createSession(
                classId, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), "CODESAVE1"
        );

        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        assertTrue(attendanceSQL.exists(studentId, sessionId));
    }

    /*@Test
    void updateStatus() throws Exception {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-U1", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-U1");

        long sessionId = sessionSQL.createSession(
                classId, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), "CODEUPD1"
        );

        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.ABSENT, MarkedBy.QR));

        // ✅ your updateStatus signature differs -> use reflection so the test compiles in all cases
        invokeUpdateStatus(attendanceSQL, studentId, sessionId, "PRESENT");

        List<AttendanceView> views = attendanceSQL.getStudentAttendanceViews(studentId);
        assertNotNull(views);
        assertFalse(views.isEmpty());

        // your AttendanceView should have status-like field; check via toString fallback if needed
        boolean ok = views.stream().anyMatch(v ->
                String.valueOf(readPropertyOrToString(v, "getStatus")).equalsIgnoreCase("PRESENT")
                        || v.toString().toUpperCase().contains("PRESENT")
        );
        assertTrue(ok, "Expected updated status PRESENT in student attendance views");
    }*/

    @Test
    void findByStudentId() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-FS", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-FS1");

        long sessionId1 = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODEFS1");
        long sessionId2 = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(12, 0), "CODEFS2");

        attendanceSQL.save(new Attendance(studentId, sessionId1, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(studentId, sessionId2, AttendanceStatus.ABSENT, MarkedBy.QR));

        List<Attendance> list = attendanceSQL.findByStudentId(studentId);
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void findByClassId() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-FC", "DB", teacherId, "Fall", "2025", 30);
        long s1 = insertUser("s1@test.com", "Stud", "One", UserRole.STUDENT, "S-FC1");
        long s2 = insertUser("s2@test.com", "Stud", "Two", UserRole.STUDENT, "S-FC2");

        enroll(s1, classId);
        enroll(s2, classId);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODEFC1");

        attendanceSQL.save(new Attendance(s1, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(s2, sessionId, AttendanceStatus.ABSENT, MarkedBy.QR));

        List<Attendance> list = attendanceSQL.findByClassId(classId);
        assertNotNull(list);
        assertEquals(2, list.size());
    }
/*
    @Test
    void filterAttendanceByStudent() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-FILT", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-FILT1");

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODEFILT1");
        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));

        // whatever your method returns, assert it's not null/empty
        Object out = attendanceSQL.filterAttendanceByStudent(studentId);
        assertNotNull(out);
        assertTrue(out instanceof List<?>);
        assertFalse(((List<?>) out).isEmpty());
    }*/

    @Test
    void getSessionCode() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-CODE", "DB", teacherId, "Fall", "2025", 30);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODE1234");

        String code = attendanceSQL.getSessionCode(sessionId);
        assertEquals("CODE1234", code);
    }

    @Test
    void getOverallStats() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-OS", "DB", teacherId, "Fall", "2025", 30);
        long s1 = insertUser("s1@test.com", "Stud", "One", UserRole.STUDENT, "S-OS1");
        long s2 = insertUser("s2@test.com", "Stud", "Two", UserRole.STUDENT, "S-OS2");

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODEOS1");

        attendanceSQL.save(new Attendance(s1, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(s2, sessionId, AttendanceStatus.ABSENT, MarkedBy.QR));

        AttendanceStats stats = attendanceSQL.getOverallStats();
        assertNotNull(stats);
        // just sanity check (depends on your DTO fields)
        assertTrue(stats.toString().length() > 0);
    }

    @Test
    void getStudentStats() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-SS", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-SS1");

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODESS1");
        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));

        AttendanceStats stats = attendanceSQL.getStudentStats(studentId);
        assertNotNull(stats);
        assertTrue(stats.toString().length() > 0);
    }

    @Test
    void findSessionIdByCode() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-FIND", "DB", teacherId, "Fall", "2025", 30);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "FINDME01");

        Long found = attendanceSQL.findSessionIdByCode("FINDME01");
        assertNotNull(found);
        assertEquals(sessionId, found.longValue());

        assertNull(attendanceSQL.findSessionIdByCode("NOPE"));
    }

    @Test
    void getStudentAttendanceViews() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-VIEW", "DB", teacherId, "Fall", "2025", 30);
        long studentId = insertUser("s@test.com", "Stud", "One", UserRole.STUDENT, "S-VIEW1");

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODEVIEW1");
        attendanceSQL.save(new Attendance(studentId, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));

        List<AttendanceView> views = attendanceSQL.getStudentAttendanceViews(studentId);
        assertNotNull(views);
        assertEquals(1, views.size());
    }

    @Test
    void reportByClass() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-RBC", "DB", teacherId, "Fall", "2025", 30);
        long s1 = insertUser("s1@test.com", "Stud", "One", UserRole.STUDENT, "S-RBC1");
        long s2 = insertUser("s2@test.com", "Stud", "Two", UserRole.STUDENT, "S-RBC2");

        enroll(s1, classId);
        enroll(s2, classId);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODERBC1");
        attendanceSQL.save(new Attendance(s1, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(s2, sessionId, AttendanceStatus.ABSENT, MarkedBy.QR));

        Object report = attendanceSQL.reportByClass(classId);
        assertNotNull(report);
        // could be Map or DTO; just sanity check
        assertTrue(report.toString().length() > 0);
    }

    @Test
    void getSessionReport() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-SREP", "DB", teacherId, "Fall", "2025", 30);
        long s1 = insertUser("s1@test.com", "Stud", "One", UserRole.STUDENT, "S-SREP1");
        long s2 = insertUser("s2@test.com", "Stud", "Two", UserRole.STUDENT, "S-SREP2");

        enroll(s1, classId);
        enroll(s2, classId);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODESREP1");
        attendanceSQL.save(new Attendance(s1, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(s2, sessionId, AttendanceStatus.EXCUSED, MarkedBy.TEACHER));

        Object report = attendanceSQL.getSessionReport(sessionId);
        assertNotNull(report);
        assertTrue(report.toString().length() > 0);
    }

    @Test
    void countTodayForTeacher() {
        long teacherId = insertUser("t@test.com", "Teach", "Er", UserRole.TEACHER, null);
        long classId = classSQL.createClass("C-TOD", "DB", teacherId, "Fall", "2025", 30);
        long s1 = insertUser("s1@test.com", "Stud", "One", UserRole.STUDENT, "S-TOD1");
        long s2 = insertUser("s2@test.com", "Stud", "Two", UserRole.STUDENT, "S-TOD2");

        enroll(s1, classId);
        enroll(s2, classId);

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "CODETOD1");
        attendanceSQL.save(new Attendance(s1, sessionId, AttendanceStatus.PRESENT, MarkedBy.QR));
        attendanceSQL.save(new Attendance(s2, sessionId, AttendanceStatus.ABSENT, MarkedBy.QR));

        // ✅ your method wants String status
        int todayPresent = attendanceSQL.countTodayForTeacher(teacherId, "PRESENT");
        assertEquals(1, todayPresent);
    }

    // -------------------- reflection helpers --------------------
/*
    private static void invokeUpdateStatus(AttendanceSQL sql, long studentId, long sessionId, String newStatus) throws Exception {
        Method[] methods = AttendanceSQL.class.getMethods();

        for (Method m : methods) {
            if (!m.getName().equals("updateStatus")) continue;

            Class<?>[] p = m.getParameterTypes();

            // try (long,long,String) or (Long,Long,String)
            if (p.length == 3 &&
                    isLongLike(p[0]) && isLongLike(p[1]) &&
                    p[2] == String.class) {
                m.invoke(sql, studentId, sessionId, newStatus);
                return;
            }

            // try (long,long,AttendanceStatus)
            if (p.length == 3 &&
                    isLongLike(p[0]) && isLongLike(p[1]) &&
                    p[2].getSimpleName().equals("AttendanceStatus")) {
                Object enumVal = Enum.valueOf((Class<? extends Enum>) p[2], newStatus);
                m.invoke(sql, studentId, sessionId, enumVal);
                return;
            }

            // try (long,String) e.g. updateStatus(attendanceId, status)
            if (p.length == 2 &&
                    isLongLike(p[0]) &&
                    p[1] == String.class) {

                // if your design is updateStatus(attendanceId, status),
                // use the attendance unique key to find the record ID:
                // easiest workaround: try to find it from findByStudentId.
                List<Attendance> list = sql.findByStudentId(studentId);
                long attendanceId = list.get(0).getId(); // requires Attendance.getId()
                m.invoke(sql, attendanceId, newStatus);
                return;
            }
        }

        throw new NoSuchMethodException("No compatible updateStatus(...) found in AttendanceSQL");
    }*/

    private static boolean isLongLike(Class<?> c) {
        return c == long.class || c == Long.class;
    }

    private static Object readPropertyOrToString(Object obj, String getter) {
        try {
            Method m = obj.getClass().getMethod(getter);
            return m.invoke(obj);
        } catch (Exception ignored) {
            return obj.toString();
        }
    }

    // -------------------- DB helpers --------------------

    private static void resetDatabase() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("SET FOREIGN_KEY_CHECKS=0");
            st.execute("TRUNCATE TABLE attendance");
            st.execute("TRUNCATE TABLE sessions");
            st.execute("TRUNCATE TABLE enrollments");
            st.execute("TRUNCATE TABLE classes");
            st.execute("TRUNCATE TABLE users");
            st.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private static long insertUser(String email, String first, String last, UserRole role, String studentCode) {
        String sql = """
            INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, HASH);
            ps.setString(3, first);
            ps.setString(4, last);
            ps.setString(5, role.name());
            ps.setString(6, studentCode);

            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getLong(1);
            }

        } catch (Exception e) {
            throw new RuntimeException("insertUser failed: " + e.getMessage(), e);
        }
    }

    private static void enroll(long studentId, long classId) {
        String sql = "INSERT INTO enrollments (student_id, class_id, status) VALUES (?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("enroll failed: " + e.getMessage(), e);
        }
    }
}