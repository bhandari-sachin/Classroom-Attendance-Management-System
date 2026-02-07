package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TeacherExcuseReasonPage {

    public Parent build(Scene scene, String teacherName, StudentRow student, Runnable onDoneBack) {

        VBox page = new VBox(12);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Excuse Reason");
        title.getStyleClass().add("title");

        Label who = new Label("Student: " + student.getStudentName() + " (" + student.getEmail() + ")");
        who.getStyleClass().add("subtitle");

        Label hint = new Label("Write the reason why this student is excused:");
        hint.getStyleClass().add("section-title");

        TextArea reason = new TextArea();
        reason.setWrapText(true);
        reason.setPrefRowCount(6);
        reason.setText(student.getExcuseReason());

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        // if you have pill styles, you can keep them. otherwise default buttons are fine.
        save.getStyleClass().addAll("pill", "pill-green");
        cancel.getStyleClass().addAll("pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, spacer, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        cancel.setOnAction(e -> onDoneBack.run());

        save.setOnAction(e -> {
            student.setStatus("Excused");
            student.setExcuseReason(reason.getText());
            onDoneBack.run();
        });

        page.getChildren().addAll(title, who, hint, reason, actions);

        return AppLayout.wrapWithSidebar(
                teacherName,
                page,
                "takeAttendance",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new TeacherDashboardApp().build(scene, teacherName)); }
                    @Override public void goTakeAttendance() { onDoneBack.run(); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, teacherName)); }
                    @Override public void goEmail() { scene.setRoot(new TeacherEmailPage().build(scene, teacherName)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }
}
