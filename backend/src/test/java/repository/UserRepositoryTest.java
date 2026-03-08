package repository;

import config.DatabaseConnection;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private final UserRepository repo = new UserRepository();

    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

            for (Long id : userIds) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
    }

    @Test
    void findByEmail_returnsUser_whenExists() throws Exception {
        String email = uniqueEmail("find");
        long id = insertUserRaw(email, "hash", "A", "B", "STUDENT", "1001");

        Optional<User> found = repo.findByEmail(email);

        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(email, found.get().getEmail());
        assertEquals(UserRole.STUDENT, found.get().getUserType());
        assertEquals("1001", found.get().getStudentCode());
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        Optional<User> found = repo.findByEmail(uniqueEmail("missing"));
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmail_true_whenExists_falseWhenMissing() throws Exception {
        String email = uniqueEmail("exists");
        insertUserRaw(email, "hash", "A", "B", "TEACHER", null);

        assertTrue(repo.existsByEmail(email));
        assertFalse(repo.existsByEmail(uniqueEmail("nope")));
    }

    @Test
    void save_insertsUserRow() {
        String email = uniqueEmail("save");

        User u = new User(
                0L,
                email,
                "hash",
                "First",
                "Last",
                UserRole.TEACHER,
                null
        );

        repo.save(u);

        // verify inserted by fetching it
        Optional<User> found = repo.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
        assertEquals(UserRole.TEACHER, found.get().getUserType());

        // track id for cleanup
        userIds.add(found.get().getId());
    }

    @Test
    void insert_insertsUserRow() {
        String email = uniqueEmail("insert");

        repo.insert(email, "hash", "Jane", "Doe", "STUDENT", "2001");

        Optional<User> found = repo.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
        assertEquals(UserRole.STUDENT, found.get().getUserType());
        assertEquals("2001", found.get().getStudentCode());

        userIds.add(found.get().getId());
    }

    @Test
    void countByRole_countsOnlyThatRole() throws Exception {
        int beforeTeachers = repo.countByRole(UserRole.TEACHER);

        insertUserRaw(uniqueEmail("t1"), "hash", "T", "One", "TEACHER", null);
        insertUserRaw(uniqueEmail("t2"), "hash", "T", "Two", "TEACHER", null);
        insertUserRaw(uniqueEmail("s1"), "hash", "S", "One", "STUDENT", "3001");

        int afterTeachers = repo.countByRole(UserRole.TEACHER);

        assertEquals(beforeTeachers + 2, afterTeachers);
    }

    @Test
    void findAll_returnsListContainingInsertedUsers() throws Exception {
        String e1 = uniqueEmail("all1");
        String e2 = uniqueEmail("all2");

        long id1 = insertUserRaw(e1, "hash", "A", "One", "ADMIN", null);
        long id2 = insertUserRaw(e2, "hash", "B", "Two", "STUDENT", "4001");

        List<User> all = repo.findAll();

        assertNotNull(all);
        Set<Long> ids = new HashSet<>();
        for (User u : all) ids.add(u.getId());

        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }

    @Test
    void findAllTeachers_returnsOnlyTeachers_withTeacherNameAndEmail() throws Exception {
        String t1 = uniqueEmail("teach1");
        String t2 = uniqueEmail("teach2");
        String s1 = uniqueEmail("stud1");

        insertUserRaw(t1, "hash", "Alice", "Zeus", "TEACHER", null);
        insertUserRaw(t2, "hash", "Bob", "Alpha", "TEACHER", null);
        insertUserRaw(s1, "hash", "Not", "Teacher", "STUDENT", "5001");

        List<Map<String, String>> teachers = repo.findAllTeachers();

        assertNotNull(teachers);
        // should include both teacher emails, not student email
        String joined = teachers.toString();
        assertTrue(joined.contains(t1));
        assertTrue(joined.contains(t2));
        assertFalse(joined.contains(s1));

        // check required keys exist
        Map<String, String> first = teachers.get(0);
        assertTrue(first.containsKey("teacherName"));
        assertTrue(first.containsKey("email"));
    }

    // ---------- helper inserts (raw SQL so we can always cleanup) ----------

    private long insertUserRaw(String email, String hash, String first, String last, String role, String studentCode) throws Exception {
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
                assertTrue(keys.next(), "No generated key returned");
                long id = keys.getLong(1);
                userIds.add(id);
                return id;
            }
        }
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }
}