package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherApiTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String TOKEN = "jwt-token";

    private JwtStore jwtStore;
    private AuthState authState;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private ObjectMapper objectMapper;
    private TeacherApi api;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        jwtStore = mock(JwtStore.class);
        httpClient = mock(HttpClient.class);
        httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        objectMapper = mock(ObjectMapper.class);

        authState = new AuthState("state-token", Role.TEACHER, "Teacher");

        api = new TeacherApi(
                BASE_URL,
                httpClient,
                objectMapper,
                TeacherApi.Paths.defaults()
        );
    }

    @Test
    void constructorShouldCreateInstance() {
        TeacherApi teacherApi = new TeacherApi(BASE_URL);
        assertNotNull(teacherApi);
    }

    @Test
    void constructorShouldThrowWhenPathsIsNull() {
        HttpClient clientRef = httpClient;
        ObjectMapper mapperRef = objectMapper;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TeacherApi(BASE_URL, clientRef, mapperRef, null)
        );

        assertEquals("Paths must not be null.", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidPathArguments")
    void pathsShouldThrowWhenAnyPathIsBlank(String classesPath,
                                            String studentsPath,
                                            String sessionsPath,
                                            String sessionReportPath,
                                            String dashboardStatsPath,
                                            String markAttendancePath,
                                            String expectedMessage) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TeacherApi.Paths(
                        classesPath,
                        studentsPath,
                        sessionsPath,
                        sessionReportPath,
                        dashboardStatsPath,
                        markAttendancePath
                )
        );

        assertEquals(expectedMessage, ex.getMessage());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> invalidPathArguments() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "", "/api/teacher/students", "/api/teacher/sessions",
                        "/api/teacher/reports/session", "/api/teacher/dashboard/stats",
                        "/api/teacher/attendance/mark",
                        "classesPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/teacher/classes", " ", "/api/teacher/sessions",
                        "/api/teacher/reports/session", "/api/teacher/dashboard/stats",
                        "/api/teacher/attendance/mark",
                        "studentsPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/teacher/classes", "/api/teacher/students", null,
                        "/api/teacher/reports/session", "/api/teacher/dashboard/stats",
                        "/api/teacher/attendance/mark",
                        "sessionsPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/teacher/classes", "/api/teacher/students", "/api/teacher/sessions",
                        "", "/api/teacher/dashboard/stats",
                        "/api/teacher/attendance/mark",
                        "sessionReportPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/teacher/classes", "/api/teacher/students", "/api/teacher/sessions",
                        "/api/teacher/reports/session", "",
                        "/api/teacher/attendance/mark",
                        "dashboardStatsPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/teacher/classes", "/api/teacher/students", "/api/teacher/sessions",
                        "/api/teacher/reports/session", "/api/teacher/dashboard/stats",
                        " ",
                        "markAttendancePath must not be null or blank."
                )
        );
    }

    @Test
    void extractCodeShouldReturnDirectCode() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "ABC123");

        String result = api.extractCode(response);

        assertEquals("ABC123", result);
    }

    @Test
    void extractCodeShouldReturnNestedCode() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", "XYZ999");

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        String result = api.extractCode(response);

        assertEquals("XYZ999", result);
    }

    @Test
    void extractCodeShouldReturnNullWhenMissing() {
        Map<String, Object> response = new HashMap<>();

        String result = api.extractCode(response);

        assertNull(result);
    }

    @Test
    void extractCodeShouldReturnNullWhenResponseNull() {
        assertNull(api.extractCode(null));
    }

    @Test
    void getMyClassesShouldUseJwtStoreTokenAndReturnWrappedList() throws Exception {
        List<Map<String, Object>> expectedList = List.of(Map.of("id", 1L, "name", "A"));
        Map<String, Object> wrapped = Map.of("data", expectedList);

        mockJwtStoreToken();
        mockGetResponse(BASE_URL + "/api/teacher/classes", "{\"data\":[]}");

        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(wrapped);

        List<Map<String, Object>> result = api.getMyClasses(jwtStore, authState);

        assertEquals(expectedList, result);
        verify(httpClient).send(
                argThat(request -> hasExpectedGetRequest(request, BASE_URL + "/api/teacher/classes", TOKEN)),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        );
    }

    @Test
    void getMyClassesShouldFallbackToStateToken() throws Exception {
        List<Map<String, Object>> expectedList = List.of(Map.of("id", 1L));
        Map<String, Object> wrapped = Map.of("data", expectedList);

        when(jwtStore.load()).thenReturn(Optional.empty());
        mockGetResponse(BASE_URL + "/api/teacher/classes",  "{\"data\":[]}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(wrapped);

        List<Map<String, Object>> result = api.getMyClasses(jwtStore, authState);

        assertEquals(expectedList, result);
        verify(httpClient).send(
                argThat(request -> hasExpectedGetRequest(request, BASE_URL + "/api/teacher/classes", "state-token")),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        );
    }

    @Test
    void getStudentsForClassShouldBuildQueryPath() throws Exception {
        List<Map<String, Object>> expectedList = List.of(Map.of("email", "student@test.com"));
        Map<String, Object> wrapped = Map.of("data", expectedList);

        mockJwtStoreToken();
        mockGetResponse(BASE_URL + "/api/teacher/students?classId=7",  "{\"data\":[]}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(wrapped);

        List<Map<String, Object>> result = api.getStudentsForClass(jwtStore, authState, 7L);

        assertEquals(expectedList, result);
    }

    @Test
    void getSessionsForClassShouldBuildQueryPath() throws Exception {
        List<Map<String, Object>> expectedList = List.of(Map.of("id", 12L));
        Map<String, Object> wrapped = Map.of("data", expectedList);

        mockJwtStoreToken();
        mockGetResponse(BASE_URL + "/api/teacher/sessions?classId=9",  "{\"data\":[]}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(wrapped);

        List<Map<String, Object>> result = api.getSessionsForClass(jwtStore, authState, 9L);

        assertEquals(expectedList, result);
    }

    @Test
    void createSessionShouldPostBodyAndReturnMap() throws Exception {
        Map<String, Object> expected = Map.of("sessionId", 10L, "code", "ABC123");

        mockJwtStoreToken();
        when(objectMapper.writeValueAsString(Map.of("classId", 5L))).thenReturn("{\"classId\":5}");
        mockPostResponse(BASE_URL + "/api/teacher/sessions", "{\"classId\":5}", "{\"sessionId\":10}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(expected);

        Map<String, Object> result = api.createSession(jwtStore, authState, 5L);

        assertEquals(expected, result);
    }

    @Test
    void getSessionReportShouldBuildQueryPath() throws Exception {
        Map<String, Object> expected = Map.of("present", 5);

        mockJwtStoreToken();
        mockGetResponse(BASE_URL + "/api/teacher/reports/session?sessionId=15", "{\"present\":5}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(expected);

        Map<String, Object> result = api.getSessionReport(jwtStore, authState, 15L);

        assertEquals(expected, result);
    }

    @Test
    void getDashboardStatsShouldReturnMap() throws Exception {
        Map<String, Object> expected = Map.of("classes", 3, "students", 40);

        mockJwtStoreToken();
        mockGetResponse(BASE_URL + "/api/teacher/dashboard/stats", "{\"classes\":3}");
        when(objectMapper.readValue(anyString(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(expected);

        Map<String, Object> result = api.getDashboardStats(jwtStore, authState);

        assertEquals(expected, result);
    }

    @Test
    void markAttendanceShouldPostJsonPayload() throws Exception {
        mockJwtStoreToken();
        when(objectMapper.writeValueAsString(Map.of(
                "studentId", 11L,
                "sessionId", 22L,
                "status", "PRESENT"
        ))).thenReturn("{\"studentId\":11,\"sessionId\":22,\"status\":\"PRESENT\"}");

        mockPostResponse(
                BASE_URL + "/api/teacher/attendance/mark",
                "{\"studentId\":11,\"sessionId\":22,\"status\":\"PRESENT\"}",
                ""
        );

        assertDoesNotThrow(() ->
                api.markAttendance(jwtStore, authState, 11L, 22L, "PRESENT")
        );
    }

    @Test
    void getDashboardStatsShouldThrowWhenNoTokenAvailable() {
        when(jwtStore.load()).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.getDashboardStats(jwtStore, null)
        );

        assertNotNull(ex);
    }

    private void mockJwtStoreToken() {
        AuthState stored = new AuthState(TeacherApiTest.TOKEN, Role.TEACHER, "Stored Teacher");
        when(jwtStore.load()).thenReturn(Optional.of(stored));
    }

    private void mockGetResponse(String expectedUrl, String responseBody) throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(
                argThat(request -> expectedUrl.equals(request.uri().toString()) && "GET".equals(request.method())),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);
    }

    private void mockPostResponse(String expectedUrl, String expectedBody, String responseBody) throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(
                argThat(request ->
                        expectedUrl.equals(request.uri().toString())
                                && "POST".equals(request.method())
                                && "application/json".equals(request.headers().firstValue("Content-Type").orElse(null))
                                && request.bodyPublisher().isPresent()
                                && expectedBody.equals(readBodyPublisher(request))
                ),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);
    }

    private boolean hasExpectedGetRequest(HttpRequest request, String expectedUrl, String token) {
        return expectedUrl.equals(request.uri().toString())
                && "GET".equals(request.method())
                && ("Bearer " + token).equals(request.headers().firstValue("Authorization").orElse(null));
    }

    private String readBodyPublisher(HttpRequest request) {
        TestBodySubscriber subscriber = new TestBodySubscriber();
        request.bodyPublisher().orElseThrow().subscribe(subscriber);
        return subscriber.getBodyAsString();
    }
}