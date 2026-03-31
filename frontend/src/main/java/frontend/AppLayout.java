package frontend;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

public class AppLayout {

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    /**
     * Default teacher sidebar using bundle labels.
     */
    public static Parent wrapWithSidebar(
            String name,
            Node content,
            String activeKey,
            Navigator nav,
            ResourceBundle bundle,
            boolean isRtl
    ) {
        return wrapWithSidebar(
                name,
                bundle.getString("teacher.sidebar.title"),
                bundle.getString("teacher.sidebar.menu.dashboard"),
                bundle.getString("teacher.sidebar.menu.take_attendance"),
                bundle.getString("teacher.sidebar.menu.reports"),
                bundle.getString("teacher.sidebar.menu.email"),
                bundle.getString("teacher.sidebar.logout"),
                content,
                activeKey,
                nav,
                isRtl
        );
    }

    /**
     * Custom labels (Teacher/Student/etc.) using bundle or passed strings.
     */
    public static Parent wrapWithSidebar(
            String name,
            String roleLabel,
            String dashboardLabel,
            String secondLabel,
            String thirdLabel,
            String fourthLabel,
            String logoutLabel,
            Node content,
            String activeKey,
            Navigator nav,
            boolean isRtl
    ) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(260);
        sidebar.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("sidebar-name");
        nameLbl.setMaxWidth(Double.MAX_VALUE);
        nameLbl.setAlignment(isRtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        nameLbl.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label role = new Label(roleLabel);
        role.getStyleClass().add("sidebar-role");
        role.setMaxWidth(Double.MAX_VALUE);
        role.setAlignment(isRtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        role.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Separator sep = new Separator();

        VBox navBox = new VBox(10);
        navBox.getStyleClass().add("sidebar-nav");
        navBox.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label dash = navLabel(dashboardLabel, "dashboard", activeKey, nav::goDashboard, isRtl);
        Label second = navLabel(secondLabel, "second", activeKey, nav::goTakeAttendance, isRtl);
        Label third = navLabel(thirdLabel, "third", activeKey, nav::goReports, isRtl);
        Label fourth = navLabel(fourthLabel, "fourth", activeKey, nav::goEmail, isRtl);

        navBox.getChildren().addAll(dash, second, third, fourth);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logout = new Label(logoutLabel);
        logout.getStyleClass().add("logout-link");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(isRtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        logout.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        logout.setOnMouseClicked(e -> nav.logout());

        sidebar.getChildren().addAll(nameLbl, role, sep, navBox, spacer, logout);

        // ===== CENTER CONTENT =====
        VBox centerWrap = new VBox();
        centerWrap.getStyleClass().add("content-wrap");
        centerWrap.setFillWidth(true);
        centerWrap.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        centerWrap.getChildren().add(content);

        if (content instanceof Region r) {
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            r.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            VBox.setVgrow(r, Priority.ALWAYS);
        }

        if (isRtl) {
            root.setRight(sidebar);
        } else {
            root.setLeft(sidebar);
        }

        root.setCenter(centerWrap);

        return root;
    }

    private static Label navLabel(String text, String key, String activeKey, Runnable action, boolean isRtl) {
        Label label = new Label(text);
        label.getStyleClass().add("nav-item");

        if (key.equals(activeKey)) {
            label.getStyleClass().add("nav-item-active");
        }

        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(isRtl ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        label.setNodeOrientation(isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        label.setOnMouseClicked(e -> action.run());

        return label;
    }
}