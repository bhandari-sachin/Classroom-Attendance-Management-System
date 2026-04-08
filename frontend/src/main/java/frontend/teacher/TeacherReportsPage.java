package frontend.teacher;

import frontend.AppLayout;
import frontend.api.ReportApi;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
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
            this.name = name;
            this.email = email;
            this.status = status;
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

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? t("teacher.fallback.name", "Teacher")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);
        ReportApi reportApi = new ReportApi(backendUrl);

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label(t("teacher.reports.title", "Reports"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(t("teacher.reports.subtitle", "View attendance reports for your classes"));
        subtitle.getStyleClass().add("subtitle");

        HBox selects = new HBox(10);
        selects.setAlignment(Pos.CENTER_LEFT);

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(t("teacher.reports.select.class", "Select class"));
        classBox.setMaxWidth(340);

        ComboBox<SessionItem> sessionBox = new ComboBox<>();
        sessionBox.setPromptText(t("teacher.reports.select.session", "Select session"));
        sessionBox.setMaxWidth(340);

        Button load = new Button(t("teacher.reports.load", "Load"));
        load.getStyleClass().addAll("pill", "pill-green");

        MenuButton exportBtn = new MenuButton(t("common.export", "Export"));
        exportBtn.getStyleClass().addAll("pill", "pill-blue");
        exportBtn.setDisable(true);

        MenuItem exportPdf = new MenuItem(t("common.export.pdf", "Export PDF"));
        MenuItem exportCsv = new MenuItem(t("common.export.csv", "Export CSV"));
        exportBtn.getItems().addAll(exportPdf, exportCsv);

        selects.getChildren().addAll(classBox, sessionBox, load, exportBtn);

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label present = new Label(t("teacher.reports.stats.present", "Present: {count}").replace("{count}", "—"));
        Label absent  = new Label(t("teacher.reports.stats.absent", "Absent: {count}").replace("{count}", "—"));
        Label excused = new Label(t("teacher.reports.stats.excused", "Excused: {count}").replace("{count}", "—"));
        Label rate    = new Label(t("teacher.reports.stats.rate", "Rate: {rate}%").replace("{rate}", "—"));

        present.getStyleClass().add("subtitle");
        absent.getStyleClass().add("subtitle");
        excused.getStyleClass().add("subtitle");
        rate.getStyleClass().add("subtitle");

        stats.getChildren().addAll(present, absent, excused, rate);

        TableView<ReportRow> table = new TableView<>(tableRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);

        TableColumn<ReportRow, String> colName = new TableColumn<>(t("teacher.reports.table.student", "Student"));
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<ReportRow, String> colEmail = new TableColumn<>(t("teacher.reports.table.email", "Email"));
        colEmail.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<ReportRow, String> colStatus = new TableColumn<>(t("teacher.reports.table.status", "Status"));
        colStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));
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

        page.getChildren().addAll(title, subtitle, selects, stats, table);

        Runnable resetReportUI = () -> {
            present.setText(t("teacher.reports.stats.present", "Present: {count}").replace("{count}", "—"));
            absent.setText(t("teacher.reports.stats.absent", "Absent: {count}").replace("{count}", "—"));
            excused.setText(t("teacher.reports.stats.excused", "Excused: {count}").replace("{count}", "—"));
            rate.setText(t("teacher.reports.stats.rate", "Rate: {rate}%").replace("{rate}", "—"));
            tableRows.clear();
        };

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
                        new Alert(
                                Alert.AlertType.ERROR,
                                t("teacher.reports.error.loadClasses", "Failed to load classes: {error}")
                                        .replace("{error}", ex.getMessage()),
                                ButtonType.OK
                        ).showAndWait()
                );
            }
        }).start();

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
                        String date = pick(s, "date", "sessionDate", "session_date");
                        String code = pick(s, "code", "qrCode", "qr_token", "qrToken");

                        String label = (date.isBlank()
                                ? t("teacher.reports.session.default", "Session {id}").replace("{id}", String.valueOf(sid))
                                : date)
                                + (code.isBlank()
                                ? ""
                                : " " + t("teacher.reports.session.code", "(Code: {code})").replace("{code}", code));

                        return new SessionItem(sid, label);
                    }).toList();

                    Platform.runLater(() -> sessionBox.getItems().setAll(items));

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            new Alert(
                                    Alert.AlertType.ERROR,
                                    t("teacher.reports.error.loadSessions", "Failed to load sessions: {error}")
                                            .replace("{error}", ex.getMessage()),
                                    ButtonType.OK
                            ).showAndWait()
                    );
                }
            }).start();
        });

        sessionBox.setOnAction(e -> resetReportUI.run());

        load.setOnAction(e -> {
            SessionItem sItem = sessionBox.getValue();
            if (sItem == null) {
                new Alert(Alert.AlertType.WARNING, t("teacher.reports.alert.selectSession", "Please select a session first."), ButtonType.OK).showAndWait();
                return;
            }

            load.setDisable(true);
            resetReportUI.run();

            new Thread(() -> {
                try {
                    Map<String, Object> res = api.getSessionReport(jwtStore, state, sItem.id);

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
                        present.setText(t("teacher.reports.stats.present", "Present: {count}").replace("{count}", String.valueOf(p)));
                        absent.setText(t("teacher.reports.stats.absent", "Absent: {count}").replace("{count}", String.valueOf(a)));
                        excused.setText(t("teacher.reports.stats.excused", "Excused: {count}").replace("{count}", String.valueOf(ex)));
                        rate.setText(t("teacher.reports.stats.rate", "Rate: {rate}%").replace("{rate}", String.format("%.1f", r)));

                        for (var row : rows) {
                            String fn = pick(row, "firstName", "first_name");
                            String ln = pick(row, "lastName", "last_name");
                            String email = pick(row, "email");
                            String rawStatus = pick(row, "status");
                            String name = (fn + " " + ln).trim();
                            tableRows.add(new ReportRow(name.isBlank() ? "—" : name, email, rawStatus));
                        }

                        load.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        load.setDisable(false);
                        new Alert(
                                Alert.AlertType.ERROR,
                                t("teacher.reports.error.loadReport", "Failed to load report:") + " " + ex.getMessage(),
                                ButtonType.OK
                        ).showAndWait();
                    });
                }
            }).start();
        });

        exportPdf.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle(t("teacher.reports.filechooser.pdf.title", "Save PDF Report"));
            fc.setInitialFileName(t("teacher.reports.filechooser.pdf.name", "teacher-report.pdf"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;

            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "pdf", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.INFORMATION,
                            t("teacher.reports.export.success.pdf", "PDF report exported successfully:") + "\n" + dest.getAbsolutePath(),
                            ButtonType.OK
                    ).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.ERROR,
                            t("teacher.reports.error.export", "Export failed:") + " " + ex2.getMessage(),
                            ButtonType.OK
                    ).showAndWait());
                }
            }).start();
        });

        exportCsv.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle(t("teacher.reports.filechooser.csv.title", "Save CSV Report"));
            fc.setInitialFileName(t("teacher.reports.filechooser.csv.name", "teacher-report.csv"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;

            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "csv", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.INFORMATION,
                            t("teacher.reports.export.success.csv", "CSV report exported successfully:") + "\n" + dest.getAbsolutePath(),
                            ButtonType.OK
                    ).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.ERROR,
                            t("teacher.reports.error.export", "Export failed:") + " " + ex2.getMessage(),
                            ButtonType.OK
                    ).showAndWait());
                }
            }).start();
        });

        return AppLayout.wrapWithSidebar(
                teacherName,
                t("teacher.sidebar.title", "Teacher Panel"),
                t("teacher.sidebar.menu.dashboard", "Dashboard"),
                t("teacher.sidebar.menu.take_attendance", "Take Attendance"),
                t("teacher.sidebar.menu.reports", "Reports"),
                t("teacher.sidebar.menu.email", "Email"),
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

    private String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank() || "—".equals(status)) {
            return "—";
        }

        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> t("student.attendance.stats.present", "Present");
            case "ABSENT" -> t("student.attendance.stats.absent", "Absent");
            case "EXCUSED" -> t("student.attendance.stats.excused", "Excused");
            default -> status;
        };
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}