package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lowagie.text.DocumentException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import security.Auth;
import security.JwtService;
import util.PDFReportExporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportHandlerTest {

    private JwtService jwtService;
    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;
    private ReportHandler handler;
    private HttpExchange exchange;

    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        attendanceSQL = mock(AttendanceSQL.class);
        classSQL = mock(ClassSQL.class);

        handler = Mockito.spy(new ReportHandler(jwtService, attendanceSQL));

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // ---------------------------------------
    // Helpers
    // ---------------------------------------

    private void request(String method, String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private void request(String method, String path, String query) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path + "?" + query));
    }

    private DecodedJWT mockJwt(Long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(id);

        return jwt;
    }

    // ---------------------------------------
    // METHOD VALIDATION
    // ---------------------------------------

    @Test
    void wrongMethod_returns405() throws Exception {
        request("POST", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Method Not Allowed"));
    }

    // ---------------------------------------
    // STUDENT
    // ---------------------------------------

    @Test
    void student_pdf_success() throws Exception {
        request("GET", "/api/reports/export/student");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(10L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "STUDENT"))
                    .thenAnswer(i -> null);

            doNothing().when(handler)
                    .sendFile(any(), anyString(), anyString(), anyString());

            when(attendanceSQL.getStudentYearlyReport(anyLong(), anyInt()))
                    .thenReturn(List.of());

            handler.handle(exchange);

            verify(attendanceSQL).getStudentYearlyReport(eq(10L), anyInt());
        }
    }

    @Test
    void student_pdf_export_throws_covers_catch() throws Exception {
        request("GET", "/api/reports/export/student");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class);
             MockedStatic<PDFReportExporter> pdf = mockStatic(PDFReportExporter.class)) {

            DecodedJWT jwt = mockJwt(10L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(attendanceSQL.getStudentYearlyReport(anyLong(), anyInt()))
                    .thenReturn(List.of());

            pdf.when(() ->
                    PDFReportExporter.studentYearReport(anyString(), anyInt(), anyList(), anyString())
            ).thenThrow(new IOException("boom"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(500), anyLong());
    }

    @Test
    void student_csv_notAllowed_returns400() throws Exception {
        request("GET", "/api/reports/export/student", "format=csv");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "STUDENT"))
                    .thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("only export PDF"));
    }

    // ---------------------------------------
    // TEACHER
    // ---------------------------------------

    @Test
    void teacher_csv_success() throws Exception {
        request("GET", "/api/reports/export/teacher", "classId=1&format=csv");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "TEACHER"))
                    .thenAnswer(i -> null);

            when(attendanceSQL.getTeacherClassReport(anyLong(), anyLong(), anyInt()))
                    .thenReturn(List.of());

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }

    @Test
    void teacher_pdf_success() throws Exception {
        request("GET", "/api/reports/export/teacher?classId=1&format=pdf");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class);
             MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {

            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(eq(jwt), anyString())).thenAnswer(i -> null);

            when(attendanceSQL.getTeacherClassReport(anyLong(), anyLong(), anyInt()))
                    .thenReturn(List.of());

            doNothing().when(handler)
                    .sendFile(any(), anyString(), anyString(), anyString());

            handler.handle(exchange);

            verify(attendanceSQL).getTeacherClassReport(eq(5L), eq(1L), anyInt());
        }
    }

    @Test
    void teacher_pdf_export_throws_covers_catch() throws Exception {
        request("GET", "/api/reports/export/teacher?classId=1&format=pdf");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class);
             MockedStatic<PDFReportExporter> pdf = mockStatic(PDFReportExporter.class)) {

            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(attendanceSQL.getTeacherClassReport(anyLong(), anyLong(), anyInt()))
                    .thenReturn(List.of());

            pdf.when(() ->
                    PDFReportExporter.teacherClassReport(anyString(), anyInt(), anyList(), anyString())
            ).thenThrow(new IOException("boom"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(500), anyLong());
    }

    @Test
    void teacher_missingClassId_returns400() throws Exception {
        request("GET", "/api/reports/export/teacher");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(5L);
            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "TEACHER"))
                    .thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Missing required query parameter"));
    }

    @Test
    void teacher_invalidClassId_returns400() throws Exception {
        request("GET", "/api/reports/export/teacher", "classId=abc");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "TEACHER"))
                    .thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Invalid numeric value"));
    }

    @Test
    void teacher_forbidden_returns403() throws Exception {

        request("GET",
                "/api/reports/export/teacher",
                "classId=1&format=pdf");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "TEACHER"))
                    .thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(1L, 5L))
                    .thenReturn(false);

            handler.handle(exchange);

            verify(attendanceSQL).getTeacherClassReport(eq(5L), eq(1L), anyInt());
        }
    }

    @Test
    void teacher_invalid_year_throws400() throws Exception {
        request("GET", "/api/reports/export/teacher?year=abc");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(eq(jwt), anyString())).thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
    }

    // ---------------------------------------
    // ADMIN
    // ---------------------------------------

    @Test
    void admin_pdf_success() throws Exception {
        request("GET", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "ADMIN"))
                    .thenAnswer(i -> null);


            doNothing().when(handler)
                    .sendFile(any(), anyString(), anyString(), anyString());

            when(attendanceSQL.getAllStudentsStats())
                    .thenReturn(List.of());

            handler.handle(exchange);

            verify(attendanceSQL).getAllStudentsStats();
        }
    }

    @Test
    void admin_pdf_export_throws_covers_catch() throws Exception {
        request("GET", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class);
             MockedStatic<PDFReportExporter> pdf = mockStatic(PDFReportExporter.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(attendanceSQL.getAllStudentsStats())
                    .thenReturn(List.of());

            pdf.when(() ->
                    PDFReportExporter.adminAllStudentsReport(anyString(), anyList(), anyString())
            ).thenThrow(new DocumentException("boom"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(500), anyLong());
    }

    @Test
    void admin_csv_success() throws Exception {
        request("GET", "/api/reports/export/admin", "format=csv");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "ADMIN"))
                    .thenAnswer(i -> null);

            when(attendanceSQL.getAllStudentsStats())
                    .thenReturn(List.of());

            handler.handle(exchange);

            verify(attendanceSQL).getAllStudentsStats();
        }
    }

    // ---------------------------------------
    // FORMAT VALIDATION
    // ---------------------------------------

    @Test
    void invalidFormat_returns400() throws Exception {
        request("GET", "/api/reports/export/admin", "format=xml");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "ADMIN"))
                    .thenAnswer(i -> null);


            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Unsupported format"));
    }

    // ---------------------------------------
    // AUTH FAILURE
    // ---------------------------------------

    @Test
    void authFails_returns401() throws Exception {
        request("GET", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Invalid token"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Invalid token"));
    }

    // ---------------------------------------
    // UNKNOWN ROUTE
    // ---------------------------------------

    @Test
    void unknownRoute_returns404() throws Exception {
        request("GET", "/api/reports/export/unknown");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
    }

    @Test
    void unexpectedException_returns500() throws Exception {
        request("GET", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(exchange, jwtService))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(jwt, "ADMIN"))
                    .thenAnswer(i -> null);

            when(attendanceSQL.getAllStudentsStats())
                    .thenThrow(new RuntimeException("DB crash"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(500), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Server error"));
    }

    @Test
    void wrong_method_returns405() throws Exception {
        request("POST", "/api/reports/export/admin");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }
}