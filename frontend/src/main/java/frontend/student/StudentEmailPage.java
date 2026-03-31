package frontend.student;

import frontend.AppLayout;
import frontend.TeacherRow;
import frontend.api.StudentTeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import util.I18n;
import util.RtlUtil;

import java.util.List;
import java.util.Map;

public class StudentEmailPage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private final ObservableList<TeacherRow> rows = FXCollections.observableArrayList();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(I18n.t("student.email.title"));
        title.getStyleClass().add("title");

        Label info = new Label(I18n.t("student.email.subtitle"));
        info.getStyleClass().add("subtitle");

        Label status = new Label(I18n.t("student.email.status.loading"));
        status.getStyleClass().add("subtitle");

        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);
        RtlUtil.apply(table);

        TableColumn<TeacherRow, String> colName = new TableColumn<>(I18n.t("student.email.table.teacher"));
        colName.setCellValueFactory(d -> d.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> colEmail = new TableColumn<>(I18n.t("student.email.table.email"));
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, status, table);

        loadTeachers(jwtStore, state, status);

        return AppLayout.wrapWithSidebar(
                studentName,
                I18n.t("student.panel.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("student.nav.markAttendance"),
                I18n.t("student.nav.myAttendance"),
                I18n.t("student.nav.email"),
                I18n.t("student.nav.logout"),
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
                },
                router,
                I18n.isRtl()
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
                        statusLabel.setText(I18n.t("common.status.noData"));
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
                        I18n.t("student.email.status.error")
                ));
                if (e != null) e.printStackTrace();
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}