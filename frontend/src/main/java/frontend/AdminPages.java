package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminPages {

    // ===== Dashboard content =====
    public static Parent dashboardPage(Scene scene, String adminName) {
        VBox content = new VBox(18);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Manage your school's attendance system");
        subtitle.getStyleClass().add("subtitle");

        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("grid");
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);

        statsGrid.add(AdminUI.makeStatCard("Total Classes", "4", "📘", "accent-purple"), 0, 0);
        statsGrid.add(AdminUI.makeStatCard("Students", "0", "🎓", "accent-green"), 1, 0);
        statsGrid.add(AdminUI.makeStatCard("Teachers", "1", "👥", "accent-orange"), 0, 1);
        statsGrid.add(AdminUI.makeStatCard("Monthly Rate", "0%", "📈", "accent-green"), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        statsGrid.getColumnConstraints().addAll(c1, c2);

        Label qaTitle = new Label("Quick Actions");
        qaTitle.getStyleClass().add("section-title");

        HBox quickActions = new HBox(14);
        quickActions.getStyleClass().add("quick-actions");

        Pane manageClasses = AdminUI.makeActionCard("Manage Classes", "Add, edit or remove classes", "📚", "qa-green");
        manageClasses.setOnMouseClicked(e ->
                scene.setRoot(new AdminManageClassesPage().build(scene, adminName))
        );

        Pane manageUsers = AdminUI.makeActionCard("Manage Users", "Student registration and details", "👤", "qa-purple");
        manageUsers.setOnMouseClicked(e ->
                scene.setRoot(new AdminManageUsersPage().build(scene, adminName))
        );

        Pane reports = AdminUI.makeActionCard("Attendance Reports", "View comprehensive reports", "🧾", "qa-green");
        reports.setOnMouseClicked(e ->
                scene.setRoot(new AdminAttendanceReportsPage().build(scene, adminName))
        );

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);

        Label rcTitle = new Label("Recent classes");
        rcTitle.getStyleClass().add("section-title");

        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);

        recentGrid.add(AdminUI.makeClassCard("Mathematics", "TX-09374", "teacher@example.com", "MWF 9:00 AM"), 0, 0);
        recentGrid.add(AdminUI.makeClassCard("Physics", "TX-09374", "teacher@example.com", "MWF 9:00 AM"), 1, 0);
        recentGrid.add(AdminUI.makeClassCard("Design Patterns", "SW-27366", "teacher@example.com", "MWF 9:00 AM"), 0, 1);
        recentGrid.add(AdminUI.makeClassCard("Software Project 1", "SW-64544", "teacher@example.com", "MWF 9:00 AM"), 1, 1);

        ColumnConstraints r1 = new ColumnConstraints();
        r1.setHgrow(Priority.ALWAYS);
        r1.setFillWidth(true);

        ColumnConstraints r2 = new ColumnConstraints();
        r2.setHgrow(Priority.ALWAYS);
        r2.setFillWidth(true);

        recentGrid.getColumnConstraints().addAll(r1, r2);

        content.getChildren().addAll(title, subtitle, statsGrid, qaTitle, quickActions, rcTitle, recentGrid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return scroll;

    }
    }
