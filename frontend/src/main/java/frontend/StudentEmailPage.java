package frontend;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class StudentEmailPage {

    // For now: show teacher contacts (replace later from backend)
    private final ObservableList<TeacherRow> rows = DataStore.getTeachers();

    public Parent build(Scene scene, String studentName) {

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
                page,
                "email",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() {
                        scene.setRoot(new StudentDashboardApp().build(scene, studentName));
                    }

                    @Override public void goTakeAttendance() {
                        scene.setRoot(new StudentMarkAttendancePage().build(scene, studentName));
                    }

                    @Override public void goReports() {
                        scene.setRoot(new StudentAttendancePage().build(scene, studentName));
                    }

                    @Override public void goEmail() {
                        scene.setRoot(build(scene, studentName));
                    }

                    @Override public void logout() {
                        System.out.println("TODO: Student Logout");
                    }
                }
        );
    }
}
