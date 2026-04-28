package frontend.teacher;

import frontend.api.ReportApi;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherReportsPage {

    private static final Logger LOGGER =
            Logger.getLogger(TeacherReportsPage.class.getName());

    private static final String UNKNOWN_ERROR = "Unknown error";
    private static final String ERROR_PLACEHOLDER = "{error}";
    private static final String STATS_PRESENT_KEY = "teacher.reports.stats.present";
    private static final String STATS_ABSENT_KEY = "teacher.reports.stats.absent";
    private static final String STATS_EXCUSED_KEY = "teacher.reports.stats.excused";
    private static final String STATS_RATE_KEY = "teacher.reports.stats.rate";
    private static final String COUNT_PLACEHOLDER = "{count}";
    private static final String RATE_PLACEHOLDER = "{rate}";
    private static final double EPSILON = 1e-9;

    static class ClassItem {
        final long id;
        final String label;

        ClassItem(long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static class SessionItem {
        final long id;
        final String label;

        SessionItem(long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static class ReportRow {
        final String name;
        final String email;
        final String status;

        ReportRow(String name, String email, String status) {
            this.name = name;
            this.email = email;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getStatus() {
            return status;
        }
    }

    private record ReportSummaryLabels(
            Label presentLabel,
            Label absentLabel,
            Label excusedLabel,
            Label rateLabel
    ) {
    }

    private final ObservableList<ReportRow> tableRows = FXCollections.observableArrayList();
    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = TeacherPageSupport.resolveTeacherName(state, helper);

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);
        ReportApi reportApi = new ReportApi(backendUrl);

        VBox page = TeacherPageSupport.buildPageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();

        ComboBox<ClassItem> classBox = buildClassBox();
        ComboBox<SessionItem> sessionBox = buildSessionBox();
        Button loadButton = buildLoadButton();
        MenuButton exportButton = buildExportButton();

        MenuItem exportPdf = new MenuItem(helper.getMessage("common.export.pdf"));
        MenuItem exportCsv = new MenuItem(helper.getMessage("common.export.csv"));
        exportButton.getItems().addAll(exportPdf, exportCsv);

        HBox selects = buildSelectRow(classBox, sessionBox, loadButton, exportButton);

        Label presentLabel = buildStatsLabel(STATS_PRESENT_KEY, COUNT_PLACEHOLDER, "—");
        Label absentLabel = buildStatsLabel(STATS_ABSENT_KEY, COUNT_PLACEHOLDER, "—");
        Label excusedLabel = buildStatsLabel(STATS_EXCUSED_KEY, COUNT_PLACEHOLDER, "—");
        Label rateLabel = buildStatsLabel(STATS_RATE_KEY, RATE_PLACEHOLDER, "—");

        ReportSummaryLabels summaryLabels =
                new ReportSummaryLabels(presentLabel, absentLabel, excusedLabel, rateLabel);

        HBox stats = buildStatsRow(presentLabel, absentLabel, excusedLabel, rateLabel);
        TableView<ReportRow> table = buildReportTable();

        page.getChildren().addAll(title, subtitle, selects, stats, table);

        Runnable resetReportUI = () -> resetReportUI(summaryLabels);

        loadClasses(api, jwtStore, state, classBox);

        classBox.setOnAction(e -> {
            ClassItem selectedClass = classBox.getValue();
            if (selectedClass == null) {
                exportButton.setDisable(true);
                return;
            }

            exportButton.setDisable(false);
            resetReportUI.run();
            sessionBox.getItems().clear();
            sessionBox.setValue(null);

            loadSessions(api, jwtStore, state, selectedClass.id, sessionBox);
        });

        sessionBox.setOnAction(e -> resetReportUI.run());

        loadButton.setOnAction(e -> {
            SessionItem selectedSession = sessionBox.getValue();
            if (selectedSession == null) {
                showWarning(helper.getMessage("teacher.reports.alert.selectSession"));
                return;
            }

            loadSessionReport(
                    api,
                    jwtStore,
                    state,
                    selectedSession.id,
                    loadButton,
                    summaryLabels
            );
        });

        exportPdf.setOnAction(e -> exportReport(scene, reportApi, jwtStore, state, classBox.getValue(), "pdf"));
        exportCsv.setOnAction(e -> exportReport(scene, reportApi, jwtStore, state, classBox.getValue(), "csv"));

        return TeacherPageSupport.wrapWithSidebar(
                teacherName,
                helper,
                page,
                "third",
                router,
                jwtStore
        );
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("teacher.reports.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("teacher.reports.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private ComboBox<ClassItem> buildClassBox() {
        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(helper.getMessage("teacher.reports.select.class"));
        classBox.setMaxWidth(340);
        return classBox;
    }

    private ComboBox<SessionItem> buildSessionBox() {
        ComboBox<SessionItem> sessionBox = new ComboBox<>();
        sessionBox.setPromptText(helper.getMessage("teacher.reports.select.session"));
        sessionBox.setMaxWidth(340);
        return sessionBox;
    }

    private Button buildLoadButton() {
        Button load = new Button(helper.getMessage("teacher.reports.load"));
        load.getStyleClass().addAll("pill", "pill-green");
        return load;
    }

    private MenuButton buildExportButton() {
        MenuButton exportBtn = new MenuButton(helper.getMessage("common.export"));
        exportBtn.getStyleClass().addAll("pill", "pill-blue");
        exportBtn.setDisable(true);
        return exportBtn;
    }

    private HBox buildSelectRow(
            ComboBox<ClassItem> classBox,
            ComboBox<SessionItem> sessionBox,
            Button loadButton,
            MenuButton exportButton
    ) {
        HBox selects = new HBox(10);
        selects.setAlignment(Pos.CENTER_LEFT);
        selects.getChildren().addAll(classBox, sessionBox, loadButton, exportButton);
        return selects;
    }

    private Label buildStatsLabel(String messageKey, String placeholder, String value) {
        String text = helper.getMessage(messageKey).replace(placeholder, value);
        Label label = new Label(text);
        label.getStyleClass().add("subtitle");
        return label;
    }

    private HBox buildStatsRow(Label present, Label absent, Label excused, Label rate) {
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(present, absent, excused, rate);
        return stats;
    }

    private TableView<ReportRow> buildReportTable() {
        TableView<ReportRow> table = new TableView<>(tableRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(340);

        TableColumn<ReportRow, String> colName = new TableColumn<>(helper.getMessage("teacher.reports.table.student"));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<ReportRow, String> colEmail = new TableColumn<>(helper.getMessage("teacher.reports.table.email"));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<ReportRow, String> colStatus = new TableColumn<>(helper.getMessage("teacher.reports.table.status"));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(localizeAttendanceStatus(item));
            }
        });

        table.getColumns().setAll(colName, colEmail, colStatus);
        return table;
    }

    private void resetReportUI(ReportSummaryLabels labels) {
        labels.presentLabel().setText(helper.getMessage(STATS_PRESENT_KEY).replace(COUNT_PLACEHOLDER, "—"));
        labels.absentLabel().setText(helper.getMessage(STATS_ABSENT_KEY).replace(COUNT_PLACEHOLDER, "—"));
        labels.excusedLabel().setText(helper.getMessage(STATS_EXCUSED_KEY).replace(COUNT_PLACEHOLDER, "—"));
        labels.rateLabel().setText(helper.getMessage(STATS_RATE_KEY).replace(RATE_PLACEHOLDER, "—"));
        tableRows.clear();
    }

    private void loadClasses(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = api.getMyClasses(jwtStore, state);
                List<ClassItem> items = mapClassItems(classes);
                Platform.runLater(() -> classBox.getItems().setAll(items));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading teacher classes for reports was interrupted.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadClasses")
                                .replace(ERROR_PLACEHOLDER, safeErrorMessage(ex)))
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load teacher classes for reports.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadClasses")
                                .replace(ERROR_PLACEHOLDER, safeErrorMessage(ex)))
                );
            }
        }).start();
    }

    List<ClassItem> mapClassItems(List<Map<String, Object>> classes) {
        return classes.stream().map(classMap -> {
            long id = Long.parseLong(String.valueOf(classMap.get("id")));
            String classCode = String.valueOf(classMap.get("classCode"));
            String name = String.valueOf(classMap.get("name"));
            return new ClassItem(id, classCode + " — " + name);
        }).toList();
    }

    private void loadSessions(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            long classId,
            ComboBox<SessionItem> sessionBox
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> sessions = api.getSessionsForClass(jwtStore, state, classId);
                List<SessionItem> items = mapSessionItems(sessions);
                Platform.runLater(() -> sessionBox.getItems().setAll(items));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading teacher sessions for reports was interrupted.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadSessions")
                                .replace(ERROR_PLACEHOLDER, safeErrorMessage(ex)))
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load teacher sessions for reports.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadSessions")
                                .replace(ERROR_PLACEHOLDER, safeErrorMessage(ex)))
                );
            }
        }).start();
    }

    List<SessionItem> mapSessionItems(List<Map<String, Object>> sessions) {
        return sessions.stream().map(session -> {
            long sessionId = Long.parseLong(String.valueOf(session.get("id")));
            String date = pick(session, "date", "sessionDate", "session_date");
            String code = pick(session, "code", "qrCode", "qr_token", "qrToken");

            String label = (date.isBlank()
                    ? helper.getMessage("teacher.reports.session.default").replace("{id}", String.valueOf(sessionId))
                    : date)
                    + (code.isBlank()
                    ? ""
                    : " " + helper.getMessage("teacher.reports.session.code").replace("{code}", code));

            return new SessionItem(sessionId, label);
        }).toList();
    }

    private void loadSessionReport(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            long sessionId,
            Button loadButton,
            ReportSummaryLabels labels
    ) {
        loadButton.setDisable(true);
        tableRows.clear();

        new Thread(() -> {
            try {
                Map<String, Object> response = api.getSessionReport(jwtStore, state, sessionId);

                Map<String, Object> report = response.containsKey("report")
                        ? asMap(response.get("report"))
                        : response;

                Map<String, Object> stats = asMap(report.get("stats"));
                List<Map<String, Object>> rows = asList(report.get("rows"));

                int presentCount = asInt(stats.get("present"));
                int absentCount = asInt(stats.get("absent"));
                int excusedCount = asInt(stats.get("excused"));
                double rate = asDouble(stats.get("rate"));

                List<ReportRow> mappedRows = mapReportRows(rows);

                Platform.runLater(() -> {
                    labels.presentLabel().setText(
                            helper.getMessage(STATS_PRESENT_KEY)
                                    .replace(COUNT_PLACEHOLDER, String.valueOf(presentCount))
                    );
                    labels.absentLabel().setText(
                            helper.getMessage(STATS_ABSENT_KEY)
                                    .replace(COUNT_PLACEHOLDER, String.valueOf(absentCount))
                    );
                    labels.excusedLabel().setText(
                            helper.getMessage(STATS_EXCUSED_KEY)
                                    .replace(COUNT_PLACEHOLDER, String.valueOf(excusedCount))
                    );
                    labels.rateLabel().setText(
                            helper.getMessage(STATS_RATE_KEY)
                                    .replace(RATE_PLACEHOLDER, formatOneDecimal(rate))
                    );
                    tableRows.setAll(mappedRows);
                    loadButton.setDisable(false);
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading teacher session report was interrupted.", ex);
                Platform.runLater(() -> {
                    loadButton.setDisable(false);
                    showError(helper.getMessage("teacher.reports.error.loadReport") + " "
                            + safeErrorMessage(ex));
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load teacher session report.", ex);
                Platform.runLater(() -> {
                    loadButton.setDisable(false);
                    showError(helper.getMessage("teacher.reports.error.loadReport") + " "
                            + safeErrorMessage(ex));
                });
            }
        }).start();
    }

    List<ReportRow> mapReportRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            String firstName = pick(row, "firstName", "first_name");
            String lastName = pick(row, "lastName", "last_name");
            String email = pick(row, "email");
            String status = pick(row, "status");

            String fullName = (firstName + " " + lastName).trim();
            if (fullName.isBlank()) {
                fullName = "—";
            }

            return new ReportRow(fullName, email, status);
        }).toList();
    }

    private void exportReport(
            Scene scene,
            ReportApi reportApi,
            JwtStore jwtStore,
            AuthState state,
            ClassItem selectedClass,
            String format
    ) {
        if (selectedClass == null) {
            return;
        }

        boolean pdf = "pdf".equalsIgnoreCase(format);

        FileChooser chooser = new FileChooser();
        chooser.setTitle(helper.getMessage(
                pdf
                        ? "teacher.reports.filechooser.pdf.title"
                        : "teacher.reports.filechooser.csv.title"
        ));
        chooser.setInitialFileName(helper.getMessage(
                pdf
                        ? "teacher.reports.filechooser.pdf.name"
                        : "teacher.reports.filechooser.csv.name"
        ));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        pdf ? "PDF Files" : "CSV Files",
                        pdf ? "*.pdf" : "*.csv"
                )
        );

        File destination = chooser.showSaveDialog(scene.getWindow());
        if (destination == null) {
            return;
        }

        new Thread(() -> {
            try {
                reportApi.exportTeacherReport(jwtStore, state, selectedClass.id, format, destination.getAbsolutePath());
                Platform.runLater(() -> showInfo(
                        helper.getMessage(
                                pdf
                                        ? "teacher.reports.export.success.pdf"
                                        : "teacher.reports.export.success.csv"
                        ) + "\n" + destination.getAbsolutePath()
                ));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Exporting teacher report was interrupted.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.export") + " "
                                + safeErrorMessage(ex))
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to export teacher report.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.export") + " "
                                + safeErrorMessage(ex))
                );
            }
        }).start();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return UNKNOWN_ERROR;
        }
        return throwable.getMessage();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object object) {
        if (object instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> asList(Object object) {
        if (object instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    static int asInt(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(object));
    }

    static double asDouble(Object object) {
        if (object == null) {
            return 0.0;
        }
        if (object instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(object));
    }

    static String pick(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank()) {
            return "—";
        }

        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> helper.getMessage("student.attendance.stats.present");
            case "ABSENT" -> helper.getMessage("student.attendance.stats.absent");
            case "EXCUSED" -> helper.getMessage("student.attendance.stats.excused");
            default -> status;
        };
    }

    String formatOneDecimal(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        long wholePart = (long) rounded;

        if (Math.abs(rounded - wholePart) < EPSILON) {
            return wholePart + ".0";
        }

        return Double.toString(rounded);
    }
}