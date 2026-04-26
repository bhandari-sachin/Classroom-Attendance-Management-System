package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;

import static org.mockito.Mockito.*;

class TeacherMarkAttendanceHandlerTest {

    private AttendanceService attendanceService;
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        attendanceService = mock(AttendanceService.class);
        jwtService = mock(JwtService.class);
    }

    @Test
    void markAttendance_present_returns200() throws IOException {

        // Fake JSON body
        String json = """
                {
                  "studentId": 1,
                  "sessionId": 10,
                  "status": "PRESENT"
                }
                """;

        TeacherClassesHandlerTest.FakeExchange ex = new TeacherClassesHandlerTest.FakeExchange("POST", "/teacher/attendance", json);
        ex.getRequestHeaders().add("Authorization", "Bearer valid-token");

        // Fake JWT
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn("TEACHER");
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        // Handler with overridden auth
        TeacherMarkAttendanceHandler handler =
                new TeacherMarkAttendanceHandler(jwtService, attendanceService) {

                    @Override
                    protected DecodedJWT requireAnyAuthenticated(HttpExchange ex, RequestContext ctx) {
                        return jwt;
                    }

                    @Override
                    protected DecodedJWT requireTeacher(HttpExchange ex, RequestContext ctx) {
                        // allow
                        return null;
                    }
                };

        handler.handle(ex);

        // Assert
        verify(attendanceService).markPresent(1L, 10L);
        org.junit.jupiter.api.Assertions.assertEquals(200, ex.getResponseCode());
    }
}
