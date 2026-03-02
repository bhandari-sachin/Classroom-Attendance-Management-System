package frontend;

import config.UserSQL;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class StudentEmailPage {

    // show teacher contacts from backend
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

        Label info = new Label("Teacher emails (connect real backend later).");
        info.getStyleClass().add("subtitle");

        TableView<TeacherRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<TeacherRow, String> colName = new TableColumn<>("Teacher");
        colName.setCellValueFactory(d -> d.getValue().teacherNameProperty());

        TableColumn<TeacherRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, table);

        return AppLayout.wrapWithSidebar(
                studentName,
                "Student Panel",
                "Dashboard",
                "Mark Attendance",
                "My Attendance",
                "Email",
                page,
                "fourth", // ✅ active = Email
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
}