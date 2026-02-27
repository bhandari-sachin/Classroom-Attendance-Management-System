package frontend;

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
import config.UserSQL;

import java.util.List;

public class StudentEmailPage {

    // show teacher contacts from backend
    private final ObservableList<TeacherRow> rows = FXCollections.observableArrayList();
    private final UserService userService = new UserService(new UserSQL());

    public Parent build(Scene scene, String studentName, Long studentId) {

        // Load teachers
        rows.clear();
        List<User> users = userService.getAllUsers();
        for (User u : users) {
            if (u.getRole() == UserRole.TEACHER) {
                rows.add(new TeacherRow(u.getName(), u.getEmail()));
            }
        }

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Email");
        title.getStyleClass().add("title");

        Label info = new Label("Teacher emails");
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

        return AdminAppLayout.wrapWithSidebar(
                studentName,
                page,
                "email",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() {
                        scene.setRoot(new StudentDashboardApp().build(scene, studentName, studentId));
                    }

                    @Override public void goTakeAttendance() {
                        scene.setRoot(new StudentMarkAttendancePage().build(scene, studentName, studentId));
                    }

                    @Override public void goReports() {
                        scene.setRoot(new StudentAttendancePage().build(scene, studentName, studentId));
                    }

                    @Override public void goEmail() {
                        scene.setRoot(build(scene, studentName, studentId));
                    }

                    @Override public void logout() {
                        System.out.println("TODO: Student Logout");
                    }
                }
        );
    }
}
