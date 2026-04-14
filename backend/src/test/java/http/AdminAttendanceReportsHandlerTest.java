package http;

import backend.exception.ApiException;
import config.AttendanceSQL;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import security.JwtService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAttendanceReportsHandlerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AttendanceSQL attendanceSQL;

    @Mock
    private HttpExchange exchange;

    @Mock
    private BaseHandler.RequestContext ctx;

    @InjectMocks
    private AdminAttendanceReportsHandler handler;

    @Test
    void testHandleRequest_success() throws Exception {
        when(ctx.getClassId()).thenReturn(1L);
        when(ctx.getPeriod()).thenReturn("2026-Q1");
        when(ctx.getQuery("search", "")).thenReturn("");
        when(ctx.getQuery("lang", "en")).thenReturn("en");

        when(attendanceSQL.getAdminAttendanceReport(
                1L, "2026-Q1", "", "en"
        )).thenReturn(Collections.emptyList());

        try (MockedStatic<HttpUtil> mocked = mockStatic(HttpUtil.class)) {

            doNothing().when(handler).requireAdmin(exchange, ctx);

            handler.handleRequest(exchange, ctx);

            verify(attendanceSQL, times(1))
                    .getAdminAttendanceReport(1L, "2026-Q1", "", "en");

            mocked.verify(() ->
                    HttpUtil.json(exchange, 200, Collections.emptyList())
            );
        }
    }

    @Test
    void testHandleRequest_missingClassId_throwsException() {
        when(ctx.getClassId()).thenReturn(null);

        doNothing().when(handler).requireAdmin(exchange, ctx);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
        assertEquals("classId is required", ex.getMessage());
    }
}