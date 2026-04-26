package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

class StudentTeachersHandlerTest {

    private JwtService jwtService;
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
    }

    @Test
    void handleRequest_returns200AndTeachersList() throws IOException {
        // Arrange
        HttpExchange exchange = mock(HttpExchange.class);

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(responseBody);

        // 🔥 Create handler WITHOUT spy()
        StudentTeachersHandler handler = new StudentTeachersHandler(jwtService, userRepository) {
            private DecodedJWT jwt;

            @Override
            protected DecodedJWT requireAnyAuthenticated(HttpExchange ex, RequestContext ctx) {
                return mock(DecodedJWT.class); // correct return type
            }

            protected RequestContext buildContext(DecodedJWT jwt) {
                this.jwt = jwt;
                return mock(RequestContext.class);
            }
        };

        // Mock repository result
        List<Map<String, String>> teachers = Arrays.asList(
                Map.of("name", "Teacher1"),
                Map.of("name", "Teacher2")
        );

        when(userRepository.findAllTeachers()).thenReturn(teachers);

        // Act
        handler.handle(exchange);

        // Assert
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        verify(userRepository, times(1)).findAllTeachers();
    }
}
