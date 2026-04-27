package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import frontend.dto.AdminUsersResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminApiTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String TOKEN = "test-token";

    private JwtStore jwtStore;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private ObjectMapper objectMapper;
    private AdminApi api;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        jwtStore = mock(JwtStore.class);
        httpClient = mock(HttpClient.class);
        httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        objectMapper = mock(ObjectMapper.class);

        api = new AdminApi(
                BASE_URL + "/",
                jwtStore,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );
    }

    @Test
    void constructorShouldCreateApiInstance() {
        AdminApi testApi = new AdminApi(
                BASE_URL,
                jwtStore,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );

        assertNotNull(testApi);

        String expectedBaseUrl = BASE_URL.endsWith("/")
                ? BASE_URL.substring(0, BASE_URL.length() - 1)
                : BASE_URL;

        assertEquals(expectedBaseUrl, testApi.getBaseUrl());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldThrowWhenBaseUrlIsNullOrBlank(String baseUrl) {
        AdminApi.Paths paths = AdminApi.Paths.defaults();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi(baseUrl, jwtStore, httpClient, objectMapper, paths)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructorShouldThrowWhenJwtStoreIsNull() {
        HttpClient clientRef = httpClient;
        ObjectMapper mapperRef = objectMapper;
        AdminApi.Paths pathsRef = AdminApi.Paths.defaults();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi(BASE_URL, null, clientRef, mapperRef, pathsRef)
        );

        assertEquals("JwtStore must not be null.", ex.getMessage());
    }

    @Test
    void constructorShouldThrowWhenHttpClientIsNull() {
        JwtStore storeRef = jwtStore;
        ObjectMapper mapperRef = objectMapper;
        AdminApi.Paths pathsRef = AdminApi.Paths.defaults();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi(BASE_URL, storeRef, null, mapperRef, pathsRef)
        );

        assertEquals("HttpClient must not be null.", ex.getMessage());
    }

    @Test
    void constructorShouldThrowWhenObjectMapperIsNull() {
        JwtStore storeRef = jwtStore;
        HttpClient clientRef = httpClient;
        AdminApi.Paths pathsRef = AdminApi.Paths.defaults();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi(BASE_URL, storeRef, clientRef, null, pathsRef)
        );

        assertEquals("ObjectMapper must not be null.", ex.getMessage());
    }

    @Test
    void constructorShouldThrowWhenPathsIsNull() {
        JwtStore storeRef = jwtStore;
        HttpClient clientRef = httpClient;
        ObjectMapper mapperRef = objectMapper;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi(BASE_URL, storeRef, clientRef, mapperRef, null)
        );

        assertEquals("Paths must not be null.", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidPathArguments")
    void pathsShouldThrowWhenAnyPathIsBlank(String classesPath,
                                            String usersPath,
                                            String attendanceStatsPath,
                                            String attendanceReportPath,
                                            String expectedMessage) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AdminApi.Paths(classesPath, usersPath, attendanceStatsPath, attendanceReportPath)
        );

        assertEquals(expectedMessage, ex.getMessage());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> invalidPathArguments() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "",
                        "/api/admin/users",
                        "/api/admin/attendance/stats",
                        "/api/admin/attendance/report",
                        "classesPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/admin/classes",
                        " ",
                        "/api/admin/attendance/stats",
                        "/api/admin/attendance/report",
                        "usersPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/admin/classes",
                        "/api/admin/users",
                        null,
                        "/api/admin/attendance/report",
                        "attendanceStatsPath must not be null or blank."
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "/api/admin/classes",
                        "/api/admin/users",
                        "/api/admin/attendance/stats",
                        "",
                        "attendanceReportPath must not be null or blank."
                )
        );
    }

    @Test
    void tokenOrThrowShouldThrowWhenNotLoggedIn() {
        when(jwtStore.load()).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.getAttendanceStats()
        );

        assertEquals("No authentication state found. Please log in first.", ex.getMessage());
    }

    @Test
    void tokenOrThrowShouldThrowWhenTokenMissing() {
        AuthState authState = mock(AuthState.class);
        when(authState.token()).thenReturn("");
        when(jwtStore.load()).thenReturn(Optional.of(authState));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.getAttendanceStats()
        );

        assertEquals("JWT token is missing or empty. Please log in again.", ex.getMessage());
    }

    @Test
    void getAttendanceStatsShouldReturnParsedMap() throws Exception {
        Map<String, Object> expected = Map.of("present", 10, "absent", 2);

        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/attendance/stats", 200, "{\"present\":10}");
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        Map<String, Object> result = api.getAttendanceStats();

        assertEquals(expected, result);
        verify(objectMapper).readValue(eq("{\"present\":10}"), typeRefAny());
    }

    @Test
    void getAttendanceStatsShouldThrowApiExceptionWhenHttpStatusIsError() throws Exception {
        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/attendance/stats", 500, "{\"error\":\"Server error\"}");

        ApiException ex = assertThrows(ApiException.class, () -> api.getAttendanceStats());

        assertEquals("HTTP 500: {\"error\":\"Server error\"}", ex.getMessage());
    }

    @Test
    void getAdminClassesShouldReturnParsedDtos() throws Exception {
        List<AdminClassDto> expected = List.of(mock(AdminClassDto.class), mock(AdminClassDto.class));

        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/classes", 200, "[{}]");
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        List<AdminClassDto> result = api.getAdminClasses();

        assertEquals(expected, result);
        verify(objectMapper).readValue(eq("[{}]"), typeRefAny());
    }

    @Test
    void getAdminClassesRawShouldReturnParsedRawMaps() throws Exception {
        List<Map<String, Object>> expected = List.of(Map.of("classCode", "CS101"));

        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/classes", 200, "[{\"classCode\":\"CS101\"}]");
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        List<Map<String, Object>> result = api.getAdminClassesRaw();

        assertEquals(expected, result);
        verify(objectMapper).readValue(eq("[{\"classCode\":\"CS101\"}]"), typeRefAny());
    }

    @Test
    void getAdminUsersShouldReturnTypedResponse() throws Exception {
        AdminUsersResponseDto expected = mock(AdminUsersResponseDto.class);

        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/users", 200, "{\"students\":[]}");
        when(objectMapper.readValue("{\"students\":[]}", AdminUsersResponseDto.class)).thenReturn(expected);

        AdminUsersResponseDto result = api.getAdminUsers();

        assertEquals(expected, result);
        verify(objectMapper).readValue("{\"students\":[]}", AdminUsersResponseDto.class);
    }

    @Test
    void getAdminUsersRawShouldReturnParsedRawMap() throws Exception {
        Map<String, Object> expected = Map.of("students", List.of());

        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/users", 200, "{\"students\":[]}");
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        Map<String, Object> result = api.getAdminUsersRaw();

        assertEquals(expected, result);
        verify(objectMapper).readValue(eq("{\"students\":[]}"), typeRefAny());
    }

    @Test
    void createClassShouldPostJsonWithNormalizedNullDefaults() throws Exception {
        mockToken();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        mockAuthorizedPostResponse(BASE_URL + "/api/admin/classes", "{\"ok\":true}");

        assertDoesNotThrow(() ->
                api.createClass("CS101", "Programming", "teacher@school.com", null, null, null)
        );

        verify(objectMapper).writeValueAsString(argThat(body -> {
            if (!(body instanceof Map<?, ?> map)) {
                return false;
            }
            return "CS101".equals(map.get("classCode"))
                    && "Programming".equals(map.get("name"))
                    && "teacher@school.com".equals(map.get("teacherEmail"))
                    && "".equals(map.get("semester"))
                    && "".equals(map.get("academicYear"))
                    && Integer.valueOf(0).equals(map.get("maxCapacity"));
        }));
    }

    @Test
    void createClassShouldPostJsonWithProvidedValues() throws Exception {
        mockToken();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        mockAuthorizedPostResponse(BASE_URL + "/api/admin/classes", "{\"ok\":true}");

        assertDoesNotThrow(() ->
                api.createClass("CS101", "Programming", "teacher@school.com", "SPRING", "2025", 30)
        );

        verify(objectMapper).writeValueAsString(argThat(body -> {
            if (!(body instanceof Map<?, ?> map)) {
                return false;
            }
            return "SPRING".equals(map.get("semester"))
                    && "2025".equals(map.get("academicYear"))
                    && Integer.valueOf(30).equals(map.get("maxCapacity"));
        }));
    }

    @Test
    void getAttendanceReportShouldBuildUrlWithoutSearch() throws Exception {
        List<Map<String, Object>> expected = List.of(Map.of("student", "John"));

        mockToken();
        mockAuthorizedGetResponse(
                BASE_URL + "/api/admin/attendance/report?classId=1&period=THIS_MONTH",
                200,
                "[]"
        );
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        List<Map<String, Object>> result = api.getAttendanceReport(1L, "THIS_MONTH", null);

        assertEquals(expected, result);
    }

    @Test
    void getAttendanceReportShouldBuildUrlWithSearch() throws Exception {
        List<Map<String, Object>> expected = List.of(Map.of("student", "John Doe"));

        mockToken();
        mockAuthorizedGetResponse(
                BASE_URL + "/api/admin/attendance/report?classId=1&period=THIS_MONTH&search=John+Doe",
                200,
                "[]"
        );
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        List<Map<String, Object>> result = api.getAttendanceReport(1L, "THIS_MONTH", "John Doe");

        assertEquals(expected, result);
    }

    @Test
    void getAttendanceReportShouldSkipBlankPeriodAndSearch() throws Exception {
        List<Map<String, Object>> expected = List.of();

        mockToken();
        mockAuthorizedGetResponse(
                BASE_URL + "/api/admin/attendance/report?classId=5",
                200,
                "[]"
        );
        when(objectMapper.readValue(anyString(), typeRefAny())).thenReturn(expected);

        List<Map<String, Object>> result = api.getAttendanceReport(5L, "   ", " ");

        assertEquals(expected, result);
    }

    @Test
    void getAllStudentsNotInClassShouldUseEncodedPathSegment() throws Exception {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        CollectionType collectionType =
                typeFactory.constructCollectionType(List.class, AdminStudentDto.class);

        List<AdminStudentDto> expected = List.of(mock(AdminStudentDto.class));

        mockToken();
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);

        mockAuthorizedGetResponse(
                BASE_URL + "/api/admin/classes/CS+101%2FA/available-students",
                200,
                "[{}]"
        );
        when(objectMapper.readValue("[{}]", collectionType)).thenReturn(expected);

        List<AdminStudentDto> result = api.getAllStudentsNotInClass("CS 101/A");

        assertEquals(expected, result);
        verify(objectMapper).readValue("[{}]", collectionType);
    }

    @Test
    void enrollStudentsToClassShouldPostToEncodedEnrollPath() throws Exception {
        List<String> emails = List.of("a@test.com", "b@test.com");

        mockToken();
        when(objectMapper.writeValueAsString(emails)).thenReturn("[\"a@test.com\",\"b@test.com\"]");
        mockAuthorizedPostResponse(
                BASE_URL + "/api/admin/classes/CS+101%2FA/enroll",
                "[\"a@test.com\",\"b@test.com\"]"
        );

        assertDoesNotThrow(() -> api.enrollStudentsToClass("CS 101/A", emails));

        verify(objectMapper).writeValueAsString(emails);
    }

    @Test
    void stripTrailingSlashShouldRemoveTrailingSlash() throws Exception {
        Method method = AdminApi.class.getDeclaredMethod("stripTrailingSlash", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, BASE_URL + "/");

        assertEquals(BASE_URL, result);
    }

    @Test
    void stripTrailingSlashShouldKeepUrlWithoutTrailingSlash() throws Exception {
        Method method = AdminApi.class.getDeclaredMethod("stripTrailingSlash", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, BASE_URL);

        assertEquals(BASE_URL, result);
    }

    @Test
    void encodePathSegmentShouldEncodeSpacesAndSlash() throws Exception {
        Method method = AdminApi.class.getDeclaredMethod("encodePathSegment", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, "CS 101/A");

        assertEquals("CS+101%2FA", result);
    }

    @Test
    void encodeQueryParamShouldEncodeSpaces() throws Exception {
        Method method = AdminApi.class.getDeclaredMethod("encodeQueryParam", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, "John Doe");

        assertEquals("John+Doe", result);
    }

    @Test
    void privateGetShouldWrapHttpErrorAsApiException() throws Exception {
        mockToken();
        mockAuthorizedGetResponse(BASE_URL + "/api/admin/classes", 404, "Not found");

        Method method = AdminApi.class.getDeclaredMethod("get", String.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(api, "/api/admin/classes")
        );

        assertInstanceOf(ApiException.class, ex.getCause());
        assertEquals("HTTP 404: Not found", ex.getCause().getMessage());
    }

    private void mockToken() {
        AuthState authState = new AuthState(TOKEN, Role.ADMIN, "Admin");
        when(jwtStore.load()).thenReturn(Optional.of(authState));
    }

    @SuppressWarnings("unchecked")
    private TypeReference<Object> typeRefAny() {
        return any(TypeReference.class);
    }

    private void mockAuthorizedGetResponse(String expectedUrl, int statusCode, String body) throws Exception {
        when(httpResponse.statusCode()).thenReturn(statusCode);
        when(httpResponse.body()).thenReturn(body);
        when(httpClient.send(
                argThat(request -> hasExpectedGetRequest(request, expectedUrl)),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);
    }

    private void mockAuthorizedPostResponse(String expectedUrl, String expectedBody)
            throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("");
        when(httpClient.send(
                argThat(request -> hasExpectedPostRequest(request, expectedUrl, expectedBody)),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);
    }

    private boolean hasExpectedGetRequest(HttpRequest request, String expectedUrl) {
        return expectedUrl.equals(request.uri().toString())
                && "GET".equals(request.method())
                && ("Bearer " + TOKEN).equals(request.headers().firstValue("Authorization").orElse(null));
    }

    private boolean hasExpectedPostRequest(HttpRequest request, String expectedUrl, String expectedBody) {
        return expectedUrl.equals(request.uri().toString())
                && "POST".equals(request.method())
                && ("Bearer " + TOKEN).equals(request.headers().firstValue("Authorization").orElse(null))
                && "application/json".equals(request.headers().firstValue("Content-Type").orElse(null))
                && request.bodyPublisher().isPresent()
                && expectedBody.equals(readBodyPublisher(request));
    }

    private String readBodyPublisher(HttpRequest request) {
        TestBodySubscriber subscriber = new TestBodySubscriber();
        request.bodyPublisher().orElseThrow().subscribe(subscriber);
        return subscriber.getBodyAsString();
    }
}