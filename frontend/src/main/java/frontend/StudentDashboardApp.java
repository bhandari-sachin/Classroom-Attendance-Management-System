package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class StudentDashboardApp extends Application {

    private Scene scene;
    private Parent dashboardRoot;

    // TODO: Replace with real user from login/backend (dummy data for now)
    private String studentName = "Name";

    // TODO: Replace with real attendance stats from backend
    private int presentCount = 0;
    private int absentCount = 0;
    private int excusedCount = 0;
    private double attendanceRate = 0.0; // 0.0 -> 0%

    // Menu
    private ContextMenu hamburgerMenu;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        // TODO: Add a shared navigation controller instead of storing roots manually (optional)
        // TODO: Add a "Back" action in top bar when on Attendance page (optional)

        // top bar
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

        // menu
        hamburgerMenu = buildHamburgerMenu();

        // Show/hide on click
        menuBtn.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (hamburgerMenu.isShowing()) {
                    hamburgerMenu.hide();
                } else {
                    hamburgerMenu.show(menuBtn, Side.BOTTOM, 0, 6);
                }
            }
        });


        root.setOnMousePressed(e -> {
            if (hamburgerMenu != null && hamburgerMenu.isShowing()) {
                hamburgerMenu.hide();
            }
        });

        topBar.getChildren().addAll(avatar, name, spacer, menuBtn);
        root.setTop(topBar);

        // content
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        Label title = new Label("Welcome back, User!");
        title.getStyleClass().add("title");
        // TODO: Replace "User" with actual first name when backend/auth is ready

        Label subtitle = new Label("Here’s your attendance overview for this month");
        subtitle.getStyleClass().add("subtitle");

        Button markAttendance = attendanceCard();
        GridPane statsGrid = statsGrid();

        // TODO: Replace empty card with real classes list (dummy data until backend)
        // TODO: When user has classes, show a list of class cards instead of "No classes yet"

        // classes header
        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label("Your classes");
        classesTitle.getStyleClass().add("section-title");

        Region classesSpacer = new Region();
        HBox.setHgrow(classesSpacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Attendance  →");
        viewAll.getStyleClass().add("link-button");

        // View All -> Attendance page
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

        // scene
        dashboardRoot = root;

        scene = new Scene(root, 600, 650);
        scene.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());

        stage.setTitle("Student-Dashboard");
        stage.setScene(scene);

        // TODO: Verify responsiveness: window resizing should not break layout
        stage.setMinWidth(420);
        stage.setMinHeight(600);

        stage.show();
    }

    private ContextMenu buildHamburgerMenu() {
        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            goBackToDashboard();
        });

        MenuItem markAttendance = new MenuItem("Mark Attendance", new Label("📖"));
        markAttendance.setOnAction(e -> {
            hamburgerMenu.hide();
            openMarkAttendancePage();
        });

        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> {
            hamburgerMenu.hide();
            openAttendancePage();
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


    private void openAttendancePage() {
        StudentAttendancePage page = new StudentAttendancePage();
        Parent view = page.createView(this::goBackToDashboard);
        scene.setRoot(view);
    }


    private void goBackToDashboard() {
        if (hamburgerMenu != null) hamburgerMenu.hide();
        scene.setRoot(dashboardRoot);
    }

    private Button attendanceCard() {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");
        // TODO: Replace placeholder icon with QR / scan icon (unicode or png)

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

        // TODO: Implement actual check-in logic (QR scan / code input) on Attendance page
        btn.setOnAction(e -> openMarkAttendancePage());

        return btn;
    }
    private void openMarkAttendancePage() {
        if (hamburgerMenu != null) hamburgerMenu.hide();
        StudentMarkAttendancePage page = new StudentMarkAttendancePage();
        Parent view = page.createView(this::goBackToDashboard);
        scene.setRoot(view);
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

        // TODO: Replace dummy values with real computed values from backend/service layer
        grid.add(statCardWithBadge("Present", String.valueOf(presentCount), "This month", "#3BAA66", "✓"), 0, 0);
        grid.add(statCardWithBadge("Absent", String.valueOf(absentCount), "This month", "#E05A5A", "✕"), 1, 0);
        grid.add(statCardWithBadge("Excused", String.valueOf(excusedCount), "This month", "#E09A3B", "⏱"), 0, 1);
        grid.add(statCardWithBadge("Rate", (int) (attendanceRate * 100) + "%", "This month", "#5AA6E0", "%"), 1, 1);

        // TODO: Verify responsiveness: on very small width, consider switching to 1-column layout (TilePane / FlowPane)
        return grid;
    }

    private VBox statCardWithBadge(String label, String value, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));

        // TODO: Move inline styles into dashboard.css (cleaner)
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

        // TODO: Ensure icon badge colors match design system (use CSS variables/colors)
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
        // TODO: Replace emoji with consistent icon style (unicode or png)

        Label t = new Label("No classes yet");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been enrolled in any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        // TODO: If backend returns classes, replace this empty state with a list of classes
        card.getChildren().addAll(cal, t, s);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
