package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StudentAttendanceFiltersHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;

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
                HttpUtil.json(ex, 405, Map.of("error", "Method Not Allowed"));
                return;
            }

            Long studentId = HttpUtil.jwtUserId(jwt);

            List<Map<String, Object>> classes = classSQL.listClassesForStudent(studentId);

            List<Map<String, String>> periods = List.of(
                    Map.of("value", "ALL", "label", "All Time"),
                    Map.of("value", "THIS_MONTH", "label", "This Month"),
                    Map.of("value", "LAST_MONTH", "label", "Last Month"),
                    Map.of("value", "THIS_YEAR", "label", "This Year")
            );

            HttpUtil.json(ex, 200, Map.of(
                    "classes", classes,
                    "periods", periods
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}