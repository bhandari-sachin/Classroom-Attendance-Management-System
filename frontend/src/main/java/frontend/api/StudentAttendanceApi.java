package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

/**
 * API client for student attendance operations.
 */
public final class StudentAttendanceApi extends BaseApiClient {

    private final Paths paths;

    public StudentAttendanceApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public StudentAttendanceApi(String baseUrl, HttpClient client, ObjectMapper objectMapper, Paths paths) {
        super(baseUrl, client, objectMapper);

        if (paths == null) {
            throw new IllegalArgumentException("Paths must not be null.");
        }
        this.paths = paths;
    }

    public record Paths(
            String summaryPath,
            String recordsPath,
            String markAttendancePath
    ) {
        public Paths {
            requirePath(summaryPath, "summaryPath");
            requirePath(recordsPath, "recordsPath");
            requirePath(markAttendancePath, "markAttendancePath");
        }

        public static Paths defaults() {
            return new Paths(
                    "/api/student/attendance/summary",
                    "/api/student/attendance/records",
                    "/api/attendance/mark"
            );
        }
    }

    public Map<String, Object> getSummary(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readGet(paths.summaryPath(), jwtStore, state);
    }

    public List<Map<String, Object>> getRecords(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readList(paths.recordsPath(), jwtStore, state);
    }

    public void submitCode(String code, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Attendance code is required.");
        }

        postJson(paths.markAttendancePath(), Map.of("code", code.trim()), jwtStore, state);
    }
}