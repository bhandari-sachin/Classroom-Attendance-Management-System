package frontend.student;

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentEmailPage.java
import config.UserSQL;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
=======
import frontend.AppLayout;
import frontend.TeacherRow;
import frontend.api.StudentTeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentEmailPage.java
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
import model.User;
import model.UserRole;
import service.UserService;

import java.util.List;

import java.util.List;
import java.util.Map;

public class StudentEmailPage {

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentEmailPage.java
    // show teacher contacts from backend
=======
    private static final String BASE_URL = "http://localhost:8081";

    // ✅ now real data, starts empty
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentEmailPage.java
    private final ObservableList<TeacherRow> rows = FXCollections.observableArrayList();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        UserSQL userSQL = new UserSQL();
        UserService userService = new UserService(userSQL);
        String studentName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        rows.clear();
        List<User> users = userService.getAllUsers();
        for (User u : users) {
            if (u.getUserType() == UserRole.TEACHER) {
                rows.add(new TeacherRow(u.getName(), u.getEmail()));
            }
        }

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Email");
        title.getStyleClass().add("title");

        Label info = new Label("Your teacher emails.");
        info.getStyleClass().add("subtitle");

        Label status = new Label("Loading…");
        status.getStyleClass().add("subtitle");

        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<TeacherRow, String> colName = new TableColumn<>("Teacher");
        colName.setCellValueFactory(d -> d.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, status, table);

        // ✅ fetch from backend
        loadTeachers(jwtStore, state, status);

        return AppLayout.wrapWithSidebar(
                studentName,
                "Student Panel",
                "Dashboard",
                "Mark Attendance",
                "My Attendance",
                "Email",
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
                        "Failed to load teachers: " + (e == null ? "" : e.getMessage())
                ));
                if (e != null) e.printStackTrace();
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}