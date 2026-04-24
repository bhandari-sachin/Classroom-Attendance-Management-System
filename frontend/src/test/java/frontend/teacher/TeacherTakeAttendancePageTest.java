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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class TeacherTakeAttendancePageTest {

    private static TeacherTakeAttendancePage page;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        page = new TeacherTakeAttendancePage();
    }

    private static class FakeTeacherApi extends TeacherApi {
        FakeTeacherApi() {
            super("http://localhost:8081");
        }

        @Override
        public List<Map<String, Object>> getMyClasses(JwtStore jwtStore, AuthState state) {
            return List.of(
                    Map.of("id", 1, "classCode", "SE101", "name", "Software Engineering")
            );
        }

        @Override
        public List<Map<String, Object>> getStudentsForClass(JwtStore jwtStore, AuthState state, long classId) {
            return List.of(
                    Map.of(
                            "id", 7,
                            "firstName", "Farah",
                            "lastName", "Smith",
                            "email", "farah@example.com"
                    )
            );
        }

        @Override
        public Map<String, Object> createSession(JwtStore jwtStore, AuthState state, long classId) {
            return Map.of(
                    "sessionId", 99,
                    "code", "ABC123"
            );
        }

        @Override
        public String extractCode(Map<String, Object> response) {
            return String.valueOf(response.get("code"));
        }

        @Override
        public void markAttendance(
                JwtStore jwtStore,
                AuthState state,
                long studentId,
                long sessionId,
                String status
        ) {
            // success
        }
    }

    @Test
    void classItemToStringReturnsLabel() {
        TeacherTakeAttendancePage.ClassItem item =
                new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering");

        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void mapClassItemMapsValuesCorrectly() {
        Map<String, Object> classMap = Map.of(
                "id", 10,
                "classCode", "SE101",
                "name", "Software Engineering"
        );

        TeacherTakeAttendancePage.ClassItem item = page.mapClassItem(classMap);

        assertEquals(10L, item.id);
        assertEquals("SE101 — Software Engineering", item.label);
    }

    @Test
    void mapStudentRowMapsValuesCorrectly() {
        Map<String, Object> student = Map.of(
                "id", 7,
                "firstName", "Farah",
                "lastName", "Smith",
                "email", "farah@example.com"
        );

        StudentRow row = page.mapStudentRow(student);

        assertEquals(7L, row.getStudentId());
        assertEquals("Farah Smith", row.studentNameProperty().get());
        assertEquals("farah@example.com", row.emailProperty().get());
        assertEquals("—", row.statusProperty().get());
    }

    @Test
    void localizeAttendanceStatusReturnsDashForNullBlankAndDash() {
        assertEquals("—", page.localizeAttendanceStatus(null));
        assertEquals("—", page.localizeAttendanceStatus(""));
        assertEquals("—", page.localizeAttendanceStatus("   "));
        assertEquals("—", page.localizeAttendanceStatus("—"));
    }

    @Test
    void localizeAttendanceStatusLocalizesKnownStatuses() {
        assertFalse(page.localizeAttendanceStatus("PRESENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("ABSENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("EXCUSED").isBlank());
    }

    @Test
    void localizeAttendanceStatusReturnsOriginalForUnknownStatus() {
        assertEquals("LATE", page.localizeAttendanceStatus("LATE"));
    }

    @Test
    void buildStudentsTableCreatesFourColumns() throws Exception {
        Object sessionState = createSessionState();

        TableView<?> table = invokePrivate(
                "buildStudentsTable",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, sessionState.getClass()},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                sessionState
        );

        assertNotNull(table);
        assertEquals(300.0, table.getPrefHeight());
        assertEquals(36.0, table.getFixedCellSize());
        assertTrue(table.getStyleClass().contains("students-table"));
        assertEquals(4, table.getColumns().size());
    }

    @Test
    void loadClassesShouldFillClassBox() throws Exception {
        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();

        invokePrivateVoid(
                "loadClasses",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox
        );

        waitUntil(() -> classBox.getItems().size() == 1);

        assertEquals(1L, classBox.getItems().getFirst().id);
        assertEquals("SE101 — Software Engineering", classBox.getItems().getFirst().label);
    }

    @Test
    void handleClassSelectionShouldLoadStudentsAndResetSession() throws Exception {
        Object sessionState = createSessionState();

        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
        classBox.setValue(new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering"));

        Label manualCode = new Label("OLD");
        ImageView qrImageView = new ImageView();
        Button generateButton = new Button("Generate");

        Object controls = createSessionControls(generateButton, manualCode, qrImageView, sessionState);

        invokePrivateVoid(
                "handleClassSelection",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class, controls.getClass()},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox,
                controls
        );

        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<StudentRow> rows =
                (javafx.collections.ObservableList<StudentRow>) getPrivateRows();

        waitUntil(() -> rows.size() == 1 && rows.get(0).getStudentId() == 7L);

        assertEquals(-1L, getSessionId(sessionState));
        assertEquals("—", manualCode.getText());
        assertEquals("Farah Smith", rows.getFirst().studentNameProperty().get());
        assertEquals("farah@example.com", rows.getFirst().emailProperty().get());
    }

    @Test
    void handleGenerateSessionShouldCreateSessionAndUpdateUi() throws Exception {
        Object sessionState = createSessionState();

        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
        classBox.setValue(new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering"));

        Button generateButton = new Button("Generate");
        Label manualCode = new Label("OLD");
        ImageView qrImageView = new ImageView();

        Object controls = createSessionControls(generateButton, manualCode, qrImageView, sessionState);

        invokePrivateVoid(
                "handleGenerateSession",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class, controls.getClass()},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox,
                controls
        );

        waitUntil(() -> !generateButton.isDisable() && getSessionIdQuietly(sessionState) == 99L);

        assertEquals("ABC123", manualCode.getText());
        assertEquals(99L, getSessionId(sessionState));
    }

    @Test
    void handleMarkAllPresentShouldUpdateAllRows() throws Exception {
        Object sessionState = createSessionState();
        setSessionId(sessionState, 99L);

        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<StudentRow> rows =
                (javafx.collections.ObservableList<StudentRow>) getPrivateRows();

        rows.clear();
        rows.add(new StudentRow(1L, "Farah Smith", "farah@example.com", "ABSENT"));
        rows.add(new StudentRow(2L, "John Doe", "john@example.com", "EXCUSED"));

        Button markAllButton = new Button("Mark all");

        invokePrivateVoid(
                "handleMarkAllPresent",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, Button.class, sessionState.getClass()},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                markAllButton,
                sessionState
        );

        waitUntil(() -> !markAllButton.isDisable()
                && rows.stream().allMatch(row -> "PRESENT".equals(row.getStatus())));

        assertEquals("PRESENT", rows.get(0).getStatus());
        assertEquals("PRESENT", rows.get(1).getStatus());
    }

    @Test
    void buildShouldReturnRootNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            Scene scene = new Scene(new StackPane());
            AppRouter router = new AppRouter(scene);
            JwtStore jwtStore = new JwtStore();
            AuthState state = new AuthState("dummy-token", Role.TEACHER, "Teacher");

            Parent root = page.build(scene, router, jwtStore, state);

            assertNotNull(root);
            return null;
        });
    }

    private static Object createSessionState() throws Exception {
        Class<?> clazz = getNestedClass("SessionState");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static Object createSessionControls(
            Button generateButton,
            Label manualCode,
            ImageView qrImageView,
            Object sessionState
    ) throws Exception {
        Class<?> clazz = getNestedClass("SessionControls");
        Constructor<?> constructor = clazz.getDeclaredConstructor(
                Button.class,
                Label.class,
                ImageView.class,
                sessionState.getClass()
        );
        constructor.setAccessible(true);
        return constructor.newInstance(generateButton, manualCode, qrImageView, sessionState);
    }

    private static Class<?> getNestedClass(String simpleName) {
        for (Class<?> clazz : TeacherTakeAttendancePage.class.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals(simpleName)) {
                return clazz;
            }
        }
        throw new IllegalStateException("Nested class not found: " + simpleName);
    }

    private static long getSessionId(Object sessionState) throws Exception {
        Method method = sessionState.getClass().getDeclaredMethod("getCurrentSessionId");
        method.setAccessible(true);
        return (long) method.invoke(sessionState);
    }

    private static long getSessionIdQuietly(Object sessionState) {
        try {
            return getSessionId(sessionState);
        } catch (Exception ex) {
            return -1L;
        }
    }

    private static void setSessionId(Object sessionState, long value) throws Exception {
        Method method = sessionState.getClass().getDeclaredMethod("setCurrentSessionId", long.class);
        method.setAccessible(true);
        method.invoke(sessionState, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(page);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(page, args);
    }

    private static void invokePrivateVoid(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(page, args);
    }

    private static Object getPrivateRows() throws Exception {
        Field field = TeacherTakeAttendancePage.class.getDeclaredField("rows");
        field.setAccessible(true);
        return field.get(page);
    }

    private static <T> T runOnFxThreadAndWait(FxSupplier<T> supplier) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }

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

        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");

        if (error.get() != null) {
            if (error.get() instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    private static void waitUntil(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.onSpinWait();
        }

        fail("Condition was not completed in time");
    }

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get() throws Exception;
    }
}