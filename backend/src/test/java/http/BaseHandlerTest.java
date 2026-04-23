package http;

import backend.exception.ApiException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;
import java.io.*;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseHandlerTest {

    private JwtService jwtService;
    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;
    private TestHandler handler;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        handler = new TestHandler(jwtService);
    }

    // -------------------------------------------------------------
    // Concrete implementation (since BaseHandler is abstract)
    // -------------------------------------------------------------
    static class TestHandler extends BaseHandler {
        boolean called = false;

        public TestHandler(JwtService jwtService) {
            super(jwtService, "GET","POST");
        }

        @Override
        protected void handleRequest(HttpExchange ex, RequestContext ctx) {
            called = true;
        }
    }

    // -------------------------------------------------------------
    // TEST: RequestContext parsing
    // -------------------------------------------------------------
    @Test
    void requestContext_readsQueryParams() throws Exception {
        when(exchange.getRequestURI()).thenReturn(
                new URI("/test?classId=10&period=WEEK")
        );

        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(exchange);

        assertEquals(10L, ctx.getClassId());
        assertEquals("WEEK", ctx.getPeriod());
    }

    @Test
    void requestContext_missingParams_returnsNull() {
        when(exchange.getRequestURI()).thenReturn(URI.create("/test"));

        BaseHandler.RequestContext ctx =
                new BaseHandler.RequestContext(exchange);

        assertNull(ctx.getClassId());
        assertNull(ctx.getPeriod());
    }

    @Test
    void requestContext_getQuery_returnsDefault() {
        when(exchange.getRequestURI()).thenReturn(URI.create("/test"));

        BaseHandler.RequestContext ctx =
                new BaseHandler.RequestContext(exchange);

        assertEquals("DEFAULT", ctx.getQuery("missing", "DEFAULT"));
    }

    // -------------------------------------------------------------
    // TEST: authentication state in RequestContext
    // -------------------------------------------------------------
    @Test
    void requestContext_authenticationFlag() {
        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(exchange);

        assertFalse(ctx.isAuthenticated());
    }

    // -------------------------------------------------------------
    // TEST: methods
    // -------------------------------------------------------------
    @Test
    void testMethodNotAllowed_returns405() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("DELETE");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void testMethodNotAllowed_doesNotCallHandleRequest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("DELETE");

        handler.handle(exchange);

        assertFalse(handler.called);
        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void testAllowedMethod_callsHandleRequest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/test"));

        handler.handle(exchange);

        assertTrue(handler.called);
    }

    @Test
    void testApiException_returns400() throws Exception {
        handler = new TestHandler(jwtService) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new ApiException(400, "Bad request");
            }
        };

        when(exchange.getRequestMethod()).thenReturn("GET");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void testSecurityException_returns403() throws Exception {
        handler = new TestHandler(jwtService) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new SecurityException("Forbidden");
            }
        };

        when(exchange.getRequestMethod()).thenReturn("GET");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(403), anyLong());
    }

    @Test
    void testUnhandledException_returns500() throws Exception {
        handler = new TestHandler(jwtService) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new RuntimeException("Crash");
            }
        };

        when(exchange.getRequestMethod()).thenReturn("GET");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(500), anyLong());
    }

    @Test
    void requireRole_delegatesToAuth() {
        HttpExchange ex = mock(HttpExchange.class);
        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(ex);

        DecodedJWT jwt = mock(DecodedJWT.class);

        try (var mocked = mockStatic(security.Auth.class)) {

            mocked.when(() -> security.Auth.requireJwt(ex, jwtService))
                    .thenReturn(jwt);

            mocked.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.requireRole(ex, ctx, "ADMIN");

            mocked.verify(() -> security.Auth.requireJwt(ex, jwtService));
            mocked.verify(() -> security.Auth.requireRole(jwt, new String[]{"ADMIN"}));
        }
    }

    @Test
    void requireRole_setsJwtInContext() {
        HttpExchange ex = mock(HttpExchange.class);
        BaseHandler.RequestContext ctx = new BaseHandler.RequestContext(ex);

        DecodedJWT jwt = mock(DecodedJWT.class);

        try (var mocked = mockStatic(security.Auth.class)) {

            mocked.when(() -> security.Auth.requireJwt(ex, jwtService))
                    .thenReturn(jwt);

            mocked.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.requireRole(ex, ctx, "ADMIN");

            assertTrue(ctx.isAuthenticated());
        }
    }

    @Test
    void getUserId_withoutJwt_throws() {
        BaseHandler.RequestContext ctx =
                new BaseHandler.RequestContext(mock(HttpExchange.class));

        assertThrows(IllegalStateException.class, ctx::getUserId);
    }
}