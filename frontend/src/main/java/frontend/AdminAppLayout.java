package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminAppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    // Default labels (Teacher)
    public static Parent wrapWithSidebar(String name, Node content, String activeKey, Navigator nav) {
        return wrapWithSidebar(
                name,
                "Student Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
                content,
                activeKey,
                nav
        );
    }

    // Custom labels (Admin/Student/etc.)
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
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

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
        Label second = navLabel(secondLabel, "takeAttendance", activeKey, nav::goTakeAttendance);
        Label third = navLabel(thirdLabel, "reports", activeKey, nav::goReports);
        Label fourth = navLabel(fourthLabel, "email", activeKey, nav::goEmail);

        navBox.getChildren().addAll(dash, second, third, fourth);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logout = new Label("Logout");
        logout.getStyleClass().add("logout-link");
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(nameLbl, role, sep, navBox, spacer, logout);

        VBox centerWrap = new VBox();
        centerWrap.getStyleClass().add("content-wrap");
        centerWrap.setFillWidth(true);

        centerWrap.getChildren().add(content);

        if (content instanceof Region r) {
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(r, Priority.ALWAYS);
        }

        root.setLeft(sidebar);
        root.setCenter(centerWrap);

        return root;
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
