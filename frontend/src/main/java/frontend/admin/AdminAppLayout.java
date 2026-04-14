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

public final class AdminAppLayout {

    private static final String KEY_DASHBOARD = "dashboard";
    private static final String KEY_SECOND = "second";
    private static final String KEY_THIRD = "third";
    private static final String KEY_FOURTH = "fourth";

    private AdminAppLayout() {
        // Utility class
    }

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
        boolean rtl = isRtl();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        HBox topBar = buildTopBar(helper, rtl, root, activeKey, nav);
        VBox sidebar = buildSidebar(
                helper,
                rtl,
                name,
                roleLabel,
                dashboardLabel,
                secondLabel,
                thirdLabel,
                fourthLabel,
                activeKey,
                nav
        );
        VBox center = buildCenter(content, topBar, rtl);

        root.setLeft(null);
        root.setRight(null);

        if (rtl) {
            root.setRight(sidebar);
        } else {
            root.setLeft(sidebar);
        }

        root.setCenter(center);
        return root;
    }

    private static boolean isRtl() {
        String lang = UiPreferences.getLanguage();
        return lang != null && lang.toLowerCase().startsWith("ar");
    }

    private static HBox buildTopBar(
            HelperClass helper,
            boolean rtl,
            BorderPane root,
            String activeKey,
            Navigator nav
    ) {
        MenuButton languageMenu = buildLanguageMenu(helper, activeKey, nav);
        MenuButton settingsMenu = buildSettingsMenu(helper, root);

        Region spacer = growRegion();

        HBox topBar = rtl
                ? new HBox(10, languageMenu, settingsMenu, spacer)
                : new HBox(10, spacer, languageMenu, settingsMenu);

        topBar.setAlignment(rtl ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-utility-bar");
        topBar.setNodeOrientation(toOrientation(rtl));
        return topBar;
    }

    private static MenuButton buildLanguageMenu(HelperClass helper, String activeKey, Navigator nav) {
        MenuButton menu = new MenuButton("🌐");
        menu.getStyleClass().add("utility-menu");

        menu.getItems().addAll(
                languageItem(helper.getMessage("language.english"), "en", activeKey, nav),
                languageItem(helper.getMessage("language.finnish"), "fi", activeKey, nav),
                languageItem(helper.getMessage("language.arabic"), "ar", activeKey, nav),
                languageItem(helper.getMessage("language.amharic"), "am", activeKey, nav),
                languageItem(helper.getMessage("language.nepali"), "ne", activeKey, nav)
        );

        return menu;
    }

    private static MenuItem languageItem(String label, String languageCode, String activeKey, Navigator nav) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(e -> {
            UiPreferences.setLanguage(languageCode);
            refreshCurrentPage(nav, activeKey);
        });
        return item;
    }

    private static MenuButton buildSettingsMenu(HelperClass helper, BorderPane root) {
        MenuButton menu = new MenuButton("⚙");
        menu.getStyleClass().add("utility-menu");

        MenuItem light = new MenuItem(helper.getMessage("settingsThemeLight"));
        light.setOnAction(e -> applyTheme(root, UiPreferences.Theme.LIGHT));

        MenuItem dark = new MenuItem(helper.getMessage("settingsThemeDark"));
        dark.setOnAction(e -> applyTheme(root, UiPreferences.Theme.DARK));

        menu.getItems().addAll(light, dark);
        return menu;
    }

    private static void applyTheme(BorderPane root, UiPreferences.Theme theme) {
        UiPreferences.setTheme(theme);
        if (root.getScene() != null) {
            UiPreferences.applyTheme(root.getScene());
        }
    }

    private static VBox buildSidebar(
            HelperClass helper,
            boolean rtl,
            String name,
            String roleLabel,
            String dashboardLabel,
            String secondLabel,
            String thirdLabel,
            String fourthLabel,
            String activeKey,
            Navigator nav
    ) {
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(260);
        sidebar.setNodeOrientation(toOrientation(rtl));

        Label nameLabel = sidebarText(name, "sidebar-name", rtl);
        Label role = sidebarText(roleLabel, "sidebar-role", rtl);

        VBox navBox = new VBox(10);
        navBox.getStyleClass().add("sidebar-nav");
        navBox.setNodeOrientation(toOrientation(rtl));
        navBox.getChildren().addAll(
                navLabel(dashboardLabel, KEY_DASHBOARD, activeKey, nav::goDashboard, rtl),
                navLabel(secondLabel, KEY_SECOND, activeKey, nav::goTakeAttendance, rtl),
                navLabel(thirdLabel, KEY_THIRD, activeKey, nav::goReports, rtl),
                navLabel(fourthLabel, KEY_FOURTH, activeKey, nav::goEmail, rtl)
        );

        Label logout = sidebarText(helper.getMessage("teacher.sidebar.logout"), "logout-link", rtl);
        logout.setOnMouseClicked(e -> nav.logout());

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                nameLabel,
                role,
                new Separator(),
                navBox,
                push,
                logout
        );

        return sidebar;
    }

    private static VBox buildCenter(Node content, HBox topBar, boolean rtl) {
        VBox center = new VBox(topBar, content);
        center.getStyleClass().add("content-wrap");
        center.setNodeOrientation(toOrientation(rtl));

        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(region, Priority.ALWAYS);
        }

        return center;
    }

    private static Label sidebarText(String text, String styleClass, boolean rtl) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(rtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return label;
    }

    private static Label navLabel(
            String text,
            String key,
            String activeKey,
            Runnable action,
            boolean rtl
    ) {
        Label label = new Label(text);
        label.getStyleClass().add("nav-item");

        if (key.equals(activeKey)) {
            label.getStyleClass().add("nav-item-active");
        }

        label.setOnMouseClicked(e -> action.run());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(rtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        label.setNodeOrientation(toOrientation(rtl));
        return label;
    }

    private static void refreshCurrentPage(Navigator nav, String activeKey) {
        switch (activeKey) {
            case KEY_DASHBOARD -> nav.goDashboard();
            case KEY_SECOND -> nav.goTakeAttendance();
            case KEY_THIRD -> nav.goReports();
            case KEY_FOURTH -> nav.goEmail();
            default -> nav.goDashboard();
        }
    }

    private static NodeOrientation toOrientation(boolean rtl) {
        return rtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
    }

    private static Region growRegion() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}