package frontend;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Paint;


public class StudentAttendancePage {

    // Side menu (drawer)
    private VBox sideMenu;
    private Pane overlay;
    private boolean menuOpen = false;
    private final double MENU_WIDTH = 240;

    public Parent createView(
            Runnable onBackToDashboard,
            Runnable onOpenMarkAttendance,
            Runnable onOpenReports // this page (can be no-op)
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
        VBox presentCard = smallStatCard("Present", "0", "#3BAA66", "check");
        VBox absentCard  = smallStatCard("Absent",  "0", "#E05A5A", "x");
        VBox excusedCard = smallStatCard("Excused", "0", "#E09A3B", "clock");
        VBox totalDaysCard = smallStatCard("Total Days", "0", "#BFC5CC", "calendar");



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
            onOpenMarkAttendance.run();
        });

        Button reportsBtn = sideMenuItem("📋", "Reports");
        reportsBtn.setOnAction(e -> {
            closeMenu();
            if (onOpenReports != null) onOpenReports.run(); // already here, can be no-op
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

    private VBox smallStatCard(String label, String value, String colorHex, String iconKey) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-stat-card");

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        badge.setPrefSize(26, 26);
        badge.setMinSize(26, 26);
        badge.setMaxSize(26, 26);

        // ✅ INLINE style overrides any CSS rules
        badge.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 8;"
        );

        Node iconNode = makeBadgeIcon(iconKey);
        badge.getChildren().add(iconNode);
        badge.setBorder(new Border(new BorderStroke(
                javafx.scene.paint.Color.BLUE,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));


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

    private Node makeBadgeIcon(String key) {
        // ✅ Use WHITE on colored badge; if still invisible, switch to BLACK
        var stroke = javafx.scene.paint.Color.WHITE;

        switch (key) {
            case "check": {
                Line l1 = new Line(6, 14, 11, 18);
                Line l2 = new Line(11, 18, 20, 8);
                l1.setStroke(stroke); l2.setStroke(stroke);
                l1.setStrokeWidth(2.6); l2.setStrokeWidth(2.6);
                l1.setStrokeLineCap(StrokeLineCap.ROUND);
                l2.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(l1, l2);
            }
            case "x": {
                Line a = new Line(7, 7, 19, 19);
                Line b = new Line(19, 7, 7, 19);
                a.setStroke(stroke); b.setStroke(stroke);
                a.setStrokeWidth(2.6); b.setStrokeWidth(2.6);
                a.setStrokeLineCap(StrokeLineCap.ROUND);
                b.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(a, b);
            }
            case "clock": {
                Circle c = new Circle(13, 13, 8);
                c.setFill(javafx.scene.paint.Color.TRANSPARENT);
                c.setStroke(stroke);
                c.setStrokeWidth(2.2);

                Line h = new Line(13, 13, 13, 9);
                Line m = new Line(13, 13, 17, 13);
                h.setStroke(stroke); m.setStroke(stroke);
                h.setStrokeWidth(2.2); m.setStrokeWidth(2.2);
                h.setStrokeLineCap(StrokeLineCap.ROUND);
                m.setStrokeLineCap(StrokeLineCap.ROUND);

                return new Group(c, h, m);
            }
            case "calendar": {
                Rectangle body = new Rectangle(7, 8, 12, 12);
                body.setFill(javafx.scene.paint.Color.TRANSPARENT);
                body.setStroke(stroke);
                body.setStrokeWidth(2.0);
                body.setArcWidth(3);
                body.setArcHeight(3);

                Line top = new Line(7, 11, 19, 11);
                top.setStroke(stroke);
                top.setStrokeWidth(2.0);

                Line ring1 = new Line(10, 6, 10, 9);
                Line ring2 = new Line(16, 6, 16, 9);
                ring1.setStroke(stroke); ring2.setStroke(stroke);
                ring1.setStrokeWidth(2.0); ring2.setStrokeWidth(2.0);
                ring1.setStrokeLineCap(StrokeLineCap.ROUND);
                ring2.setStrokeLineCap(StrokeLineCap.ROUND);

                return new Group(body, top, ring1, ring2);
            }
            default: {
                Circle dot = new Circle(13, 13, 3);
                dot.setFill(stroke);
                return dot;
            }
        }


}



}
