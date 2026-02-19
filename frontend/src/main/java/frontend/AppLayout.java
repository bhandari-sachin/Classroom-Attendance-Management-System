package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
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

    public static Parent wrapWithSidebar(String teacherName, String studentPanel, String dashboard, String markAttendance, String myAttendance, String contact, Node content, String activeKey, Navigator nav) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(260);

        Label name = new Label(teacherName);
        name.getStyleClass().add("sidebar-name");

        Label role = new Label("Teacher Panel");
        role.getStyleClass().add("sidebar-role");

        Separator sep = new Separator();

        VBox navBox = new VBox(10);
        navBox.getStyleClass().add("sidebar-nav");

        Label dash = navLabel("Dashboard", "dashboard", activeKey, nav::goDashboard);
        Label take = navLabel("Take Attendance", "takeAttendance", activeKey, nav::goTakeAttendance);
        Label reports = navLabel("Reports", "reports", activeKey, nav::goReports);
        Label email = navLabel("Email", "email", activeKey, nav::goEmail);

        navBox.getChildren().addAll(dash, take, reports, email);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Logout as red text link (not a button)
        Label logout = new Label("Logout");
        logout.getStyleClass().add("logout-link");
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(name, role, sep, navBox, spacer, logout);

        // ===== CENTER CONTENT =====
        VBox centerWrap = new VBox();
        centerWrap.getStyleClass().add("content-wrap");
        centerWrap.getChildren().add(content);

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
