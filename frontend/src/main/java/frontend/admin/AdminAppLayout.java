package frontend.admin;

import frontend.ui.HelperClass;
import frontend.ui.UiPreferences;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

public class AdminAppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    private static boolean isRtl() {
        String lang = UiPreferences.getLanguage();
        return lang != null && lang.toLowerCase().startsWith("ar");
    }

    public static Parent wrapWithSidebar(
            String name,
            String roleLabel,
            String dashboardLabel,
            String secondLabel,
            String thirdLabel,
            String fourthLabel,
            Node content,
            String activeKey,
            Navigator nav
    ) {

        HelperClass helper = new HelperClass();
        boolean isArabic = isRtl();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        // IMPORTANT: do NOT set root BorderPane to RTL

        MenuButton globe = new MenuButton("🌐");
        globe.getStyleClass().add("utility-menu");

        MenuItem en = new MenuItem(helper.getMessage("language.english"));
        MenuItem fi = new MenuItem(helper.getMessage("language.finnish"));
        MenuItem ar = new MenuItem(helper.getMessage("language.arabic"));
        MenuItem am = new MenuItem(helper.getMessage("language.amharic"));
        MenuItem ne = new MenuItem(helper.getMessage("language.nepali"));

        en.setOnAction(e -> {
            UiPreferences.setLanguage("en");
            refreshCurrentPage(nav, activeKey);
        });
        fi.setOnAction(e -> {
            UiPreferences.setLanguage("fi");
            refreshCurrentPage(nav, activeKey);
        });
        ar.setOnAction(e -> {
            UiPreferences.setLanguage("ar");
            refreshCurrentPage(nav, activeKey);
        });
        am.setOnAction(e -> {
            UiPreferences.setLanguage("am");
            refreshCurrentPage(nav, activeKey);
        });
        ne.setOnAction(e -> {
            UiPreferences.setLanguage("ne");
            refreshCurrentPage(nav, activeKey);
        });

        globe.getItems().addAll(en, fi, ar, am, ne);

        MenuButton settings = new MenuButton("⚙");
        settings.getStyleClass().add("utility-menu");

        MenuItem light = new MenuItem(helper.getMessage("settingsThemeLight"));
        MenuItem dark = new MenuItem(helper.getMessage("settingsThemeDark"));

        light.setOnAction(e -> {
            UiPreferences.setTheme(UiPreferences.Theme.LIGHT);
            if (root.getScene() != null) {
                UiPreferences.applyTheme(root.getScene());
            }
        });

        dark.setOnAction(e -> {
            UiPreferences.setTheme(UiPreferences.Theme.DARK);
            if (root.getScene() != null) {
                UiPreferences.applyTheme(root.getScene());
            }
        });

        settings.getItems().addAll(light, dark);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = isArabic
                ? new HBox(10, globe, settings, spacer)
                : new HBox(10, spacer, globe, settings);

        topBar.setAlignment(isArabic ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-utility-bar");
        topBar.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(260);
        sidebar.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("sidebar-name");
        nameLbl.setMaxWidth(Double.MAX_VALUE);
        nameLbl.setAlignment(isArabic ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label role = new Label(roleLabel);
        role.getStyleClass().add("sidebar-role");
        role.setMaxWidth(Double.MAX_VALUE);
        role.setAlignment(isArabic ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Separator sep = new Separator();

        VBox navBox = new VBox(10);
        navBox.getStyleClass().add("sidebar-nav");
        navBox.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label dash = navLabel(dashboardLabel, "dashboard", activeKey, nav::goDashboard, isArabic);
        Label second = navLabel(secondLabel, "second", activeKey, nav::goTakeAttendance, isArabic);
        Label third = navLabel(thirdLabel, "third", activeKey, nav::goReports, isArabic);
        Label fourth = navLabel(fourthLabel, "fourth", activeKey, nav::goEmail, isArabic);

        navBox.getChildren().addAll(dash, second, third, fourth);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        Label logout = new Label(helper.getMessage("teacher.sidebar.logout"));
        logout.getStyleClass().add("logout-link");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(isArabic ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(nameLbl, role, sep, navBox, push, logout);

        VBox center = new VBox(topBar, content);
        center.getStyleClass().add("content-wrap");
        center.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        if (content instanceof Region r) {
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(r, Priority.ALWAYS);
        }

        root.setLeft(null);
        root.setRight(null);

        if (isArabic) {
            root.setRight(sidebar);
        } else {
            root.setLeft(sidebar);
        }

        root.setCenter(center);

        return root;
    }

    private static void refreshCurrentPage(Navigator nav, String activeKey) {
        switch (activeKey) {
            case "dashboard" -> nav.goDashboard();
            case "second" -> nav.goTakeAttendance();
            case "third" -> nav.goReports();
            case "fourth" -> nav.goEmail();
        }
    }

    private static Label navLabel(String text, String key, String activeKey, Runnable action, boolean isArabic) {
        Label l = new Label(text);
        l.getStyleClass().add("nav-item");

        if (key.equals(activeKey)) {
            l.getStyleClass().add("nav-item-active");
        }

        l.setOnMouseClicked(e -> action.run());
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(isArabic ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        l.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        return l;
    }
}