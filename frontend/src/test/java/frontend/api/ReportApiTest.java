package frontend.api;

import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportApiTest {

    private HttpClient client;
    private JwtStore jwtStore;
    private AuthState authState;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(HttpClient.class);
        jwtStore = Mockito.mock(JwtStore.class);
        authState = Mockito.mock(AuthState.class);
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ReportApi(null, client)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ReportApi("   ", client)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void exportStudentPdf_shouldUseStoredTokenAndWriteFile() throws IOException, InterruptedException {
        ReportApi api = new ReportApi("http://localhost:8081/", client);
        byte[] expectedBytes = "student-report".getBytes(StandardCharsets.UTF_8);

        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.token()).thenReturn("store-token");

        HttpResponse<InputStream> response = mockInputStreamResponse(200, expectedBytes);
        when(client.send(any(HttpRequest.class), anyInputStreamBodyHandler()))
                .thenReturn(response);

        Path destination = tempDir.resolve("student-report.pdf");
        String destinationPath = destination.toString();

        api.exportStudentPdf(jwtStore, authState, destinationPath);

        assertTrue(Files.exists(destination));
        assertArrayEquals(expectedBytes, Files.readAllBytes(destination));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(captor.capture(), anyInputStreamBodyHandler());

        HttpRequest request = captor.getValue();
        String uri = request.uri().toString();

        assertTrue(uri.startsWith("http://localhost:8081/api/reports/export/student?format=pdf"));
        assertTrue(uri.contains("&lang="));
        assertEquals("Bearer store-token", request.headers().firstValue("Authorization").orElse(""));
    }

    @Test
    void exportTeacherReport_shouldFallbackToStateTokenAndEncodeFormat() throws IOException, InterruptedException {
        ReportApi api = new ReportApi("http://localhost:8081", client);
        byte[] expectedBytes = "teacher-report".getBytes(StandardCharsets.UTF_8);

        when(jwtStore.load()).thenReturn(Optional.empty());
        when(authState.token()).thenReturn("state-token");

        HttpResponse<InputStream> response = mockInputStreamResponse(200, expectedBytes);
        when(client.send(any(HttpRequest.class), anyInputStreamBodyHandler()))
                .thenReturn(response);

        Path destination = tempDir.resolve("teacher-report.pdf");
        String destinationPath = destination.toString();

        api.exportTeacherReport(jwtStore, authState, 7L, "pdf report", destinationPath);

        assertTrue(Files.exists(destination));
        assertArrayEquals(expectedBytes, Files.readAllBytes(destination));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(captor.capture(), anyInputStreamBodyHandler());

        HttpRequest request = captor.getValue();
        String uri = request.uri().toString();

        assertTrue(uri.startsWith("http://localhost:8081/api/reports/export/teacher?classId=7"));
        assertTrue(uri.contains("format=pdf+report"));
        assertTrue(uri.contains("&lang="));
        assertEquals("Bearer state-token", request.headers().firstValue("Authorization").orElse(""));
    }

    @Test
    void exportAdminReport_shouldThrowWhenTokenMissing() {
        ReportApi api = new ReportApi("http://localhost:8081", client);

        when(jwtStore.load()).thenReturn(Optional.empty());
        when(authState.token()).thenReturn(null);

        Path destination = tempDir.resolve("admin-report.pdf");
        String destinationPath = destination.toString();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.exportAdminReport(jwtStore, authState, "pdf", destinationPath)
        );

        assertEquals("JWT token is missing. Please log in again.", ex.getMessage());
    }

    @Test
    void exportAdminReport_shouldThrowApiExceptionWhenBackendReturnsError() throws IOException, InterruptedException {
        ReportApi api = new ReportApi("http://localhost:8081", client);

        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.token()).thenReturn("admin-token");

        HttpResponse<InputStream> response = mockInputStreamResponse(500, "{\"error\":\"failed\"}".getBytes(StandardCharsets.UTF_8));
        when(client.send(any(HttpRequest.class), anyInputStreamBodyHandler()))
                .thenReturn(response);

        Path destination = tempDir.resolve("admin-report.pdf");
        String destinationPath = destination.toString();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> api.exportAdminReport(jwtStore, authState, "pdf", destinationPath)
        );

        assertEquals("Export failed (HTTP 500): {\"error\":\"failed\"}", ex.getMessage());
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<InputStream> mockInputStreamResponse(int statusCode, byte[] bodyBytes) {
        HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(new ByteArrayInputStream(bodyBytes));
        return response;
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<InputStream> anyInputStreamBodyHandler() {
        return (HttpResponse.BodyHandler<InputStream>) any(HttpResponse.BodyHandler.class);
    }
}