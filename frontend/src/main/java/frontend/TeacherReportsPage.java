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

public class TeacherReportsPage {

    private ContextMenu hamburgerMenu;

    public Parent createView(Runnable onBackToDashboard) {
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

        /* ================= CONTENT ================= */
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        Label title = new Label("Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View class attendance reports");
        subtitle.getStyleClass().add("subtitle");

        VBox empty = new VBox(8);
        empty.getStyleClass().add("classes-card");
        empty.setAlignment(Pos.CENTER);
        empty.setMinHeight(200);

        Label icon = new Label("📋");
        icon.getStyleClass().add("empty-icon");

        Label t = new Label("No reports yet");
        t.getStyleClass().add("empty-title");

        Label s = new Label("Reports will appear here when classes have attendance.");
        s.getStyleClass().add("empty-subtitle");

        empty.getChildren().addAll(icon, t, s);

        content.getChildren().addAll(title, subtitle, empty);

        return root;
    }

    private ContextMenu buildHamburgerMenu(Runnable onBackToDashboard) {

        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            onBackToDashboard.run();
        });

        MenuItem takeAttendance = new MenuItem("Take Attendance", new Label("📷"));
        takeAttendance.setOnAction(e -> {
            hamburgerMenu.hide();
            TeacherTakeAttendancePage page = new TeacherTakeAttendancePage();
            Parent view = page.createView(onBackToDashboard);
            hamburgerMenu.getOwnerNode().getScene().setRoot(view);
        });

        // this page
        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> hamburgerMenu.hide());

        MenuItem signout = new MenuItem("Sign out", new Label("⎋"));
        signout.setOnAction(e -> {
            hamburgerMenu.hide();
            System.out.println("TODO: Sign out");
        });

        ContextMenu menu = new ContextMenu(
                dashboard,
                takeAttendance,
                reports,
                new SeparatorMenuItem(),
                signout
        );

        menu.getStyleClass().add("hamburger-menu");
        return menu;
    }
}
