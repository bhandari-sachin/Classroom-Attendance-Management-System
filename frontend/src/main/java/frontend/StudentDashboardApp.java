package frontend;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StudentDashboardApp extends Application {

    private Scene scene;
    private Parent dashboardRoot;
    private StackPane dashboardStack;


    // TODO: Replace with real user from login/backend (dummy data for now)
    private String studentName = "Name";

    // TODO: Replace with real attendance stats from backend
    private int presentCount = 0;
    private int absentCount = 0;
    private int excusedCount = 0;
    private double attendanceRate = 0.0; // 0.0 -> 0%

    // Side menu (drawer)
    private VBox sideMenu;
    private Pane overlay;
    private boolean menuOpen = false;
    private final double MENU_WIDTH = 240;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        // ---- Top bar ----
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label(studentName);
        name.getStyleClass().add("topbar-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button menuBtn = new Button("≡");
        menuBtn.getStyleClass().add("icon-button");

        topBar.getChildren().addAll(avatar, name, spacer, menuBtn);
        root.setTop(topBar);

        // ---- Content ----
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        Label title = new Label("Welcome back, User!");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Here’s your attendance overview for this month");
        subtitle.getStyleClass().add("subtitle");

        Button markAttendance = attendanceCard();
        GridPane statsGrid = statsGrid();

        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label("Your classes");
        classesTitle.getStyleClass().add("section-title");

        Region classesSpacer = new Region();
        HBox.setHgrow(classesSpacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Attendance  →");
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> openAttendancePage());

        classesHeader.getChildren().addAll(classesTitle, classesSpacer, viewAll);

        VBox classesCard = emptyClassesCard();

        content.getChildren().addAll(
                title,
                subtitle,
                markAttendance,
                statsGrid,
                new Separator(),
                classesHeader,
                classesCard
        );

        // ---- Wrap root in a StackPane so we can overlay the drawer ----
        dashboardRoot = root;

        sideMenu = buildSideMenu();
        overlay = buildOverlay();

        dashboardStack = new StackPane(root, overlay, sideMenu);
        StackPane.setAlignment(sideMenu, Pos.TOP_LEFT);

// Start hidden (offscreen)
        sideMenu.setTranslateX(-MENU_WIDTH);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);

        menuBtn.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) toggleMenu();
        });

// Scene
        scene = new Scene(dashboardStack, 600, 650);
        scene.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());

        stage.setTitle("Student-Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(600);
        stage.show();
    }

    // ---------------- Side Menu ----------------

    private VBox buildSideMenu() {
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
            goBackToDashboard();
        });

        Button markBtn = sideMenuItem("📖", "Mark Attendance");
        markBtn.setOnAction(e -> {
            closeMenu();
            openMarkAttendancePage();
        });

        Button reportsBtn = sideMenuItem("📋", "Reports");
        reportsBtn.setOnAction(e -> {
            closeMenu();
            openAttendancePage();
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

        // Click outside closes
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

    // ---------------- Navigation ----------------

    private void openAttendancePage() {
        closeMenu();
        StudentAttendancePage page = new StudentAttendancePage();
        Parent view = page.createView(
                this::goBackToDashboard,
                this::openMarkAttendancePage,
                () -> {}
        );
        scene.setRoot(view);
    }


    private void goBackToDashboard() {
        closeMenu();
        scene.setRoot(dashboardStack);
    }


    private void openMarkAttendancePage() {
        closeMenu();
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();
        Parent view = page.createView(
                this::goBackToDashboard,
                this::openAttendancePage,
                () -> {}
        );
        scene.setRoot(view);
    }


    // UI blocks

    private Button attendanceCard() {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);
        Label big = new Label("Mark Attendance");
        big.getStyleClass().add("attendance-title");

        Label small = new Label("Scan the QR code to check in");
        small.getStyleClass().add("attendance-subtitle");

        texts.getChildren().addAll(big, small);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("attendance-arrow");

        box.getChildren().addAll(icon, texts, spacer, arrow);
        btn.setGraphic(box);

        btn.setOnAction(e -> openMarkAttendancePage());
        return btn;
    }

    private GridPane statsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(statCardWithBadge("Present", String.valueOf(presentCount), "This month", "#3BAA66", "✓"), 0, 0);
        grid.add(statCardWithBadge("Absent", String.valueOf(absentCount), "This month", "#E05A5A", "✕"), 1, 0);
        grid.add(statCardWithBadge("Excused", String.valueOf(excusedCount), "This month", "#E09A3B", "⏱"), 0, 1);
        grid.add(statCardWithBadge("Rate", (int) (attendanceRate * 100) + "%", "This month", "#5AA6E0", "%"), 1, 1);

        return grid;
    }

    private VBox statCardWithBadge(String label, String value, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #ECEFF2;" +
                        "-fx-border-radius: 12;"
        );

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #4B5563;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 10;"
        );

        Label icon = new Label(iconChar);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(lbl, spacer, badge);

        Label big = new Label(value);
        big.setStyle("-fx-font-size: 28px; -fx-font-weight: 900;");

        Label small = new Label(hint);
        small.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        card.getChildren().addAll(top, big, small);
        return card;
    }

    private VBox emptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);

        Label cal = new Label("📅");
        cal.getStyleClass().add("empty-icon");

        Label t = new Label("No classes yet");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been enrolled in any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(cal, t, s);
        return card;
    }
}
