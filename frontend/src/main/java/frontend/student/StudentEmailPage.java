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
        String studentName = resolveStudentName(state);

        VBox page = buildPageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();
        Label statusLabel = buildStatusLabel();
        TableView<TeacherRow> table = buildTeacherTable();

        page.getChildren().addAll(title, subtitle, statusLabel, table);

        loadTeachers(jwtStore, state, statusLabel);

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
                    @Override
                    public void goDashboard() {
                        router.go("student-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("student-mark");
                    }

                    @Override
                    public void goReports() {
                        router.go("student-attendance");
                    }

                    @Override
                    public void goEmail() {
                        router.go("student-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveStudentName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("student.name.placeholder")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("student.email.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("student.email.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private Label buildStatusLabel() {
        Label status = new Label(helper.getMessage("student.email.status.loading"));
        status.getStyleClass().add("subtitle");
        return status;
    }

    private TableView<TeacherRow> buildTeacherTable() {
        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<TeacherRow, String> nameColumn =
                new TableColumn<>(helper.getMessage("student.email.table.teacher"));
        nameColumn.setCellValueFactory(data -> data.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> emailColumn =
                new TableColumn<>(helper.getMessage("student.email.table.email"));
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        table.getColumns().addAll(nameColumn, emailColumn);
        return table;
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
                List<Map<String, Object>> teachers = getValue();

                Platform.runLater(() -> {
                    rows.clear();

                    for (Map<String, Object> teacher : teachers) {
                        rows.add(mapTeacherRow(teacher));
                    }

                    statusLabel.setText("");
                    statusLabel.setManaged(false);
                    statusLabel.setVisible(false);
                });
            }

            @Override
            protected void failed() {
                Throwable exception = getException();

                Platform.runLater(() -> statusLabel.setText(
                        helper.getMessage("student.email.status.error")
                                + " "
                                + (exception == null ? "" : exception.getMessage())
                ));

                if (exception != null) {
                    exception.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private TeacherRow mapTeacherRow(Map<String, Object> teacherData) {
        String name = String.valueOf(teacherData.getOrDefault("teacherName", ""));
        String email = String.valueOf(teacherData.getOrDefault("email", ""));
        return new TeacherRow(name, email);
    }
}