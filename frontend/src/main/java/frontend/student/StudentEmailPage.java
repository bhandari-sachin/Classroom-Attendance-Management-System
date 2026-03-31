package frontend.student;

import frontend.AppLayout;
import frontend.TeacherRow;
import frontend.api.StudentTeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
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
    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("student.name.placeholder")
                : state.getName();

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label(helper.getMessage("student.email.title"));
        title.getStyleClass().add("title");

        Label info = new Label(helper.getMessage("student.email.subtitle"));
        info.getStyleClass().add("subtitle");

        Label status = new Label(helper.getMessage("student.email.status.loading"));
        status.getStyleClass().add("subtitle");

        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<TeacherRow, String> colName = new TableColumn<>(helper.getMessage("student.email.table.teacher"));
        colName.setCellValueFactory(d -> d.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> colEmail = new TableColumn<>(helper.getMessage("student.email.table.email"));
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, status, table);

        loadTeachers(jwtStore, state, status);

        return AppLayout.wrapWithSidebar(
                studentName,
                helper.getMessage("student.panel.title"),
                helper.getMessage("student.nav.dashboard"),
                helper.getMessage("student.nav.markAttendance"),
                helper.getMessage("student.nav.myAttendance"),
                helper.getMessage("student.nav.email"),
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
                        helper.getMessage("student.email.status.error") + " " + (e == null ? "" : e.getMessage())
                ));
                if (e != null) e.printStackTrace();
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}