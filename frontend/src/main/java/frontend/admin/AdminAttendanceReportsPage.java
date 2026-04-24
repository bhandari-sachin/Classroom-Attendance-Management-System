package frontend.admin;

import frontend.api.AdminApi;
import frontend.api.ReportApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import frontend.ui.ReportRow;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminAttendanceReportsPage {

    private static final Logger LOGGER =
            Logger.getLogger(AdminAttendanceReportsPage.class.getName());

    static final class ClassItem {
        private final Long id;
        private final String label;

        ClassItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    record ReportSummary(
            List<ReportRow> rows,
            int present,
            int absent,
            int excused,
            int total,
            double rate
    ) {
    }

    private record ReportFilters(
            ComboBox<ClassItem> classFilter,
            ComboBox<String> timeFilter,
            TextField studentSearch
    ) {
    }

    private record ReportView(
            GridPane statsGrid,
            TableView<ReportRow> table,
            Label errorLabel
    ) {
    }

    private record ExportRequest(
            String format,
            String chooserTitle,
            String initialFileName
    ) {
    }

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        HelperClass helper = new HelperClass();

        String adminName = AdminPageSupport.resolveAdminName(state, helper);

        VBox content = AdminPageSupport.buildContentContainer();

        Label title = buildTitle(helper);
        Label subtitle = buildSubtitle(helper);

        ComboBox<ClassItem> classFilter = buildClassFilter(helper);
        ComboBox<String> timeFilter = buildTimeFilter(helper);
        TextField studentSearch = buildStudentSearch(helper);

        GridPane filters = buildFilters(helper, classFilter, timeFilter, studentSearch);
        GridPane statsGrid = buildStatsGrid();

        Label errorLabel = buildErrorLabel();
        Label tableTitle = buildTableTitle(helper);
        TableView<ReportRow> table = buildReportTable(helper);
        MenuButton exportButton = buildExportButton(helper);

        AdminApi adminApi = new AdminApi("http://localhost:8081", jwtStore);
        ReportApi reportApi = new ReportApi(System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081"));

        addExportActions(exportButton, scene, helper, reportApi, jwtStore, state);

        ReportFilters reportFilters = new ReportFilters(classFilter, timeFilter, studentSearch);
        ReportView reportView = new ReportView(statsGrid, table, errorLabel);

        Runnable loadReport = () -> loadReport(adminApi, helper, reportFilters, reportView);

        classFilter.setOnAction(e -> loadReport.run());
        timeFilter.setOnAction(e -> loadReport.run());
        studentSearch.textProperty().addListener((obs, oldValue, newValue) -> loadReport.run());

        loadClasses(adminApi, helper, classFilter, errorLabel, loadReport);

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                exportButton,
                errorLabel,
                statsGrid,
                tableTitle,
                table
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminPageSupport.wrapWithSidebar(
                adminName,
                helper,
                scroll,
                "third",
                router,
                jwtStore
        );
    }

    private Label buildTitle(HelperClass helper) {
        Label title = new Label(helper.getMessage("admin.reports.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle(HelperClass helper) {
        Label subtitle = new Label(helper.getMessage("admin.reports.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private ComboBox<ClassItem> buildClassFilter(HelperClass helper) {
        ComboBox<ClassItem> classFilter = new ComboBox<>();
        classFilter.setPromptText(helper.getMessage("admin.reports.filter.class.loading"));
        return classFilter;
    }

    private ComboBox<String> buildTimeFilter(HelperClass helper) {
        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll(
                helper.getMessage("admin.reports.filter.time.all"),
                helper.getMessage("admin.reports.filter.time.thisMonth"),
                helper.getMessage("admin.reports.filter.time.lastMonth"),
                helper.getMessage("admin.reports.filter.time.thisYear")
        );
        timeFilter.setValue(helper.getMessage("admin.reports.filter.time.thisMonth"));
        return timeFilter;
    }

    private TextField buildStudentSearch(HelperClass helper) {
        TextField studentSearch = new TextField();
        studentSearch.setPromptText(helper.getMessage("admin.reports.filter.search.placeholder"));
        return studentSearch;
    }

    private GridPane buildFilters(
            HelperClass helper,
            ComboBox<ClassItem> classFilter,
            ComboBox<String> timeFilter,
            TextField studentSearch
    ) {
        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(8);

        filters.add(new VBox(new Label(helper.getMessage("admin.reports.filter.class")), classFilter), 0, 0);
        filters.add(new VBox(new Label(helper.getMessage("admin.reports.filter.time")), timeFilter), 1, 0);
        filters.add(new VBox(new Label(helper.getMessage("admin.reports.filter.search")), studentSearch), 2, 0);

        return filters;
    }

    private GridPane buildStatsGrid() {
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        ColumnConstraints first = new ColumnConstraints();
        first.setHgrow(Priority.ALWAYS);
        first.setFillWidth(true);

        ColumnConstraints second = new ColumnConstraints();
        second.setHgrow(Priority.ALWAYS);
        second.setFillWidth(true);

        stats.getColumnConstraints().addAll(first, second);
        return stats;
    }

    private Label buildErrorLabel() {
        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);
        return error;
    }

    private Label buildTableTitle(HelperClass helper) {
        Label tableTitle = new Label(helper.getMessage("admin.reports.table.title"));
        tableTitle.getStyleClass().add("section-title");
        return tableTitle;
    }

    private TableView<ReportRow> buildReportTable(HelperClass helper) {
        TableView<ReportRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ReportRow, String> studentColumn =
                new TableColumn<>(helper.getMessage("admin.reports.table.student"));
        studentColumn.setCellValueFactory(data -> data.getValue().studentProperty());

        TableColumn<ReportRow, String> dateColumn =
                new TableColumn<>(helper.getMessage("admin.reports.table.date"));
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());

        TableColumn<ReportRow, String> statusColumn =
                new TableColumn<>(helper.getMessage("admin.reports.table.status"));
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        table.getColumns().addAll(studentColumn, dateColumn, statusColumn);
        return table;
    }

    private MenuButton buildExportButton(HelperClass helper) {
        MenuButton exportButton = new MenuButton(helper.getMessage("common.export"));
        exportButton.getStyleClass().addAll("pill", "pill-blue");
        return exportButton;
    }

    private void addExportActions(
            MenuButton exportButton,
            Scene scene,
            HelperClass helper,
            ReportApi reportApi,
            JwtStore jwtStore,
            AuthState state
    ) {
        MenuItem exportPdf = new MenuItem(helper.getMessage("common.export.pdf"));
        MenuItem exportCsv = new MenuItem(helper.getMessage("common.export.csv"));

        ExportRequest pdfRequest = new ExportRequest(
                "pdf",
                helper.getMessage("admin.reports.export.pdf.title"),
                helper.getMessage("admin.reports.export.pdf.filename")
        );

        ExportRequest csvRequest = new ExportRequest(
                "csv",
                helper.getMessage("admin.reports.export.csv.title"),
                helper.getMessage("admin.reports.export.csv.filename")
        );

        exportPdf.setOnAction(e -> exportAdminReport(
                scene,
                helper,
                reportApi,
                jwtStore,
                state,
                pdfRequest
        ));

        exportCsv.setOnAction(e -> exportAdminReport(
                scene,
                helper,
                reportApi,
                jwtStore,
                state,
                csvRequest
        ));

        exportButton.getItems().addAll(exportPdf, exportCsv);
    }

    private void exportAdminReport(
            Scene scene,
            HelperClass helper,
            ReportApi reportApi,
            JwtStore jwtStore,
            AuthState state,
            ExportRequest request
    ) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(request.chooserTitle());
        chooser.setInitialFileName(request.initialFileName());
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "pdf".equalsIgnoreCase(request.format()) ? "PDF Files" : "CSV Files",
                        "pdf".equalsIgnoreCase(request.format()) ? "*.pdf" : "*.csv"
                )
        );

        File destination = chooser.showSaveDialog(scene.getWindow());
        if (destination == null) {
            return;
        }

        new Thread(() -> {
            try {
                reportApi.exportAdminReport(jwtStore, state, request.format(), destination.getAbsolutePath());

                Platform.runLater(() -> showInfo(
                        helper.getMessage(
                                "pdf".equalsIgnoreCase(request.format())
                                        ? "admin.reports.export.success.pdf"
                                        : "admin.reports.export.success.csv"
                        ).replace("{path}", destination.getAbsolutePath())
                ));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Admin report export was interrupted.", ex);
                Platform.runLater(() -> showError(
                        helper.getMessage("admin.reports.export.error")
                                .replace("{error}", safeErrorMessage(ex))
                ));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to export admin report.", ex);
                Platform.runLater(() -> showError(
                        helper.getMessage("admin.reports.export.error")
                                .replace("{error}", safeErrorMessage(ex))
                ));
            }
        }).start();
    }

    private void loadClasses(
            AdminApi adminApi,
            HelperClass helper,
            ComboBox<ClassItem> classFilter,
            Label errorLabel,
            Runnable loadReport
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = adminApi.getAdminClassesRaw();
                List<ClassItem> items = mapClassItems(classes);

                Platform.runLater(() -> {
                    classFilter.getItems().setAll(items);
                    if (!items.isEmpty()) {
                        classFilter.setValue(items.get(0));
                        loadReport.run();
                    }
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading admin classes for reports page was interrupted.", ex);
                Platform.runLater(() -> showInlineError(
                        errorLabel,
                        helper.getMessage("admin.reports.error.loadClasses")
                ));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load admin classes for reports page.", ex);
                Platform.runLater(() -> showInlineError(
                        errorLabel,
                        helper.getMessage("admin.reports.error.loadClasses")
                ));
            }
        }).start();
    }

    List<ClassItem> mapClassItems(List<Map<String, Object>> classes) {
        List<ClassItem> items = new ArrayList<>();

        for (Map<String, Object> classData : classes) {
            Long id = Long.valueOf(((Number) classData.get("id")).longValue());
            String label =
                    valueOr(classData.get("classCode"), "—")
                            + " — "
                            + valueOr(classData.get("name"), "Unnamed");

            items.add(new ClassItem(id, label));
        }

        return items;
    }

    private void loadReport(
            AdminApi adminApi,
            HelperClass helper,
            ReportFilters filters,
            ReportView view
    ) {
        ClassItem selectedClass = filters.classFilter().getValue();
        if (selectedClass == null || selectedClass.getId() == null) {
            view.statsGrid().getChildren().clear();
            view.table().getItems().clear();
            return;
        }

        String period = filters.timeFilter().getValue();
        String searchText = filters.studentSearch().getText();

        new Thread(() -> {
            try {
                List<Map<String, Object>> rows = adminApi.getAttendanceReport(
                        selectedClass.getId(),
                        period,
                        searchText
                );

                ReportSummary summary = mapReportSummary(rows);

                Platform.runLater(() -> {
                    hideInlineError(view.errorLabel());
                    renderStats(view.statsGrid(), helper, summary);
                    view.table().getItems().setAll(summary.rows());
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading attendance report was interrupted.", ex);
                Platform.runLater(() ->
                        showInlineError(view.errorLabel(), helper.getMessage("admin.reports.error.loadReport"))
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load attendance report.", ex);
                Platform.runLater(() ->
                        showInlineError(view.errorLabel(), helper.getMessage("admin.reports.error.loadReport"))
                );
            }
        }).start();
    }

    ReportSummary mapReportSummary(List<Map<String, Object>> rawRows) {
        int present = 0;
        int absent = 0;
        int excused = 0;
        List<ReportRow> mappedRows = new ArrayList<>();

        for (Map<String, Object> row : rawRows) {
            String firstName = valueOr(row.get("firstName"), "");
            String lastName = valueOr(row.get("lastName"), "");
            String studentName = (firstName + " " + lastName).trim();
            String date = valueOr(row.get("sessionDate"), "—");
            String status = valueOr(row.get("status"), "—");

            mappedRows.add(new ReportRow(studentName, date, status));

            switch (status.toUpperCase()) {
                case "PRESENT" -> present++;
                case "ABSENT" -> absent++;
                case "EXCUSED" -> excused++;
                default -> {
                    // No counter update for unknown status values
                }
            }
        }

        int total = rawRows.size();
        double rate = total == 0 ? 0.0 : (present * 100.0) / total;

        return new ReportSummary(mappedRows, present, absent, excused, total, rate);
    }

    private void renderStats(GridPane statsGrid, HelperClass helper, ReportSummary summary) {
        statsGrid.getChildren().clear();

        statsGrid.add(
                AdminUI.makeStatCard(
                        helper.getMessage("admin.reports.stats.rate"),
                        formatOneDecimal(summary.rate()) + "%",
                        "📈",
                        "accent-green"
                ),
                0,
                0
        );

        statsGrid.add(
                AdminUI.makeStatCard(
                        helper.getMessage("admin.reports.stats.present"),
                        String.valueOf(summary.present()),
                        "🟢",
                        "accent-green"
                ),
                1,
                0
        );

        statsGrid.add(
                AdminUI.makeStatCard(
                        helper.getMessage("admin.reports.stats.absent"),
                        String.valueOf(summary.absent()),
                        "🔴",
                        "accent-orange"
                ),
                0,
                1
        );

        statsGrid.add(
                AdminUI.makeStatCard(
                        helper.getMessage("admin.reports.stats.excused"),
                        String.valueOf(summary.excused()),
                        "🟠",
                        "accent-purple"
                ),
                1,
                1
        );

        statsGrid.add(
                AdminUI.makeStatCard(
                        helper.getMessage("admin.reports.stats.total"),
                        String.valueOf(summary.total()),
                        "📄",
                        "accent-purple"
                ),
                0,
                2
        );
    }

    private void showInlineError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideInlineError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    String valueOr(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "Unknown error";
        }
        return throwable.getMessage();
    }

    String formatOneDecimal(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        long wholePart = (long) rounded;

        if (rounded == wholePart) {
            return wholePart + ".0";
        }

        return Double.toString(rounded);
    }
}