package frontend.teacher;

import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.ui.StudentRow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherEmailPageTest {

    private static TeacherEmailPage page;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        page = new TeacherEmailPage();
    }

    @Test
    void mapClassItemMapsValuesCorrectly() {
        Map<String, Object> classData = Map.of(
                "id", 15,
                "classCode", "SE101",
                "name", "Software Engineering"
        );

        TeacherEmailPage.ClassItem item = page.mapClassItem(classData);

        assertEquals(15L, item.id);
        assertEquals("SE101 — Software Engineering", item.label);
        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void mapClassItemParsesStringIdCorrectly() {
        Map<String, Object> classData = Map.of(
                "id", "22",
                "classCode", "WD202",
                "name", "Web Development"
        );

        TeacherEmailPage.ClassItem item = page.mapClassItem(classData);

        assertEquals(22L, item.id);
        assertEquals("WD202 — Web Development", item.label);
    }

    @Test
    void mapStudentRowMapsValuesCorrectly() {
        Map<String, Object> studentData = Map.of(
                "id", 7,
                "firstName", "Farah",
                "lastName", "Smith",
                "email", "farah@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals(7L, row.getStudentId());
        assertEquals("Farah Smith", row.getStudentName());
        assertEquals("farah@example.com", row.getEmail());
        assertEquals("—", row.getStatus());
    }

    @Test
    void mapStudentRowParsesStringIdCorrectly() {
        Map<String, Object> studentData = Map.of(
                "id", "9",
                "firstName", "John",
                "lastName", "Doe",
                "email", "john@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals(9L, row.getStudentId());
        assertEquals("John Doe", row.getStudentName());
        assertEquals("john@example.com", row.getEmail());
        assertEquals("—", row.getStatus());
    }

    @Test
    void mapStudentRowTrimsFullName() {
        Map<String, Object> studentData = Map.of(
                "id", 10,
                "firstName", "Anna",
                "lastName", "",
                "email", "anna@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals("Anna", row.getStudentName());
    }

    @Test
    void buildTitleReturnsStyledLabel() throws Exception {
        Label title = invokePrivate("buildTitle");

        assertNotNull(title);
        assertFalse(title.getText().isBlank());
        assertTrue(title.getStyleClass().contains("title"));
    }

    @Test
    void buildSubtitleReturnsStyledLabel() throws Exception {
        Label subtitle = invokePrivate("buildSubtitle");

        assertNotNull(subtitle);
        assertFalse(subtitle.getText().isBlank());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildClassBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> classBox = invokePrivate("buildClassBox");

        assertNotNull(classBox);
        assertFalse(classBox.getPromptText().isBlank());
        assertEquals(360.0, classBox.getMaxWidth());
    }

    @Test
    void buildRefreshButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildRefreshButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildTopRowContainsControlsAndAlignment() throws Exception {
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();
        Button refreshButton = new Button("Refresh");

        HBox topRow = invokePrivate(
                "buildTopRow",
                new Class<?>[]{ComboBox.class, Button.class},
                classBox,
                refreshButton
        );

        assertNotNull(topRow);
        assertEquals(javafx.geometry.Pos.CENTER_LEFT, topRow.getAlignment());
        assertEquals(2, topRow.getChildren().size());
        assertSame(classBox, topRow.getChildren().get(0));
        assertSame(refreshButton, topRow.getChildren().get(1));
    }

    @Test
    void buildStudentTableCreatesExpectedColumnsAndSettings() throws Exception {
        TableView<?> table = invokePrivate("buildStudentTable");

        assertNotNull(table);
        assertEquals(340.0, table.getPrefHeight());
        assertEquals(2, table.getColumns().size());

        TableColumn<?, ?> firstColumn = table.getColumns().get(0);
        TableColumn<?, ?> secondColumn = table.getColumns().get(1);

        assertFalse(firstColumn.getText().isBlank());
        assertFalse(secondColumn.getText().isBlank());
    }

    @Test
    void buildShouldReturnParent() throws Exception {
        runOnFxThreadAndWait(() -> {
            Scene scene = new Scene(new VBox());
            AppRouter router = mock(AppRouter.class);
            JwtStore jwtStore = mock(JwtStore.class);
            AuthState state = new AuthState("token", Role.TEACHER, "Teacher");

            Parent root = page.build(scene, router, jwtStore, state);

            assertNotNull(root);
        });
    }

    @Test
    void loadClassesShouldPopulateComboBox() throws Exception {
        TeacherApi api = mock(TeacherApi.class);
        JwtStore jwtStore = mock(JwtStore.class);
        AuthState state = new AuthState("token", Role.TEACHER, "Teacher");
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();

        when(api.getMyClasses(jwtStore, state)).thenReturn(List.of(
                Map.of("id", 1, "classCode", "SE101", "name", "Software Engineering"),
                Map.of("id", 2, "classCode", "WD202", "name", "Web Development")
        ));

        invokePrivateVoid(
                "loadClasses",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                api, jwtStore, state, classBox
        );

        waitForFxEvents(() -> classBox.getItems().size() == 2);

        assertEquals(2, classBox.getItems().size());
        assertEquals("SE101 — Software Engineering", classBox.getItems().get(0).toString());
        assertEquals("WD202 — Web Development", classBox.getItems().get(1).toString());
    }

    @Test
    void loadClassesShouldHandleExceptionWithoutCrashing() throws Exception {
        TeacherApi api = mock(TeacherApi.class);
        JwtStore jwtStore = mock(JwtStore.class);
        AuthState state = new AuthState("token", Role.TEACHER, "Teacher");
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();

        when(api.getMyClasses(jwtStore, state)).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() ->
                invokePrivateVoid(
                        "loadClasses",
                        new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                        api, jwtStore, state, classBox
                )
        );

        waitForFxEvents(() -> classBox.getItems().isEmpty());
        assertTrue(classBox.getItems().isEmpty());
    }

    @Test
    void loadStudentsForSelectedClassShouldClearRowsWhenNoSelection() throws Exception {
        TeacherApi api = mock(TeacherApi.class);
        JwtStore jwtStore = mock(JwtStore.class);
        AuthState state = new AuthState("token", Role.TEACHER, "Teacher");
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();

        ObservableRowsAccessor rowsAccessor = new ObservableRowsAccessor(page);
        runOnFxThreadAndWait(() -> {
            try {
                rowsAccessor.rows().setAll(
                        new StudentRow(1L, "Old Student", "old@example.com", "—")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        invokePrivateVoid(
                "loadStudentsForSelectedClass",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                api, jwtStore, state, classBox
        );

        waitForFxEvents(() -> {
            try {
                return rowsAccessor.rows().isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(rowsAccessor.rows().isEmpty());
    }

    @Test
    void loadStudentsForSelectedClassShouldPopulateRows() throws Exception {
        TeacherApi api = mock(TeacherApi.class);
        JwtStore jwtStore = mock(JwtStore.class);
        AuthState state = new AuthState("token", Role.TEACHER, "Teacher");
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();
        TeacherEmailPage.ClassItem item = new TeacherEmailPage.ClassItem(5L, "SE101 — Software Engineering");

        runOnFxThreadAndWait(() -> {
            classBox.getItems().add(item);
            classBox.setValue(item);
        });

        when(api.getStudentsForClass(jwtStore, state, 5L)).thenReturn(List.of(
                Map.of("id", 7, "firstName", "Farah", "lastName", "Smith", "email", "farah@example.com"),
                Map.of("id", 8, "firstName", "John", "lastName", "Doe", "email", "john@example.com")
        ));

        invokePrivateVoid(
                "loadStudentsForSelectedClass",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                api, jwtStore, state, classBox
        );

        ObservableRowsAccessor rowsAccessor = new ObservableRowsAccessor(page);
        waitForFxEvents(() -> {
            try {
                return rowsAccessor.rows().size() == 2;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(2, rowsAccessor.rows().size());
        assertEquals("Farah Smith", rowsAccessor.rows().get(0).getStudentName());
        assertEquals("john@example.com", rowsAccessor.rows().get(1).getEmail());
    }

    @Test
    void loadStudentsForSelectedClassShouldClearRowsOnException() throws Exception {
        TeacherApi api = mock(TeacherApi.class);
        JwtStore jwtStore = mock(JwtStore.class);
        AuthState state = new AuthState("token", Role.TEACHER, "Teacher");
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();
        TeacherEmailPage.ClassItem item = new TeacherEmailPage.ClassItem(5L, "SE101 — Software Engineering");
        ObservableRowsAccessor rowsAccessor = new ObservableRowsAccessor(page);

        runOnFxThreadAndWait(() -> {
            classBox.getItems().add(item);
            classBox.setValue(item);
            try {
                rowsAccessor.rows().setAll(new StudentRow(99L, "Before", "before@example.com", "—"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        when(api.getStudentsForClass(jwtStore, state, 5L)).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() ->
                invokePrivateVoid(
                        "loadStudentsForSelectedClass",
                        new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                        api, jwtStore, state, classBox
                )
        );

        waitForFxEvents(() -> {
            try {
                return rowsAccessor.rows().isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(rowsAccessor.rows().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherEmailPage.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(page);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherEmailPage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(page, args);
    }

    private static void invokePrivateVoid(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherEmailPage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(page, args);
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");

        if (error[0] != null) {
            throw new RuntimeException(error[0]);
        }
    }

    private static void waitForFxEvents(BooleanSupplier condition) throws Exception {
        long timeoutMs = 2000;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutMs) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();

            if (condition.getAsBoolean()) {
                return;
            }
        }
        fail("FX updates did not finish in time");
    }

    private static final class ObservableRowsAccessor {
        private final TeacherEmailPage target;

        private ObservableRowsAccessor(TeacherEmailPage target) {
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<StudentRow> rows() throws Exception {
            Field field = TeacherEmailPage.class.getDeclaredField("rows");
            field.setAccessible(true);
            return (javafx.collections.ObservableList<StudentRow>) field.get(target);
        }
    }
}