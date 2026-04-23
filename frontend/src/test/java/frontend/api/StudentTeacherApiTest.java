package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
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

class StudentTeacherApiTest {

    private HttpClient client;
    private ObjectMapper objectMapper;
    private JwtStore jwtStore;
    private AuthState authState;
    private StudentTeacherApi api;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(HttpClient.class);
        objectMapper = new ObjectMapper();
        jwtStore = Mockito.mock(JwtStore.class);
        authState = Mockito.mock(AuthState.class);
        api = new StudentTeacherApi(
                "http://localhost:8081/",
                client,
                objectMapper,
                StudentTeacherApi.Paths.defaults()
        );
    }

    @Test
    void constructor_shouldThrowWhenPathsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentTeacherApi("http://localhost:8081", client, objectMapper, null)
        );

        assertEquals("Paths must not be null.", ex.getMessage());
    }

    @Test
    void paths_shouldThrowWhenTeachersPathNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentTeacherApi.Paths(null)
        );

        assertEquals("teachersPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void paths_shouldThrowWhenTeachersPathBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new StudentTeacherApi.Paths("   ")
        );

        assertEquals("teachersPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void defaults_shouldReturnExpectedPath() {
        StudentTeacherApi.Paths paths = StudentTeacherApi.Paths.defaults();

        assertEquals("/api/student/teachers", paths.teachersPath());
    }

    @Test
    void getTeachers_shouldReturnParsedList() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.token()).thenReturn("student-token");

        HttpResponse<String> response = mockStringResponse(
                200,
                "[{\"name\":\"Teacher One\",\"email\":\"one@example.com\"}," +
                        "{\"name\":\"Teacher Two\",\"email\":\"two@example.com\"}]"
        );
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.getTeachers(jwtStore, authState);

        assertEquals(2, result.size());
        assertEquals("Teacher One", result.get(0).get("name"));
        assertEquals("one@example.com", result.get(0).get("email"));
        assertEquals("Teacher Two", result.get(1).get("name"));
        assertEquals("two@example.com", result.get(1).get("email"));
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