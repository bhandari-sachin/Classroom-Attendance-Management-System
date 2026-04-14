package http;

import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStatsHandlerTest {

    @Mock
    JwtService jwtService;

    @Mock
    AttendanceService attendanceService;

    @Mock
    HttpExchange exchange;

    @Mock
    BaseHandler.RequestContext ctx;

    @Test
    void testHandleRequest_success() throws IOException {

        AdminStatsHandler handler = new AdminStatsHandler(jwtService, attendanceService);

        AttendanceStats stats = new AttendanceStats(10, 2, 1, 13);

        when(attendanceService.getOverallStats()).thenReturn(stats);

        try (MockedStatic<HttpUtil> httpMock = mockStatic(HttpUtil.class)) {

            doNothing().when(handler).requireAdmin(exchange, ctx);

            handler.handle(exchange);

            verify(attendanceService, times(1)).getOverallStats();

            httpMock.verify(() ->
                    HttpUtil.json(eq(exchange), eq(200), eq(stats))
            );
        }
    }
}