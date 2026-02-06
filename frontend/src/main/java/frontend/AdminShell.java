package frontend;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminShell extends Application {

    private Scene scene;

    // Shell layout
    private final BorderPane host = new BorderPane();     // top bar + center page content
    private final StackPane shellRoot = new StackPane();  // host + overlay + drawer

    // Drawer
    private VBox sideMenu;
    private Pane overlay;
    private boolean menuOpen = false;
    private final double MENU_WIDTH = 240;

    // Keep title label so pages can change it if you want later
    private final Label topName = new Label("Name");

    @Override
    public void start(Stage stage) {
        host.getStyleClass().add("page");

        // ---------- Top bar (shared) ----------
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        topName.getStyleClass().add("topbar-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button menuBtn = new Button("☰");
        menuBtn.getStyleClass().add("menu-btn");

        topBar.getChildren().addAll(avatar, topName, spacer, menuBtn);

        VBox headerWrap = new VBox(10, topBar, new Separator());
        headerWrap.getStyleClass().add("header-wrap");
        host.setTop(headerWrap);

        // ---------- Drawer + overlay ----------
        sideMenu = buildSideMenu();
        overlay = buildOverlay();

        shellRoot.getChildren().addAll(host, overlay, sideMenu);
        StackPane.setAlignment(sideMenu, Pos.TOP_LEFT);

        sideMenu.setTranslateX(-MENU_WIDTH);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);

        menuBtn.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) toggleMenu();
        });

        // ---------- Scene ----------
        scene = new Scene(shellRoot, 560, 720);

        var css = AdminShell.class.getResource("/admin-dashboard.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("Admin");
        stage.setScene(scene);
        stage.show();

        // ---------- Start page ----------
        showDashboard();
    }

    // ========== Navigation (swap CENTER only) ==========
    public void showDashboard() {
        closeMenu();
        Parent view = AdminPages.dashboard(this);
        host.setCenter(view);
    }

    public void showManageClasses() {
        closeMenu();
        Parent view = AdminPages.manageClasses(this);
        host.setCenter(view);
    }
    public void showAttendanceReports() {
        closeMenu();
        Parent view = AdminPages.attendanceReports(this);
        host.setCenter(view);
    }

    public void showManageUsers() {
        closeMenu();
        Parent view = AdminPages.manageUsers(this);
        host.setCenter(view);
    }

    // ========== Drawer ==========
    private VBox buildSideMenu() {
        VBox menu = new VBox(10);
        menu.setPrefWidth(MENU_WIDTH);
        menu.setMinWidth(MENU_WIDTH);
        menu.setMaxWidth(MENU_WIDTH);
        menu.setPadding(new Insets(18));
        menu.getStyleClass().add("side-menu");

        Label header = new Label("Admin Menu");
        header.getStyleClass().add("side-menu-title");

        Button dashboardBtn = sideMenuItem("▦", "Dashboard");
        dashboardBtn.setOnAction(e -> showDashboard());

        Button manageClassesBtn = sideMenuItem("📚", "Manage Classes");
        manageClassesBtn.setOnAction(e -> showManageClasses());

        Button manageUsersBtn = sideMenuItem("👤", "Manage Users");
        manageUsersBtn.setOnAction(e -> showManageUsers());

        Button reportsBtn = sideMenuItem("📊", "Attendance");
        reportsBtn.setOnAction(e -> showAttendanceReports());


        Separator sep = new Separator();

        Button signOutBtn = sideMenuItem("⎋", "Sign out");
        signOutBtn.setOnAction(e -> {
            closeMenu();
            System.out.println("TODO: Sign out");
        });

        menu.getChildren().addAll(header, dashboardBtn, manageClassesBtn, manageUsersBtn, reportsBtn, sep, signOutBtn);
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

    public static void main(String[] args) {
        launch(args);
    }
}
