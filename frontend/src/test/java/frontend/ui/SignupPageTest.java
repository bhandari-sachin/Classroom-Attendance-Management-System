package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class SignupPageTest {

    private static final int FX_TIMEOUT_SECONDS = 5;
    private static final int WAIT_TIMEOUT_MS = 5000;
    private static final int WAIT_STEP_MS = 50;

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void constructorShouldBuildSignupForm() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        assertNotNull(findTextFieldByPrompt(page, "First name"));
        assertNotNull(findTextFieldByPrompt(page, "Last name"));
        assertNotNull(findTextFieldByPrompt(page, "Email"));
        assertNotNull(findPasswordField(page));
        assertNotNull(findRoleComboBox(page));
        assertNotNull(findTextFieldByPrompt(page, "Enter student code"));
        assertNotNull(findButtonByText(page, "Sign Up"));
        assertNotNull(findButtonByText(page, "Back to Login"));
    }

    @Test
    void loginButtonShouldNavigateToLogin() throws Exception {
        TestRouter router = new TestRouter();
        SignupPage page = createPage(router, new SuccessfulAuthService(), new TestJwtStore());

        Button loginButton = findButtonByText(page, "Back to Login");
        assertNotNull(loginButton);

        runOnFxThread(() -> {
            loginButton.fire();
            return null;
        });

        assertEquals("login", router.lastRoute);
    }

    @Test
    void constructorShouldRedirectImmediatelyWhenAlreadyLoggedIn() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        store.loadedState = new AuthState("existing-token", Role.STUDENT, "Demo");

        createPage(router, new SuccessfulAuthService(), store);

        assertEquals("student-dashboard", router.lastRoute);
    }

    @Test
    void studentCodeShouldBeVisibleForStudentRole() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            return null;
        });

        waitForFxEvents();

        assertTrue(studentCodeField.isVisible());
        assertTrue(studentCodeField.isManaged());
    }

    @Test
    void studentCodeShouldBeHiddenForTeacherRole() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.TEACHER);
            return null;
        });

        waitForFxEvents();

        assertFalse(studentCodeField.isVisible());
        assertFalse(studentCodeField.isManaged());
    }

    @Test
    void signupShouldShowErrorWhenRequiredFieldsAreMissing() throws Exception {
        TestRouter router = new TestRouter();
        SignupPage page = createPage(router, new SuccessfulAuthService(), new TestJwtStore());

        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            firstNameField.setText("");
            lastNameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            studentCodeField.setText("");
            signupButton.fire();
            return null;
        });

        waitForFxEvents();

        assertNotNull(errorLabel);
        assertTrue(errorLabel.isVisible());
        assertTrue(errorLabel.isManaged());
        assertFalse(errorLabel.getText().isBlank());
        assertNull(router.lastRoute);
    }

    @Test
    void signupShouldShowErrorForInvalidEmail() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            firstNameField.setText("Farah");
            lastNameField.setText("Test");
            emailField.setText("invalid-email");
            passwordField.setText("123456");
            studentCodeField.setText("ST123");
            signupButton.fire();
            return null;
        });

        waitForFxEvents();

        assertTrue(errorLabel.isVisible());
        assertFalse(errorLabel.getText().isBlank());
    }

    @Test
    void signupShouldShowErrorForShortPassword() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            firstNameField.setText("Farah");
            lastNameField.setText("Test");
            emailField.setText("farah@test.com");
            passwordField.setText("123");
            studentCodeField.setText("ST123");
            signupButton.fire();
            return null;
        });

        waitForFxEvents();

        assertTrue(errorLabel.isVisible());
        assertFalse(errorLabel.getText().isBlank());
    }

    @Test
    void signupShouldRequireStudentCodeForStudentRole() throws Exception {
        SignupPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            firstNameField.setText("Farah");
            lastNameField.setText("Test");
            emailField.setText("farah@test.com");
            passwordField.setText("123456");
            studentCodeField.setText("");
            signupButton.fire();
            return null;
        });

        waitForFxEvents();

        assertTrue(errorLabel.isVisible());
        assertFalse(errorLabel.getText().isBlank());
    }

    @Test
    void signupShouldSucceedForTeacherWithoutStudentCode() throws Exception {
        TestRouter router = new TestRouter();
        SuccessfulAuthService authService = new SuccessfulAuthService();
        SignupPage page = createPage(router, authService, new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        Button signupButton = findButtonByText(page, "Sign Up");
        Label infoLabel = findInfoLabel(page);

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.TEACHER);
            firstNameField.setText("Farah");
            lastNameField.setText("Teacher");
            emailField.setText("teacher@test.com");
            passwordField.setText("123456");
            signupButton.fire();
            return null;
        });

        waitUntil(() -> authService.called);
        waitUntil(() -> "login".equals(router.lastRoute));

        assertEquals("Farah", authService.firstName);
        assertEquals("Teacher", authService.lastName);
        assertEquals("teacher@test.com", authService.email);
        assertEquals("123456", authService.password);
        assertEquals(Role.TEACHER, authService.role);
        assertNull(authService.studentCode);
        assertFalse(signupButton.isDisable());
        assertNotNull(infoLabel);
        assertTrue(infoLabel.isVisible());
    }

    @Test
    void signupShouldSucceedForStudentWithStudentCode() throws Exception {
        TestRouter router = new TestRouter();
        SuccessfulAuthService authService = new SuccessfulAuthService();
        SignupPage page = createPage(router, authService, new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            firstNameField.setText("Farah");
            lastNameField.setText("Student");
            emailField.setText("student@test.com");
            passwordField.setText("123456");
            studentCodeField.setText("ST999");
            signupButton.fire();
            return null;
        });

        waitUntil(() -> authService.called);
        waitUntil(() -> "login".equals(router.lastRoute));

        assertEquals("Farah", authService.firstName);
        assertEquals("Student", authService.lastName);
        assertEquals("student@test.com", authService.email);
        assertEquals("123456", authService.password);
        assertEquals(Role.STUDENT, authService.role);
        assertEquals("ST999", authService.studentCode);
        assertFalse(signupButton.isDisable());
    }

    @Test
    void signupShouldShowErrorAndReEnableButtonOnRuntimeFailure() throws Exception {
        SignupPage page = createPage(new TestRouter(), new FailingAuthService("Signup failed"), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            firstNameField.setText("Farah");
            lastNameField.setText("Student");
            emailField.setText("student@test.com");
            passwordField.setText("123456");
            studentCodeField.setText("ST999");
            signupButton.fire();
            return null;
        });

        waitUntil(errorLabel::isVisible);

        assertTrue(errorLabel.getText().contains("Signup failed"));
        assertFalse(signupButton.isDisable());
    }

    @Test
    void signupShouldShowErrorAndReEnableButtonOnIoFailure() throws Exception {
        SignupPage page = createPage(new TestRouter(), new IoFailingAuthService("IO failed"), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            firstNameField.setText("Farah");
            lastNameField.setText("Student");
            emailField.setText("student@test.com");
            passwordField.setText("123456");
            studentCodeField.setText("ST999");
            signupButton.fire();
            return null;
        });

        waitUntil(errorLabel::isVisible);

        assertTrue(errorLabel.getText().contains("IO failed"));
        assertFalse(signupButton.isDisable());
    }

    @Test
    void signupShouldHandleInterruptedException() throws Exception {
        SignupPage page = createPage(new TestRouter(), new InterruptedAuthService(), new TestJwtStore());

        ComboBox<Role> roleComboBox = findRoleComboBox(page);
        TextField firstNameField = findTextFieldByPrompt(page, "First name");
        TextField lastNameField = findTextFieldByPrompt(page, "Last name");
        TextField emailField = findTextFieldByPrompt(page, "Email");
        PasswordField passwordField = findPasswordField(page);
        TextField studentCodeField = findTextFieldByPrompt(page, "Enter student code");
        Button signupButton = findButtonByText(page, "Sign Up");
        Label errorLabel = findErrorLabel(page);

        runOnFxThread(() -> {
            roleComboBox.setValue(Role.STUDENT);
            firstNameField.setText("Farah");
            lastNameField.setText("Student");
            emailField.setText("student@test.com");
            passwordField.setText("123456");
            studentCodeField.setText("ST999");
            signupButton.fire();
            return null;
        });

        waitUntil(errorLabel::isVisible);

        assertFalse(errorLabel.getText().isBlank());
        assertFalse(signupButton.isDisable());
    }

    private SignupPage createPage(AppRouter router, AuthService authService, TestJwtStore store) throws Exception {
        return runOnFxThread(() -> new SignupPage(router, authService, store));
    }

    private static void waitForFxEvents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS), "Timed out waiting for FX events");
    }

    private static void waitUntil(Check condition) throws Exception {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            waitForFxEvents();
            if (condition.ok()) {
                return;
            }
            pauseBriefly();
        }

        fail("Condition was not met in time");
    }

    private static void pauseBriefly() {
        LockSupport.parkNanos(WAIT_STEP_MS * 1_000_000L);
    }

    private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS), "FX operation timed out");

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    private static PasswordField findPasswordField(SignupPage page) {
        return findAll(page, PasswordField.class).stream()
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<Role> findRoleComboBox(SignupPage page) {
        return findAll(page, ComboBox.class).stream()
                .findFirst()
                .orElseThrow();
    }

    private static TextField findTextFieldByPrompt(SignupPage page, String prompt) {
        return findAll(page, TextField.class).stream()
                .filter(field -> !(field instanceof PasswordField))
                .filter(field -> prompt.equals(field.getPromptText()))
                .findFirst()
                .orElseThrow();
    }

    private static Button findButtonByText(SignupPage page, String text) {
        return findAll(page, Button.class).stream()
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .orElse(null);
    }

    private static Label findErrorLabel(SignupPage page) {
        return findAll(page, Label.class).stream()
                .filter(label -> label.getStyleClass().contains("error"))
                .findFirst()
                .orElse(null);
    }

    private static Label findInfoLabel(SignupPage page) {
        List<Label> labels = findAll(page, Label.class).stream()
                .filter(label -> label.getStyleClass().contains("subtitle"))
                .toList();
        return labels.size() > 1 ? labels.get(1) : null;
    }

    private static <T> List<T> findAll(javafx.scene.Parent root, Class<T> type) {
        List<T> results = new ArrayList<>();
        collect(root, type, results);
        return results;
    }

    private static <T> void collect(javafx.scene.Node node, Class<T> type, List<T> results) {
        if (type.isInstance(node)) {
            results.add(type.cast(node));
        }

        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                collect(child, type, results);
            }
        }
    }

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface Check {
        boolean ok();
    }

    private static class TestRouter extends AppRouter {
        private String lastRoute;

        TestRouter() {
            super(new Scene(new StackPane()));
        }

        @Override
        public void go(String route) {
            this.lastRoute = route;
        }
    }

    private static class TestJwtStore extends JwtStore {
        private AuthState loadedState;

        @Override
        public Optional<AuthState> load() {
            return Optional.ofNullable(loadedState);
        }
    }

    private static class SuccessfulAuthService extends AuthService {
        private volatile boolean called;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private Role role;
        private String studentCode;

        SuccessfulAuthService() {
            super("http://localhost");
        }

        @Override
        public void signup(String firstName,
                           String lastName,
                           String email,
                           String password,
                           Role role,
                           String studentCode) {
            this.called = true;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.role = role;
            this.studentCode = studentCode;
        }
    }

    private static class FailingAuthService extends AuthService {
        private final String message;

        FailingAuthService(String message) {
            super("http://localhost");
            this.message = message;
        }

        @Override
        public void signup(String firstName,
                           String lastName,
                           String email,
                           String password,
                           Role role,
                           String studentCode) {
            throw new RuntimeException(message);
        }
    }

    private static class IoFailingAuthService extends AuthService {
        private final String message;

        IoFailingAuthService(String message) {
            super("http://localhost");
            this.message = message;
        }

        @Override
        public void signup(String firstName,
                           String lastName,
                           String email,
                           String password,
                           Role role,
                           String studentCode) throws IOException {
            throw new IOException(message);
        }
    }

    private static class InterruptedAuthService extends AuthService {
        InterruptedAuthService() {
            super("http://localhost");
        }

        @Override
        public void signup(String firstName,
                           String lastName,
                           String email,
                           String password,
                           Role role,
                           String studentCode) throws InterruptedException {
            throw new InterruptedException("Interrupted");
        }
    }
}