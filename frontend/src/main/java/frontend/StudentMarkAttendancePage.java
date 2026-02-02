package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class StudentMarkAttendancePage {

    private ContextMenu hamburgerMenu;

    public Parent createView(Runnable onBackToDashboard) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        // top bar
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label("Name");
        name.getStyleClass().add("topbar-name");
        // TODO: Replace with logged-in name

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label menu = new Label("≡");
        menu.getStyleClass().add("icon-button");

        hamburgerMenu = buildHamburgerMenu(onBackToDashboard);

        menu.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (hamburgerMenu.isShowing()) hamburgerMenu.hide();
                else hamburgerMenu.show(menu, Side.BOTTOM, 0, 6);
            }
        });

        root.setOnMousePressed(e -> {
            if (hamburgerMenu != null && hamburgerMenu.isShowing()) hamburgerMenu.hide();
        });

        topBar.getChildren().addAll(avatar, name, spacer, menu);
        root.setTop(topBar);

        // content
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        // Back link
        Button back = new Button("← Back to Dashboard");
        back.getStyleClass().add("back-link");
        back.setOnAction(e -> onBackToDashboard.run());

        Label title = new Label("Scan QR Code");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Mark your attendance by scanning the class QR code");
        subtitle.getStyleClass().add("subtitle");

        // QR placeholder card
        VBox qrCard = new VBox();
        qrCard.getStyleClass().add("qr-card");
        qrCard.setMinHeight(200);
        qrCard.setAlignment(Pos.CENTER);

        // inner "camera" box
        StackPane cameraBox = new StackPane();
        cameraBox.getStyleClass().add("camera-box");

        Label cameraIcon = new Label("📷");
        cameraIcon.setFont(Font.font("Segoe UI Emoji", 34));
        cameraIcon.getStyleClass().add("camera-icon");
        cameraBox.getChildren().add(cameraIcon);

        qrCard.getChildren().add(cameraBox);

        // TODO: Implement real QR scanning using webcam library (e.g., ZXing + webcam-capture)

        // manual entry
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
            // TODO: Validate input + send to backend
            System.out.println("Submitted code: " + code);
        });

        manualCard.getChildren().addAll(
                manualHeader,
                manualSub,
                codeLabel,
                codeField,
                submit
        );

        // how it work
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

        content.getChildren().addAll(
                back,
                title,
                subtitle,
                qrCard,
                manualCard,
                howCard
        );

        return root;
    }

    private ContextMenu buildHamburgerMenu(Runnable onBackToDashboard) {
        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            onBackToDashboard.run();
        });

        // Mark Attendance = this page -> do nothing
        MenuItem markAttendance = new MenuItem("Mark Attendance", new Label("📖"));
        markAttendance.setOnAction(e -> hamburgerMenu.hide());

        // ✅ Reports = AttendancePage
        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> {
            hamburgerMenu.hide();
            StudentAttendancePage page = new StudentAttendancePage();
            Parent view = page.createView(onBackToDashboard);
            hamburgerMenu.getOwnerNode().getScene().setRoot(view);
        });

        MenuItem signout = new MenuItem("Sign out", new Label("⎋"));
        signout.setOnAction(e -> {
            hamburgerMenu.hide();
            System.out.println("TODO: Sign out");
        });

        ContextMenu menu = new ContextMenu(
                dashboard,
                markAttendance,
                reports,
                new SeparatorMenuItem(),
                signout
        );

        menu.getStyleClass().add("hamburger-menu");
        return menu;
    }


}
