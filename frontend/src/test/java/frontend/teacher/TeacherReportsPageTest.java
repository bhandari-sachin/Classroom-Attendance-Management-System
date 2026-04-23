package frontend.teacher;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TeacherReportsPageTest {

    private static TeacherReportsPage page;

    @BeforeAll
    static void initJavaFx() throws Exception {
        new JFXPanel();
        Platform.setImplicitExit(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            page = new TeacherReportsPage();
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void classItemToStringReturnsLabel() {
        TeacherReportsPage.ClassItem item =
                new TeacherReportsPage.ClassItem(1L, "SE101 — Software Engineering");

        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void sessionItemToStringReturnsLabel() {
        TeacherReportsPage.SessionItem item =
                new TeacherReportsPage.SessionItem(2L, "2026-04-22 CODE123");

        assertEquals("2026-04-22 CODE123", item.toString());
    }

    @Test
    void reportRowGettersReturnExpectedValues() {
        TeacherReportsPage.ReportRow row =
                new TeacherReportsPage.ReportRow("Farah Smith", "farah@example.com", "PRESENT");

        assertEquals("Farah Smith", row.getName());
        assertEquals("farah@example.com", row.getEmail());
        assertEquals("PRESENT", row.getStatus());
    }

    @Test
    void asMapReturnsMapWhenInputIsMap() {
        Map<String, Object> input = Map.of("a", 1, "b", "x");

        Map<String, Object> result = TeacherReportsPage.asMap(input);

        assertEquals(2, result.size());
        assertEquals(1, result.get("a"));
        assertEquals("x", result.get("b"));
    }

    @Test
    void asMapReturnsEmptyMapWhenInputIsNotMap() {
        Map<String, Object> result = TeacherReportsPage.asMap("not-a-map");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void asListReturnsListWhenInputIsList() {
        List<Map<String, Object>> input = List.of(
                Map.of("name", "A"),
                Map.of("name", "B")
        );

        List<Map<String, Object>> result = TeacherReportsPage.asList(input);

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).get("name"));
        assertEquals("B", result.get(1).get("name"));
    }

    @Test
    void asListReturnsEmptyListWhenInputIsNotList() {
        List<Map<String, Object>> result = TeacherReportsPage.asList("not-a-list");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void asIntReturnsZeroForNull() {
        assertEquals(0, TeacherReportsPage.asInt(null));
    }

    @Test
    void asIntReturnsIntForNumber() {
        assertEquals(12, TeacherReportsPage.asInt(12));
        assertEquals(7, TeacherReportsPage.asInt(7L));
        assertEquals(4, TeacherReportsPage.asInt(4.8));
    }

    @Test
    void asIntParsesStringNumber() {
        assertEquals(25, TeacherReportsPage.asInt("25"));
    }

    @Test
    void asDoubleReturnsZeroForNull() {
        assertEquals(0.0, TeacherReportsPage.asDouble(null));
    }

    @Test
    void asDoubleReturnsDoubleForNumber() {
        assertEquals(12.0, TeacherReportsPage.asDouble(12));
        assertEquals(7.5, TeacherReportsPage.asDouble(7.5));
    }

    @Test
    void asDoubleParsesStringNumber() {
        assertEquals(25.4, TeacherReportsPage.asDouble("25.4"));
    }

    @Test
    void pickReturnsFirstMatchingValue() {
        Map<String, Object> map = Map.of(
                "session_date", "2026-04-22",
                "qrCode", "ABC123"
        );

        assertEquals("2026-04-22", TeacherReportsPage.pick(map, "date", "session_date"));
        assertEquals("ABC123", TeacherReportsPage.pick(map, "code", "qrCode"));
    }

    @Test
    void pickReturnsEmptyStringWhenNoKeyMatches() {
        Map<String, Object> map = Map.of("x", "y");

        assertEquals("", TeacherReportsPage.pick(map, "date", "code"));
    }

    @Test
    void mapClassItemsMapsCorrectly() {
        List<Map<String, Object>> classes = List.of(
                Map.of("id", 1, "classCode", "SE101", "name", "Software Engineering"),
                Map.of("id", "2", "classCode", "WD202", "name", "Web Development")
        );

        List<TeacherReportsPage.ClassItem> result = page.mapClassItems(classes);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals("SE101 — Software Engineering", result.get(0).label);
        assertEquals(2L, result.get(1).id);
        assertEquals("WD202 — Web Development", result.get(1).label);
    }

    @Test
    void mapSessionItemsUsesDateAndCodeWhenPresent() {
        List<Map<String, Object>> sessions = List.of(
                Map.of("id", 5, "date", "2026-04-22", "code", "ABC123")
        );

        List<TeacherReportsPage.SessionItem> result = page.mapSessionItems(sessions);

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().id);
        assertTrue(result.getFirst().label.contains("2026-04-22"));
        assertTrue(result.getFirst().label.contains("ABC123"));
    }

    @Test
    void mapSessionItemsUsesFallbackWhenDateMissing() {
        List<Map<String, Object>> sessions = List.of(
                Map.of("id", 7, "qrCode", "QWERTY")
        );

        List<TeacherReportsPage.SessionItem> result = page.mapSessionItems(sessions);

        assertEquals(1, result.size());
        assertEquals(7L, result.getFirst().id);
        assertTrue(result.getFirst().label.contains("7"));
        assertTrue(result.getFirst().label.contains("QWERTY"));
    }

    @Test
    void mapSessionItemsHandlesDifferentKeyNames() {
        List<Map<String, Object>> sessions = List.of(
                Map.of("id", "8", "sessionDate", "2026-05-01", "qr_token", "ZXCV")
        );

        List<TeacherReportsPage.SessionItem> result = page.mapSessionItems(sessions);

        assertEquals(1, result.size());
        assertEquals(8L, result.getFirst().id);
        assertTrue(result.getFirst().label.contains("2026-05-01"));
        assertTrue(result.getFirst().label.contains("ZXCV"));
    }

    @Test
    void mapReportRowsMapsCorrectly() {
        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "firstName", "Farah",
                        "lastName", "Smith",
                        "email", "farah@example.com",
                        "status", "PRESENT"
                )
        );

        List<TeacherReportsPage.ReportRow> result = page.mapReportRows(rows);

        assertEquals(1, result.size());
        assertEquals("Farah Smith", result.getFirst().getName());
        assertEquals("farah@example.com", result.getFirst().getEmail());
        assertEquals("PRESENT", result.getFirst().getStatus());
    }

    @Test
    void mapReportRowsUsesSnakeCaseKeys() {
        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "first_name", "John",
                        "last_name", "Doe",
                        "email", "john@example.com",
                        "status", "ABSENT"
                )
        );

        List<TeacherReportsPage.ReportRow> result = page.mapReportRows(rows);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.getFirst().getName());
        assertEquals("john@example.com", result.getFirst().getEmail());
        assertEquals("ABSENT", result.getFirst().getStatus());
    }

    @Test
    void mapReportRowsUsesDashWhenFullNameBlank() {
        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "firstName", "",
                        "lastName", "",
                        "email", "anon@example.com",
                        "status", "EXCUSED"
                )
        );

        List<TeacherReportsPage.ReportRow> result = page.mapReportRows(rows);

        assertEquals(1, result.size());
        assertEquals("—", result.getFirst().getName());
    }

    @Test
    void localizeAttendanceStatusHandlesKnownValues() {
        assertFalse(page.localizeAttendanceStatus("PRESENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("ABSENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("EXCUSED").isBlank());
    }

    @Test
    void localizeAttendanceStatusHandlesLowerCaseAndWhitespace() {
        String result = page.localizeAttendanceStatus("  present ");

        assertFalse(result.isBlank());
        assertNotEquals("  present ", result);
    }

    @Test
    void localizeAttendanceStatusReturnsDashForNullOrBlank() {
        assertEquals("—", page.localizeAttendanceStatus(null));
        assertEquals("—", page.localizeAttendanceStatus("   "));
    }

    @Test
    void localizeAttendanceStatusReturnsOriginalValueForUnknownStatus() {
        assertEquals("LATE", page.localizeAttendanceStatus("LATE"));
    }

    @Test
    void formatOneDecimalFormatsWholeNumberWithTrailingZero() {
        assertEquals("5.0", page.formatOneDecimal(5.0));
        assertEquals("8.0", page.formatOneDecimal(8.04));
    }

    @Test
    void formatOneDecimalFormatsDecimalValue() {
        assertEquals("5.2", page.formatOneDecimal(5.24));
        assertEquals("5.3", page.formatOneDecimal(5.25));
    }

    @Test
    void buildTitleReturnsStyledLabel() throws Exception {
        Label title = runOnFxThreadAndWait(() -> invokePrivate("buildTitle"));

        assertNotNull(title);
        assertFalse(title.getText().isBlank());
        assertTrue(title.getStyleClass().contains("title"));
    }

    @Test
    void buildSubtitleReturnsStyledLabel() throws Exception {
        Label subtitle = runOnFxThreadAndWait(() -> invokePrivate("buildSubtitle"));

        assertNotNull(subtitle);
        assertFalse(subtitle.getText().isBlank());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildClassBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> classBox = runOnFxThreadAndWait(() -> invokePrivate("buildClassBox"));

        assertNotNull(classBox);
        assertFalse(classBox.getPromptText().isBlank());
        assertEquals(340.0, classBox.getMaxWidth());
    }

    @Test
    void buildSessionBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> sessionBox = runOnFxThreadAndWait(() -> invokePrivate("buildSessionBox"));

        assertNotNull(sessionBox);
        assertFalse(sessionBox.getPromptText().isBlank());
        assertEquals(340.0, sessionBox.getMaxWidth());
    }

    @Test
    void buildLoadButtonReturnsStyledButton() throws Exception {
        Button button = runOnFxThreadAndWait(() -> invokePrivate("buildLoadButton"));

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildExportButtonReturnsDisabledStyledMenuButton() throws Exception {
        MenuButton button = runOnFxThreadAndWait(() -> invokePrivate("buildExportButton"));

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.isDisable());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-blue"));
    }

    @Test
    void buildSelectRowContainsExpectedControls() throws Exception {
        HBox row = runOnFxThreadAndWait(() -> {
            ComboBox<TeacherReportsPage.ClassItem> classBox = new ComboBox<>();
            ComboBox<TeacherReportsPage.SessionItem> sessionBox = new ComboBox<>();
            Button loadButton = new Button("Load");
            MenuButton exportButton = new MenuButton("Export");

            return invokePrivate(
                    "buildSelectRow",
                    new Class<?>[]{ComboBox.class, ComboBox.class, Button.class, MenuButton.class},
                    classBox,
                    sessionBox,
                    loadButton,
                    exportButton
            );
        });

        assertEquals(Pos.CENTER_LEFT, row.getAlignment());
        assertEquals(4, row.getChildren().size());
    }

    @Test
    void buildStatsLabelReturnsStyledLabel() throws Exception {
        Label label = runOnFxThreadAndWait(() -> invokePrivate(
                "buildStatsLabel",
                new Class<?>[]{String.class, String.class, String.class},
                "teacher.reports.stats.present",
                "{count}",
                "10"
        ));

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getText().contains("10"));
        assertTrue(label.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildStatsRowContainsLabels() throws Exception {
        HBox row = runOnFxThreadAndWait(() -> {
            Label present = new Label("P");
            Label absent = new Label("A");
            Label excused = new Label("E");
            Label rate = new Label("R");

            return invokePrivate(
                    "buildStatsRow",
                    new Class<?>[]{Label.class, Label.class, Label.class, Label.class},
                    present,
                    absent,
                    excused,
                    rate
            );
        });

        assertEquals(Pos.CENTER_LEFT, row.getAlignment());
        assertEquals(4, row.getChildren().size());
    }

    @Test
    void buildReportTableCreatesThreeColumns() throws Exception {
        TableView<?> table = runOnFxThreadAndWait(() -> invokePrivate("buildReportTable"));

        assertNotNull(table);
        assertEquals(340.0, table.getPrefHeight());
        assertEquals(3, table.getColumns().size());
        assertFalse(table.getColumns().get(0).getText().isBlank());
        assertFalse(table.getColumns().get(1).getText().isBlank());
        assertFalse(table.getColumns().get(2).getText().isBlank());
    }

    @Test
    void resetReportUIClearsRowsAndResetsLabels() throws Exception {
        Field field = TeacherReportsPage.class.getDeclaredField("tableRows");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<TeacherReportsPage.ReportRow> rows =
                (javafx.collections.ObservableList<TeacherReportsPage.ReportRow>) field.get(page);

        runOnFxThreadAndWait(() -> {
            rows.add(new TeacherReportsPage.ReportRow("X", "x@test.com", "PRESENT"));

            Label present = new Label("old");
            Label absent = new Label("old");
            Label excused = new Label("old");
            Label rate = new Label("old");

            invokePrivateVoid(
                    "resetReportUI",
                    new Class<?>[]{Label.class, Label.class, Label.class, Label.class},
                    present,
                    absent,
                    excused,
                    rate
            );

            assertTrue(present.getText().contains("—"));
            assertTrue(absent.getText().contains("—"));
            assertTrue(excused.getText().contains("—"));
            assertTrue(rate.getText().contains("—"));
            assertTrue(rows.isEmpty());

            return null;
        });
    }

    @Test
    void buildReportTableStatusCellShouldLocalizeValueAndClearWhenEmpty() throws Exception {
        runOnFxThreadAndWait(() -> {
            TableView<TeacherReportsPage.ReportRow> table = invokePrivate("buildReportTable");

            @SuppressWarnings("unchecked")
            TableColumn<TeacherReportsPage.ReportRow, String> statusColumn =
                    (TableColumn<TeacherReportsPage.ReportRow, String>) table.getColumns().get(2);

            TableCell<TeacherReportsPage.ReportRow, String> cell =
                    statusColumn.getCellFactory().call(statusColumn);

            Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
            updateItem.setAccessible(true);

            updateItem.invoke(cell, "PRESENT", false);
            assertNotNull(cell.getText());
            assertFalse(cell.getText().isBlank());
            assertNotEquals("PRESENT", cell.getText());

            updateItem.invoke(cell, null, true);
            assertNull(cell.getText());

            return null;
        });
    }

    @Test
    void buildReportTableStatusCellShouldKeepUnknownStatusText() throws Exception {
        runOnFxThreadAndWait(() -> {
            TableView<TeacherReportsPage.ReportRow> table = invokePrivate("buildReportTable");

            @SuppressWarnings("unchecked")
            TableColumn<TeacherReportsPage.ReportRow, String> statusColumn =
                    (TableColumn<TeacherReportsPage.ReportRow, String>) table.getColumns().get(2);

            TableCell<TeacherReportsPage.ReportRow, String> cell =
                    statusColumn.getCellFactory().call(statusColumn);

            Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
            updateItem.setAccessible(true);

            updateItem.invoke(cell, "LATE", false);
            assertEquals("LATE", cell.getText());

            return null;
        });
    }

    @Test
    void buildShouldReturnRootNode() throws Exception {
        Parent root = runOnFxThreadAndWait(() -> {
            Scene scene = new Scene(new StackPane(), 1000, 700);
            AppRouter router = new AppRouter(scene);
            JwtStore jwtStore = new JwtStore();
            AuthState state = new AuthState("dummy-token", Role.TEACHER, "Teacher Test");

            return page.build(scene, router, jwtStore, state);
        });

        assertNotNull(root);

        // prevent later interference if an async error alert appears
        closeAllWindowsQuietly();
    }

    @Test
    void exportReportShouldReturnImmediatelyWhenSelectedClassIsNull() throws Exception {
        runOnFxThreadAndWait(() -> {
            Scene scene = new Scene(new StackPane(), 800, 600);

            invokePrivateVoid(
                    "exportReport",
                    new Class<?>[]{
                            Scene.class,
                            frontend.api.ReportApi.class,
                            JwtStore.class,
                            AuthState.class,
                            TeacherReportsPage.ClassItem.class,
                            String.class
                    },
                    scene,
                    null,
                    null,
                    null,
                    null,
                    "pdf"
            );

            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherReportsPage.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(page);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherReportsPage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(page, args);
    }

    private static void invokePrivateVoid(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherReportsPage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(page, args);
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

    private static void closeAllWindowsQuietly() throws Exception {
        runOnFxThreadAndWait(() -> {
            for (Window window : Window.getWindows()) {
                try {
                    window.hide();
                } catch (Exception ignored) {// close quietly
                }
            }
            return null;
        });
    }

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get() throws Exception;
    }
}