package frontend;

import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import service.AttendanceService;
import service.ClassService;
import service.UserService;

public class AdminManageUsersPage {
    private final UserService userSerice;

    public AdminManageUsersPage(UserService userSerice) {
        this.userSerice = userSerice;
    }

    private void loadUsers(TableView<UserRow> table) {
        table.getItems().clear();

        for (User u : userSerice.getAllUsers()) {
            table.getItems().add(
                    new UserRow(
                            u.getName(),
                            u.getEmail(),
                            u.getRole(),
                            String.valueOf(userSerice.getEnrolledClasses(u.getId()))
                    )
            );
        }
    }

    public Parent build(Scene scene, String adminName) {
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Manage Users");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View and manage students, teachers, and their enrollments");
        subtitle.getStyleClass().add("subtitle");

        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");
        summary.getChildren().addAll(
                AdminUI.smallSummaryCard("Students", "0", "🎓", "accent-green"),
                AdminUI.smallSummaryCard("Teachers", "1", "👥", "accent-purple"),
                AdminUI.smallSummaryCard("Admins", "1", "🛡", "accent-orange")
        );

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search by name or email...");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("All Types", "Student", "Teacher", "Admin");
        type.setValue("All Types");
        type.getStyleClass().add("filter-combo");

        filters.getChildren().addAll(search, type);

        TableView<UserRow> table = AdminUI.buildUsersTable();
        loadUsers(table);

        content.getChildren().addAll(title, subtitle, summary, filters, table);

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
                "reports", // active = Manage Users
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new AdminDashboardApp().build(scene, adminName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new AdminManageClassesPage(new ClassService(new ClassSQL())).build(scene, adminName)); }
                    @Override public void goReports() { scene.setRoot(build(scene, adminName)); }
                    @Override public void goEmail() { scene.setRoot(new AdminAttendanceReportsPage(new AttendanceService(new AttendanceSQL(), new SessionSQL()),
                            new ClassService(new ClassSQL())).build(scene, adminName)); }
                    @Override public void logout() { System.out.println("TODO: Admin Logout"); }
                }
        );

    }
}
