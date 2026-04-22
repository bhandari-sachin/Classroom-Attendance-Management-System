package frontend.teacher;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeacherReportsPageTest {

    private static TeacherReportsPage page;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        page = new TeacherReportsPage();
    }

    @Test
    void classItemToStringReturnsLabel() {
        TeacherReportsPage.ClassItem item = new TeacherReportsPage.ClassItem(1L, "SE101 — Software Engineering");
        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void sessionItemToStringReturnsLabel() {
        TeacherReportsPage.SessionItem item = new TeacherReportsPage.SessionItem(2L, "2026-04-22 CODE123");
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
        assertEquals(340.0, classBox.getMaxWidth());
    }

    @Test
    void buildSessionBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> sessionBox = invokePrivate("buildSessionBox");

        assertNotNull(sessionBox);
        assertFalse(sessionBox.getPromptText().isBlank());
        assertEquals(340.0, sessionBox.getMaxWidth());
    }

    @Test
    void buildLoadButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildLoadButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildExportButtonReturnsDisabledStyledMenuButton() throws Exception {
        MenuButton button = invokePrivate("buildExportButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.isDisable());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-blue"));
    }

    @Test
    void buildSelectRowContainsExpectedControls() throws Exception {
        ComboBox<TeacherReportsPage.ClassItem> classBox = new ComboBox<>();
        ComboBox<TeacherReportsPage.SessionItem> sessionBox = new ComboBox<>();
        Button loadButton = new Button("Load");
        MenuButton exportButton = new MenuButton("Export");

        HBox row = invokePrivate(
                "buildSelectRow",
                new Class<?>[]{ComboBox.class, ComboBox.class, Button.class, MenuButton.class},
                classBox,
                sessionBox,
                loadButton,
                exportButton
        );

        assertEquals(Pos.CENTER_LEFT, row.getAlignment());
        assertEquals(4, row.getChildren().size());
        assertSame(classBox, row.getChildren().get(0));
        assertSame(sessionBox, row.getChildren().get(1));
        assertSame(loadButton, row.getChildren().get(2));
        assertSame(exportButton, row.getChildren().get(3));
    }

    @Test
    void buildStatsLabelReturnsStyledLabel() throws Exception {
        Label label = invokePrivate(
                "buildStatsLabel",
                new Class<?>[]{String.class, String.class, String.class},
                "teacher.reports.stats.present",
                "{count}",
                "10"
        );

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getText().contains("10"));
        assertTrue(label.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildStatsRowContainsLabels() throws Exception {
        Label present = new Label("P");
        Label absent = new Label("A");
        Label excused = new Label("E");
        Label rate = new Label("R");

        HBox row = invokePrivate(
                "buildStatsRow",
                new Class<?>[]{Label.class, Label.class, Label.class, Label.class},
                present,
                absent,
                excused,
                rate
        );

        assertEquals(Pos.CENTER_LEFT, row.getAlignment());
        assertEquals(4, row.getChildren().size());
    }

    @Test
    void buildReportTableCreatesThreeColumns() throws Exception {
        TableView<?> table = invokePrivate("buildReportTable");

        assertNotNull(table);
        assertEquals(340.0, table.getPrefHeight());
        assertEquals(3, table.getColumns().size());
        assertFalse(table.getColumns().get(0).getText().isBlank());
        assertFalse(table.getColumns().get(1).getText().isBlank());
        assertFalse(table.getColumns().get(2).getText().isBlank());
    }

    @Test
    void resetReportUIClearsRowsAndResetsLabels() throws Exception {
        var field = TeacherReportsPage.class.getDeclaredField("tableRows");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        javafx.collections.ObservableList<TeacherReportsPage.ReportRow> rows =
                (javafx.collections.ObservableList<TeacherReportsPage.ReportRow>) field.get(page);

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
}