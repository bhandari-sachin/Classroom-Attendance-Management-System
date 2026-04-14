package http;

import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceView;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.*;

class StudentAttendanceRecordsHandlerTest {

    private HttpExchange mockExchange() throws Exception {
        HttpExchange ex = mock(HttpExchange.class);

        when(ex.getRequestMethod()).thenReturn("GET");
        when(ex.getRequestURI()).thenReturn(
                URI.create("/student/attendance?classId=1&period=2024&lang=en")
        );

        when(ex.getRequestBody())
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(ex.getResponseBody()).thenReturn(out);

        when(ex.getResponseHeaders())
                .thenReturn(new com.sun.net.httpserver.Headers());

        doNothing().when(ex).sendResponseHeaders(anyInt(), anyLong());
        doNothing().when(ex).close();

        return ex;
    }

    @Test
    void success_returnsAttendanceRecords() throws Exception {

        JwtService jwt = mock(JwtService.class);
        AttendanceService service = mock(AttendanceService.class);

        StudentAttendanceRecordsHandler handler =
                new StudentAttendanceRecordsHandler(jwt, service);

        HttpExchange ex = mockExchange();

        when(service.getStudentAttendanceViews(
                anyLong(), anyLong(), any(), anyString()
        )).thenReturn(List.of(mock(AttendanceView.class)));

        handler.handle(ex);

        verify(service).getStudentAttendanceViews(
                anyLong(), anyLong(), any(), anyString()
        );

        verify(ex).sendResponseHeaders(eq(200), anyLong());
    }
}