package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

/**
 * API client for student-teacher related requests.
 */
public final class StudentTeacherApi extends BaseApiClient {

    private final Paths paths;

    public StudentTeacherApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public StudentTeacherApi(String baseUrl, HttpClient client, ObjectMapper objectMapper, Paths paths) {
        super(baseUrl, client, objectMapper);

        if (paths == null) {
            throw new IllegalArgumentException("Paths must not be null.");
        }
        this.paths = paths;
    }

    public record Paths(String teachersPath) {
        public Paths {
            requirePath(teachersPath, "teachersPath");
        }

        public static Paths defaults() {
            return new Paths("/api/student/teachers");
        }
    }

    public List<Map<String, Object>> getTeachers(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readList(paths.teachersPath(), jwtStore, state);
    }
}