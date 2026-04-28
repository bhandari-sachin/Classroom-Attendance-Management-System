package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TranslationApiTest {

    private HttpClient client;
    private ObjectMapper objectMapper;
    private TranslationApi api;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(HttpClient.class);
        objectMapper = new ObjectMapper();
        api = new TranslationApi("http://localhost:8081/", client, objectMapper);
    }

    @Test
    void constructor_shouldStripTrailingSlash() {
        TranslationApi localApi = new TranslationApi("http://localhost:8081/", client, objectMapper);

        // indirect check via request
        assertEquals("http://localhost:8081", getBaseUrl(localApi));
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TranslationApi(null, client, objectMapper)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TranslationApi("   ", client, objectMapper)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void getUiTranslations_shouldReturnParsedMap() throws IOException, InterruptedException {
        String json = "{\"hello\":\"Hello\",\"bye\":\"Goodbye\"}";

        HttpResponse<String> response = mockStringResponse(200, json);
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        Map<String, String> result = api.getUiTranslations("en");

        assertEquals("Hello", result.get("hello"));
        assertEquals("Goodbye", result.get("bye"));
    }

    @Test
    void getUiTranslations_shouldEncodeLanguageParameter() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(200, "{}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        api.getUiTranslations("en test");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(captor.capture(), anyStringBodyHandler());

        String uri = captor.getValue().uri().toString();

        assertEquals(
                "http://localhost:8081/api/i18n/ui?lang=en+test",
                uri
        );
    }

    @Test
    void getUiTranslations_shouldThrowIOExceptionWhenStatusError() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(500, "boom");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        IOException ex = assertThrows(
                IOException.class,
                () -> api.getUiTranslations("en")
        );

        assertEquals("Failed to load translations. HTTP 500 - boom", ex.getMessage());
    }

    @Test
    void getUiTranslations_shouldHandleNullLanguage() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(200, "{}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        api.getUiTranslations(null);

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(captor.capture(), anyStringBodyHandler());

        String uri = captor.getValue().uri().toString();

        assertEquals(
                "http://localhost:8081/api/i18n/ui?lang=",
                uri
        );
    }

    // --- helpers (NO nested stubbing) ---

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyStringBodyHandler() {
        return (HttpResponse.BodyHandler<String>) any(HttpResponse.BodyHandler.class);
    }

    private HttpResponse<String> mockStringResponse(int statusCode, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // small helper to access private field indirectly (optional)
    private String getBaseUrl(TranslationApi api) {
        try {
            var field = TranslationApi.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            return (String) field.get(api);
        } catch (Exception e) {
            return null;
        }
    }
}