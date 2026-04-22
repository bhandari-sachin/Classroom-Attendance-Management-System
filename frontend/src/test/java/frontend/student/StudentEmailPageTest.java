package frontend.student;

import frontend.auth.AppRouter;
import frontend.ui.TeacherRow;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StudentEmailPageTest {

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
    void mapTeacherRowShouldMapBothFields() {
        StudentEmailPage page = new StudentEmailPage();

        TeacherRow row = page.mapTeacherRow(Map.of(
                "teacherName", "John Smith",
                "email", "john@school.com"
        ));

        assertEquals("John Smith", row.getTeacherName());
        assertEquals("john@school.com", row.getEmail());
    }

    @Test
    void mapTeacherRowShouldUseEmptyStringsWhenFieldsMissing() {
        StudentEmailPage page = new StudentEmailPage();

        TeacherRow row = page.mapTeacherRow(Map.of());

        assertEquals("", row.getTeacherName());
        assertEquals("", row.getEmail());
    }

    @Test
    void mapTeacherRowShouldConvertNonStringValuesToString() {
        StudentEmailPage page = new StudentEmailPage();

        TeacherRow row = page.mapTeacherRow(Map.of(
                "teacherName", 123,
                "email", 456
        ));

        assertEquals("123", row.getTeacherName());
        assertEquals("456", row.getEmail());
    }

    @Test
    void buildTitleShouldCreateStyledLabel() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        Label title = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildTitle",
                new Class[]{}
        ));

        assertNotNull(title);
        assertTrue(title.getStyleClass().contains("title"));
        assertNotNull(title.getText());
        assertFalse(title.getText().isBlank());
    }

    @Test
    void buildSubtitleShouldCreateStyledLabel() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        Label subtitle = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildSubtitle",
                new Class[]{}
        ));

        assertNotNull(subtitle);
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
        assertNotNull(subtitle.getText());
        assertFalse(subtitle.getText().isBlank());
    }

    @Test
    void buildStatusLabelShouldCreateStyledLoadingLabel() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        Label status = runOnFxThread(() -> (Label) invokePrivate(
                page,
                "buildStatusLabel",
                new Class[]{}
        ));

        assertNotNull(status);
        assertTrue(status.getStyleClass().contains("subtitle"));
        assertNotNull(status.getText());
        assertFalse(status.getText().isBlank());
    }

    @Test
    void buildTeacherTableShouldCreateTableWithTwoColumns() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        TableView<?> table = runOnFxThread(() -> (TableView<?>) invokePrivate(
                page,
                "buildTeacherTable",
                new Class[]{}
        ));

        assertNotNull(table);
        assertEquals(2, table.getColumns().size());
        assertEquals(320.0, table.getPrefHeight());
        assertNotNull(table.getItems());

        TableColumn<?, ?> first = table.getColumns().get(0);
        TableColumn<?, ?> second = table.getColumns().get(1);

        assertNotNull(first.getText());
        assertFalse(first.getText().isBlank());
        assertNotNull(second.getText());
        assertFalse(second.getText().isBlank());
    }

    @Test
    void buildTeacherTableShouldUseEmptyRowsInitially() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        TableView<?> table = runOnFxThread(() -> (TableView<?>) invokePrivate(
                page,
                "buildTeacherTable",
                new Class[]{}
        ));

        assertTrue(table.getItems().isEmpty());
    }

    @Test
    void tableItemsShouldReflectMappedTeacherRows() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        TableView<TeacherRow> table = runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            TableView<TeacherRow> built = (TableView<TeacherRow>) invokePrivate(
                    page,
                    "buildTeacherTable",
                    new Class[]{}
            );
            return built;
        });

        TeacherRow row = page.mapTeacherRow(Map.of(
                "teacherName", "Emma Brown",
                "email", "emma@school.com"
        ));

        runOnFxThread(() -> {
            table.getItems().add(row);
            return null;
        });

        assertEquals(1, table.getItems().size());
        assertEquals("Emma Brown", table.getItems().getFirst().getTeacherName());
        assertEquals("emma@school.com", table.getItems().getFirst().getEmail());
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
         String lastRoute;

        TestRouter() {
            super(new Scene(new StackPane()));
        }

        @Override
        public void go(String route) {
            this.lastRoute = route;
        }
    }
}