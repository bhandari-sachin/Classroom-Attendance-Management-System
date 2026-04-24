package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.*;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.JwtService;
import http.BaseHandler.RequestContext;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUsersHandlerTest {

    // -------------------------------------------------------------
    // TEST
    // -------------------------------------------------------------
    @Test
    void getUsers_returns200AndList() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);

        AdminUsersHandler handler = new AdminUsersHandler(repo, jwtService);
        AdminUsersHandler spyHandler = spy(handler);

        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("GET", "/admin/users", null);
        ex.getRequestHeaders().add("Authorization", "Bearer test-token");

        // ---------------------------------------------------------
        // ✅ INLINE JWT MOCK (FIXED — NO HELPER METHOD)
        // ---------------------------------------------------------
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(role);

        when(jwtService.verify("test-token")).thenReturn(jwt);

        RequestContext ctx = mock(RequestContext.class);

        // ---------------------------------------------------------
        // USER MOCK
        // ---------------------------------------------------------
        User u = mock(User.class);
        when(u.getFirstName()).thenReturn("john");
        when(u.getLastName()).thenReturn("doe");
        when(u.getEmail()).thenReturn("john@example.com");
        when(u.getUserType()).thenReturn(UserRole.STUDENT);

        when(repo.findAll()).thenReturn(List.of(u));

        when(repo.countByRole(UserRole.STUDENT)).thenReturn(1);
        when(repo.countByRole(UserRole.TEACHER)).thenReturn(0);
        when(repo.countByRole(UserRole.ADMIN)).thenReturn(0);

        spyHandler.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();
        assertFalse(body.isBlank());
        assertTrue(body.contains("john@example.com"));
        assertTrue(body.contains("students"));
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
        @Override public void close() {
            // No-op for testing
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
        @Override public void setAttribute(String name, Object value) {
            // No-op for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {
            // No-op for testing
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}