package frontend;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TeacherEmailPage {

    private final ObservableList<StudentRow> rows = DataStore.getStudents();

    public Parent build(Scene scene, String teacherName) {

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Email");
        title.getStyleClass().add("title");

        Label info = new Label("Student emails (connect real backend later).");
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
                    @Override public void goDashboard() { scene.setRoot(build(scene, teacherName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new TeacherTakeAttendancePage().build(scene, teacherName)); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, teacherName)); }
                    @Override public void goEmail() { scene.setRoot(new TeacherEmailPage().build(scene, teacherName)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }
}
