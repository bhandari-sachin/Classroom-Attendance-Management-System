package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ApiException;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

abstract class BaseHandler implements HttpHandler {

    protected abstract void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException;

    private static final String ERROR = "error";
    private static final String TEACHER = "TEACHER";
    private static final String ADMIN = "ADMIN";
    protected final JwtService jwtService;
    private final String[] methods;

    protected BaseHandler(JwtService jwtService, String... methods) {
        this.jwtService = jwtService;
        this.methods = methods;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!isAllowedMethod(ex)) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            RequestContext ctx = new RequestContext(ex);

            handleRequest(ex, ctx);

        } catch (ApiException ae) {
            HttpUtil.json(ex, ae.getStatus(), Map.of(ERROR, ae.getMessage()));
        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of(ERROR, se.getMessage()));
        } catch (Exception e) {
            HttpUtil.json(ex, 500, Map.of(ERROR, e.getMessage()));
        }
    }

    private boolean isAllowedMethod(HttpExchange ex) {
        String reqMethod = ex.getRequestMethod();
        for (String m : methods) {
            if (m.equalsIgnoreCase(reqMethod)) {
                return true;
            }
        }
        return false;
    }

    protected DecodedJWT requireRole(HttpExchange ex, RequestContext ctx, String... roles) {
        DecodedJWT jwt = security.Auth.requireJwt(ex, jwtService);
        security.Auth.requireRole(jwt, roles);
        ctx.setJwt(jwt);
        return jwt;
    }

    protected DecodedJWT requireStudent(HttpExchange ex, RequestContext ctx) {
        return requireRole(ex, ctx, "STUDENT");
    }

    protected DecodedJWT requireTeacher(HttpExchange ex, RequestContext ctx) {
        return requireRole(ex, ctx, TEACHER);
    }

    protected DecodedJWT requireAdmin(HttpExchange ex, RequestContext ctx) {
        return requireRole(ex, ctx, ADMIN);
    }

    protected DecodedJWT requireAnyAuthenticated(HttpExchange ex, RequestContext ctx) {
        return requireRole(ex, ctx, "STUDENT", TEACHER, ADMIN);
    }

    protected DecodedJWT requireTeacherOrAdmin(HttpExchange ex, RequestContext ctx) {
        return requireRole(ex, ctx, TEACHER, ADMIN);
    }

    protected static class RequestContext {

        private final HttpExchange ex;
        private DecodedJWT jwt;

        public RequestContext(HttpExchange ex) {
            this.ex = ex;
        }

        void setJwt(DecodedJWT jwt) {
            this.jwt = jwt;
        }

        public DecodedJWT getJwt() {
            return jwt;
        }

        public boolean isAuthenticated() {
            return jwt != null;
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