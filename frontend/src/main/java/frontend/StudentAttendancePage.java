package frontend;

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
import javafx.scene.text.Font;

public class StudentAttendancePage {

    private ContextMenu hamburgerMenu;
    private Scene scene;

    public Parent createView(Runnable onBackToDashboard) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        // Keep reference to Scene when attached
        root.sceneProperty().addListener((obs, oldScene, newScene) -> this.scene = newScene);

        /* ================= TOP BAR ================= */
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label("Name");
        name.getStyleClass().add("topbar-name");
        // TODO: replace with logged-in user name

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
            if (hamburgerMenu.isShowing()) hamburgerMenu.hide();
        });

        topBar.getChildren().addAll(avatar, name, spacer, menu);
        root.setTop(topBar);

        /* ================= CONTENT ================= */
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        Label title = new Label("My Attendance");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View your attendance history and statistics");
        subtitle.getStyleClass().add("subtitle");

        /* ================= FILTERS ================= */
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterIcon = new Label("⏷");
        filterIcon.getStyleClass().add("filter-icon");

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll("All Classes", "OOP1", "Databases", "Web Dev");
        classFilter.setValue("All Classes");
        classFilter.getStyleClass().add("filter-combo");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("This Month", "Last Month", "This Year");
        timeFilter.setValue("This Month");
        timeFilter.getStyleClass().add("filter-combo");

        // TODO: Add listeners to refresh stats & records
        filters.getChildren().addAll(filterIcon, classFilter, timeFilter);

        /* ================= STATS ================= */
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setPercentWidth(60);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setPercentWidth(40);

        stats.getColumnConstraints().addAll(c1, c2);

        VBox rateCard = rateCard("Attendance Rate", "0%");
        VBox presentCard = smallStatCard("Present", "0", "#3BAA66", "✓");
        VBox absentCard = smallStatCard("Absent", "0", "#E05A5A", "✕");
        VBox excusedCard = smallStatCard("Excused", "0", "#E09A3B", "⏱");
        VBox totalDaysCard = smallStatCard("Total Days", "0", "#BFC5CC", "📅");

        stats.add(rateCard, 0, 0);
        stats.add(presentCard, 1, 0);
        stats.add(absentCard, 0, 1);
        stats.add(excusedCard, 1, 1);
        stats.add(totalDaysCard, 0, 2);

        /* ================= RECORDS ================= */
        Label recordsTitle = new Label("Attendance Records");
        recordsTitle.getStyleClass().add("section-title");

        VBox recordsCard = new VBox(8);
        recordsCard.getStyleClass().add("records-card");
        recordsCard.setAlignment(Pos.CENTER);
        recordsCard.setMinHeight(160);

        Label recIcon = new Label("📅");
        recIcon.getStyleClass().add("empty-icon");
        recIcon.setFont(Font.font("Segoe UI Emoji", 18));

        Label recT = new Label("No Records Found");
        recT.getStyleClass().add("empty-title");

        Label recS = new Label("No attendance records for the selected filters");
        recS.getStyleClass().add("empty-subtitle");

        // TODO: Replace with TableView when records exist
        recordsCard.getChildren().addAll(recIcon, recT, recS);

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                stats,
                new Separator(),
                recordsTitle,
                recordsCard
        );

        return root;
    }

    /* ================= HAMBURGER MENU ================= */
    private ContextMenu buildHamburgerMenu(Runnable onBackToDashboard) {

        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            onBackToDashboard.run();
        });

        MenuItem markAttendance = new MenuItem("Mark Attendance", new Label("📖"));
        markAttendance.setOnAction(e -> {
            hamburgerMenu.hide();
            StudentMarkAttendancePage page = new StudentMarkAttendancePage();
            Parent view = page.createView(onBackToDashboard);
            scene.setRoot(view);
        });

        // Reports = THIS PAGE → do nothing
        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> hamburgerMenu.hide());

        MenuItem signout = new MenuItem("Sign out", new Label("⎋"));
        signout.setOnAction(e -> {
            hamburgerMenu.hide();
            // TODO: clear session & navigate to login
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

    /* ================= COMPONENTS ================= */
    private VBox rateCard(String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("rate-card");

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("rate-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label trend = new Label("↗");
        trend.getStyleClass().add("rate-trend");

        top.getChildren().addAll(lbl, spacer, trend);

        Label big = new Label(value);
        big.getStyleClass().add("rate-value");

        card.getChildren().addAll(top, big);
        return card;
    }

    private VBox smallStatCard(String label, String value, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-stat-card");

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        badge.getStyleClass().add("mini-badge");
        badge.setMinSize(26, 26);
        badge.setBackground(new Background(
                new BackgroundFill(Color.web(colorHex), new CornerRadii(8), Insets.EMPTY)
        ));

        Label icon = new Label(iconChar);
        icon.setFont(Font.font("Segoe UI Emoji", 13));
        icon.getStyleClass().add("mini-badge-icon");
        badge.getChildren().add(icon);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("mini-label");

        row1.getChildren().addAll(badge, lbl);

        Label big = new Label(value);
        big.getStyleClass().add("mini-value");

        HBox row2 = new HBox(big);
        row2.setPadding(new Insets(0, 0, 0, 36));

        card.getChildren().addAll(row1, row2);
        return card;
    }
}
