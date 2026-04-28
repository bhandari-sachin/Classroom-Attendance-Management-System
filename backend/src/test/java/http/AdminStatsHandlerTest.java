package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import security.JwtService;
import service.AttendanceService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminStatsHandlerTest {

    private JwtService jwtService;
    private AttendanceService attendanceService;
    private AdminStatsHandler handler;
    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        attendanceService = mock(AttendanceService.class);

        handler = Mockito.spy(new AdminStatsHandler(jwtService, attendanceService));

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // -----------------------------------
    // Helpers
    // -----------------------------------

    private void request(String method, String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private String rawResponse() {
        return responseBody.toString(StandardCharsets.UTF_8);
    }

    // -----------------------------------
    // SUCCESS
    // -----------------------------------

    @Test
    void getStats_success_returns200AndStats() throws Exception {
        AttendanceStats stats = mock(AttendanceStats.class);

        doReturn(null).when(handler).requireAdmin(any(), any());
        when(attendanceService.getOverallStats()).thenReturn(stats);

        request("GET", "/api/admin/stats");
        handler.handle(exchange);
        verify(attendanceService).getOverallStats();
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }

    // -----------------------------------
    // METHOD VALIDATION
    // -----------------------------------

    @Test
    void wrongMethod_returns405() throws Exception {
        request("POST", "/api/admin/stats");

        handler.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(rawResponse().contains("Method Not Allowed"));
    }

    // -----------------------------------
    // NULL RESPONSE EDGE CASE
    // -----------------------------------

    @Test
    void serviceReturnsNull_stillReturns200() throws Exception {
        doReturn(null).when(handler).requireAdmin(any(), any());

        when(attendanceService.getOverallStats()).thenReturn(null);

        request("GET", "/api/admin/stats");
        handler.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }
}