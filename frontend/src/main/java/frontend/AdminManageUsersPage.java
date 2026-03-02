package frontend;

import config.UserSQL;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import model.UserRole;
import service.UserService;

import java.util.List;
import java.util.Locale;

public class AdminManageUsersPage {

    UserSQL userSQL = new UserSQL();
    UserService userService = new UserService(userSQL);

    private void loadUsers(TableView<UserRow> table, String typeFilter) {
        table.getItems().clear();

        String selectedType = typeFilter == null ? "All Types" : typeFilter;

        List<User> users;
        UserRole selectedRole = null;

        if (!"All Types".equalsIgnoreCase(selectedType)) {
            switch (selectedType.toLowerCase(Locale.ROOT)) {
                case "student" -> selectedRole = UserRole.STUDENT;
                case "teacher" -> selectedRole = UserRole.TEACHER;
                case "admin" -> selectedRole = UserRole.ADMIN;
            }
        }

        if (selectedRole == null) {
            users = userService.getAllUsers();
        } else {
            users = userService.filterByRole(selectedRole, null);
        }

        for (User u : users) {
            table.getItems().add(
                    new UserRow(
                            u.getName(),
                            u.getEmail(),
                            u.getUserType(),
                            String.valueOf(userService.getEnrolledClasses(u.getId()))
                    )
            );
        }
    }

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        // Title and subtitle
        Label title = new Label("Manage Users");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View and manage students, teachers, and their enrollments");
        subtitle.getStyleClass().add("subtitle");

        // Summary cards
        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");

        List<User> allUsers = userService.getAllUsers();
        long studentsCount = allUsers.stream().filter(u -> u.getUserType() == UserRole.STUDENT).count();
        long teachersCount = allUsers.stream().filter(u -> u.getUserType() == UserRole.TEACHER).count();
        long adminsCount = allUsers.stream().filter(u -> u.getUserType() == UserRole.ADMIN).count();

        summary.getChildren().addAll(
                AdminUI.smallSummaryCard("Students", String.valueOf(studentsCount), "🎓", "accent-green"),
                AdminUI.smallSummaryCard("Teachers", String.valueOf(teachersCount), "👥", "accent-purple"),
                AdminUI.smallSummaryCard("Admins", String.valueOf(adminsCount), "🛡", "accent-orange")
        );

        // Filters (search and type)
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

        // Users table
        TableView<UserRow> table = AdminUI.buildUsersTable();
        loadUsers(table, type.getValue());

        // Reload table when type changes
        type.setOnAction(e -> loadUsers(table, type.getValue()));

        content.getChildren().addAll(title, subtitle, summary, filters, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        // Wrap with sidebar
        return AdminAppLayout.wrapWithSidebar(
                adminName,
                "Admin Panel",
                "Dashboard",
                "Manage Classes",
                "Manage Users",
                "Attendance Reports",
                scroll,
                "third", // ✅ active = Manage Users
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-users"); }
                    @Override public void goEmail() { router.go("admin-reports"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}