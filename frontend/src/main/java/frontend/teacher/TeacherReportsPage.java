package frontend.teacher;

import frontend.AppLayout;
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
import javafx.geometry.Insets;
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

public class TeacherReportsPage {

    private static class ClassItem {
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

    private static class SessionItem {
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

    private static class ReportRow {
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

    private final ObservableList<ReportRow> tableRows = FXCollections.observableArrayList();
    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = resolveTeacherName(state);

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);
        ReportApi reportApi = new ReportApi(backendUrl);

        VBox page = buildPageContainer();

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

        Label presentLabel = buildStatsLabel("teacher.reports.stats.present", "{count}", "—");
        Label absentLabel = buildStatsLabel("teacher.reports.stats.absent", "{count}", "—");
        Label excusedLabel = buildStatsLabel("teacher.reports.stats.excused", "{count}", "—");
        Label rateLabel = buildStatsLabel("teacher.reports.stats.rate", "{rate}", "—");

        HBox stats = buildStatsRow(presentLabel, absentLabel, excusedLabel, rateLabel);
        TableView<ReportRow> table = buildReportTable();

        page.getChildren().addAll(title, subtitle, selects, stats, table);

        Runnable resetReportUI = () -> resetReportUI(
                presentLabel,
                absentLabel,
                excusedLabel,
                rateLabel
        );

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
                    presentLabel,
                    absentLabel,
                    excusedLabel,
                    rateLabel
            );
        });

        exportPdf.setOnAction(e -> exportReport(scene, reportApi, jwtStore, state, classBox.getValue(), "pdf"));
        exportCsv.setOnAction(e -> exportReport(scene, reportApi, jwtStore, state, classBox.getValue(), "csv"));

        return AppLayout.wrapWithSidebar(
                teacherName,
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email"),
                page,
                "third",
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("teacher-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("teacher-take");
                    }

                    @Override
                    public void goReports() {
                        router.go("teacher-reports");
                    }

                    @Override
                    public void goEmail() {
                        router.go("teacher-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveTeacherName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

    private void resetReportUI(
            Label present,
            Label absent,
            Label excused,
            Label rate
    ) {
        present.setText(helper.getMessage("teacher.reports.stats.present").replace("{count}", "—"));
        absent.setText(helper.getMessage("teacher.reports.stats.absent").replace("{count}", "—"));
        excused.setText(helper.getMessage("teacher.reports.stats.excused").replace("{count}", "—"));
        rate.setText(helper.getMessage("teacher.reports.stats.rate").replace("{rate}", "—"));
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
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadClasses").replace("{error}", ex.getMessage()))
                );
            }
        }).start();
    }

    private List<ClassItem> mapClassItems(List<Map<String, Object>> classes) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.loadSessions").replace("{error}", ex.getMessage()))
                );
            }
        }).start();
    }

    private List<SessionItem> mapSessionItems(List<Map<String, Object>> sessions) {
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
            Label presentLabel,
            Label absentLabel,
            Label excusedLabel,
            Label rateLabel
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
                    presentLabel.setText(
                            helper.getMessage("teacher.reports.stats.present")
                                    .replace("{count}", String.valueOf(presentCount))
                    );
                    absentLabel.setText(
                            helper.getMessage("teacher.reports.stats.absent")
                                    .replace("{count}", String.valueOf(absentCount))
                    );
                    excusedLabel.setText(
                            helper.getMessage("teacher.reports.stats.excused")
                                    .replace("{count}", String.valueOf(excusedCount))
                    );
                    rateLabel.setText(
                            helper.getMessage("teacher.reports.stats.rate")
                                    .replace("{rate}", formatOneDecimal(rate))
                    );

                    tableRows.setAll(mappedRows);
                    loadButton.setDisable(false);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    loadButton.setDisable(false);
                    showError(helper.getMessage("teacher.reports.error.loadReport") + " " + ex.getMessage());
                });
            }
        }).start();
    }

    private List<ReportRow> mapReportRows(List<Map<String, Object>> rows) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.reports.error.export") + " " + ex.getMessage())
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object object) {
        if (object instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object object) {
        if (object instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private static int asInt(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(object));
    }

    private static double asDouble(Object object) {
        if (object == null) {
            return 0.0;
        }
        if (object instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(object));
    }

    private static String pick(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private static String formatOneDecimal(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        long wholePart = (long) rounded;

        if (rounded == wholePart) {
            return wholePart + ".0";
        }

        return Double.toString(rounded);
    }

    private String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank() || "—".equals(status)) {
            return "—";
        }

        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> helper.getMessage("student.attendance.stats.present");
            case "ABSENT" -> helper.getMessage("student.attendance.stats.absent");
            case "EXCUSED" -> helper.getMessage("student.attendance.stats.excused");
            default -> status;
        };
    }
}