package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class StudentMarkAttendancePage {

    public Parent build(Scene scene, String studentName) {

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Optional back link (keep if you want)
        Button back = new Button("← Back to Dashboard");
        back.getStyleClass().add("back-link");
        back.setOnAction(e ->
                scene.setRoot(new StudentDashboardApp().build(scene, studentName))
        );

        Label title = new Label("Scan QR Code");
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label("Mark your attendance by scanning the class QR code");
        subtitle.getStyleClass().add("dash-subtitle");

        // QR placeholder card
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

        // Manual entry
        VBox manualCard = new VBox(10);
        manualCard.getStyleClass().add("manual-card");

        HBox manualHeader = new HBox(8);
        manualHeader.setAlignment(Pos.CENTER_LEFT);

        Label manualIcon = new Label("⌨");
        manualIcon.getStyleClass().add("manual-icon");

        Label manualTitle = new Label("Manual Code Entry");
        manualTitle.getStyleClass().add("manual-title");

        manualHeader.getChildren().addAll(manualIcon, manualTitle);

        Label manualSub = new Label("Enter the attendance code provided by your teacher");
        manualSub.getStyleClass().add("manual-subtitle");

        Label codeLabel = new Label("Attendance Code");
        codeLabel.getStyleClass().add("field-label");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter code");
        codeField.getStyleClass().add("code-field");

        Button submit = new Button("Submit");
        submit.getStyleClass().add("submit-button");
        submit.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            String code = codeField.getText().trim();
            System.out.println("Submitted code: " + code);
            // TODO: call backend / validate code / show success toast
        });

        manualCard.getChildren().addAll(
                manualHeader,
                manualSub,
                codeLabel,
                codeField,
                submit
        );

        // How it works
        VBox howCard = new VBox(8);
        howCard.getStyleClass().add("how-card");

        Label howTitle = new Label("How it works:");
        howTitle.getStyleClass().add("how-title");

        Label step1 = new Label("1. Get the attendance code from your teacher");
        Label step2 = new Label("2. Enter the code in the field above");
        Label step3 = new Label("3. Click submit to mark your attendance");

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

        return AdminAppLayout.wrapWithSidebar(
                studentName,
                page,
                "takeAttendance", // for student: "Mark Attendance" page
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() {
                        scene.setRoot(new StudentDashboardApp().build(scene, studentName));
                    }

                    @Override public void goTakeAttendance() {
                        scene.setRoot(build(scene, studentName)); // already here
                    }

                    @Override public void goReports() {
                        scene.setRoot(new StudentAttendancePage().build(scene, studentName));
                    }

                    @Override public void goEmail() {
                        scene.setRoot(new StudentEmailPage().build(scene, studentName));
                    }

                    @Override public void logout() {
                        System.out.println("TODO: Student Logout");
                    }
                }
        );
    }
}
