package config;

import model.UserRole;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClassSQLTest {

    private final ClassSQL classSQL = new ClassSQL();

    private final List<Long> enrollmentIds = new ArrayList<>();
    private final List<Long> classIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // enrollments -> classes -> users
            if (!enrollmentIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM enrollments WHERE id = ?")) {
                    for (Long id : enrollmentIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }

            if (!classIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM classes WHERE id = ?")) {
                    for (Long id : classIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }

            if (!userIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    for (Long id : userIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void listAllForAdmin() throws Exception {
        long teacherId = insertUser(uniqueEmail("teacher"), "hash", "Tina", "Teach", "TEACHER", null);

        long classId = insertClass(uniqueClassCode(), "DB Admin View", teacherId, "SPRING", "2025/2026", 30);

        // 2 students enrolled so students_count = 2
        long s1 = insertUser(uniqueEmail("s1"), "hash", "A", "One", "STUDENT", "1001");
        long s2 = insertUser(uniqueEmail("s2"), "hash", "B", "Two", "STUDENT", "1002");
        insertEnrollment(s1, classId, "ACTIVE");
        insertEnrollment(s2, classId, "ACTIVE");

        List<ClassSQL.ClassView> list = classSQL.listAllForAdmin();
        assertNotNull(list);

        // find our class in list
        ClassSQL.ClassView found = list.stream()
                .filter(c -> c.id == classId)
                .findFirst()
                .orElse(null);

        assertNotNull(found);
        assertEquals("DB Admin View", found.name);
        assertEquals(teacherId, teacherId); // sanity
        assertEquals(2, found.studentsCount);
        assertNotNull(found.teacherEmail);
        assertFalse(found.teacherEmail.isBlank());
    }

    @Test
    void findTeacherIdByEmail() throws Exception {
        String teacherEmail = uniqueEmail("teacher");
        long teacherId = insertUser(teacherEmail, "hash", "Tom", "Teacher", "TEACHER", null);

        // same email but different role should not matter (unique email in schema, so we can't insert duplicate)
        Long got = classSQL.findTeacherIdByEmail(teacherEmail);

        assertNotNull(got);
        assertEquals(teacherId, got);
    }

    @Test
    void findTeacherIdByEmail_returnsNull_whenNotTeacher() throws Exception {
        String email = uniqueEmail("student");
        insertUser(email, "hash", "Sara", "Student", "STUDENT", "2001");

        Long got = classSQL.findTeacherIdByEmail(email);
        assertNull(got);
    }

    @Test
    void createClass_insertsRow_andReturnsId() throws Exception {
        long teacherId = insertUser(uniqueEmail("teacher"), "hash", "Tina", "Teach", "TEACHER", null);

        String code = uniqueClassCode();
        long newId = classSQL.createClass(code, "New Class", teacherId, "SPRING", "2025/2026", 25);

        assertTrue(newId > 0);
        classIds.add(newId); // ensure cleanup even if helper didn't insert via insertClass

        // verify via admin listing (simple smoke)
        var list = classSQL.listAllForAdmin();
        assertTrue(list.stream().anyMatch(c -> c.id == newId && code.equals(c.classCode)));
    }

    @Test
    void listForTeacher_returnsOnlyTeachersClasses() throws Exception {
        long t1 = insertUser(uniqueEmail("t1"), "hash", "A", "Teacher", "TEACHER", null);
        long t2 = insertUser(uniqueEmail("t2"), "hash", "B", "Teacher", "TEACHER", null);

        long c1 = insertClass(uniqueClassCode(), "T1 Class", t1, "SPRING", "2025/2026", 30);
        insertClass(uniqueClassCode(), "T2 Class", t2, "SPRING", "2025/2026", 30);

        List<Map<String, Object>> list = classSQL.listForTeacher(t1);

        assertNotNull(list);
        String asText = list.toString();
        assertTrue(asText.contains("T1 Class"));
        assertTrue(asText.contains(String.valueOf(c1)));
        assertFalse(asText.contains("T2 Class"));
    }

    @Test
    void isClassOwnedByTeacher_trueWhenOwned_falseOtherwise() throws Exception {
        long t1 = insertUser(uniqueEmail("t1"), "hash", "A", "Teacher", "TEACHER", null);
        long t2 = insertUser(uniqueEmail("t2"), "hash", "B", "Teacher", "TEACHER", null);

        long c1 = insertClass(uniqueClassCode(), "Owned", t1, "SPRING", "2025/2026", 30);

        assertTrue(classSQL.isClassOwnedByTeacher(c1, t1));
        assertFalse(classSQL.isClassOwnedByTeacher(c1, t2));
        assertFalse(classSQL.isClassOwnedByTeacher(99999999L, t1));
    }

    @Test
    void listStudentsForClass_returnsOnlyStudentsInThatClass() throws Exception {
        long teacherId = insertUser(uniqueEmail("teacher"), "hash", "Tina", "Teach", "TEACHER", null);
        long classId = insertClass(uniqueClassCode(), "Students List", teacherId, "SPRING", "2025/2026", 30);

        long s1 = insertUser(uniqueEmail("s1"), "hash", "Alice", "One", "STUDENT", "3001");
        long s2 = insertUser(uniqueEmail("s2"), "hash", "Bob", "Two", "STUDENT", "3002");
        long otherStudent = insertUser(uniqueEmail("s3"), "hash", "Chris", "Three", "STUDENT", "3003");
        long teacher2 = insertUser(uniqueEmail("t2"), "hash", "Not", "Student", "TEACHER", null);

        insertEnrollment(s1, classId, "ACTIVE");
        insertEnrollment(s2, classId, "ACTIVE");

        // enroll otherStudent into different class -> should not show
        long class2 = insertClass(uniqueClassCode(), "Other", teacherId, "SPRING", "2025/2026", 30);
        insertEnrollment(otherStudent, class2, "ACTIVE");

        // even if a teacher is in enrollments, query filters STUDENT, so should not show
        insertEnrollment(teacher2, classId, "ACTIVE");

        List<Map<String, Object>> students = classSQL.listStudentsForClass(classId);

        assertNotNull(students);
        assertEquals(2, students.size());

        String text = students.toString();
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("Bob"));
        assertFalse(text.contains("Chris"));
        assertFalse(text.contains("Not"));
    }

    @Test
    void countForTeacher_countsClasses() throws Exception {
        long teacherId = insertUser(uniqueEmail("teacher"), "hash", "Tina", "Teach", "TEACHER", null);

        int before = classSQL.countForTeacher(teacherId);

        insertClass(uniqueClassCode(), "C1", teacherId, "SPRING", "2025/2026", 30);
        insertClass(uniqueClassCode(), "C2", teacherId, "SPRING", "2025/2026", 30);

        int after = classSQL.countForTeacher(teacherId);

        assertEquals(before + 2, after);
    }

    @Test
    void countStudentsForTeacher_countsDistinctAcrossTeachersClasses() throws Exception {
        long teacherId = insertUser(uniqueEmail("teacher"), "hash", "Tina", "Teach", "TEACHER", null);

        long c1 = insertClass(uniqueClassCode(), "C1", teacherId, "SPRING", "2025/2026", 30);
        long c2 = insertClass(uniqueClassCode(), "C2", teacherId, "SPRING", "2025/2026", 30);

        long s1 = insertUser(uniqueEmail("s1"), "hash", "A", "One", "STUDENT", "7001");
        long s2 = insertUser(uniqueEmail("s2"), "hash", "B", "Two", "STUDENT", "7002");

        // s1 enrolled in BOTH classes, s2 only in one -> distinct = 2
        insertEnrollment(s1, c1, "ACTIVE");
        insertEnrollment(s1, c2, "ACTIVE");
        insertEnrollment(s2, c1, "ACTIVE");

        int count = classSQL.countStudentsForTeacher(teacherId);
        assertEquals(2, count);
    }

    // ---------------- helper inserts ----------------

    private long insertUser(String email, String hash, String first, String last, String role, String studentCode) throws Exception {
        String sql = """
            INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, first);
            ps.setString(4, last);
            ps.setString(5, role);
            ps.setString(6, studentCode);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for users insert");
                long id = keys.getLong(1);
                userIds.add(id);
                return id;
            }
        }
    }

    private long insertClass(String classCode, String name, long teacherId,
                             String semester, String academicYear, Integer maxCapacity) throws Exception {
        String sql = """
            INSERT INTO classes (class_code, name, teacher_id, semester, academic_year, max_capacity)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, classCode);
            ps.setString(2, name);
            ps.setLong(3, teacherId);
            ps.setString(4, semester);
            ps.setString(5, academicYear);
            if (maxCapacity == null) ps.setObject(6, null);
            else ps.setInt(6, maxCapacity);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for classes insert");
                long id = keys.getLong(1);
                classIds.add(id);
                return id;
            }
        }
    }

    private long insertEnrollment(long studentId, long classId, String status) throws Exception {
        String sql = """
            INSERT INTO enrollments (student_id, class_id, status)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.setString(3, status);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for enrollments insert");
                long id = keys.getLong(1);
                enrollmentIds.add(id);
                return id;
            }
        }
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    private static String uniqueClassCode() {
        return "CLS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}