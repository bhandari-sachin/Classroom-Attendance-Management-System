package frontend;

import frontend.api.LanguageApi;
import frontend.api.TranslationApi;
import frontend.i18n.FrontendI18n;
import frontend.ui.UiPreferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class AppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    private static final String BACKEND_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

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
        boolean isArabic = isRtl();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        MenuButton globe = new MenuButton("🌐");
        globe.getStyleClass().add("utility-menu");
        loadLanguageMenu(globe, nav, activeKey);

        MenuButton settings = new MenuButton("⚙");
        settings.getStyleClass().add("utility-menu");

        MenuItem light = new MenuItem(t("settings.theme.light", "Light"));
        MenuItem dark = new MenuItem(t("settings.theme.dark", "Dark"));

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
        navBox.setNodeOrientation(isArabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label dash = navLabel(dashboardLabel, "dashboard", activeKey, nav::goDashboard, isArabic);
        Label second = navLabel(secondLabel, "second", activeKey, nav::goTakeAttendance, isArabic);
        Label third = navLabel(thirdLabel, "third", activeKey, nav::goReports, isArabic);
        Label fourth = navLabel(fourthLabel, "fourth", activeKey, nav::goEmail, isArabic);

        navBox.getChildren().addAll(dash, second, third, fourth);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        Label logout = new Label(t("teacher.sidebar.logout", "Logout"));
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

    private static void loadLanguageMenu(MenuButton globe, Navigator nav, String activeKey) {
        new Thread(() -> {
            try {
                LanguageApi languageApi = new LanguageApi(BACKEND_URL);
                List<LanguageApi.LanguageItem> languages = languageApi.getActiveLanguages();

                Platform.runLater(() -> {
                    globe.getItems().clear();

                    for (LanguageApi.LanguageItem lang : languages) {
                        MenuItem item = new MenuItem(lang.name());
                        item.setOnAction(e -> loadLanguageAndRefresh(lang.code(), nav, activeKey));
                        globe.getItems().add(item);
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();

                Platform.runLater(() -> {
                    globe.getItems().clear();

                    MenuItem en = new MenuItem("English");
                    MenuItem fi = new MenuItem("Finnish");
                    MenuItem ar = new MenuItem("Arabic");
                    MenuItem am = new MenuItem("Amharic");
                    MenuItem ne = new MenuItem("Nepali");

                    en.setOnAction(e -> loadLanguageAndRefresh("en", nav, activeKey));
                    fi.setOnAction(e -> loadLanguageAndRefresh("fi", nav, activeKey));
                    ar.setOnAction(e -> loadLanguageAndRefresh("ar", nav, activeKey));
                    am.setOnAction(e -> loadLanguageAndRefresh("am", nav, activeKey));
                    ne.setOnAction(e -> loadLanguageAndRefresh("ne", nav, activeKey));

                    globe.getItems().addAll(en, fi, ar, am, ne);
                });
            }
        }).start();
    }

    private static void loadLanguageAndRefresh(String languageCode, Navigator nav, String activeKey) {
        UiPreferences.setLanguage(languageCode);

        new Thread(() -> {
            try {
                TranslationApi translationApi = new TranslationApi(BACKEND_URL);
                Map<String, String> translations = translationApi.getUiTranslations(languageCode);

                Platform.runLater(() -> {
                    FrontendI18n.setLanguage(languageCode);
                    FrontendI18n.setTranslations(translations);
                    refreshCurrentPage(nav, activeKey);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> refreshCurrentPage(nav, activeKey));
            }
        }).start();
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

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}