package config;

import model.Session;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionSQLTest {

    private SessionSQL sessionSQL;
    private final long classId = 1; // must exist in DB

    @BeforeEach
    void setUp() {
        sessionSQL = new SessionSQL();
    }

    private LocalDate uniqueDate() {
        return LocalDate.now().plusDays((int) (System.nanoTime() % 20));
    }

    private LocalTime uniqueStart() {
        long n = System.nanoTime();
        int min = (int) (n % 50) + 5;
        int sec = (int) (n % 50) + 5;
        return LocalTime.of(9, min, sec);
    }

    private LocalTime plusOneHour(LocalTime start) {
        return start.plusHours(1);
    }

    @AfterEach
    void cleanup() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM sessions WHERE qr_token LIKE 'TEST-%'"
             )) {
            ps.executeUpdate();
        }
    }

    @Test
    void createSession_returnsGeneratedId() {
        LocalDate d = uniqueDate();
        LocalTime start = uniqueStart();
        LocalTime end = plusOneHour(start);

        long id = sessionSQL.createSession(classId, d, start, end, "TEST-CREATE-" + System.nanoTime());
        assertTrue(id > 0);
    }

    @Test
    void listForClass_containsCreatedSession() {
        LocalDate d = uniqueDate();
        LocalTime start = uniqueStart();
        LocalTime end = plusOneHour(start);

        String token = "TEST-LIST-" + System.nanoTime();
        long id = sessionSQL.createSession(classId, d, start, end, token);

        List<Map<String, Object>> list = sessionSQL.listForClass(classId);
        assertNotNull(list);

        boolean found = list.stream().anyMatch(m ->
                ((Long) m.get("id")) == id && token.equals(m.get("code"))
        );
        assertTrue(found);
    }

    @Test
    void findById_returnsSession() throws Exception {
        LocalDate d = uniqueDate();
        LocalTime start = uniqueStart();
        LocalTime end = plusOneHour(start);

        String token = "TEST-FIND-" + System.nanoTime();
        long id = sessionSQL.createSession(classId, d, start, end, token);

        Session s = sessionSQL.findById(id);
        assertNotNull(s);
        assertEquals(id, s.getId());
    }

    @Test
    void updateQRCode_updatesValue() throws Exception {
        LocalDate d = uniqueDate();
        LocalTime start = uniqueStart();
        LocalTime end = plusOneHour(start);

        long id = sessionSQL.createSession(classId, d, start, end, "TEST-OLD-" + System.nanoTime());

        String newToken = "TEST-NEW-" + System.nanoTime();
        sessionSQL.updateQRCode(id, newToken);

        Session updated = sessionSQL.findById(id);
        assertNotNull(updated);
    }
}