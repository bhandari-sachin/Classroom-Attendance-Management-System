package frontend.student;

import frontend.auth.AppRouter;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StudentMarkAttendancePageTest {

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
    void safeMessageShouldReturnUnknownErrorForNullThrowable() {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        assertEquals("Unknown error", page.safeMessage(null));
    }

    @Test
    void safeMessageShouldReturnUnknownErrorForNullMessage() {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        assertEquals("Unknown error", page.safeMessage(new RuntimeException((String) null)));
    }

    @Test
    void safeMessageShouldReturnUnknownErrorForBlankMessage() {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        assertEquals("Unknown error", page.safeMessage(new RuntimeException("   ")));
    }

    @Test
    void safeMessageShouldReturnActualMessage() {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        assertEquals("Boom", page.safeMessage(new RuntimeException("Boom")));
    }

    @Test
    void buildTitleShouldCreateStyledLabel() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        Label label = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildTitle",
                new Class[]{}
        ));

        assertNotNull(label);
        assertTrue(label.getStyleClass().contains("dash-title"));
        assertNotNull(label.getText());
        assertFalse(label.getText().isBlank());
    }

    @Test
    void buildSubtitleShouldCreateStyledLabel() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        Label label = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildSubtitle",
                new Class[]{}
        ));

        assertNotNull(label);
        assertTrue(label.getStyleClass().contains("dash-subtitle"));
        assertNotNull(label.getText());
        assertFalse(label.getText().isBlank());
    }

    @Test
    void buildCodeFieldShouldCreateStyledTextField() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        TextField field = runOnFxThread(() -> (TextField) invokePrivate(
                page,
                "buildCodeField",
                new Class[]{}
        ));

        assertNotNull(field);
        assertTrue(field.getStyleClass().contains("code-field"));
        assertNotNull(field.getPromptText());
        assertFalse(field.getPromptText().isBlank());
    }

    @Test
    void buildHowStepShouldCreateStyledLabel() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        Label step = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildHowStep",
                new Class[]{String.class},
                "Step text"
        ));

        assertNotNull(step);
        assertEquals("Step text", step.getText());
        assertTrue(step.getStyleClass().contains("how-step"));
    }

    @Test
    void buildQrCardShouldCreateCenteredCard() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                page,
                "buildQrCard",
                new Class[]{}
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("qr-card"));
        assertEquals(1, card.getChildren().size());
    }

    @Test
    void buildHowCardShouldCreateCardWithFourChildren() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                page,
                "buildHowCard",
                new Class[]{}
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("how-card"));
        assertEquals(4, card.getChildren().size());
        assertInstanceOf(Label.class, card.getChildren().get(0));
    }

    @Test
    void buildManualCardShouldCreateExpectedStructure() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();
        TextField field = new TextField();
        Button submit = new Button("Submit");

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                page,
                "buildManualCard",
                new Class[]{TextField.class, Button.class},
                field,
                submit
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("manual-card"));
        assertEquals(5, card.getChildren().size());
        assertTrue(card.getChildren().contains(field));
        assertTrue(card.getChildren().contains(submit));
    }

    @Test
    void buildBackButtonShouldNavigateToStudentDashboard() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();
        TestRouter router = new TestRouter();

        Button back = runOnFxThread(() -> (Button) invokePrivate(
                page,
                "buildBackButton",
                new Class[]{AppRouter.class},
                router
        ));

        assertNotNull(back);
        assertTrue(back.getStyleClass().contains("back-link"));

        runOnFxThread(() -> {
            back.fire();
            return null;
        });

        assertEquals("student-dashboard", router.lastRoute);
    }

    @Test
    void buildSubmitButtonShouldCreateStyledButton() throws Exception {
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();
        TextField field = new TextField();

        Button submit = runOnFxThread(() -> (Button) invokePrivate(
                page,
                "buildSubmitButton",
                new Class[]{TextField.class, frontend.auth.JwtStore.class, frontend.auth.AuthState.class},
                field,
                null,
                null
        ));

        assertNotNull(submit);
        assertTrue(submit.getStyleClass().contains("submit-button"));
        assertEquals(Double.MAX_VALUE, submit.getMaxWidth());
    }

    private static Object invokePrivate(
            Object target,
            String methodName,
            Class<?>[] paramTypes,
            Object... args
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
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

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get() throws Exception;
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
}