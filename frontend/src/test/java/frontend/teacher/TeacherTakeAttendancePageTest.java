package frontend.teacher;

import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.ui.StudentRow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    void mapClassItemParsesStringIdCorrectly() {
        Map<String, Object> classMap = Map.of(
                "id", "22",
                "classCode", "WD202",
                "name", "Web Development"
        );

        TeacherTakeAttendancePage.ClassItem item = page.mapClassItem(classMap);

        assertEquals(22L, item.id);
        assertEquals("WD202 — Web Development", item.label);
    }

    @Test
    void mapStudentRowMapsValuesCorrectly() throws Exception {
        Map<String, Object> student = Map.of(
                "id", 7,
                "firstName", "Farah",
                "lastName", "Smith",
                "email", "farah@example.com"
        );

        StudentRow row = page.mapStudentRow(student);

        assertEquals("Farah Smith", row.studentNameProperty().get());
        assertEquals("farah@example.com", row.emailProperty().get());
        assertEquals("—", row.statusProperty().get());

        Field studentIdField = StudentRow.class.getDeclaredField("studentId");
        studentIdField.setAccessible(true);

        javafx.beans.property.LongProperty studentIdProperty =
                (javafx.beans.property.LongProperty) studentIdField.get(row);

        assertEquals(7L, studentIdProperty.get());
    }

    @Test
    void mapStudentRowParsesStringIdCorrectly() throws Exception {
        Map<String, Object> student = Map.of(
                "id", "9",
                "firstName", "John",
                "lastName", "Doe",
                "email", "john@example.com"
        );

        StudentRow row = page.mapStudentRow(student);

        assertEquals("John Doe", row.studentNameProperty().get());
        assertEquals("john@example.com", row.emailProperty().get());
        assertEquals("—", row.statusProperty().get());

        Field studentIdField = StudentRow.class.getDeclaredField("studentId");
        studentIdField.setAccessible(true);

        javafx.beans.property.LongProperty studentIdProperty =
                (javafx.beans.property.LongProperty) studentIdField.get(row);

        assertEquals(9L, studentIdProperty.get());
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
    void localizeAttendanceStatusHandlesWhitespaceAndCase() {
        String result = page.localizeAttendanceStatus("  present  ");

        assertFalse(result.isBlank());
        assertNotEquals("  present  ", result);
    }

    @Test
    void localizeAttendanceStatusReturnsOriginalForUnknownStatus() {
        assertEquals("LATE", page.localizeAttendanceStatus("LATE"));
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
    void buildSelectClassLabelReturnsStyledLabel() throws Exception {
        Label label = invokePrivate("buildSelectClassLabel");

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getStyleClass().contains("section-title"));
    }

    @Test
    void buildClassBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> classBox = invokePrivate("buildClassBox");

        assertNotNull(classBox);
        assertFalse(classBox.getPromptText().isBlank());
        assertEquals(320.0, classBox.getMaxWidth());
    }

    @Test
    void buildQrImageViewReturnsConfiguredImageView() throws Exception {
        ImageView imageView = invokePrivate("buildQrImageView");

        assertNotNull(imageView);
        assertEquals(180.0, imageView.getFitWidth());
        assertEquals(180.0, imageView.getFitHeight());
        assertTrue(imageView.isPreserveRatio());
    }

    @Test
    void buildManualCodeLabelReturnsDefaultStyledLabel() throws Exception {
        Label label = invokePrivate("buildManualCodeLabel");

        assertEquals("—", label.getText());
        assertTrue(label.getStyleClass().contains("small-subtitle"));
    }

    @Test
    void buildGenerateButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildGenerateButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertEquals(Double.MAX_VALUE, button.getMaxWidth());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildQrCardContainsExpectedChildren() throws Exception {
        ImageView imageView = new ImageView();
        Label manualCode = new Label("CODE123");
        Button generateButton = new Button("Generate");

        VBox card = invokePrivate(
                "buildQrCard",
                new Class<?>[]{ImageView.class, Label.class, Button.class},
                imageView,
                manualCode,
                generateButton
        );

        assertNotNull(card);
        assertEquals(4, card.getChildren().size());
        assertTrue(card.getStyleClass().contains("card"));
        assertEquals(16.0, card.getPadding().getTop());
    }

    @Test
    void buildStudentsTitleReturnsStyledEmptyLabel() throws Exception {
        Label label = invokePrivate("buildStudentsTitle");

        assertNotNull(label);
        assertEquals("", label.getText());
        assertTrue(label.getStyleClass().contains("section-title"));
    }

    @Test
    void buildMarkAllPresentButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildMarkAllPresentButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildStudentsHeaderContainsExpectedNodes() throws Exception {
        Label studentsTitle = new Label("Students");
        Button markAllPresentButton = new Button("Mark all");

        HBox header = invokePrivate(
                "buildStudentsHeader",
                new Class<?>[]{Label.class, Button.class},
                studentsTitle,
                markAllPresentButton
        );

        assertNotNull(header);
        assertEquals(Pos.CENTER_LEFT, header.getAlignment());
        assertEquals(3, header.getChildren().size());
        assertSame(studentsTitle, header.getChildren().get(0));
        assertInstanceOf(Region.class, header.getChildren().get(1));
        assertSame(markAllPresentButton, header.getChildren().get(2));
    }

    @Test
    void createActionButtonReturnsConfiguredButton() throws Exception {
        Button button = invokePrivate(
                "createActionButton",
                new Class<?>[]{String.class},
                "✓"
        );

        assertEquals("✓", button.getText());
        assertEquals(30.0, button.getMinWidth());
        assertEquals(30.0, button.getMinHeight());
        assertEquals(30.0, button.getPrefWidth());
        assertEquals(30.0, button.getPrefHeight());
    }

    @Test
    void buildStudentsTableCreatesFourColumns() throws Exception {
        TableView<?> table = invokePrivate(
                "buildStudentsTable",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, long[].class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                new long[]{-1L}
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
    void handleClassSelectionShouldReturnWhenNoClassSelected() throws Exception {
        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
        Label manualCode = new Label("OLD");
        ImageView qrImageView = new ImageView();
        long[] currentSessionId = {99L};

        invokePrivateVoid(
                "handleClassSelection",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class, Label.class, ImageView.class, long[].class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox,
                manualCode,
                qrImageView,
                currentSessionId
        );

        assertEquals("OLD", manualCode.getText());
        assertEquals(99L, currentSessionId[0]);
    }

    @Test
    void handleClassSelectionShouldLoadStudents() throws Exception {
        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
        classBox.setValue(new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering"));

        Label manualCode = new Label("OLD");
        ImageView qrImageView = new ImageView();
        long[] currentSessionId = {55L};

        invokePrivateVoid(
                "handleClassSelection",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class, Label.class, ImageView.class, long[].class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox,
                manualCode,
                qrImageView,
                currentSessionId
        );

        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<StudentRow> rows =
                (javafx.collections.ObservableList<StudentRow>) getPrivateField();

        waitUntil(() -> rows.size() == 1 && rows.getFirst().getStudentId() == 7L);

        assertEquals(-1L, currentSessionId[0]);
        assertEquals("—", manualCode.getText());
        assertEquals("Farah Smith", rows.getFirst().studentNameProperty().get());
        assertEquals("farah@example.com", rows.getFirst().emailProperty().get());
    }

    @Test
    void handleGenerateSessionShouldCreateSessionAndUpdateUi() throws Exception {
        ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
        classBox.setValue(new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering"));

        Button generateButton = new Button("Generate");
        Label manualCode = new Label("OLD");
        ImageView qrImageView = new ImageView();
        long[] currentSessionId = {-1L};

        invokePrivateVoid(
                "handleGenerateSession",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, ComboBox.class, Button.class, Label.class, ImageView.class, long[].class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                classBox,
                generateButton,
                manualCode,
                qrImageView,
                currentSessionId
        );

        waitUntil(() -> !generateButton.isDisable() && currentSessionId[0] == 99L);

        assertEquals("ABC123", manualCode.getText());
        assertEquals(99L, currentSessionId[0]);
    }

    @Test
    void handleMarkAllPresentShouldUpdateAllRows() throws Exception {
        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<StudentRow> rows =
                (javafx.collections.ObservableList<StudentRow>) getPrivateField();

        rows.clear();
        rows.add(new StudentRow(1L, "Farah Smith", "farah@example.com", "ABSENT"));
        rows.add(new StudentRow(2L, "John Doe", "john@example.com", "EXCUSED"));

        Button markAllButton = new Button("Mark all");
        long[] currentSessionId = {99L};

        invokePrivateVoid(
                "handleMarkAllPresent",
                new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, Button.class, long[].class},
                new FakeTeacherApi(),
                new JwtStore(),
                new AuthState("token", Role.TEACHER, "Teacher"),
                markAllButton,
                currentSessionId
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

    private static Object getPrivateField() throws Exception {
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