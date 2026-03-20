package frontend.teacher;

import frontend.AppLayout;
import frontend.api.ReportApi;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TeacherReportsPage {

    private static class ClassItem {
        final long id;
        final String label;
        ClassItem(long id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class SessionItem {
        final long id;
        final String label;
        SessionItem(long id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class ReportRow {
        final String name;
        final String email;
        final String status;
        ReportRow(String name, String email, String status) {
            this.name = name; this.email = email; this.status = status;
        }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
    }

    private final ObservableList<ReportRow> tableRows = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object o) {
        if (o instanceof List<?> l) return (List<Map<String, Object>>) l;
        return List.of();
    }

    private static int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    private static double asDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(o));
    }

    private static String pick(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return String.valueOf(v);
        }
        return "";
    }

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String teacherName = (state.getName() == null || state.getName().isBlank()) ? "Name" : state.getName();
        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);
        ReportApi reportApi = new ReportApi(backendUrl);

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Session reports (attendance stats + list)");
        subtitle.getStyleClass().add("subtitle");

        // Selectors
        HBox selects = new HBox(10);
        selects.setAlignment(Pos.CENTER_LEFT);

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText("Select class");
        classBox.setMaxWidth(340);

        ComboBox<SessionItem> sessionBox = new ComboBox<>();
        sessionBox.setPromptText("Select session");
        sessionBox.setMaxWidth(340);

        Button load = new Button("Load Report");
        load.getStyleClass().addAll("pill", "pill-green");

        MenuButton exportBtn = new MenuButton("Export");
        exportBtn.getStyleClass().addAll("pill", "pill-blue");
        exportBtn.setDisable(true); // enabled after a report is loaded

        MenuItem exportPdf = new MenuItem("Export as PDF");
        MenuItem exportCsv = new MenuItem("Export as CSV");
        exportBtn.getItems().addAll(exportPdf, exportCsv);

        selects.getChildren().addAll(classBox, sessionBox, load, exportBtn);

        // Stats line
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label present = new Label("Present: —");
        Label absent  = new Label("Absent: —");
        Label excused = new Label("Excused: —");
        Label rate    = new Label("Rate: —");

        present.getStyleClass().add("subtitle");
        absent.getStyleClass().add("subtitle");
        excused.getStyleClass().add("subtitle");
        rate.getStyleClass().add("subtitle");

        stats.getChildren().addAll(present, absent, excused, rate);

        // Table
        TableView<ReportRow> table = new TableView<>(tableRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);

        TableColumn<ReportRow, String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<ReportRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<ReportRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        table.getColumns().setAll(colName, colEmail, colStatus);

        page.getChildren().addAll(title, subtitle, selects, stats, table);

        // Helper to reset UI when switching
        Runnable resetReportUI = () -> {
            present.setText("Present: —");
            absent.setText("Absent: —");
            excused.setText("Excused: —");
            rate.setText("Rate: —");
            tableRows.clear();
        };

        // Load classes on open
        new Thread(() -> {
            try {
                List<Map<String, Object>> list = api.getMyClasses(jwtStore, state);

                var items = list.stream().map(m -> {
                    long id = Long.parseLong(String.valueOf(m.get("id")));
                    String classCode = String.valueOf(m.get("classCode"));
                    String name2 = String.valueOf(m.get("name"));
                    return new ClassItem(id, classCode + " — " + name2);
                }).toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Failed to load classes: " + ex.getMessage(), ButtonType.OK).showAndWait()
                );
            }
        }).start();

        // When class changes -> load sessions
        classBox.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) {
                exportBtn.setDisable(true);
                return;
            }

            exportBtn.setDisable(false);

            resetReportUI.run();
            sessionBox.getItems().clear();
            sessionBox.setValue(null);

            new Thread(() -> {
                try {
                    List<Map<String, Object>> sessions = api.getSessionsForClass(jwtStore, state, c.id);

                    var items = sessions.stream().map(s -> {
                        long sid = Long.parseLong(String.valueOf(s.get("id")));

                        // backend might return: date / sessionDate / session_date
                        String date = pick(s, "date", "sessionDate", "session_date");

                        // backend might return: code / qrCode / qr_token
                        String code = pick(s, "code", "qrCode", "qr_token", "qrToken");

                        String label = (date.isBlank() ? "Session " + sid : date) +
                                (code.isBlank() ? "" : " (code: " + code + ")");
                        return new SessionItem(sid, label);
                    }).toList();

                    Platform.runLater(() -> sessionBox.getItems().setAll(items));

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            new Alert(Alert.AlertType.ERROR, "Failed to load sessions: " + ex.getMessage(), ButtonType.OK).showAndWait()
                    );
                }
            }).start();
        });

        // When session changes -> clear old report
        sessionBox.setOnAction(e -> resetReportUI.run());

        // Load report
        load.setOnAction(e -> {
            SessionItem sItem = sessionBox.getValue();
            if (sItem == null) {
                new Alert(Alert.AlertType.WARNING, "Select a session first.", ButtonType.OK).showAndWait();
                return;
            }

            load.setDisable(true);
            resetReportUI.run();

            new Thread(() -> {
                try {
                    Map<String, Object> res = api.getSessionReport(jwtStore, state, sItem.id);

                    // supports either:
                    // 1) { report: { stats: {...}, rows: [...] } }
                    // 2) { stats: {...}, rows: [...] }
                    Map<String, Object> report = res.containsKey("report")
                            ? asMap(res.get("report"))
                            : res;

                    Map<String, Object> st = asMap(report.get("stats"));
                    List<Map<String, Object>> rows = asList(report.get("rows"));

                    int p = asInt(st.get("present"));
                    int a = asInt(st.get("absent"));
                    int ex = asInt(st.get("excused"));
                    double r = asDouble(st.get("rate"));

                    Platform.runLater(() -> {
                        present.setText("Present: " + p);
                        absent.setText("Absent: " + a);
                        excused.setText("Excused: " + ex);
                        rate.setText(String.format("Rate: %.1f%%", r));

                        for (var row : rows) {
                            String fn = pick(row, "firstName", "first_name");
                            String ln = pick(row, "lastName", "last_name");
                            String email = pick(row, "email");
                            String status = pick(row, "status");
                            String name = (fn + " " + ln).trim();
                            tableRows.add(new ReportRow(name.isBlank() ? "—" : name, email, status));
                        }

                        load.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        load.setDisable(false);
                        new Alert(Alert.AlertType.ERROR, "Report failed: " + ex.getMessage(), ButtonType.OK).showAndWait();
                    });
                }
            }).start();
        });

        exportPdf.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) return;
            FileChooser fc = new FileChooser();
            fc.setTitle("Save PDF Report");
            fc.setInitialFileName("teacher-report.pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;
            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "pdf", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "PDF saved to:\n" + dest.getAbsolutePath(), ButtonType.OK).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Export failed: " + ex2.getMessage(), ButtonType.OK).showAndWait());
                }
            }).start();
        });

        exportCsv.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) return;
            FileChooser fc = new FileChooser();
            fc.setTitle("Save CSV Report");
            fc.setInitialFileName("teacher-report.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;
            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "csv", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "CSV saved to:\n" + dest.getAbsolutePath(), ButtonType.OK).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Export failed: " + ex2.getMessage(), ButtonType.OK).showAndWait());
                }
            }).start();
        });

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Teacher Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
                page,
                "third",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("teacher-take"); }
                    @Override public void goReports() { router.go("teacher-reports"); }
                    @Override public void goEmail() { router.go("teacher-email"); }
                    @Override public void logout() { jwtStore.clear(); router.go("login"); }
                }
        );
    }
}