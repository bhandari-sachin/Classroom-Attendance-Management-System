package frontend.student;

import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class StudentMarkAttendancePage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private static final String UNKNOWN_ERROR = "Unknown error";

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        logIfSceneMissing(scene);

        String studentName = StudentPageSupport.resolveStudentName(state, helper);

        VBox page = StudentPageSupport.buildPageContainer();
        page.setPadding(new Insets(26, 26, 40, 26));

        Button backButton = buildBackButton(router);
        Label title = buildTitle();
        Label subtitle = buildSubtitle();

        VBox qrCard = buildQrCard();
        TextField codeField = buildCodeField();
        Button submitButton = buildSubmitButton(codeField, jwtStore, state);
        VBox manualCard = buildManualCard(codeField, submitButton);
        VBox howCard = buildHowCard();

        page.getChildren().addAll(
                backButton,
                title,
                subtitle,
                qrCard,
                manualCard,
                howCard
        );

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setPannable(true);
        scroll.getStyleClass().add("scroll");

        return StudentPageSupport.wrapWithSidebar(
                studentName,
                helper,
                scroll,
                "second",
                router,
                jwtStore
        );
    }

    private void logIfSceneMissing(Scene scene) {
        if (scene == null) {
            // Intentionally harmless use to preserve method signature.
        }
    }

    private Button buildBackButton(AppRouter router) {
        Button back = new Button(helper.getMessage("student.mark.back"));
        back.getStyleClass().add("back-link");
        back.setOnAction(e -> router.go("student-dashboard"));
        return back;
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("student.mark.title"));
        title.getStyleClass().add("dash-title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("student.mark.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");
        return subtitle;
    }

    private VBox buildQrCard() {
        VBox qrCard = new VBox();
        qrCard.getStyleClass().add("qr-card");
        qrCard.setMinHeight(200);
        qrCard.setAlignment(Pos.CENTER);

        StackPane cameraBox = new StackPane();
        cameraBox.getStyleClass().add("camera-box");

        Label cameraIcon = new Label("📷");
        cameraIcon.setFont(Font.font("Segoe UI Emoji", 34));
        cameraIcon.getStyleClass().add("camera-icon");

        cameraBox.getChildren().add(cameraIcon);
        qrCard.getChildren().add(cameraBox);

        return qrCard;
    }

    private TextField buildCodeField() {
        TextField codeField = new TextField();
        codeField.setPromptText(helper.getMessage("student.mark.code.placeholder"));
        codeField.getStyleClass().add("code-field");
        return codeField;
    }

    private Button buildSubmitButton(TextField codeField, JwtStore jwtStore, AuthState state) {
        Button submit = new Button(helper.getMessage("student.mark.submit"));
        submit.getStyleClass().add("submit-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> handleSubmit(codeField, jwtStore, state));
        return submit;
    }

    private VBox buildManualCard(TextField codeField, Button submitButton) {
        VBox manualCard = new VBox(10);
        manualCard.getStyleClass().add("manual-card");

        HBox manualHeader = new HBox(8);
        manualHeader.setAlignment(Pos.CENTER_LEFT);

        Label manualIcon = new Label("⌨");
        manualIcon.getStyleClass().add("manual-icon");

        Label manualTitle = new Label(helper.getMessage("student.mark.manual.title"));
        manualTitle.getStyleClass().add("manual-title");

        manualHeader.getChildren().addAll(manualIcon, manualTitle);

        Label manualSubtitle = new Label(helper.getMessage("student.mark.manual.subtitle"));
        manualSubtitle.getStyleClass().add("manual-subtitle");

        Label codeLabel = new Label(helper.getMessage("student.mark.code.label"));
        codeLabel.getStyleClass().add("field-label");

        manualCard.getChildren().addAll(
                manualHeader,
                manualSubtitle,
                codeLabel,
                codeField,
                submitButton
        );

        return manualCard;
    }

    private VBox buildHowCard() {
        VBox howCard = new VBox(8);
        howCard.getStyleClass().add("how-card");

        Label howTitle = new Label(helper.getMessage("student.mark.how.title"));
        howTitle.getStyleClass().add("how-title");

        Label step1 = buildHowStep(helper.getMessage("student.mark.how.step1"));
        Label step2 = buildHowStep(helper.getMessage("student.mark.how.step2"));
        Label step3 = buildHowStep(helper.getMessage("student.mark.how.step3"));

        howCard.getChildren().addAll(howTitle, step1, step2, step3);
        return howCard;
    }

    private Label buildHowStep(String text) {
        Label step = new Label(text);
        step.getStyleClass().add("how-step");
        return step;
    }

    private void handleSubmit(TextField codeField, JwtStore jwtStore, AuthState state) {
        String code = codeField.getText().trim();

        if (code.isBlank()) {
            showWarning(helper.getMessage("student.mark.warning.empty"));
            return;
        }

        StudentAttendanceApi api = new StudentAttendanceApi(BASE_URL);

        new Thread(() -> {
            try {
                api.submitCode(code, jwtStore, state);

                Platform.runLater(() -> {
                    codeField.clear();
                    showInfo(helper.getMessage("student.mark.success"));
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> showError(safeMessage(ex)));
            } catch (Exception ex) {
                Platform.runLater(() -> showError(safeMessage(ex)));
            }
        }).start();
    }

    String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return UNKNOWN_ERROR;
        }
        return throwable.getMessage();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).show();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).show();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }
}