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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

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
                ? I18n.t("teacher.fallback.name")
                : state.getName();

        VBox page = new VBox(12);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(I18n.t("teacher.excuse.title"));
        title.getStyleClass().add("title");

        Label who = new Label(
                I18n.t("teacher.excuse.student")
                        .replace("{name}", student.getStudentName() == null ? "" : student.getStudentName())
                        .replace("{email}", student.getEmail() == null ? "" : student.getEmail())
        );
        who.getStyleClass().add("subtitle");

        Label hint = new Label(I18n.t("teacher.excuse.hint"));
        hint.getStyleClass().add("section-title");

        TextArea reason = new TextArea();
        reason.setWrapText(true);
        reason.setPrefRowCount(6);
        reason.setText(student.getExcuseReason());
        RtlUtil.apply(reason);

        Button save = new Button(I18n.t("teacher.excuse.save"));
        Button cancel = new Button(I18n.t("teacher.excuse.cancel"));

        save.getStyleClass().addAll("pill", "pill-green");
        cancel.getStyleClass().addAll("pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, spacer, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        RtlUtil.apply(actions);

        cancel.setOnAction(e -> onDoneBack.run());

        save.setOnAction(e -> {
            student.setStatus("Excused");
            student.setExcuseReason(reason.getText());
            onDoneBack.run();
        });

        page.getChildren().addAll(title, who, hint, reason, actions);

        return AppLayout.wrapWithSidebar(
                teacherName,
                I18n.t("teacher.sidebar.title"),
                I18n.t("teacher.sidebar.menu.dashboard"),
                I18n.t("teacher.sidebar.menu.take_attendance"),
                I18n.t("teacher.sidebar.menu.reports"),
                I18n.t("teacher.sidebar.menu.email"),
                I18n.t("teacher.sidebar.logout"),
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
                },
                router,
                I18n.isRtl()
        );
    }
}