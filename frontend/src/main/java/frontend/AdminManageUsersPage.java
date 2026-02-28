package frontend;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminManageUsersPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

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