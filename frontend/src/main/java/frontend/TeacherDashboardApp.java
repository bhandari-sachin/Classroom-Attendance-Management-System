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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TeacherDashboardApp extends Application {

    private Scene scene;
    private Parent dashboardRoot;
    private ContextMenu hamburgerMenu;

    // Dummy data (later from backend)
    private String teacherName = "Name";
    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        /* ================= TOP BAR ================= */
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label(teacherName);
        name.getStyleClass().add("topbar-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label menu = new Label("≡");
        menu.getStyleClass().add("icon-button");

        hamburgerMenu = buildHamburgerMenu();

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

        /* ================= CONTENT ================= */
        VBox content = new VBox(16);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("content");
        root.setCenter(content);

        Label greeting = new Label("Good afternoon, User!");
        greeting.getStyleClass().add("title");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("subtitle");

        /* ================= ACTION CARDS ================= */
        HBox actions = new HBox(12);

        Button takeAttendance = actionCard(
                "Take Attendance",
                "Generate QR code for your class",
                "take-card",
                this::openTakeAttendancePage
        );

        Button viewReports = actionCard(
                "View Reports",
                "Class Attendance Reports",
                "report-card",
                this::openReportsPage
        );

        actions.getChildren().addAll(takeAttendance, viewReports);

        /* ================= STATS ================= */
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        stats.add(statCard("My Classes", totalClasses, "stat-badge-purple"), 0, 0);
        stats.add(statCard("Total Students", totalStudents, "stat-badge-teal"), 1, 0);
        stats.add(statCard("Present Today", presentToday, "stat-badge-green"), 0, 1);
        stats.add(statCard("Absent Today", absentToday, "stat-badge-red"), 1, 1);

        /* ================= MY CLASSES ================= */
        Label classesTitle = new Label("My classes");
        classesTitle.getStyleClass().add("section-title");

        VBox emptyClasses = new VBox(8);
        emptyClasses.setAlignment(Pos.CENTER);
        emptyClasses.setMinHeight(160);
        emptyClasses.getStyleClass().add("classes-card");

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");

        Label t = new Label("No classes Assigned");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been assigned any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        emptyClasses.getChildren().addAll(icon, t, s);

        content.getChildren().addAll(
                greeting,
                dateLabel,
                actions,
                stats,
                new Separator(),
                classesTitle,
                emptyClasses
        );

        dashboardRoot = root;

        scene = new Scene(root, 900, 750);
        scene.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());

        stage.setTitle("Teacher Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(600);
        stage.show();
    }

    /* ================= MENU ================= */
    private ContextMenu buildHamburgerMenu() {

        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            scene.setRoot(dashboardRoot);
        });

        MenuItem takeAttendance = new MenuItem("Take Attendance", new Label("📷"));
        takeAttendance.setOnAction(e -> {
            hamburgerMenu.hide();
            openTakeAttendancePage();
        });

        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> {
            hamburgerMenu.hide();
            openReportsPage();
        });

        MenuItem signOut = new MenuItem("Sign out", new Label("⎋"));
        signOut.setOnAction(e -> {
            hamburgerMenu.hide();
            System.out.println("TODO: Sign out");
        });

        ContextMenu menu = new ContextMenu(
                dashboard,
                takeAttendance,
                reports,
                new SeparatorMenuItem(),
                signOut
        );

        menu.getStyleClass().add("hamburger-menu");
        return menu;
    }

    /* ================= NAVIGATION ================= */
    private void openTakeAttendancePage() {
        TeacherTakeAttendancePage page = new TeacherTakeAttendancePage();
        Parent view = page.createView(() -> scene.setRoot(dashboardRoot));
        scene.setRoot(view);
    }

    private void openReportsPage() {
        TeacherReportsPage page = new TeacherReportsPage();
        Parent view = page.createView(() -> scene.setRoot(dashboardRoot));
        scene.setRoot(view);
    }

    /* ================= COMPONENTS ================= */
    private Button actionCard(String title, String subtitle, String styleClass, Runnable action) {
        Button btn = new Button();
        btn.getStyleClass().addAll("action-card", styleClass);
        btn.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(4);
        Label t = new Label(title);
        t.getStyleClass().add("action-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("action-subtitle");

        Region spacer = new Region();
        HBox line = new HBox(10);
        line.setAlignment(Pos.CENTER_LEFT);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("action-arrow");

        line.getChildren().addAll(box, spacer, arrow);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(t, s);
        btn.setGraphic(line);

        btn.setOnAction(e -> action.run());
        return btn;
    }

    private VBox statCard(String label, int value, String badgeClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(14));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.getStyleClass().addAll("stat-badge", badgeClass);

        top.getChildren().addAll(l, spacer, badge);

        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("stat-value");

        card.getChildren().addAll(top, v);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
