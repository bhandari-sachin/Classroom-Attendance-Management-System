package frontend.student;

import frontend.AppLayout;
import frontend.TeacherRow;
import frontend.api.StudentTeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class StudentEmailPage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private final ObservableList<TeacherRow> rows = FXCollections.observableArrayList();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? t("student.name.placeholder", "Name")
                : state.getName();

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label(t("student.email.title", "Teacher Emails"));
        title.getStyleClass().add("title");

        Label info = new Label(t("student.email.subtitle", "View your teachers and their email addresses"));
        info.getStyleClass().add("subtitle");

        Label status = new Label(t("student.email.status.loading", "Loading teachers..."));
        status.getStyleClass().add("subtitle");

        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<TeacherRow, String> colName = new TableColumn<>(t("student.email.table.teacher", "Teacher"));
        colName.setCellValueFactory(d -> d.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> colEmail = new TableColumn<>(t("student.email.table.email", "Email"));
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, status, table);

        loadTeachers(jwtStore, state, status);

        return AppLayout.wrapWithSidebar(
                studentName,
                t("student.panel.title", "Student Panel"),
                t("student.nav.dashboard", "Dashboard"),
                t("student.nav.markAttendance", "Mark Attendance"),
                t("student.nav.myAttendance", "My Attendance"),
                t("student.nav.email", "Email"),
                page,
                "fourth",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("student-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("student-mark"); }
                    @Override public void goReports() { router.go("student-attendance"); }
                    @Override public void goEmail() { router.go("student-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private void loadTeachers(JwtStore jwtStore, AuthState state, Label statusLabel) {
        StudentTeacherApi api = new StudentTeacherApi(BASE_URL);

        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return api.getTeachers(jwtStore, state);
            }

            @Override
            protected void succeeded() {
                List<Map<String, Object>> list = getValue();

                Platform.runLater(() -> {
                    rows.clear();

                    if (list == null || list.isEmpty()) {
                        statusLabel.setText(t("student.email.status.empty", "No teachers found."));
                        return;
                    }

                    statusLabel.setText("");

                    for (Map<String, Object> t : list) {
                        String name = String.valueOf(t.getOrDefault("teacherName", ""));
                        String email = String.valueOf(t.getOrDefault("email", ""));
                        rows.add(new TeacherRow(name, email));
                    }
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                Platform.runLater(() -> statusLabel.setText(
                        t("student.email.status.error", "Failed to load teachers:") + " " + (e == null ? "" : e.getMessage())
                ));
                if (e != null) e.printStackTrace();
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}