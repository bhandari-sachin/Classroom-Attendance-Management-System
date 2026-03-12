package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;

public class TeacherExcuseReasonPage {

    public Parent build(
            Scene scene,
            AppRouter router,
            JwtStore jwtStore,
            AuthState state,
            StudentRow student,
            Runnable onDoneBack
    ) {

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

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

        // Keep your pill styles if you have them
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
                "Teacher Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
                page,
                "second", // ✅ Take Attendance section
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { onDoneBack.run(); } // stay consistent with flow
                    @Override public void goReports() { router.go("teacher-reports"); }
                    @Override public void goEmail() { router.go("teacher-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}