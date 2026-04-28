package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import security.JwtService;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AdminClassesHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private AdminClassesHandler handler;
    private HttpExchange exchange;
    private final ObjectMapper om = new ObjectMapper();
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        handler = new AdminClassesHandler(jwtService, classSQL);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();
        handler = Mockito.spy(new AdminClassesHandler(jwtService, classSQL));

        doReturn(null).when(handler).requireAdmin(any(), any());
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // -----------------------------------------------------
    // Helpers
    // -----------------------------------------------------

    private void request(String method, String path, String body) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));

        if (body != null) {
            when(exchange.getRequestBody())
                    .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private Map<?, ?> jsonResponse() throws Exception {
        return om.readValue(responseBody.toByteArray(), Map.class);
    }

    private String rawResponse() {
        return responseBody.toString(StandardCharsets.UTF_8);
    }

    private void assertError(String message) throws Exception {
        assertEquals(message, jsonResponse().get("error"));
    }

    // -------------------------------------------------------------
    // GET /api/admin/classes
    // -------------------------------------------------------------
    @Test
    void getClasses_returns200AndList() throws Exception {
        var mockClass = mock(ClassSQL.ClassView.class);

        when(mockClass.id()).thenReturn(1L);
        when(mockClass.classCode()).thenReturn("MATH101");
        when(mockClass.name()).thenReturn("Math");
        when(mockClass.teacherEmail()).thenReturn("teacher@test.com");
        when(mockClass.semester()).thenReturn("Fall");
        when(mockClass.academicYear()).thenReturn("2025");
        when(mockClass.studentsCount()).thenReturn(30);

        when(classSQL.listAllForAdmin()).thenReturn(List.of(mockClass));

        request("GET", "/api/admin/classes", null);
        handler.handle(exchange);
        verify(classSQL).listAllForAdmin();
    }

    // -------------------------------------------------------------
    // POST /api/admin/classes
    // -------------------------------------------------------------
    @Test
    void postClasses_createsClassAndReturns201() throws Exception {
        request("POST", "/api/admin/classes", """
                {
                  "classCode": "MATH101",
                  "name": "Math",
                  "teacherEmail": "teacher@test.com"
                }
                """);

        when(classSQL.findTeacherIdByEmail("teacher@test.com")).thenReturn(5L);
        when(classSQL.createClass(anyString(), anyString(), anyLong(), any(), any(), any()))
                .thenReturn(100L);

        handler.handle(exchange);

        verify(classSQL).createClass(eq("MATH101"), eq("Math"), eq(5L), any(), any(), any());
    }

    @Test
    void createClass_invalidJson_returns400() throws Exception {
        request("POST", "/api/admin/classes", "{invalid}");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertError("Invalid JSON");
    }

    @Test
    void createClass_teacherNotFound_returns400() throws Exception {
        request("POST", "/api/admin/classes", """
                {
                  "classCode": "MATH101",
                  "name": "Math",
                  "teacherEmail": "missing@test.com"
                }
                """);

        when(classSQL.findTeacherIdByEmail("missing@test.com")).thenReturn(null);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(jsonResponse().get("error").toString().contains("Teacher not found"));
    }

    // -----------------------------
    // GET available students
    // -----------------------------
    @Test
    void availableStudents_success() throws Exception {
        when(classSQL.listStudentsNotEnrolledInClass("CS101"))
                .thenReturn(List.of());

        request("GET", "/api/admin/classes/CS101/available-students", null);

        handler.handle(exchange);

        verify(classSQL).listStudentsNotEnrolledInClass("CS101");
    }

    @Test
    void availableStudents_missingClass_returns400() throws Exception {
        request("GET", "/api/admin/classes//available-students", null);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertError("Missing class code");
    }

    @Test
    void availableStudents_wrongMethod_returns405() throws Exception {
        request("POST", "/api/admin/classes/CS101/available-students", "[]");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(rawResponse().contains("Method Not Allowed"));
    }


    // -----------------------------
    // POST enroll students
    // -----------------------------
    @Test
    void enroll_success() throws Exception {
        request("POST", "/api/admin/classes/CS101/enroll",
                "[\"a@test.com\",\"b@test.com\"]");

        handler.handle(exchange);

        verify(classSQL).enrollStudentsByEmails(
                "CS101",
                List.of("a@test.com", "b@test.com")
        );
    }

    @Test
    void enroll_invalidJson_returns400() throws Exception {
        request("POST", "/api/admin/classes/CS101/enroll", "{bad}");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertEquals(
                "Invalid JSON body, expected array of student emails",
                jsonResponse().get("error")
        );

        verify(classSQL, never()).enrollStudentsByEmails(any(), any());
    }

    @Test
    void enroll_missingClass_returns400() throws Exception {
        request("POST", "/api/admin/classes//enroll", "[]");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertError("Missing class code");
    }

    @Test
    void enroll_wrongMethod_returns405() throws Exception {
        request("GET", "/api/admin/classes/CS101/enroll", null);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(rawResponse().contains("Method Not Allowed"));
    }

    // -----------------------------
    // Routing
    // -----------------------------

    @Test
    void unknownRoute_returns404() throws Exception {
        request("GET", "/api/admin/classes/random/route", null);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertEquals("Not Found", jsonResponse().get("error"));
    }
}