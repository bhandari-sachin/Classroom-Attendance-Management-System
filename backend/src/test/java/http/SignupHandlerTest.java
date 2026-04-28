package http;

import com.sun.net.httpserver.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupHandlerTest {

    // -------------------------------------------------------------
    // SUCCESS CASE
    // -------------------------------------------------------------
    @Test
    void signup_success_returns201() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        when(repo.existsByEmail("john@test.com")).thenReturn(false);

        FakeExchange ex = new FakeExchange("POST", "/signup",
                """
                {
                  "email": "john@test.com",
                  "password": "1234",
                  "firstName": "John",
                  "lastName": "Doe",
                  "role": "STUDENT",
                  "studentCode": "S123"
                }
                """);

        handler.handle(ex);

        verify(repo).insert(
                eq("john@test.com"),
                anyString(), // bcrypt hash
                eq("John"),
                eq("Doe"),
                eq("STUDENT"),
                eq("S123")
        );

        assertEquals(201, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("created"));
    }

    // -------------------------------------------------------------
    // INVALID JSON
    // -------------------------------------------------------------
    @Test
    void signup_invalidJson_returns400() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        FakeExchange ex = new FakeExchange("POST", "/signup", "NOT_JSON");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid JSON"));
    }

    // -------------------------------------------------------------
    // MISSING REQUIRED FIELDS
    // -------------------------------------------------------------
    @Test
    void signup_missingFields_returns400() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        FakeExchange ex = new FakeExchange("POST", "/signup",
                "{\"email\":\"a@test.com\"}");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("required"));
    }

    // -------------------------------------------------------------
    // INVALID ROLE
    // -------------------------------------------------------------
    @ParameterizedTest
    @MethodSource("invalidSignupCases")
    void signup_invalidInputs_returnExpectedStatus(String requestBody, int expectedStatus) throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        FakeExchange ex = new FakeExchange("POST", "/signup", requestBody);

        handler.handle(ex);

        assertEquals(expectedStatus, ex.statusCode);
    }

    private static Stream<Arguments> invalidSignupCases() {
        return Stream.of(
                Arguments.of(
                        """
                        {
                          "email": "a@test.com",
                          "password": "1234",
                          "firstName": "A",
                          "lastName": "B",
                          "role": "INVALID"
                        }
                        """,
                        400
                ),
                Arguments.of(
                        """
                        {
                          "email": "admin@test.com",
                          "password": "1234",
                          "firstName": "A",
                          "lastName": "B",
                          "role": "ADMIN"
                        }
                        """,
                        403
                )
        );
    }

    // -------------------------------------------------------------
    // STUDENT WITHOUT CODE
    // -------------------------------------------------------------
    @Test
    void signup_studentMissingCode_returns400() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        FakeExchange ex = new FakeExchange("POST", "/signup",
                """
                {
                  "email": "s@test.com",
                  "password": "1234",
                  "firstName": "S",
                  "lastName": "T",
                  "role": "STUDENT"
                }
                """);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
    }

    // -------------------------------------------------------------
    // EMAIL EXISTS
    // -------------------------------------------------------------
    @Test
    void signup_emailExists_returns400() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        when(repo.existsByEmail("john@test.com")).thenReturn(true);

        FakeExchange ex = new FakeExchange("POST", "/signup",
                """
                {
                  "email": "john@test.com",
                  "password": "1234",
                  "firstName": "John",
                  "lastName": "Doe",
                  "role": "STUDENT",
                  "studentCode": "S1"
                }
                """);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
    }

    // -------------------------------------------------------------
    // WRONG METHOD
    // -------------------------------------------------------------
    @Test
    void signup_wrongMethod_returns405() throws Exception {

        UserRepository repo = mock(UserRepository.class);
        SignupHandler handler = new SignupHandler(repo);

        FakeExchange ex = new FakeExchange("GET", "/signup", null);

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
        @Override public void setAttribute(String name, Object value) {     // No-op: attributes not needed for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {     // Not used in this fake exchange
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}