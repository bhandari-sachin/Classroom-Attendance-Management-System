package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StudentAttendanceFiltersHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private static final String ERROR = "error";
    private static final String LABEL = "label";
    private static final String VALUE = "value";

    public StudentAttendanceFiltersHandler(JwtService jwtService, ClassSQL classSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "STUDENT");

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of(ERROR, "Method Not Allowed"));
                return;
            }

            Long studentId = HttpUtil.jwtUserId(jwt);

            List<Map<String, Object>> classes = classSQL.listClassesForStudent(studentId);

            List<Map<String, String>> periods = List.of(
                    Map.of(VALUE, "ALL", LABEL, "All Time"),
                    Map.of(VALUE, "THIS_MONTH", LABEL, "This Month"),
                    Map.of(VALUE, "LAST_MONTH", LABEL, "Last Month"),
                    Map.of(VALUE, "THIS_YEAR", LABEL, "This Year")
            );

            HttpUtil.json(ex, 200, Map.of(
                    "classes", classes,
                    "periods", periods
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of(ERROR, se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of(ERROR, "Server error"));
        }
    }
}