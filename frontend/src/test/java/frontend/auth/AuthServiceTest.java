package frontend.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private HttpClient client;
    private ObjectMapper objectMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        client = mock(HttpClient.class);
        objectMapper = mock(ObjectMapper.class);
        authService = new AuthService("http://localhost:8081/", client, objectMapper);
    }

    @Test
    void constructorShouldCreateInstance() {
        AuthService service = new AuthService("http://localhost:8081");
        assertNotNull(service);
    }

    @Test
    void constructorShouldRejectNullHttpClient() {
        ObjectMapper mapper = new ObjectMapper();

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new AuthService("http://localhost:8081", null, mapper)
        );

        assertEquals("HttpClient must not be null", ex.getMessage());
    }

    @Test
    void constructorShouldRejectNullObjectMapper() {
        HttpClient httpClient = HttpClient.newHttpClient();

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new AuthService("http://localhost:8081", httpClient, null)
        );

        assertEquals("ObjectMapper must not be null", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructorShouldRejectNullOrBlankBaseUrl(String baseUrl) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AuthService(baseUrl, client, objectMapper)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "'http://localhost:8081/', 'http://localhost:8081'",
            "'http://localhost:8081', 'http://localhost:8081'"
    })
    void stripTrailingSlashShouldNormalizeUrl(String input, String expected) throws Exception {
        assertEquals(expected, invokeStringMethod("stripTrailingSlash", input));
    }

    @ParameterizedTest
    @CsvSource({
            "'  TEST@Example.COM  ', 'test@example.com'",
            "'john@doe.com', 'john@doe.com'",
            "'', ''"
    })
    void normalizeEmailShouldTrimAndLowercase(String input, String expected) throws Exception {
        assertEquals(expected, invokeStringMethod("normalizeEmail", input));
    }

    @Test
    void normalizeEmailShouldReturnEmptyStringForNull() throws Exception {
        assertEquals("", invokeStringMethod("normalizeEmail", null));
    }

    @ParameterizedTest
    @CsvSource({
            "'secret', 'secret'",
            "'', ''"
    })
    void normalizePasswordShouldKeepValue(String input, String expected) throws Exception {
        assertEquals(expected, invokeStringMethod("normalizePassword", input));
    }

    @Test
    void normalizePasswordShouldReturnEmptyStringForNull() throws Exception {
        assertEquals("", invokeStringMethod("normalizePassword", null));
    }

    @ParameterizedTest
    @CsvSource({
            "'  John  ', 'John'",
            "'Doe', 'Doe'",
            "'   ', ''",
            "'', ''"
    })
    void normalizeTextShouldTrimValue(String input, String expected) throws Exception {
        assertEquals(expected, invokeStringMethod("normalizeText", input));
    }

    @Test
    void normalizeTextShouldReturnEmptyStringForNull() throws Exception {
        assertEquals("", invokeStringMethod("normalizeText", null));
    }

    @ParameterizedTest
    @CsvSource({
            "'  S123  ', 'S123'",
            "'ABC', 'ABC'",
            "'   ', ''",
            "'', ''"
    })
    void normalizeNullableTextShouldTrimValue(String input, String expected) throws Exception {
        assertEquals(expected, invokeStringMethod("normalizeNullableText", input));
    }

    @Test
    void normalizeNullableTextShouldReturnNullForNull() throws Exception {
        assertNull(invokeStringMethod("normalizeNullableText", null));
    }

    @Test
    void valueAsStringShouldReturnNullForNull() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("valueAsString", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(authService, new Object[]{null});

        assertNull(result);
    }

    @ParameterizedTest
    @CsvSource({
            "'123', '123'",
            "'hello', 'hello'"
    })
    void valueAsStringShouldConvertValueToString(String input, String expected) throws Exception {
        Method method = AuthService.class.getDeclaredMethod("valueAsString", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(authService, input);

        assertEquals(expected, result);
    }

    @Test
    void loginShouldSendNormalizedCredentialsAndParseResponse() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"email\":\"test@example.com\",\"password\":\"secret\"}");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"token\":\"jwt-token\",\"role\":\"STUDENT\",\"name\":\"John Doe\"}");
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of(
                        "token", "jwt-token",
                        "role", "STUDENT",
                        "name", "John Doe"
                ));

        AuthState state = authService.login("  TEST@Example.COM  ", "secret");

        assertNotNull(state);
        assertEquals("jwt-token", state.token());
        assertEquals(Role.STUDENT, state.role());
        assertEquals("John Doe", state.name());

        verify(objectMapper).writeValueAsString(any());
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void loginShouldThrowAuthServiceExceptionWhenHttpStatusIsError() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"email\":\"test@example.com\",\"password\":\"secret\"}");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(401);
        when(response.body()).thenReturn("Unauthorized");

        AuthServiceException ex = assertThrows(
                AuthServiceException.class,
                () -> authService.login("test@example.com", "secret")
        );

        assertTrue(ex.getMessage().contains("HTTP 401"));
        assertTrue(ex.getMessage().contains("Unauthorized"));
    }

    @Test
    void loginShouldPropagateJsonSerializationFailure() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization failed") { });

        assertThrows(
                JsonProcessingException.class,
                () -> authService.login("test@example.com", "secret")
        );
    }

    @Test
    void signupShouldSendRequestWithStudentCodeWhenPresent() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(201);
        when(response.body()).thenReturn("");

        assertDoesNotThrow(() ->
                authService.signup("John", "Doe", "john@example.com", "pass", Role.STUDENT, " S123 ")
        );

        verify(objectMapper).writeValueAsString(any());
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void signupShouldUseDefaultStudentRoleWhenRoleIsNull() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(201);
        when(response.body()).thenReturn("");

        assertDoesNotThrow(() ->
                authService.signup("John", "Doe", "john@example.com", "pass", null, null)
        );

        verify(objectMapper).writeValueAsString(any());
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void signupShouldOmitBlankStudentCodeBranchAndStillSucceed() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(201);
        when(response.body()).thenReturn("");

        assertDoesNotThrow(() ->
                authService.signup("John", "Doe", "john@example.com", "pass", Role.STUDENT, "   ")
        );
    }

    @Test
    void signupShouldPropagateJsonSerializationFailure() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization failed") { });

        assertThrows(
                JsonProcessingException.class,
                () -> authService.signup("John", "Doe", "john@example.com", "pass", Role.STUDENT, "S123")
        );
    }

    @Test
    void parseAuthStateShouldReturnAuthStateForValidJson() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("parseAuthState", String.class);
        method.setAccessible(true);

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of(
                        "token", "jwt-token",
                        "role", "TEACHER",
                        "name", "Alice"
                ));

        AuthState state = (AuthState) method.invoke(authService, "{\"token\":\"jwt-token\",\"role\":\"TEACHER\",\"name\":\"Alice\"}");

        assertNotNull(state);
        assertEquals("jwt-token", state.token());
        assertEquals(Role.TEACHER, state.role());
        assertEquals("Alice", state.name());
    }

    @Test
    void parseAuthStateShouldThrowWhenTokenMissing() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("parseAuthState", String.class);
        method.setAccessible(true);

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of(
                        "role", "STUDENT",
                        "name", "John"
                ));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(authService, "{\"role\":\"STUDENT\",\"name\":\"John\"}")
        );

        assertInstanceOf(AuthServiceException.class, ex.getCause());
        assertEquals("Login response is missing token.", ex.getCause().getMessage());
    }

    @Test
    void parseAuthStateShouldThrowWhenRoleMissing() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("parseAuthState", String.class);
        method.setAccessible(true);

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of(
                        "token", "jwt-token",
                        "name", "John"
                ));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(authService, "{\"token\":\"jwt-token\",\"name\":\"John\"}")
        );

        assertInstanceOf(AuthServiceException.class, ex.getCause());
        assertEquals("Login response is missing role.", ex.getCause().getMessage());
    }

    @Test
    void parseAuthStateShouldWrapParsingFailure() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("parseAuthState", String.class);
        method.setAccessible(true);

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("bad json") { });

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(authService, "not-json")
        );

        assertInstanceOf(AuthServiceException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Failed to parse login response"));
    }

    private String invokeStringMethod(String methodName, String value) throws Exception {
        Method method = AuthService.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(authService, new Object[]{value});
    }
}