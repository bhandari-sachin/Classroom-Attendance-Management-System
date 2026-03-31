package frontend;

import frontend.ui.HelperClass;
import frontend.ui.UiPreferences;
import javafx.geometry.Insets;
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

public class AppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
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

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

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

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, topSpacer, globe, settings);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-utility-bar");

        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(260);

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("sidebar-name");

        Label role = new Label(roleLabel);
        role.getStyleClass().add("sidebar-role");

        Separator sep = new Separator();

        VBox navBox = new VBox(10);
        navBox.getStyleClass().add("sidebar-nav");

        Label dash = navLabel(dashboardLabel, "dashboard", activeKey, nav::goDashboard);
        Label second = navLabel(secondLabel, "second", activeKey, nav::goTakeAttendance);
        Label third = navLabel(thirdLabel, "third", activeKey, nav::goReports);
        Label fourth = navLabel(fourthLabel, "fourth", activeKey, nav::goEmail);

        navBox.getChildren().addAll(dash, second, third, fourth);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logout = new Label(helper.getMessage("logout"));
        logout.getStyleClass().add("logout-link");
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(nameLbl, role, sep, navBox, spacer, logout);

        VBox centerWrap = new VBox(topBar, content);
        centerWrap.getStyleClass().add("content-wrap");
        centerWrap.setFillWidth(true);

        if (content instanceof Region r) {
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(r, Priority.ALWAYS);
        }

        root.setLeft(sidebar);
        root.setCenter(centerWrap);
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

    private static Label navLabel(String text, String key, String activeKey, Runnable action) {
        Label l = new Label(text);
        l.getStyleClass().add("nav-item");
        if (key.equals(activeKey)) {
            l.getStyleClass().add("nav-item-active");
        }
        l.setOnMouseClicked(e -> action.run());
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }
}