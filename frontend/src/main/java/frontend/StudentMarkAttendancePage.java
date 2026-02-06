package frontend;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class StudentMarkAttendancePage {

    // Side menu (drawer)
    private VBox sideMenu;
    private Pane overlay;
    private boolean menuOpen = false;
    private final double MENU_WIDTH = 240;

    public Parent createView(
            Runnable onBackToDashboard,
            Runnable onOpenReports,
            Runnable onOpenMarkAttendance // this page (can be no-op)
    ) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        /* ================= TOP BAR ================= */
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label("Name");
        name.getStyleClass().add("topbar-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button menuBtn = new Button("≡");
        menuBtn.getStyleClass().add("icon-button");

        topBar.getChildren().addAll(avatar, name, spacer, menuBtn);
        root.setTop(topBar);

        /* ================= CONTENT ================= */
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

        StackPane cameraBox = new StackPane();
        cameraBox.getStyleClass().add("camera-box");

        Label cameraIcon = new Label("📷");
        cameraIcon.setFont(Font.font("Segoe UI Emoji", 34));
        cameraIcon.getStyleClass().add("camera-icon");
        cameraBox.getChildren().add(cameraIcon);

        qrCard.getChildren().add(cameraBox);

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
            System.out.println("Submitted code: " + code);
        });

        manualCard.getChildren().addAll(
                manualHeader,
                manualSub,
                codeLabel,
                codeField,
                submit
        );

        // how it works
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

        // ---------------- Drawer wrapper ----------------
        sideMenu = buildSideMenu(onBackToDashboard, onOpenMarkAttendance, onOpenReports);
        overlay = buildOverlay();

        StackPane stack = new StackPane(root, overlay, sideMenu);
        StackPane.setAlignment(sideMenu, Pos.TOP_LEFT);

        sideMenu.setTranslateX(-MENU_WIDTH);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);

        menuBtn.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) toggleMenu();
        });

        return stack;
    }

    // ---------------- Side Menu ----------------

    private VBox buildSideMenu(Runnable onBackToDashboard, Runnable onOpenMarkAttendance, Runnable onOpenReports) {
        VBox menu = new VBox(10);
        menu.setPrefWidth(MENU_WIDTH);
        menu.setMinWidth(MENU_WIDTH);
        menu.setMaxWidth(MENU_WIDTH);
        menu.setPadding(new Insets(18));
        menu.getStyleClass().add("side-menu");

        Label header = new Label("Menu");
        header.getStyleClass().add("side-menu-title");

        Button dashboardBtn = sideMenuItem("▦", "Dashboard");
        dashboardBtn.setOnAction(e -> {
            closeMenu();
            onBackToDashboard.run();
        });

        Button markBtn = sideMenuItem("📖", "Mark Attendance");
        markBtn.setOnAction(e -> {
            closeMenu();
            if (onOpenMarkAttendance != null) onOpenMarkAttendance.run(); // already here can be no-op
        });

        Button reportsBtn = sideMenuItem("📋", "Reports");
        reportsBtn.setOnAction(e -> {
            closeMenu();
            if (onOpenReports != null) onOpenReports.run();
        });

        Separator sep = new Separator();

        Button signOutBtn = sideMenuItem("⎋", "Sign out");
        signOutBtn.setOnAction(e -> {
            closeMenu();
            System.out.println("TODO: Sign out");
        });

        menu.getChildren().addAll(header, dashboardBtn, markBtn, reportsBtn, sep, signOutBtn);
        return menu;
    }

    private Button sideMenuItem(String icon, String text) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("side-menu-icon");

        Label t = new Label(text);
        t.getStyleClass().add("side-menu-text");

        HBox row = new HBox(10, ic, t);
        row.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(row);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("side-menu-btn");
        return btn;
    }

    private Pane buildOverlay() {
        Pane p = new Pane();
        p.getStyleClass().add("drawer-overlay");
        p.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        p.setOnMouseClicked(e -> closeMenu());
        return p;
    }

    private void toggleMenu() {
        if (menuOpen) closeMenu();
        else openMenu();
    }

    private void openMenu() {
        menuOpen = true;
        overlay.setVisible(true);
        overlay.setMouseTransparent(false);

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), sideMenu);
        tt.setToX(0);
        tt.play();
    }

    private void closeMenu() {
        if (!menuOpen) return;
        menuOpen = false;

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), sideMenu);
        tt.setToX(-MENU_WIDTH);
        tt.setOnFinished(e -> {
            overlay.setVisible(false);
            overlay.setMouseTransparent(true);
        });
        tt.play();
    }
}
