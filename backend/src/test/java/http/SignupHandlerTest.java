package http;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupHandlerTest {

    @Test
    void methodNotAllowed_returns405() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/signup", null);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        assertEquals("Method Not Allowed", ex.responseBodyString());
        verifyNoInteractions(users);
    }

    @Test
    void invalidJson_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", "{bad json");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid JSON"));
        verifyNoInteractions(users);
    }

    @Test
    void missingRequiredFields_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        // missing lastName + role
        String json = """
            {"email":"a@b.com","password":"pw","firstName":"A"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("email, password, firstName, lastName, role are required"));
        verifyNoInteractions(users);
    }

    @Test
    void invalidRole_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        String json = """
            {"email":"a@b.com","password":"pw","firstName":"A","lastName":"B","role":"MANAGER"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("role must be STUDENT or TEACHER"));
        verifyNoInteractions(users);
    }

    @Test
    void adminCannotSelfRegister_returns403() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        String json = """
            {"email":"admin@b.com","password":"pw","firstName":"A","lastName":"B","role":"ADMIN"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Admin cannot self-register"));
        verifyNoInteractions(users);
    }

    @Test
    void studentMissingStudentCode_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        String json = """
            {"email":"s@b.com","password":"pw","firstName":"S","lastName":"T","role":"STUDENT"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Student code required"));
        verifyNoInteractions(users);
    }

    @Test
    void emailAlreadyExists_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        when(users.existsByEmail("a@b.com")).thenReturn(true);

        String json = """
            {"email":"a@b.com","password":"pw","firstName":"A","lastName":"B","role":"TEACHER"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Email already exists"));
        verify(users).existsByEmail("a@b.com");
        verify(users, never()).insert(any(), any(), any(), any(), any(), any());
    }

    @Test
    void teacher_forcesStudentCodeNull_andCreatesUser_returns201() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        when(users.existsByEmail("t@b.com")).thenReturn(false);

        // even if studentCode is provided, it MUST be set to null for TEACHER
        String json = """
            {"email":"t@b.com","password":"pw","firstName":"T","lastName":"E","role":"TEACHER","studentCode":"SHOULD-NOT-SAVE"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(201, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("created"));

        // Capture insert args
        ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hashCap  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fnCap    = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> lnCap    = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCap  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> scCap    = ArgumentCaptor.forClass(String.class);

        verify(users).insert(emailCap.capture(), hashCap.capture(), fnCap.capture(), lnCap.capture(), roleCap.capture(), scCap.capture());

        assertEquals("t@b.com", emailCap.getValue());
        assertEquals("T", fnCap.getValue());
        assertEquals("E", lnCap.getValue());
        assertEquals("TEACHER", roleCap.getValue());
        assertNull(scCap.getValue(), "studentCode must be NULL for non-students");
        assertNotNull(hashCap.getValue());
        assertTrue(hashCap.getValue().startsWith("$2"), "bcrypt hash should start with $2...");
    }

    @Test
    void student_success_returns201_andSavesStudentCode() throws Exception {
        UserRepository users = mock(UserRepository.class);
        var handler = new SignupHandler(users);

        when(users.existsByEmail("s@b.com")).thenReturn(false);

        String json = """
            {"email":"s@b.com","password":"pw","firstName":"S","lastName":"T","role":"student","studentCode":"STU001"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/signup", json);

        handler.handle(ex);

        assertEquals(201, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("created"));

        verify(users).insert(
                eq("s@b.com"),
                anyString(),      // hash
                eq("S"),
                eq("T"),
                eq("STUDENT"),
                eq("STU001")
        );
    }
}