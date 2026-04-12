package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StudentAttendanceFiltersHandler extends BaseHandler implements HttpHandler {

    private final ClassSQL classSQL;
    private static final String LABEL = "label";
    private static final String VALUE = "value";

    public StudentAttendanceFiltersHandler(JwtService jwtService, ClassSQL classSQL) {
        super(jwtService, "GET");
        this.classSQL = classSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireStudent(ex, ctx);
        Long studentId = ctx.getUserId();

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
    }
}