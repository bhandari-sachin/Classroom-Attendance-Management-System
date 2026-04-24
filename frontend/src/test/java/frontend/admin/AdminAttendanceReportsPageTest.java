package frontend.admin;

import frontend.ui.HelperClass;
import frontend.ui.ReportRow;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AdminAttendanceReportsPageTest {

    private final AdminAttendanceReportsPage page = new AdminAttendanceReportsPage();

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
    void valueOrShouldReturnFallbackWhenValueIsNull() {
        assertEquals("fallback", page.valueOr(null, "fallback"));
    }

    @Test
    void valueOrShouldReturnStringValueWhenValueExists() {
        assertEquals("hello", page.valueOr("hello", "fallback"));
        assertEquals("123", page.valueOr(123, "fallback"));
    }

    @Test
    void formatOneDecimalShouldKeepSingleDecimalForWholeNumbers() {
        assertEquals("0.0", page.formatOneDecimal(0.0));
        assertEquals("10.0", page.formatOneDecimal(10.0));
        assertEquals("12.0", page.formatOneDecimal(12.04));
    }

    @Test
    void formatOneDecimalShouldRoundToOneDecimalPlace() {
        assertEquals("12.3", page.formatOneDecimal(12.34));
        assertEquals("12.4", page.formatOneDecimal(12.35));
        assertEquals("99.9", page.formatOneDecimal(99.94));
    }

    @Test
    void mapClassItemsShouldMapClassDataCorrectly() {
        List<Map<String, Object>> classes = List.of(
                Map.of("id", 1L, "classCode", "MTH101", "name", "Mathematics"),
                Map.of("id", 2, "classCode", "PHY202", "name", "Physics")
        );

        List<AdminAttendanceReportsPage.ClassItem> result = page.mapClassItems(classes);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("MTH101 — Mathematics", result.get(0).toString());

        assertEquals(2L, result.get(1).getId());
        assertEquals("PHY202 — Physics", result.get(1).toString());
    }

    @Test
    void mapClassItemsShouldUseFallbackValuesWhenFieldsAreNull() {
        Map<String, Object> classData = new HashMap<>();
        classData.put("id", 5L);
        classData.put("classCode", null);
        classData.put("name", null);

        List<AdminAttendanceReportsPage.ClassItem> result = page.mapClassItems(List.of(classData));

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
        assertEquals("— — Unnamed", result.getFirst().toString());
    }

    @Test
    void mapReportSummaryShouldCalculateCountsRateAndRows() {
        List<Map<String, Object>> rawRows = List.of(
                Map.of("firstName", "Oscar", "lastName", "al", "sessionDate", "2026-04-01", "status", "PRESENT"),
                Map.of("firstName", "Anna", "lastName", "Smith", "sessionDate", "2026-04-02", "status", "ABSENT"),
                Map.of("firstName", "John", "lastName", "Doe", "sessionDate", "2026-04-03", "status", "EXCUSED"),
                Map.of("firstName", "Lina", "lastName", "Ray", "sessionDate", "2026-04-04", "status", "present")
        );

        AdminAttendanceReportsPage.ReportSummary summary = page.mapReportSummary(rawRows);

        assertEquals(4, summary.rows().size());
        assertEquals(2, summary.present());
        assertEquals(1, summary.absent());
        assertEquals(1, summary.excused());
        assertEquals(4, summary.total());
        assertEquals(50.0, summary.rate());

        ReportRow firstRow = summary.rows().getFirst();
        assertEquals("Oscar al", firstRow.studentProperty().get());
        assertEquals("2026-04-01", firstRow.dateProperty().get());
        assertEquals("PRESENT", firstRow.statusProperty().get());
    }

    @Test
    void mapReportSummaryShouldHandleMissingNamesAndUnknownStatuses() {
        List<Map<String, Object>> rawRows = List.of(
                Map.of("firstName", "", "lastName", "", "sessionDate", "2026-04-01", "status", "LATE"),
                Map.of("firstName", "Oscar", "lastName", "", "sessionDate", "2026-04-02", "status", "—")
        );

        AdminAttendanceReportsPage.ReportSummary summary = page.mapReportSummary(rawRows);

        assertEquals(2, summary.rows().size());
        assertEquals(0, summary.present());
        assertEquals(0, summary.absent());
        assertEquals(0, summary.excused());
        assertEquals(2, summary.total());
        assertEquals(0.0, summary.rate());

        assertEquals("", summary.rows().get(0).studentProperty().get());
        assertEquals("Oscar", summary.rows().get(1).studentProperty().get());
    }

    @Test
    void mapReportSummaryShouldReturnZeroRateForEmptyRows() {
        AdminAttendanceReportsPage.ReportSummary summary = page.mapReportSummary(List.of());

        assertTrue(summary.rows().isEmpty());
        assertEquals(0, summary.present());
        assertEquals(0, summary.absent());
        assertEquals(0, summary.excused());
        assertEquals(0, summary.total());
        assertEquals(0.0, summary.rate());
    }

    @Test
    void buildTitleShouldReturnStyledLabel() throws Exception {
        HelperClass helper = new HelperClass();

        Label title = invokePrivate(page, "buildTitle", Label.class, HelperClass.class, helper);

        assertNotNull(title);
        assertNotNull(title.getText());
        assertFalse(title.getText().isBlank());
        assertTrue(title.getStyleClass().contains("title"));
    }

    @Test
    void buildSubtitleShouldReturnStyledLabel() throws Exception {
        HelperClass helper = new HelperClass();

        Label subtitle = invokePrivate(page, "buildSubtitle", Label.class, HelperClass.class, helper);

        assertNotNull(subtitle);
        assertNotNull(subtitle.getText());
        assertFalse(subtitle.getText().isBlank());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildClassFilterShouldSetPromptText() throws Exception {
        HelperClass helper = new HelperClass();

        ComboBox<?> classFilter =
                invokePrivate(page, "buildClassFilter", ComboBox.class, HelperClass.class, helper);

        assertNotNull(classFilter);
        assertNotNull(classFilter.getPromptText());
        assertFalse(classFilter.getPromptText().isBlank());
    }

    @Test
    void buildTimeFilterShouldContainOptionsAndDefaultValue() throws Exception {
        HelperClass helper = new HelperClass();

        ComboBox<?> timeFilter =
                invokePrivate(page, "buildTimeFilter", ComboBox.class, HelperClass.class, helper);

        assertNotNull(timeFilter);
        assertEquals(4, timeFilter.getItems().size());
        assertNotNull(timeFilter.getValue());
        assertFalse(timeFilter.getValue().toString().isBlank());
    }

    @Test
    void buildStudentSearchShouldSetPromptText() throws Exception {
        HelperClass helper = new HelperClass();

        TextField studentSearch =
                invokePrivate(page, "buildStudentSearch", TextField.class, HelperClass.class, helper);

        assertNotNull(studentSearch);
        assertNotNull(studentSearch.getPromptText());
        assertFalse(studentSearch.getPromptText().isBlank());
    }

    @Test
    void buildFiltersShouldCreateThreeFilterColumns() throws Exception {
        HelperClass helper = new HelperClass();
        ComboBox<AdminAttendanceReportsPage.ClassItem> classFilter = new ComboBox<>();
        ComboBox<String> timeFilter = new ComboBox<>();
        TextField studentSearch = new TextField();

        GridPane filters = invokePrivate(
                page,
                "buildFilters",
                GridPane.class,
                new Class<?>[]{HelperClass.class, ComboBox.class, ComboBox.class, TextField.class},
                helper,
                classFilter,
                timeFilter,
                studentSearch
        );

        assertNotNull(filters);
        assertEquals(12.0, filters.getHgap());
        assertEquals(8.0, filters.getVgap());
        assertEquals(3, filters.getChildren().size());
    }

    @Test
    void buildStatsGridShouldSetTwoResizableColumns() throws Exception {
        GridPane statsGrid = invokePrivate(page, "buildStatsGrid", GridPane.class);

        assertNotNull(statsGrid);
        assertEquals(12.0, statsGrid.getHgap());
        assertEquals(12.0, statsGrid.getVgap());
        assertEquals(2, statsGrid.getColumnConstraints().size());
    }

    @Test
    void buildErrorLabelShouldBeHiddenByDefault() throws Exception {
        Label errorLabel = invokePrivate(page, "buildErrorLabel", Label.class);

        assertNotNull(errorLabel);
        assertFalse(errorLabel.isVisible());
        assertFalse(errorLabel.isManaged());
        assertTrue(errorLabel.getStyleClass().contains("error"));
    }

    @Test
    void buildTableTitleShouldReturnStyledLabel() throws Exception {
        HelperClass helper = new HelperClass();

        Label tableTitle = invokePrivate(page, "buildTableTitle", Label.class, HelperClass.class, helper);

        assertNotNull(tableTitle);
        assertNotNull(tableTitle.getText());
        assertFalse(tableTitle.getText().isBlank());
        assertTrue(tableTitle.getStyleClass().contains("section-title"));
    }

    @Test
    void buildReportTableShouldCreateThreeColumns() throws Exception {
        HelperClass helper = new HelperClass();

        TableView<?> table =
                invokePrivate(page, "buildReportTable", TableView.class, HelperClass.class, helper);

        assertNotNull(table);
        assertTrue(table.getStyleClass().contains("table"));
        assertEquals(3, table.getColumns().size());

        TableColumn<?, ?> studentColumn = table.getColumns().get(0);
        TableColumn<?, ?> dateColumn = table.getColumns().get(1);
        TableColumn<?, ?> statusColumn = table.getColumns().get(2);

        assertNotNull(studentColumn.getText());
        assertNotNull(dateColumn.getText());
        assertNotNull(statusColumn.getText());
    }

    @Test
    void buildExportButtonShouldContainExpectedStyles() throws Exception {
        HelperClass helper = new HelperClass();

        MenuButton exportButton =
                invokePrivate(page, "buildExportButton", MenuButton.class, HelperClass.class, helper);

        assertNotNull(exportButton);
        assertNotNull(exportButton.getText());
        assertTrue(exportButton.getStyleClass().contains("pill"));
        assertTrue(exportButton.getStyleClass().contains("pill-blue"));
    }

    @Test
    void renderStatsShouldPopulateFiveCards() throws Exception {
        HelperClass helper = new HelperClass();
        GridPane statsGrid = new GridPane();

        AdminAttendanceReportsPage.ReportSummary summary =
                new AdminAttendanceReportsPage.ReportSummary(List.of(), 8, 2, 1, 11, 72.7);

        invokePrivateVoid(
                page,
                "renderStats",
                new Class<?>[]{GridPane.class, HelperClass.class, AdminAttendanceReportsPage.ReportSummary.class},
                statsGrid,
                helper,
                summary
        );

        assertEquals(5, statsGrid.getChildren().size());
    }

    @Test
    void showInlineErrorShouldShowAndSetMessage() throws Exception {
        Label errorLabel = new Label();

        invokePrivateVoid(
                page,
                "showInlineError",
                new Class<?>[]{Label.class, String.class},
                errorLabel,
                "Load failed"
        );

        assertEquals("Load failed", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
        assertTrue(errorLabel.isManaged());
    }

    @Test
    void hideInlineErrorShouldHideLabel() throws Exception {
        Label errorLabel = new Label();
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        invokePrivateVoid(
                page,
                "hideInlineError",
                new Class<?>[]{Label.class},
                errorLabel
        );

        assertFalse(errorLabel.isVisible());
        assertFalse(errorLabel.isManaged());
    }

    private static void invokePrivateVoid(
            Object target,
            String methodName,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(target, args);
    }

    private static <T> T invokePrivate(Object target, String methodName, Class<T> returnType) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        Object result = method.invoke(target);
        return returnType.cast(result);
    }

    private static <T> T invokePrivate(
            Object target,
            String methodName,
            Class<T> returnType,
            Class<?> parameterType,
            Object arg
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterType);
        method.setAccessible(true);
        Object result = method.invoke(target, arg);
        return returnType.cast(result);
    }

    private static <T> T invokePrivate(
            Object target,
            String methodName,
            Class<T> returnType,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(target, args);
        return returnType.cast(result);
    }
}