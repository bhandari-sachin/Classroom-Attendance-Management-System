package frontend.student;

import frontend.AppLayout;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class StudentMarkAttendancePage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? t("student.name.placeholder", "Name")
                : state.getName();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        Button back = new Button(t("student.mark.back", "Back"));
        back.getStyleClass().add("back-link");
        back.setOnAction(e -> router.go("student-dashboard"));

        Label title = new Label(t("student.mark.title", "Mark Attendance"));
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label(t("student.mark.subtitle", "Scan a QR code or enter the attendance code manually"));
        subtitle.getStyleClass().add("dash-subtitle");

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

        VBox manualCard = new VBox(10);
        manualCard.getStyleClass().add("manual-card");

        HBox manualHeader = new HBox(8);
        manualHeader.setAlignment(Pos.CENTER_LEFT);

        Label manualIcon = new Label("⌨");
        manualIcon.getStyleClass().add("manual-icon");

        Label manualTitle = new Label(t("student.mark.manual.title", "Enter Code Manually"));
        manualTitle.getStyleClass().add("manual-title");

        manualHeader.getChildren().addAll(manualIcon, manualTitle);

        Label manualSub = new Label(t("student.mark.manual.subtitle", "Use the code shared by your teacher"));
        manualSub.getStyleClass().add("manual-subtitle");

        Label codeLabel = new Label(t("student.mark.code.label", "Attendance Code"));
        codeLabel.getStyleClass().add("field-label");

        TextField codeField = new TextField();
        codeField.setPromptText(t("student.mark.code.placeholder", "Enter code"));
        codeField.getStyleClass().add("code-field");

        Button submit = new Button(t("student.mark.submit", "Submit"));
        submit.getStyleClass().add("submit-button");
        submit.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (code.isBlank()) {
                new Alert(Alert.AlertType.WARNING, t("student.mark.warning.empty", "Please enter a code.")).show();
                return;
            }

            String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
            StudentAttendanceApi api = new StudentAttendanceApi(backendUrl);

            new Thread(() -> {
                try {
                    api.submitCode(code, jwtStore, state);

                    javafx.application.Platform.runLater(() -> {
                        codeField.clear();
                        new Alert(Alert.AlertType.INFORMATION, t("student.mark.success", "Attendance marked successfully.")).show();
                    });

                } catch (Exception exx) {
                    javafx.application.Platform.runLater(() ->
                            new Alert(Alert.AlertType.ERROR, exx.getMessage()).show()
                    );
                }
            }).start();
        });

        manualCard.getChildren().addAll(
                manualHeader,
                manualSub,
                codeLabel,
                codeField,
                submit
        );

        VBox howCard = new VBox(8);
        howCard.getStyleClass().add("how-card");

        Label howTitle = new Label(t("student.mark.how.title", "How it works"));
        howTitle.getStyleClass().add("how-title");

        Label step1 = new Label(t("student.mark.how.step1", "1. Get the attendance code from your teacher."));
        Label step2 = new Label(t("student.mark.how.step2", "2. Enter the code in the field above."));
        Label step3 = new Label(t("student.mark.how.step3", "3. Submit to record your attendance."));

        step1.getStyleClass().add("how-step");
        step2.getStyleClass().add("how-step");
        step3.getStyleClass().add("how-step");

        howCard.getChildren().addAll(howTitle, step1, step2, step3);

        page.getChildren().addAll(
                back,
                title,
                subtitle,
                qrCard,
                manualCard,
                howCard
        );

        return AppLayout.wrapWithSidebar(
                studentName,
                t("student.panel.title", "Student Panel"),
                t("student.nav.dashboard", "Dashboard"),
                t("student.nav.markAttendance", "Mark Attendance"),
                t("student.nav.myAttendance", "My Attendance"),
                t("student.nav.email", "Email"),
                page,
                "second",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("student-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("student-mark"); }
                    @Override public void goReports() { router.go("student-attendance"); }
                    @Override public void goEmail() { router.go("student-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}