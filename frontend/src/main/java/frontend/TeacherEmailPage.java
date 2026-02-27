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

public class TeacherEmailPage {

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();
    private final UserService userService = new UserService(new UserSQL());

    public Parent build(Scene scene, String teacherName, Long teacherId) {

        // Load students from backend
        rows.clear();
        List<User> users = userService.getAllUsers();
        for (User u : users) {
            if (u.getRole() == UserRole.STUDENT) {
                rows.add(new StudentRow(u.getName(), u.getEmail(), ""));
            }
        }

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Email");
        title.getStyleClass().add("title");

        Label info = new Label("Student emails");
        info.getStyleClass().add("subtitle");

        TableView<StudentRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<StudentRow, String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(d -> d.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, table);

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Student Panel", "Dashboard", "Mark Attendance", "My Attendance", "Contact", page,
                "email",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new TeacherDashboardApp().build(scene, teacherName, teacherId)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new TeacherTakeAttendancePage().build(scene, teacherName, teacherId)); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, teacherName, teacherId)); }
                    @Override public void goEmail() { scene.setRoot(build(scene, teacherName, teacherId)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }
}
