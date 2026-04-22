package handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.LocalizationSQL;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import service.TranslationService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class I18nHandlerTest {

    @Test
    void handleUiShouldReturnTranslations() throws Exception {

        try (MockedConstruction<TranslationService> mocked =
                     mockConstruction(TranslationService.class,
                             (mock, context) -> {
                                 when(mock.getUiTranslations("en"))
                                         .thenReturn(Map.of("hello", "Hello"));
                             })) {

            HttpExchange exchange = mock(HttpExchange.class);
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(new URI("/i18n/ui?lang=en"));
            when(exchange.getResponseBody()).thenReturn(response);
            when(exchange.getResponseHeaders()).thenReturn(new Headers());

            I18nHandler handler = new I18nHandler();
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(200), anyLong());

            String body = response.toString();
            assertTrue(body.contains("\"hello\":\"Hello\""));
        }
    }

    @Test
    void handleLanguagesShouldReturnLanguages() throws Exception {

        LocalizationSQL.LanguageItem lang =
                new LocalizationSQL.LanguageItem("en", "English", true, true);

        try (MockedConstruction<TranslationService> mocked =
                     mockConstruction(TranslationService.class,
                             (mock, context) -> {
                                 when(mock.getActiveLanguages())
                                         .thenReturn(List.of(lang));
                             })) {

            HttpExchange exchange = mock(HttpExchange.class);
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(new URI("/i18n/languages"));
            when(exchange.getResponseBody()).thenReturn(response);
            when(exchange.getResponseHeaders()).thenReturn(new Headers());

            I18nHandler handler = new I18nHandler();
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(200), anyLong());

            String body = response.toString();
            assertTrue(body.contains("\"code\":\"en\""));
        }
    }

    // 405 Method Not Allowed
    @Test
    void handleShouldReturn405IfNotGET() throws Exception {

        HttpExchange exchange = mock(HttpExchange.class);
        ByteArrayOutputStream response = new ByteArrayOutputStream();

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getResponseBody()).thenReturn(response);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        I18nHandler handler = new I18nHandler();
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());

        assertTrue(response.toString().contains("Method not allowed"));
    }

    // 404 Not Found
    @Test
    void handleShouldReturn404IfUnknownPath() throws Exception {

        HttpExchange exchange = mock(HttpExchange.class);
        ByteArrayOutputStream response = new ByteArrayOutputStream();

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/i18n/unknown"));
        when(exchange.getResponseBody()).thenReturn(response);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        I18nHandler handler = new I18nHandler();
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
    }

    // 500 Internal Server Error
    @Test
    void handleShouldReturn500IfExceptionThrown() throws Exception {

        try (MockedConstruction<TranslationService> mocked =
                     mockConstruction(TranslationService.class,
                             (mock, context) -> {
                                 when(mock.getUiTranslations(any()))
                                         .thenThrow(new RuntimeException("fail"));
                             })) {

            HttpExchange exchange = mock(HttpExchange.class);
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(new URI("/i18n/ui"));
            when(exchange.getResponseBody()).thenReturn(response);
            when(exchange.getResponseHeaders()).thenReturn(new Headers());

            I18nHandler handler = new I18nHandler();
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }
    }
}