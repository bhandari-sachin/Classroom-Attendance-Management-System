package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

/**
 * API client for teacher-related operations.
 */
public final class TeacherApi extends BaseApiClient {

    private static final String QUERY_PREFIX = "?";
    private static final String CLASS_ID_PARAM = "classId=";
    private static final String SESSION_ID_PARAM = "sessionId=";

    private final Paths paths;

    public TeacherApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public TeacherApi(String baseUrl, HttpClient client, ObjectMapper objectMapper, Paths paths) {
        super(baseUrl, client, objectMapper);

        if (paths == null) {
            throw new IllegalArgumentException("Paths must not be null.");
        }
        this.paths = paths;
    }

    public record Paths(
            String classesPath,
            String studentsPath,
            String sessionsPath,
            String sessionReportPath,
            String dashboardStatsPath,
            String markAttendancePath
    ) {
        public Paths {
            requirePath(classesPath, "classesPath");
            requirePath(studentsPath, "studentsPath");
            requirePath(sessionsPath, "sessionsPath");
            requirePath(sessionReportPath, "sessionReportPath");
            requirePath(dashboardStatsPath, "dashboardStatsPath");
            requirePath(markAttendancePath, "markAttendancePath");
        }

        public static Paths defaults() {
            return new Paths(
                    "/api/teacher/classes",
                    "/api/teacher/students",
                    "/api/teacher/sessions",
                    "/api/teacher/reports/session",
                    "/api/teacher/dashboard/stats",
                    "/api/teacher/attendance/mark"
            );
        }
    }

    public List<Map<String, Object>> getMyClasses(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readWrappedList(paths.classesPath(), jwtStore, state);
    }

    public List<Map<String, Object>> getStudentsForClass(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {
        return readWrappedList(paths.studentsPath() + QUERY_PREFIX + CLASS_ID_PARAM + classId, jwtStore, state);
    }

    public List<Map<String, Object>> getSessionsForClass(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {
        return readWrappedList(paths.sessionsPath() + QUERY_PREFIX + CLASS_ID_PARAM + classId, jwtStore, state);
    }

    public Map<String, Object> createSession(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {
        return readPost(paths.sessionsPath(), Map.of("classId", classId), jwtStore, state);
    }

    public Map<String, Object> getSessionReport(JwtStore jwtStore, AuthState state, long sessionId)
            throws IOException, InterruptedException {
        return readGet(paths.sessionReportPath() + QUERY_PREFIX + SESSION_ID_PARAM + sessionId, jwtStore, state);
    }

    public Map<String, Object> getDashboardStats(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readGet(paths.dashboardStatsPath(), jwtStore, state);
    }

    public void markAttendance(JwtStore jwtStore, AuthState state, long studentId, long sessionId, String status)
            throws IOException, InterruptedException {
        postJson(
                paths.markAttendancePath(),
                Map.of(
                        "studentId", studentId,
                        "sessionId", sessionId,
                        "status", status
                ),
                jwtStore,
                state
        );
    }

    public String extractCode(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object directCode = response.get("code");
        if (directCode != null) {
            return String.valueOf(directCode);
        }

        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object nestedCode = dataMap.get("code");
            if (nestedCode != null) {
                return String.valueOf(nestedCode);
            }
        }

        return null;
    }
}