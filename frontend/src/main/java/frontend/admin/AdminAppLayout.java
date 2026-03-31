package frontend.admin;

import frontend.auth.AppRouter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

public class AdminAppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    /**
     * Default labels for ADMIN sidebar using existing bundle keys.
     */
    public static Parent wrapWithSidebar(
            String name,
            Node content,
            String activeKey,
            Navigator nav,
            AppRouter router
    ) {
        return wrapWithSidebar(
                name,
                I18n.t("admin.dashboard.title"),
                I18n.t("nav.dashboard"),
                I18n.t("admin.classes.title"),
                I18n.t("admin.reports.title"),
                I18n.t("admin.users.title"),
                content,
                activeKey,
                nav,
                router
        );
    }

    /**
     * Custom labels.
     */
    public static Parent wrapWithSidebar(
            String name,
            String roleLabel,
            String dashboardLabel,
            String secondLabel,
            String thirdLabel,
            String fourthLabel,
            Node content,
            String activeKey,
            Navigator nav,
            AppRouter router
    ) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        // ===== TOP BAR =====
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(12, 18, 0, 18));
        topBar.setAlignment(Pos.CENTER_RIGHT);

        ComboBox<String> languageSwitch = new ComboBox<>();
        languageSwitch.getItems().addAll("English", "العربية");
        languageSwitch.setValue(I18n.isArabic() ? "العربية" : "English");
        languageSwitch.setPrefWidth(130);
        languageSwitch.getStyleClass().add("lang-switch");

        languageSwitch.setOnAction(e -> {
            String selected = languageSwitch.getValue();

            if ("العربية".equals(selected)) {
                I18n.setArabic();
            } else {
                I18n.setEnglish();
            }

            if (router != null) {
                router.refresh();
            }
        });

        topBar.getChildren().add(languageSwitch);

        // ===== SIDEBAR =====
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

        Label logout = new Label(I18n.t("student.nav.logout"));
        logout.getStyleClass().add("logout-link");
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(nameLbl, role, sep, navBox, spacer, logout);

        // ===== CENTER CONTENT =====
        VBox centerWrap = new VBox(12);
        centerWrap.getStyleClass().add("content-wrap");
        centerWrap.setFillWidth(true);
        centerWrap.setPadding(new Insets(0, 18, 18, 18));
        centerWrap.getChildren().addAll(topBar, content);

        if (content instanceof Region r) {
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(r, Priority.ALWAYS);
        }

        if (I18n.isRtl()) {
            root.setRight(sidebar);
        } else {
            root.setLeft(sidebar);
        }

        root.setCenter(centerWrap);

        RtlUtil.apply(topBar);
        RtlUtil.apply(sidebar);
        RtlUtil.apply(navBox);
        RtlUtil.apply(centerWrap);
        RtlUtil.apply(languageSwitch);

        return root;
    }

    private static Label navLabel(String text, String key, String activeKey, Runnable action) {
        Label l = new Label(text);
        l.getStyleClass().add("nav-item");

        if (key.equals(activeKey)) {
            l.getStyleClass().add("nav-item-active");
        }

        l.setOnMouseClicked(e -> action.run());
        l.setAlignment(I18n.isRtl() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        RtlUtil.apply(l);

        return l;
    }
}