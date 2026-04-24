package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.UiPreferences;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StudentAttendancePageTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
        UiPreferences.setLanguage("en");
    }

    @Test
    void numShouldReturnZeroForNull() throws Exception {
        assertEquals(0, invokePrivateStatic("num", new Class[]{Object.class}, new Object[]{null}));
    }

    @Test
    void numShouldReturnIntValueForNumber() throws Exception {
        assertEquals(7, invokePrivateStatic("num", new Class[]{Object.class}, 7));
        assertEquals(12, invokePrivateStatic("num", new Class[]{Object.class}, 12.9));
    }

    @Test
    void numShouldParseStringNumber() throws Exception {
        assertEquals(42, invokePrivateStatic("num", new Class[]{Object.class}, "42"));
    }

    @Test
    void numShouldReturnZeroForInvalidValue() throws Exception {
        assertEquals(0, invokePrivateStatic("num", new Class[]{Object.class}, "abc"));
    }

    @Test
    void dblShouldReturnZeroForNull() throws Exception {
        assertEquals(0.0, (Double) invokePrivateStatic("dbl", new Class[]{Object.class}, new Object[]{null}), 0.0001);
    }

    @Test
    void dblShouldReturnDoubleValueForNumber() throws Exception {
        assertEquals(8.5, (Double) invokePrivateStatic("dbl", new Class[]{Object.class}, 8.5), 0.0001);
        assertEquals(3.0, (Double) invokePrivateStatic("dbl", new Class[]{Object.class}, 3), 0.0001);
    }

    @Test
    void dblShouldParseStringNumber() throws Exception {
        assertEquals(15.25, (Double) invokePrivateStatic("dbl", new Class[]{Object.class}, "15.25"), 0.0001);
    }

    @Test
    void dblShouldReturnZeroForInvalidValue() throws Exception {
        assertEquals(0.0, (Double) invokePrivateStatic("dbl", new Class[]{Object.class}, "not-a-number"), 0.0001);
    }

    @Test
    void localizeAttendanceStatusShouldReturnDashForNullOrBlank() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        assertEquals("—", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, new Object[]{null}));
        assertEquals("—", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, "   "));
    }

    @Test
    void localizeAttendanceStatusShouldMapKnownStatuses() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        assertEquals("Present", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, "PRESENT"));
        assertEquals("Absent", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, "ABSENT"));
        assertEquals("Excused", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, "EXCUSED"));
    }

    @Test
    void localizeAttendanceStatusShouldReturnOriginalForUnknownStatus() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        assertEquals("LATE", invokePrivate(page, "localizeAttendanceStatus", new Class[]{String.class}, "LATE"));
    }

    @Test
    void emptyRecordsShouldCreateVBoxWithAtLeastIconAndTitle() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        VBox box = runOnFxThread(() -> (VBox) invokePrivate(page, "emptyRecords", new Class[]{}, new Object[]{}));

        assertNotNull(box);
        assertTrue(box.getChildren().size() >= 2);
        assertInstanceOf(Label.class, box.getChildren().get(0));
        assertInstanceOf(Label.class, box.getChildren().get(1));
    }

    @Test
    void recordRowShouldCreatePresentChipClass() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        HBox row = runOnFxThread(() -> (HBox) invokePrivate(
                page,
                "recordRow",
                new Class[]{String.class, String.class, String.class},
                "2026-04-22",
                "Present",
                "PRESENT"
        ));

        assertNotNull(row);
        assertTrue(row.getStyleClass().contains("record-row"));

        Label chip = (Label) row.getChildren().get(2);
        assertEquals("Present", chip.getText());
        assertTrue(chip.getStyleClass().contains("record-chip"));
        assertTrue(chip.getStyleClass().contains("record-chip-present"));
    }

    @Test
    void recordRowShouldCreateAbsentChipClass() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        HBox row = runOnFxThread(() -> (HBox) invokePrivate(
                page,
                "recordRow",
                new Class[]{String.class, String.class, String.class},
                "2026-04-22",
                "Absent",
                "ABSENT"
        ));

        Label chip = (Label) row.getChildren().get(2);
        assertTrue(chip.getStyleClass().contains("record-chip-absent"));
    }

    @Test
    void recordRowShouldCreateExcusedChipClass() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        HBox row = runOnFxThread(() -> (HBox) invokePrivate(
                page,
                "recordRow",
                new Class[]{String.class, String.class, String.class},
                "2026-04-22",
                "Excused",
                "EXCUSED"
        ));

        Label chip = (Label) row.getChildren().get(2);
        assertTrue(chip.getStyleClass().contains("record-chip-excused"));
    }

    @Test
    void recordRowShouldNotAddStatusSpecificClassForUnknownStatus() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        HBox row = runOnFxThread(() -> (HBox) invokePrivate(
                page,
                "recordRow",
                new Class[]{String.class, String.class, String.class},
                "2026-04-22",
                "Late",
                "LATE"
        ));

        Label chip = (Label) row.getChildren().get(2);
        assertTrue(chip.getStyleClass().contains("record-chip"));
        assertFalse(chip.getStyleClass().contains("record-chip-present"));
        assertFalse(chip.getStyleClass().contains("record-chip-absent"));
        assertFalse(chip.getStyleClass().contains("record-chip-excused"));
    }

    @Test
    void rateCardWithValueShouldBuildCard() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();
        Label value = new Label("85%");

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                page,
                "rateCardWithValue",
                new Class[]{String.class, Label.class},
                "Attendance Rate",
                value
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("rate-card"));
        assertEquals(2, card.getChildren().size());
        assertSame(value, card.getChildren().get(1));
    }

    @Test
    void smallStatCardWithValueShouldAddMiniValueStyleWhenMissing() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();
        Label value = new Label("12");

        VBox card = runOnFxThread(() -> (VBox) invokePrivate(
                page,
                "smallStatCardWithValue",
                new Class[]{String.class, Label.class, String.class, String.class},
                "Present",
                value,
                "#3BAA66",
                "check"
        ));

        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("mini-stat-card"));
        assertTrue(value.getStyleClass().contains("mini-value"));
    }

    @Test
    void makeBadgeIconShouldReturnNodeForKnownKeys() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        Node check = runOnFxThread(() -> (Node) invokePrivate(page, "makeBadgeIcon", new Class[]{String.class}, "check"));
        Node x = runOnFxThread(() -> (Node) invokePrivate(page, "makeBadgeIcon", new Class[]{String.class}, "x"));
        Node clock = runOnFxThread(() -> (Node) invokePrivate(page, "makeBadgeIcon", new Class[]{String.class}, "clock"));
        Node calendar = runOnFxThread(() -> (Node) invokePrivate(page, "makeBadgeIcon", new Class[]{String.class}, "calendar"));

        assertNotNull(check);
        assertNotNull(x);
        assertNotNull(clock);
        assertNotNull(calendar);

        assertInstanceOf(Group.class, check);
        assertInstanceOf(Group.class, x);
        assertInstanceOf(Group.class, clock);
        assertInstanceOf(Group.class, calendar);
    }

    @Test
    void makeBadgeIconShouldReturnDefaultDotForUnknownKey() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        Node node = runOnFxThread(() -> (Node) invokePrivate(page, "makeBadgeIcon", new Class[]{String.class}, "unknown"));

        assertNotNull(node);
        assertInstanceOf(javafx.scene.shape.Circle.class, node);
    }

    @Test
    void msgShouldReturnFallbackWhenKeyIsMissing() throws Exception {
        StudentAttendancePage page = new StudentAttendancePage();

        String value = (String) invokePrivate(
                page,
                "msg",
                new Class[]{String.class, String.class},
                "missing.key.for.test",
                "Fallback Value"
        );

        assertEquals("Fallback Value", value);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object invokePrivateStatic(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = StudentAttendancePage.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
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
    @Test
    void buildShouldReturnParent() {
        StudentAttendancePage page = new StudentAttendancePage();

        Scene scene = new Scene(new VBox());
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.name()).thenReturn("Student User");

        Parent result = page.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }
}