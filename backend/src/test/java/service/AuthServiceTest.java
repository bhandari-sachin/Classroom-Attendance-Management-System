package service;

import dto.AuthResponse;
import backend.exception.ApiException;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import repository.UserRepository;
import security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Login tests
    @Test
    void loginShouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> authService.login("test@test.com", "pass"));
    }

    @Test
    void loginShouldThrowIfPasswordWrong() {
        User user = new User(1L, "test@test.com", "HASH", "A", "B", UserRole.STUDENT, "code");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "HASH"))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> authService.login("test@test.com", "wrong"));
    }

    @Test
    void loginShouldReturnAuthResponseIfSuccess() {
        User user = new User(1L, "test@test.com", "HASH", "A", "B", UserRole.STUDENT, "code");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("pass", "HASH"))
                .thenReturn(true);

        when(jwtService.issueToken(1L, "test@test.com", "STUDENT"))
                .thenReturn("JWT_TOKEN");

        AuthResponse response = authService.login("test@test.com", "pass");

        assertEquals("JWT_TOKEN", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
    }

    // Signup tests
    @Test
    void signupShouldThrowIfEmailExists() {
        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        assertThrows(ApiException.class,
                () -> authService.signup(
                        "test@test.com", "pass",
                        "A", "B",
                        UserRole.STUDENT,
                        "code"
                ));
    }

    @Test
    void signupShouldThrowIfAdminRegistration() {
        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> authService.signup(
                        "test@test.com", "pass",
                        "A", "B",
                        UserRole.ADMIN,
                        null
                ));
    }

    @Test
    void signupShouldThrowIfStudentWithoutCode() {
        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> authService.signup(
                        "test@test.com", "pass",
                        "A", "B",
                        UserRole.STUDENT,
                        ""
                ));
    }

    @Test
    void signupShouldSaveUserCorrectlyForStudent() {
        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("pass"))
                .thenReturn("HASH");

        authService.signup(
                "test@test.com",
                "pass",
                "John",
                "Doe",
                UserRole.STUDENT,
                "STU123"
        );

        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();

        assertEquals("test@test.com", saved.getEmail());
        assertEquals("HASH", saved.getPasswordHash());
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals(UserRole.STUDENT, saved.getUserType());
        assertEquals("STU123", saved.getStudentCode());
    }

    @Test
    void signupShouldNullStudentCodeForTeacher() {
        when(userRepository.existsByEmail("teacher@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("pass"))
                .thenReturn("HASH");

        authService.signup(
                "teacher@test.com",
                "pass",
                "Jane",
                "Doe",
                UserRole.TEACHER,
                "SHOULD_BE_REMOVED"
        );

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertNull(saved.getStudentCode());
    }
}