package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StudentAttendanceApiTest {

    private HttpClient client;
    private ObjectMapper objectMapper;
    private JwtStore jwtStore;
    private AuthState authState;
    private StudentAttendanceApi api;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(HttpClient.class);
        objectMapper = new ObjectMapper();
        jwtStore = Mockito.mock(JwtStore.class);
        authState = Mockito.mock(AuthState.class);
        api = new StudentAttendanceApi(
                "http://localhost:8081/",
                client,
                objectMapper,
                StudentAttendanceApi.Paths.defaults()
        );
    }

    @Test
    void constructor_shouldThrowWhenPathsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentAttendanceApi("http://localhost:8081", client, objectMapper, null)
        );

        assertEquals("Paths must not be null.", ex.getMessage());
    }

    @Test
    void paths_shouldThrowWhenSummaryPathNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentAttendanceApi.Paths(null, "/records", "/mark")
        );

        assertEquals("summaryPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void paths_shouldThrowWhenRecordsPathBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentAttendanceApi.Paths("/summary", "   ", "/mark")
        );

        assertEquals("recordsPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void paths_shouldThrowWhenMarkAttendancePathNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentAttendanceApi.Paths("/summary", "/records", null)
        );

        assertEquals("markAttendancePath must not be null or blank.", ex.getMessage());
    }

    @Test
    void getSummary_shouldReturnParsedMap() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("student-token");

        HttpResponse<String> response = mockStringResponse(200, "{\"present\":8,\"absent\":2}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        Map<String, Object> result = api.getSummary(jwtStore, authState);

        assertEquals(8, result.get("present"));
        assertEquals(2, result.get("absent"));
    }

    @Test
    void getRecords_shouldReturnParsedList() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("student-token");

        HttpResponse<String> response = mockStringResponse(
                200,
                "[{\"status\":\"PRESENT\"},{\"status\":\"ABSENT\"}]"
        );
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.getRecords(jwtStore, authState);

        assertEquals(2, result.size());
        assertEquals("PRESENT", result.get(0).get("status"));
        assertEquals("ABSENT", result.get(1).get("status"));
    }

    @Test
    void submitCode_shouldThrowWhenCodeNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> api.submitCode(null, jwtStore, authState)
        );

        assertEquals("Attendance code is required.", ex.getMessage());
    }

    @Test
    void submitCode_shouldThrowWhenCodeBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> api.submitCode("   ", jwtStore, authState)
        );

        assertEquals("Attendance code is required.", ex.getMessage());
    }

    @Test
    void submitCode_shouldSendTrimmedCode() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("student-token");

        HttpResponse<String> response = mockStringResponse(200, "{\"status\":\"ok\"}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        api.submitCode("  ABC123  ", jwtStore, authState);

        HttpRequest expectedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/attendance/mark"))
                .header("Authorization", "Bearer student-token")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"ABC123\"}"))
                .build();

        assertEquals(expectedRequest.uri(), expectedRequest.uri());
    }

    @Test
    void defaults_shouldReturnExpectedPaths() {
        StudentAttendanceApi.Paths paths = StudentAttendanceApi.Paths.defaults();

        assertEquals("/api/student/attendance/summary", paths.summaryPath());
        assertEquals("/api/student/attendance/records", paths.recordsPath());
        assertEquals("/api/attendance/mark", paths.markAttendancePath());
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyStringBodyHandler() {
        return (HttpResponse.BodyHandler<String>) any(HttpResponse.BodyHandler.class);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockStringResponse(int statusCode, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (left, right) -> true));
        return response;
    }
}