package frontend.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontend.auth.JwtStore;
import model.CourseClass;
import model.Session;
import model.Student;
import model.User;
import dto.AttendanceStats;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private static ApiClient instance;
    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper om = new ObjectMapper();
    private final JwtStore store = new JwtStore();

    private ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        om.registerModule(new JavaTimeModule());
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        om.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static synchronized ApiClient init(String baseUrl) {
        if (instance == null) instance = new ApiClient(baseUrl);
        return instance;
    }

    public static ApiClient get() {
        if (instance == null) throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init(baseUrl) in MainApp.");
        return instance;
    }

    private HttpRequest.Builder withAuth(HttpRequest.Builder b) {
        store.load().ifPresent(s -> b.header("Authorization", "Bearer " + s.getToken()));
        return b;
    }

    private <T> T parseResponse(HttpResponse<String> resp, Class<T> cls) throws IOException {
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            try {
                return om.readValue(resp.body(), cls);
            } catch (IOException ex) {
                System.err.println("Failed to parse response as " + cls.getSimpleName() + ": " + ex.getMessage());
                System.err.println("Response body:\n" + resp.body());
                throw ex;
            }
        }
        throw new IOException("HTTP " + resp.statusCode() + ": " + resp.body());
    }

    // classes/users/sessions/attendance methods
    public List<CourseClass> getClassesByTeacher(Long teacherId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/classes" + (teacherId != null ? "?teacherId=" + teacherId : "");
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("ApiClient GET: " + url + " -> " + resp.statusCode());
        if (resp.body() != null && resp.body().length() > 200) System.out.println(resp.body().substring(0,200)+"...");
        CourseClass[] arr = parseResponse(resp, CourseClass[].class);
        return Arrays.asList(arr);
    }

    public List<Student> getStudentsInClass(Long classId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/classes/" + classId + "/students";
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("ApiClient GET: " + url + " -> " + resp.statusCode());
        if (resp.body() != null && resp.body().length() > 200) System.out.println(resp.body().substring(0,200)+"...");
        Student[] arr = parseResponse(resp, Student[].class);
        return Arrays.asList(arr);
    }

    public List<Session> getSessionsByClass(Long classId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/classes/" + classId + "/sessions";
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("ApiClient GET: " + url + " -> " + resp.statusCode());
        if (resp.body() != null && resp.body().length() > 200) System.out.println(resp.body().substring(0,200)+"...");
        Session[] arr = parseResponse(resp, Session[].class);
        return Arrays.asList(arr);
    }

    public String startSession(Long sessionId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/sessions/" + sessionId + "/start";
        HttpRequest req = withAuth(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.noBody()).uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map m = parseResponse(resp, Map.class);
        return (String) m.getOrDefault("code", "");
    }

    public void endSession(Long sessionId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/sessions/" + sessionId + "/end";
        HttpRequest req = withAuth(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.noBody()).uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) throw new IOException("End session failed: " + resp.body());
    }

    public void markAttendance(Long studentId, Long sessionId, String status, String reason) throws IOException, InterruptedException {
        String url = baseUrl + "/api/attendance/mark";
        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "sessionId", sessionId,
                "status", status,
                "reason", reason
        );
        String json = om.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(URI.create(url))).header("Content-Type", "application/json").build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) throw new IOException("Mark attendance failed: " + resp.body());
    }

    public boolean submitAttendanceCode(Long studentId, String code) throws IOException, InterruptedException {
        String url = baseUrl + "/api/attendance/submit-code";
        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "code", code
        );
        String json = om.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(URI.create(url))).header("Content-Type", "application/json").build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map m = parseResponse(resp, Map.class);
        return Boolean.TRUE.equals(m.getOrDefault("success", false));
    }

    public List<User> getUsers(String role) throws IOException, InterruptedException {
        String url = baseUrl + "/api/users" + (role == null || role.isBlank() || "All Types".equalsIgnoreCase(role) ? "" : "?role=" + role.toUpperCase());
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        User[] arr = parseResponse(resp, User[].class);
        return Arrays.asList(arr);
    }

    public int getUserEnrolledCount(Long userId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/users/" + userId + "/enrolled-count";
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map m = parseResponse(resp, Map.class);
        return ((Number) m.getOrDefault("count", 0)).intValue();
    }

    public List<CourseClass> getClassesForStudent(Long studentId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/classes" + (studentId != null ? "?studentId=" + studentId : "");
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        CourseClass[] arr = parseResponse(resp, CourseClass[].class);
        return Arrays.asList(arr);
    }

    public int getClassEnrollmentCount(Long classId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/classes/" + classId + "/count";
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map m = parseResponse(resp, Map.class);
        return ((Number) m.getOrDefault("count", 0)).intValue();
    }

    // stats endpoints
    public AttendanceStats getOverallStats() throws IOException, InterruptedException {
        String url = baseUrl + "/api/stats";
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, AttendanceStats.class);
    }

    public AttendanceStats getOverallStats(String range) throws IOException, InterruptedException {
        String rangeVal = range == null ? "This Month" : range;
        String enc = URLEncoder.encode(rangeVal, StandardCharsets.UTF_8);
        String url = baseUrl + "/api/stats?range=" + enc;
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, AttendanceStats.class);
    }

    public AttendanceStats getClassStats(Long classId, String range) throws IOException, InterruptedException {
        String rangeVal = range == null ? "This Month" : range;
        String enc = URLEncoder.encode(rangeVal, StandardCharsets.UTF_8);
        String url = baseUrl + "/api/stats/class/" + classId + "?range=" + enc;
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, AttendanceStats.class);
    }

    public AttendanceStats getStudentStats(Long studentId, String range, Long classId) throws IOException, InterruptedException {
        String rangeVal = range == null ? "This Month" : range;
        String enc = URLEncoder.encode(rangeVal, StandardCharsets.UTF_8);
        String url = baseUrl + "/api/stats/student/" + studentId + "?range=" + enc + (classId == null ? "" : "&classId=" + classId);
        HttpRequest req = withAuth(HttpRequest.newBuilder().GET().uri(URI.create(url))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, AttendanceStats.class);
    }
}
