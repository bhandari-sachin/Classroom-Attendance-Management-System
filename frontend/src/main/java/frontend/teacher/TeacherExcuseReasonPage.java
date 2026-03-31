package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;

public class TeacherExcuseReasonPage {

    private final HelperClass helper = new HelperClass();

    public Parent build(
            Scene scene,
            AppRouter router,
            JwtStore jwtStore,
            AuthState state,
            StudentRow student,
            Runnable onDoneBack
    ) {

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();

        VBox page = new VBox(12);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label(helper.getMessage("teacher.excuse.title"));
        title.getStyleClass().add("title");

        Label who = new Label(
                helper.getMessage("teacher.excuse.student")
                        .replace("{name}", student.getStudentName())
                        .replace("{email}", student.getEmail())
        );
        who.getStyleClass().add("subtitle");

        Label hint = new Label(helper.getMessage("teacher.excuse.hint"));
        hint.getStyleClass().add("section-title");

        TextArea reason = new TextArea();
        reason.setWrapText(true);
        reason.setPrefRowCount(6);
        reason.setText(student.getExcuseReason());

        Button save = new Button(helper.getMessage("teacher.excuse.save"));
        Button cancel = new Button(helper.getMessage("teacher.excuse.cancel"));

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
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email"),
                page,
                "second",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { onDoneBack.run(); }
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