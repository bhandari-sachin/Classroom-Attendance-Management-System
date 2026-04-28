package http;

import com.sun.net.httpserver.*;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginHandlerTest {

    // -------------------------------------------------------------
    // SUCCESS TEST (FIXED)
    // -------------------------------------------------------------
    @Test
    void login_success_returns200AndToken() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwtService);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1234";
        String hashed = encoder.encode(rawPassword);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("john@test.com");
        when(user.getPasswordHash()).thenReturn(hashed);
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");
        when(user.getUserType()).thenReturn(UserRole.STUDENT);

        when(repo.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtService.issueToken(1L, "john@test.com", "STUDENT"))
                .thenReturn("fake-token");

        FakeExchange ex = new FakeExchange("POST", "/login",
                "{\"email\":\"john@test.com\",\"password\":\"1234\"}");

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("fake-token"));
    }

    // -------------------------------------------------------------
    // INVALID CREDENTIALS
    // -------------------------------------------------------------
    @Test
    void login_invalidCredentials_returns401() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwtService);

        when(repo.findByEmail("john@test.com")).thenReturn(Optional.empty());

        FakeExchange ex = new FakeExchange("POST", "/login",
                "{\"email\":\"john@test.com\",\"password\":\"1234\"}");

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
    }

    // -------------------------------------------------------------
    // INVALID JSON
    // -------------------------------------------------------------
    @Test
    void login_invalidJson_returns400() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwtService);

        FakeExchange ex = new FakeExchange("POST", "/login", "NOT_JSON");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
    }

    // -------------------------------------------------------------
    // WRONG METHOD
    // -------------------------------------------------------------
    @Test
    void login_wrongMethod_returns405() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwtService);

        FakeExchange ex = new FakeExchange("GET", "/login", null);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
    }

    // -------------------------------------------------------------
    // Fake HttpExchange
    // -------------------------------------------------------------
    static class FakeExchange extends HttpExchange {

        private final Headers reqHeaders = new Headers();
        private final Headers respHeaders = new Headers();
        private final String method;
        private final URI uri;
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        int statusCode = -1;

        FakeExchange(String method, String path, String body) {
            this.method = method;
            this.uri = URI.create("http://localhost" + path);
            this.requestBody = body == null
                    ? InputStream.nullInputStream()
                    : new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        String responseBodyString() {
            return responseBody.toString(StandardCharsets.UTF_8);
        }

        @Override public Headers getRequestHeaders() { return reqHeaders; }
        @Override public Headers getResponseHeaders() { return respHeaders; }
        @Override public URI getRequestURI() { return uri; }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {     // No-op: not needed for unit testing
        }
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.statusCode = rCode;
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public int getResponseCode() { return statusCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {    // Not required for this test scenario
        }
        @Override public void setStreams(InputStream i, OutputStream o) {    // No-op: attributes not needed for testing
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}