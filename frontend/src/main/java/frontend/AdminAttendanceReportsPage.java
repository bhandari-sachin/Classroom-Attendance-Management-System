package frontend;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminAttendanceReportsPage {

    public Parent build(Scene scene, String adminName) {

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Comprehensive attendance analytics and reports");
        subtitle.getStyleClass().add("subtitle");

        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(8);

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll("All Classes", "Mathematics", "Physics");
        classFilter.setValue("All Classes");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("This Month", "Last Month", "This Year");
        timeFilter.setValue("This Month");

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search by name...");

        filters.add(new VBox(new Label("Class"), classFilter), 0, 0);
        filters.add(new VBox(new Label("Time Period"), timeFilter), 1, 0);
        filters.add(new VBox(new Label("Search Student"), studentSearch), 2, 0);

        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        stats.add(AdminUI.makeStatCard("Overall Attendance Rate", "0%", "📈", "accent-green"), 0, 0);
        stats.add(AdminUI.makeStatCard("Present", "0", "🟢", "accent-green"), 1, 0);
        stats.add(AdminUI.makeStatCard("Absent", "0", "🔴", "accent-orange"), 0, 1);
        stats.add(AdminUI.makeStatCard("Excused", "0", "🟠", "accent-purple"), 1, 1);
        stats.add(AdminUI.makeStatCard("Total Records", "0", "📄", "accent-purple"), 0, 2);

        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.ALWAYS);
        c.setFillWidth(true);
        stats.getColumnConstraints().addAll(c, c);

        Label summaryTitle = new Label("Class summary");
        summaryTitle.getStyleClass().add("section-title");

        Pane classSummary = AdminUI.makeClassCard(
                "Mathematics",
                "TX-09374",
                "2 present · 1 absent · 1 excused",
                "50%"
        );

        Label recordsTitle = new Label("All Records");
        recordsTitle.getStyleClass().add("section-title");

        TableView<UserRow> table = AdminUI.buildUsersTable();

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                stats,
                summaryTitle,
                classSummary,
                recordsTitle,
                table
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                "Admin Panel",
                "Dashboard",
                "Manage Classes",
                "Manage Users",
                "Attendance Reports",
                scroll,
                "email", // activeKey for Attendance Reports (4th item)
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new AdminDashboardApp().build(scene, adminName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new AdminManageClassesPage().build(scene, adminName)); }
                    @Override public void goReports() { scene.setRoot(new AdminManageUsersPage().build(scene, adminName)); }
                    @Override public void goEmail() { scene.setRoot(build(scene, adminName)); }
                    @Override public void logout() { System.out.println("TODO: Admin Logout"); }
                }
        );

    }
}
