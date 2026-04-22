package frontend.teacher;

import frontend.ui.StudentRow;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
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

    private final HelperClass helper = new HelperClass();

    public Parent build(
            Scene scene,
            AppRouter router,
            JwtStore jwtStore,
            AuthState state,
            StudentRow student,
            Runnable onDoneBack
    ) {
        String teacherName = TeacherPageSupport.resolveTeacherName(state, helper);

        VBox page = TeacherPageSupport.buildExcusePageContainer();

        Label title = buildTitle();
        Label studentInfo = buildStudentInfo(student);
        Label hint = buildHint();
        TextArea reasonArea = buildReasonArea(student);

        Button saveButton = buildSaveButton(student, reasonArea, onDoneBack);
        Button cancelButton = buildCancelButton(onDoneBack);
        HBox actions = buildActionsRow(cancelButton, saveButton);

        page.getChildren().addAll(title, studentInfo, hint, reasonArea, actions);

        return TeacherPageSupport.wrapWithSidebar(
                teacherName,
                helper,
                page,
                "second",
                router,
                jwtStore,
                onDoneBack
        );
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("teacher.excuse.title"));
        title.getStyleClass().add("title");
        return title;
    }

    Label buildStudentInfo(StudentRow student) {
        Label studentInfo = new Label(
                helper.getMessage("teacher.excuse.student")
                        .replace("{name}", student.getStudentName())
                        .replace("{email}", student.getEmail())
        );
        studentInfo.getStyleClass().add("subtitle");
        return studentInfo;
    }

    private Label buildHint() {
        Label hint = new Label(helper.getMessage("teacher.excuse.hint"));
        hint.getStyleClass().add("section-title");
        return hint;
    }

    private TextArea buildReasonArea(StudentRow student) {
        TextArea reasonArea = new TextArea();
        reasonArea.setWrapText(true);
        reasonArea.setPrefRowCount(6);
        reasonArea.setText(student.getExcuseReason());
        return reasonArea;
    }

    private Button buildSaveButton(StudentRow student, TextArea reasonArea, Runnable onDoneBack) {
        Button save = new Button(helper.getMessage("teacher.excuse.save"));
        save.getStyleClass().addAll("pill", "pill-green");

        save.setOnAction(e -> {
            student.setStatus("Excused");
            student.setExcuseReason(reasonArea.getText());
            onDoneBack.run();
        });

        return save;
    }

    private Button buildCancelButton(Runnable onDoneBack) {
        Button cancel = new Button(helper.getMessage("teacher.excuse.cancel"));
        cancel.getStyleClass().add("pill");
        cancel.setOnAction(e -> onDoneBack.run());
        return cancel;
    }

    private HBox buildActionsRow(Button cancelButton, Button saveButton) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, spacer, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        return actions;
    }
}