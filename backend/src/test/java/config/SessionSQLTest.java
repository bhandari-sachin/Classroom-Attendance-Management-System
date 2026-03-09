package config;

import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionSQLTest {

    private SessionSQL sessionSQL;
    private final long classId = 1;

    @BeforeEach
    void setUp() {
        sessionSQL = mock(SessionSQL.class);
    }

    @Test
    void createSession_returnsGeneratedId() {

        LocalDate d = LocalDate.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        when(sessionSQL.createSession(
                classId,
                d,
                start,
                end,
                "TEST-CREATE"
        )).thenReturn(123L);

        long id = sessionSQL.createSession(
                classId,
                d,
                start,
                end,
                "TEST-CREATE"
        );

        assertEquals(123L, id);
    }

    @Test
    void listForClass_containsMockedSession() {

        Map<String, Object> mockSession =
                Map.of("id", 10L, "code", "TEST-LIST");

        when(sessionSQL.listForClass(classId))
                .thenReturn(List.of(mockSession));

        List<Map<String, Object>> list = sessionSQL.listForClass(classId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("TEST-LIST", list.get(0).get("code"));
    }

    @Test
    void findById_returnsMockedSession() throws SQLException {

        Session s = mock(Session.class);

        when(s.getId()).thenReturn(55L);
        when(sessionSQL.findById(55L)).thenReturn(s);

        Session result = sessionSQL.findById(55L);

        assertNotNull(result);
        assertEquals(55L, result.getId());
    }

    @Test
    void updateQRCode_updatesMockedValue() {

        doNothing().when(sessionSQL).updateQRCode(99L, "NEW-TOKEN");

        // Call the method
        sessionSQL.updateQRCode(99L, "NEW-TOKEN");

        // Verify it was called
        verify(sessionSQL, times(1))
                .updateQRCode(99L, "NEW-TOKEN");
    }
}
