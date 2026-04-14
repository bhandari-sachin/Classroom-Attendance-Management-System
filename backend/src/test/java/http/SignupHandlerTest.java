package http;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupHandlerTest {

    // ---------------- helper ----------------

    private HttpExchange mockExchange(String method, String body) throws Exception {
        HttpExchange ex = mock(HttpExchange.class);

        when(ex.getRequestMethod()).thenReturn(method);
        when(ex.getRequestURI()).thenReturn(URI.create("/signup"));

        ByteArrayInputStream in =
                new ByteArrayInputStream(body.getBytes());
        when(ex.getRequestBody()).thenReturn(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(ex.getResponseBody()).thenReturn(out);

        when(ex.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        doNothing().when(ex).sendResponseHeaders(anyInt(), anyLong());
        doNothing().when(ex).close();

        return ex;
    }

    // ---------------- tests ----------------

    @Test
    void methodNotAllowed_returns405() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        SignupHandler handler = new SignupHandler(repo);

        HttpExchange ex = mockExchange("GET", "{}");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void invalidJson_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        SignupHandler handler = new SignupHandler(repo);

        HttpExchange ex = mockExchange("POST", "invalid-json");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void missingRequiredFields_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        SignupHandler handler = new SignupHandler(repo);

        String body = "{\"email\":\"test@test.com\"}";

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void adminRole_returns403() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        SignupHandler handler = new SignupHandler(repo);

        String body = """
        {
          "email":"a@a.com",
          "password":"123",
          "firstName":"A",
          "lastName":"B",
          "role":"ADMIN"
        }
        """;

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(403), anyLong());
    }

    @Test
    void studentWithoutCode_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        SignupHandler handler = new SignupHandler(repo);

        String body = """
        {
          "email":"s@a.com",
          "password":"123",
          "firstName":"S",
          "lastName":"T",
          "role":"STUDENT"
        }
        """;

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void emailExists_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        when(repo.existsByEmail("test@test.com")).thenReturn(true);

        SignupHandler handler = new SignupHandler(repo);

        String body = """
        {
          "email":"test@test.com",
          "password":"123",
          "firstName":"A",
          "lastName":"B",
          "role":"TEACHER"
        }
        """;

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void validTeacherSignup_returns201() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        when(repo.existsByEmail("t@t.com")).thenReturn(false);

        SignupHandler handler = new SignupHandler(repo);

        String body = """
        {
          "email":"t@t.com",
          "password":"123",
          "firstName":"John",
          "lastName":"Doe",
          "role":"TEACHER"
        }
        """;

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(repo).insert(
                eq("t@t.com"),
                anyString(),
                eq("John"),
                eq("Doe"),
                eq("TEACHER"),
                isNull()
        );

        verify(ex).sendResponseHeaders(eq(201), anyLong());
    }

    @Test
    void validStudentSignup_returns201() throws Exception {
        UserRepository repo = mock(UserRepository.class);

        when(repo.existsByEmail("s@a.com")).thenReturn(false);

        SignupHandler handler = new SignupHandler(repo);

        String body = """
        {
          "email":"s@a.com",
          "password":"123",
          "firstName":"Sam",
          "lastName":"Smith",
          "role":"STUDENT",
          "studentCode":"S123"
        }
        """;

        HttpExchange ex = mockExchange("POST", body);

        handler.handle(ex);

        verify(repo).insert(
                eq("s@a.com"),
                anyString(),
                eq("Sam"),
                eq("Smith"),
                eq("STUDENT"),
                eq("S123")
        );

        verify(ex).sendResponseHeaders(eq(201), anyLong());
    }
}