package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import config.AttendanceSQL;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;
import util.CSVReportExporter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportHandlerTest {

    @Test
    void shouldReturn405_whenMethodIsNotGet() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        ReportHandler handler = new ReportHandler(jwtService, attendanceSQL);

        FakeExchange ex = new FakeExchange("POST", "/api/reports/export/admin", null);

        DecodedJWT jwt = mock(DecodedJWT.class);

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {
            authMock.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);

            handler.handle(ex);

            assertEquals(405, ex.statusCode);
        }
    }

    @Test
    void shouldHandleAdminCsvExport() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        ReportHandler handler = new ReportHandler(jwtService, attendanceSQL);

        FakeExchange ex = new FakeExchange(
                "GET",
                "/api/reports/export/admin?format=csv",
                null
        );

        DecodedJWT jwt = mock(DecodedJWT.class);

        try (
                MockedStatic<Auth> authMock = mockStatic(Auth.class);
                MockedStatic<CSVReportExporter> csvMock = mockStatic(CSVReportExporter.class)
        ) {
            // ✅ Auth passes
            authMock.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            authMock.when(() -> Auth.requireRole(jwt, "ADMIN"))
                    .thenAnswer(inv -> null);

            // ✅ Mock DB
            when(attendanceSQL.getAllStudentsStats()).thenReturn(List.of());

            // ✅ CRITICAL FIX: prevent real CSV writing
            csvMock.when(() ->
                    CSVReportExporter.adminAllStudentsReport(any(), any(), any())
            ).thenAnswer(inv -> null);

            handler.handle(ex);

            assertEquals(200, ex.statusCode);
            assertTrue(ex.getResponseHeaders()
                    .getFirst("content-type")
                    .contains("csv"));
        }
    }

    @Test
    void shouldReturn400_whenInvalidFormat() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        ReportHandler handler = new ReportHandler(jwtService, attendanceSQL);

        FakeExchange ex = new FakeExchange(
                "GET",
                "/api/reports/export/admin?format=xml",
                null
        );

        DecodedJWT jwt = mock(DecodedJWT.class);

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {
            authMock.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);

            handler.handle(ex);

            assertEquals(400, ex.statusCode);
        }
    }

    @Test
    void shouldReturn401_whenAuthFails() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        ReportHandler handler = new ReportHandler(jwtService, attendanceSQL);

        FakeExchange ex = new FakeExchange(
                "GET",
                "/api/reports/export/admin",
                null
        );

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {
            authMock.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Unauthorized"));

            handler.handle(ex);

            assertEquals(401, ex.statusCode);
        }
    }

    // reuse your existing FakeExchange
    static class FakeExchange extends AdminAttendanceReportsHandlerTest.FakeExchange {
        FakeExchange(String method, String path, String body) {
            super(method, path, body);
        }
    }
}