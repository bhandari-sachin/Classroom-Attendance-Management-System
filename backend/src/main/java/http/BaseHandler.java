package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

abstract class BaseHandler implements HttpHandler {

    protected abstract void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException;
    protected final JwtService jwtService;

    protected BaseHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static final String ERROR = "error";

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String reqMethod = ex.getRequestMethod();

            if (!supportsMethod(reqMethod)) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            String auth = ex.getRequestHeaders().getFirst("Authorization");

            if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }

            String token = auth.substring(7);

            if (token.isBlank()) {
                throw new SecurityException("Empty JWT token");
            }

            DecodedJWT jwt = security.Auth.requireJwt(ex, jwtService);
            security.Auth.requireRole(jwt, roles());
            RequestContext ctx = new RequestContext(ex, jwt);

            handleRequest(ex, ctx);

        } catch (ApiException ae) {
            HttpUtil.json(ex, ae.getStatus(), Map.of(ERROR, ae.getMessage()));
        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of(ERROR, se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of(ERROR, e.getMessage()));
        }
    }

    protected abstract String[] roles();
    protected abstract boolean supportsMethod(String method);

    protected static class RequestContext {

        private final HttpExchange ex;
        private final DecodedJWT jwt; // may be null

        public RequestContext(HttpExchange ex, DecodedJWT jwt) {
            this.ex = ex;
            this.jwt = jwt;
        }

        public DecodedJWT getJwt() {
            return jwt;
        }

        public Long getUserId() {
            if (jwt == null) {
                throw new IllegalStateException("No authenticated user");
            }
            return HttpUtil.jwtUserId(jwt);
        }

        public Long getClassId() {
            return queryLong("classId");
        }

        public String getPeriod() {
            return queryString("period");
        }

        public Long getLongQuery(String key) {
            return queryLong(key);
        }

        public String getQuery(String key, String defaultValue) {
            String value = queryString(key);
            return value != null ? value : defaultValue;
        }

        private String queryString(String key) {
            return HttpUtil.queryString(ex.getRequestURI().getQuery(), key);
        }

        private Long queryLong(String key) {
            return HttpUtil.queryLong(ex.getRequestURI().getQuery(), key);
        }
    }
}