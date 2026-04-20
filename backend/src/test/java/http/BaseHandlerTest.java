package http;

import com.sun.net.httpserver.*;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseHandlerTest {

    // -------------------------------------------------------------
    // Concrete implementation (since BaseHandler is abstract)
    // -------------------------------------------------------------
    static class TestHandler extends BaseHandler {
        public TestHandler(JwtService jwtService) {
            super(jwtService, "GET");
        }

        @Override
        protected void handleRequest(HttpExchange ex, RequestContext ctx) {
            try {
                requireAdmin(ex, ctx);
                HttpUtil.json(ex, 200, "OK");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // -------------------------------------------------------------
    // TEST: method allowed + admin access
    // -------------------------------------------------------------
    @Test
    void handle_validRequest_callsRequireAdmin() {

        JwtService jwtService = mock(JwtService.class);
        TestHandler handler = new TestHandler(jwtService);

        TestHandler spy = spy(handler);

        doReturn(null).when(spy).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("GET", "/test", null);

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        spy.handleRequest(ex, ctx);

        verify(spy).requireAdmin(ex, ctx);
    }

    // -------------------------------------------------------------
    // TEST: RequestContext parsing
    // -------------------------------------------------------------
    @Test
    void requestContext_readsQueryParams() {


        FakeExchange ex = new FakeExchange(
                "GET",
                "/test?classId=10&period=WEEK",
                null
        );

        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(ex);

        assertEquals(10L, ctx.getClassId());
        assertEquals("WEEK", ctx.getPeriod());
    }

    // -------------------------------------------------------------
    // TEST: authentication state in RequestContext
    // -------------------------------------------------------------
    @Test
    void requestContext_authenticationFlag() {


        FakeExchange ex = new FakeExchange("GET", "/test", null);

        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(ex);

        assertFalse(ctx.isAuthenticated());
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
        @Override public void setAttribute(String name, Object value) {    // No-op: attributes not needed for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {    // Not used in this fake exchange
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}