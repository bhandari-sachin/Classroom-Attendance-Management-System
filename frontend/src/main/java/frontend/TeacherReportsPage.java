package frontend;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TeacherReportsPage {

    public Parent build(Scene scene, String teacherName) {

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Reports");
        title.getStyleClass().add("title");

        Label info = new Label("Reports page (connect backend later).");
        info.getStyleClass().add("subtitle");

        page.getChildren().addAll(title, info);

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Student Panel", "Dashboard", "Mark Attendance", "My Attendance", "Contact", page,
                "reports",
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        scene.setRoot(new TeacherDashboardApp().build(scene, teacherName));
                    }

                    @Override
                    public void goTakeAttendance() {
                        scene.setRoot(new TeacherTakeAttendancePage().build(scene, teacherName));
                    }

                    @Override
                    public void goReports() {
                        scene.setRoot(build(scene, teacherName));
                    }

                    @Override
                    public void goEmail() {
                        scene.setRoot(new TeacherEmailPage().build(scene, teacherName));
                    }

                    @Override
                    public void logout() {
                        System.out.println("TODO: Logout");
                    }
                }
        );
    }
}
