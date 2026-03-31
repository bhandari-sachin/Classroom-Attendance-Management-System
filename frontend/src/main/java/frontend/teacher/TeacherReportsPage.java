package frontend.teacher;

import frontend.AppLayout;
import frontend.api.ReportApi;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import util.I18n;
import util.RtlUtil;

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
                ? I18n.t("teacher.fallback.name")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);
        ReportApi reportApi = new ReportApi(backendUrl);

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(I18n.t("teacher.reports.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(I18n.t("teacher.reports.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        HBox selects = new HBox(10);
        selects.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(selects);

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(I18n.t("teacher.reports.select.class"));
        classBox.setMaxWidth(340);
        RtlUtil.apply(classBox);

        ComboBox<SessionItem> sessionBox = new ComboBox<>();
        sessionBox.setPromptText(I18n.t("teacher.reports.select.session"));
        sessionBox.setMaxWidth(340);
        RtlUtil.apply(sessionBox);

        Button load = new Button(I18n.t("teacher.reports.load"));
        load.getStyleClass().addAll("pill", "pill-green");

        MenuButton exportBtn = new MenuButton(I18n.t("common.export"));
        exportBtn.getStyleClass().addAll("pill", "pill-blue");
        exportBtn.setDisable(true);

        MenuItem exportPdf = new MenuItem(I18n.t("common.export.pdf"));
        MenuItem exportCsv = new MenuItem(I18n.t("common.export.csv"));
        exportBtn.getItems().addAll(exportPdf, exportCsv);

        selects.getChildren().addAll(classBox, sessionBox, load, exportBtn);

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(stats);

        Label present = new Label(I18n.t("teacher.reports.stats.present").replace("{count}", "—"));
        Label absent = new Label(I18n.t("teacher.reports.stats.absent").replace("{count}", "—"));
        Label excused = new Label(I18n.t("teacher.reports.stats.excused").replace("{count}", "—"));
        Label rate = new Label(I18n.t("teacher.reports.stats.rate").replace("{rate}", "—"));

        present.getStyleClass().add("subtitle");
        absent.getStyleClass().add("subtitle");
        excused.getStyleClass().add("subtitle");
        rate.getStyleClass().add("subtitle");

        stats.getChildren().addAll(present, absent, excused, rate);

        TableView<ReportRow> table = new TableView<>(tableRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);
        RtlUtil.apply(table);

        TableColumn<ReportRow, String> colName = new TableColumn<>(I18n.t("teacher.reports.table.student"));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<ReportRow, String> colEmail = new TableColumn<>(I18n.t("teacher.reports.table.email"));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<ReportRow, String> colStatus = new TableColumn<>(I18n.t("teacher.reports.table.status"));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        table.getColumns().setAll(colName, colEmail, colStatus);

        page.getChildren().addAll(title, subtitle, selects, stats, table);

        Runnable resetReportUI = () -> {
            present.setText(I18n.t("teacher.reports.stats.present").replace("{count}", "—"));
            absent.setText(I18n.t("teacher.reports.stats.absent").replace("{count}", "—"));
            excused.setText(I18n.t("teacher.reports.stats.excused").replace("{count}", "—"));
            rate.setText(I18n.t("teacher.reports.stats.rate").replace("{rate}", "—"));
            tableRows.clear();
        };

        new Thread(() -> {
            try {
                List<Map<String, Object>> list = api.getMyClasses(jwtStore, state);

                var items = list.stream().map(m -> {
                    long id = Long.parseLong(String.valueOf(m.get("id")));
                    String classCode = String.valueOf(m.getOrDefault("classCode", "—"));
                    String name2 = String.valueOf(m.getOrDefault("name", I18n.t("teacher.dashboard.classes.unnamed")));
                    return new ClassItem(id, classCode + " — " + name2);
                }).toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(
                                Alert.AlertType.ERROR,
                                I18n.t("teacher.reports.error.loadClasses").replace("{error}", ex.getMessage()),
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

                        String sessionLabel = date.isBlank()
                                ? I18n.t("teacher.reports.session.default").replace("{id}", String.valueOf(sid))
                                : date;

                        String label = sessionLabel + (code.isBlank()
                                ? ""
                                : " " + I18n.t("teacher.reports.session.code").replace("{code}", code));

                        return new SessionItem(sid, label);
                    }).toList();

                    Platform.runLater(() -> sessionBox.getItems().setAll(items));

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            new Alert(
                                    Alert.AlertType.ERROR,
                                    I18n.t("teacher.reports.error.loadSessions").replace("{error}", ex.getMessage()),
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
                new Alert(
                        Alert.AlertType.WARNING,
                        I18n.t("teacher.reports.alert.selectSession"),
                        ButtonType.OK
                ).showAndWait();
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
                        present.setText(I18n.t("teacher.reports.stats.present").replace("{count}", String.valueOf(p)));
                        absent.setText(I18n.t("teacher.reports.stats.absent").replace("{count}", String.valueOf(a)));
                        excused.setText(I18n.t("teacher.reports.stats.excused").replace("{count}", String.valueOf(ex)));
                        rate.setText(I18n.t("teacher.reports.stats.rate").replace("{rate}", String.format("%.1f", r)));

                        for (var row : rows) {
                            String fn = pick(row, "firstName", "first_name");
                            String ln = pick(row, "lastName", "last_name");
                            String email = pick(row, "email");
                            String rawStatus = pick(row, "status");
                            String name = (fn + " " + ln).trim();

                            tableRows.add(new ReportRow(
                                    name.isBlank() ? "—" : name,
                                    email,
                                    localizeStatus(rawStatus)
                            ));
                        }

                        load.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        load.setDisable(false);
                        new Alert(
                                Alert.AlertType.ERROR,
                                I18n.t("teacher.reports.error.loadReport"),
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
            fc.setTitle(I18n.t("teacher.reports.filechooser.pdf.title"));
            fc.setInitialFileName(I18n.t("teacher.reports.filechooser.pdf.name"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;

            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "pdf", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.INFORMATION,
                            I18n.t("teacher.reports.export.success.pdf") + "\n" + dest.getAbsolutePath(),
                            ButtonType.OK
                    ).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.ERROR,
                            I18n.t("teacher.reports.error.export"),
                            ButtonType.OK
                    ).showAndWait());
                }
            }).start();
        });

        exportCsv.setOnAction(e -> {
            ClassItem c = classBox.getValue();
            if (c == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle(I18n.t("teacher.reports.filechooser.csv.title"));
            fc.setInitialFileName(I18n.t("teacher.reports.filechooser.csv.name"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;

            new Thread(() -> {
                try {
                    reportApi.exportTeacherReport(jwtStore, state, c.id, "csv", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.INFORMATION,
                            I18n.t("teacher.reports.export.success.csv") + "\n" + dest.getAbsolutePath(),
                            ButtonType.OK
                    ).showAndWait());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.ERROR,
                            I18n.t("teacher.reports.error.export"),
                            ButtonType.OK
                    ).showAndWait());
                }
            }).start();
        });

        return AppLayout.wrapWithSidebar(
                teacherName,
                I18n.t("teacher.sidebar.title"),
                I18n.t("teacher.sidebar.menu.dashboard"),
                I18n.t("teacher.sidebar.menu.take_attendance"),
                I18n.t("teacher.sidebar.menu.reports"),
                I18n.t("teacher.sidebar.menu.email"),
                I18n.t("teacher.sidebar.logout"),
                page,
                "third",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("teacher-take"); }
                    @Override public void goReports() { router.go("teacher-reports"); }
                    @Override public void goEmail() { router.go("teacher-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                },
                router,
                I18n.isRtl()
        );
    }

    private static String localizeStatus(String rawStatus) {
        if (rawStatus == null) return "—";

        return switch (rawStatus.toUpperCase()) {
            case "PRESENT" -> I18n.t("common.attendance.present");
            case "ABSENT" -> I18n.t("common.attendance.absent");
            case "EXCUSED" -> I18n.t("common.attendance.excused");
            default -> rawStatus;
        };
    }
}