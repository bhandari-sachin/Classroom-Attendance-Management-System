package frontend.student;

import frontend.auth.AppRouter;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StudentDashboardAppTest {

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
    void numShouldReturnZeroForNull() {
        assertEquals(0, StudentDashboardApp.num(null));
    }

    @Test
    void numShouldReturnIntValueForNumber() {
        assertEquals(7, StudentDashboardApp.num(7));
        assertEquals(12, StudentDashboardApp.num(12.8));
    }

    @Test
    void numShouldParseStringNumber() {
        assertEquals(42, StudentDashboardApp.num("42"));
    }

    @Test
    void numShouldReturnZeroForInvalidValue() {
        assertEquals(0, StudentDashboardApp.num("abc"));
    }

    @Test
    void pctShouldReturnZeroPercentForNull() {
        assertEquals("0%", StudentDashboardApp.pct(null));
    }

    @Test
    void pctShouldRoundNumberValues() {
        assertEquals("85%", StudentDashboardApp.pct(84.6));
        assertEquals("91%", StudentDashboardApp.pct(91));
    }

    @Test
    void pctShouldParseStringNumbers() {
        assertEquals("73%", StudentDashboardApp.pct("72.6"));
    }

    @Test
    void pctShouldReturnZeroPercentForInvalidValue() {
        assertEquals("0%", StudentDashboardApp.pct("oops"));
    }

    @Test
    void resolveSummaryValueShouldReturnNumericStringForNonPercentage() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("5", app.resolveSummaryValue(Map.of("presentCount", 5), "presentCount", false));
    }

    @Test
    void resolveSummaryValueShouldReturnZeroForMissingNonPercentageKey() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("0", app.resolveSummaryValue(Map.of(), "presentCount", false));
    }

    @Test
    void resolveSummaryValueShouldReturnPercentForPercentageKey() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("88%", app.resolveSummaryValue(Map.of("attendanceRate", 87.6), "attendanceRate", true));
    }

    @Test
    void resolveSummaryValueShouldReturnZeroPercentForMissingPercentageKey() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("0%", app.resolveSummaryValue(Map.of(), "attendanceRate", true));
    }

    @Test
    void resolveSummaryValueShouldHandleNullSummary() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("0", app.resolveSummaryValue(null, "presentCount", false));
        assertEquals("0%", app.resolveSummaryValue(null, "attendanceRate", true));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorForNullThrowable() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("Unknown error", app.safeErrorMessage(null));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorForNullMessage() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("Unknown error", app.safeErrorMessage(new RuntimeException((String) null)));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorForBlankMessage() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("Unknown error", app.safeErrorMessage(new RuntimeException("   ")));
    }

    @Test
    void safeErrorMessageShouldReturnActualMessage() {
        StudentDashboardApp app = new StudentDashboardApp();

        assertEquals("Boom", app.safeErrorMessage(new RuntimeException("Boom")));
    }

    @Test
    void createStatValueLabelShouldReturnZeroLabel() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();

        Label label = runOnFxThread(() -> (Label) invokePrivate(
                app,
                "createStatValueLabel",
                new Class[]{}
        ));

        assertEquals("0", label.getText());
    }

    @Test
    void createRateValueLabelShouldReturnZeroPercentLabel() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();

        Label label = runOnFxThread(() -> (Label) invokePrivate(
                app,
                "createRateValueLabel",
                new Class[]{}
        ));

        assertEquals("0%", label.getText());
    }

    @Test
    void statCardWithBadgeShouldBuildCardAndAddStatValueStyle() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();
        Label valueLabel = new Label("10");

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                app,
                "statCardWithBadge",
                new Class[]{String.class, Label.class, String.class, String.class, String.class},
                "Present",
                valueLabel,
                "hint",
                "#3BAA66",
                "✓"
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("stat-card"));
        assertTrue(valueLabel.getStyleClass().contains("stat-value"));
        assertEquals(2, card.getChildren().size());
    }

    @Test
    void buildEmptyClassesCardShouldCreateExpectedCard() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                app,
                "buildEmptyClassesCard",
                new Class[]{}
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("classes-card"));
        assertTrue(card.getChildren().size() >= 3);
        assertInstanceOf(Label.class, card.getChildren().getFirst());
    }

    @Test
    void buildClassesHeaderShouldCreateHeaderWithViewAllButton() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();
        TestRouter router = new TestRouter();

        HBox header = runOnFxThread(() -> (HBox) invokePrivate(
                app,
                "buildClassesHeader",
                new Class[]{AppRouter.class},
                router
        ));

        assertNotNull(header);
        Button button = findFirstButton(header);
        assertNotNull(button);

        runOnFxThread(() -> {
            button.fire();
            return null;
        });

        assertEquals("student-attendance", router.lastRoute);
    }

    @Test
    void buildAttendanceCardShouldNavigateToStudentMark() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();
        TestRouter router = new TestRouter();

        Button button = runOnFxThread(() -> (Button) invokePrivate(
                app,
                "buildAttendanceCard",
                new Class[]{AppRouter.class},
                router
        ));

        assertNotNull(button);
        assertTrue(button.getStyleClass().contains("attendance-card"));

        runOnFxThread(() -> {
            button.fire();
            return null;
        });

        assertEquals("student-mark", router.lastRoute);
    }

    @Test
    void buildStatsGridShouldCreateGridWithFourCards() throws Exception {
        StudentDashboardApp app = new StudentDashboardApp();

        Label present = new Label("1");
        Label absent = new Label("2");
        Label excused = new Label("3");
        Label rate = new Label("75%");

        GridPane grid = runOnFxThread(() -> (GridPane) invokePrivate(
                app,
                "buildStatsGrid",
                new Class[]{Label.class, Label.class, Label.class, Label.class},
                present,
                absent,
                excused,
                rate
        ));

        assertNotNull(grid);
        assertTrue(grid.getStyleClass().contains("dash-stats"));
        assertEquals(2, grid.getColumnConstraints().size());
        assertEquals(4, grid.getChildren().size());
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

    private static Button findFirstButton(javafx.scene.Parent parent) {
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Button button) {
                return button;
            }
            if (child instanceof javafx.scene.Parent nested) {
                Button found = findFirstButton(nested);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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