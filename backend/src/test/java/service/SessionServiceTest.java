package service;

import config.SessionSQL;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import util.QRCodeGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Test
    void startSessionShouldGenerateCodeAndUpdateDatabase() {
        SessionSQL sessionSQL = mock(SessionSQL.class);
        SessionService service = new SessionService(sessionSQL);

        try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
            mocked.when(QRCodeGenerator::generate).thenReturn("TEST_CODE");
            String result = service.startSession(10L);
            assertEquals("TEST_CODE", result);
            verify(sessionSQL).updateQRCode(10L, "TEST_CODE");
        }
    }
}