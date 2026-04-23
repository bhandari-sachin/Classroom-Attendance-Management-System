package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LoginPageTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void constructorShouldBuildLoginForm() throws Exception {
        LoginPage page = createPage(new TestRouter(), new SuccessfulAuthService(), new TestJwtStore());

        TextField emailField = findEmailField(page);
        PasswordField passwordField = findPasswordField(page);
        Button loginButton = findButtonByText(page, "Login");
        Button signupButton = findButtonByText(page, "Create account");

        assertNotNull(emailField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        assertNotNull(signupButton);
    }

    @Test
    void signupButtonShouldNavigateToSignup() throws Exception {
        TestRouter router = new TestRouter();
        LoginPage page = createPage(router, new SuccessfulAuthService(), new TestJwtStore());

        Button signupButton = findButtonByText(page, "Create account");
        assertNotNull(signupButton);

        runOnFxThread(() -> {
            signupButton.fire();
            return null;
        });

        assertEquals("signup", router.lastRoute);
    }

    @Test
    void loginShouldShowErrorWhenFieldsAreEmpty() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        LoginPage page = createPage(router, new SuccessfulAuthService(), store);

        TextField emailField = findEmailField(page);
        PasswordField passwordField = findPasswordField(page);
        Button loginButton = findButtonByText(page, "Login");
        Label errorLabel = findLabelByStyleClass(page);

        runOnFxThread(() -> {
            emailField.setText("   ");
            passwordField.setText("");
            loginButton.fire();
            return null;
        });

        waitForFxEvents();

        assertNotNull(errorLabel);
        assertTrue(errorLabel.isVisible());
        assertTrue(errorLabel.isManaged());
        assertFalse(errorLabel.getText().isBlank());
        assertNull(router.lastRoute);
        assertNull(store.savedState);
    }

    @Test
    void loginShouldSaveStateAndRedirectOnSuccess() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        LoginPage page = createPage(router, new SuccessfulAuthService(), store);

        TextField emailField = findEmailField(page);
        PasswordField passwordField = findPasswordField(page);
        Button loginButton = findButtonByText(page, "Login");

        runOnFxThread(() -> {
            emailField.setText("test@example.com");
            passwordField.setText("secret");
            loginButton.fire();
            return null;
        });

        waitUntil(() -> store.savedState != null);

        assertNotNull(store.savedState);
        assertEquals("demo-token", store.savedState.token());
        assertEquals(Role.STUDENT, store.savedState.role());
        assertEquals("Demo User", store.savedState.name());
        assertEquals("student-dashboard", router.lastRoute);
    }

    @Test
    void loginShouldShowErrorAndReEnableButtonOnFailure() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        LoginPage page = createPage(router, new FailingAuthService("Bad credentials"), store);

        TextField emailField = findEmailField(page);
        PasswordField passwordField = findPasswordField(page);
        Button loginButton = findButtonByText(page, "Login");
        Label errorLabel = findLabelByStyleClass(page);

        runOnFxThread(() -> {
            emailField.setText("wrong@example.com");
            passwordField.setText("wrong");
            loginButton.fire();
            return null;
        });

        waitUntil(errorLabel::isVisible);

        assertTrue(errorLabel.getText().contains("Bad credentials"));
        assertFalse(loginButton.isDisable());
        assertNull(router.lastRoute);
        assertNull(store.savedState);
    }

    @Test
    void loginShouldHandleInterruptedException() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        LoginPage page = createPage(router, new InterruptedAuthService(), store);

        TextField emailField = findEmailField(page);
        PasswordField passwordField = findPasswordField(page);
        Button loginButton = findButtonByText(page, "Login");
        Label errorLabel = findLabelByStyleClass(page);

        runOnFxThread(() -> {
            emailField.setText("user@example.com");
            passwordField.setText("pw");
            loginButton.fire();
            return null;
        });

        waitUntil(errorLabel::isVisible);

        assertFalse(errorLabel.getText().isBlank());
        assertFalse(loginButton.isDisable());
        assertNull(router.lastRoute);
        assertNull(store.savedState);
    }

    @Test
    void constructorShouldRedirectImmediatelyWhenAlreadyLoggedIn() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        store.loadedState = new AuthState("existing-token", Role.TEACHER, "Teacher User");

        createPage(router, new SuccessfulAuthService(), store);

        assertEquals("teacher-dashboard", router.lastRoute);
    }

    private LoginPage createPage(AppRouter router, AuthService authService, TestJwtStore store) throws Exception {
        return runOnFxThread(() -> new LoginPage(router, authService, store));
    }

    private static void waitForFxEvents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for FX events");
    }

    private static void waitUntil(Check condition) throws Exception {
        long deadline = System.currentTimeMillis() + 5 * 1000L;

        while (System.currentTimeMillis() < deadline) {
            waitForFxEvents();
            if (condition.ok()) {
                return;
            }
        }

        fail("Condition was not met in time");
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

        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX operation timed out");

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        return result.get();
    }


    private static Button findButtonByText(LoginPage page, String text) {
        return findAll(page, Button.class).stream()
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .orElse(null);
    }

    private static Label findLabelByStyleClass(LoginPage page) {
        return findAll(page, Label.class).stream()
                .filter(label -> label.getStyleClass().contains("error"))
                .findFirst()
                .orElse(null);
    }

    private static <T> List<T> findAll(javafx.scene.Parent root, Class<T> type) {
        List<T> results = new java.util.ArrayList<>();
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
        private AuthState savedState;

        @Override
        public Optional<AuthState> load() {
            return Optional.ofNullable(loadedState);
        }

        @Override
        public void save(AuthState authState) {
            this.savedState = authState;
        }

        @Override
        public void clear() {
            this.loadedState = null;
            this.savedState = null;
        }
    }

    private static class SuccessfulAuthService extends AuthService {
        SuccessfulAuthService() {
            super("http://localhost");
        }

        @Override
        public AuthState login(String email, String password) {
            return new AuthState("demo-token", Role.STUDENT, "Demo User");
        }
    }

    private static class FailingAuthService extends AuthService {
        private final String message;

        FailingAuthService(String message) {
            super("http://localhost");
            this.message = message;
        }

        @Override
        public AuthState login(String email, String password) {
            throw new RuntimeException(message);
        }
    }

    private static class InterruptedAuthService extends AuthService {
        InterruptedAuthService() {
            super("http://localhost");
        }

        @Override
        public AuthState login(String email, String password) throws InterruptedException {
            throw new InterruptedException("Interrupted");
        }
    }
    private static TextField findEmailField(LoginPage page) {
        return findAll(page, TextField.class).stream()
                .filter(field -> !(field instanceof PasswordField))
                .findFirst()
                .orElseThrow();
    }

    private static PasswordField findPasswordField(LoginPage page) {
        return findAll(page, PasswordField.class).stream()
                .findFirst()
                .orElseThrow();
    }
}