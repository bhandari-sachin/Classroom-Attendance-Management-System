package frontend.admin;

import frontend.UserRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminUsersResponseDto;
import frontend.dto.AdminUserDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminManageUsersPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank()) ? "Name" : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Manage Users");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View and manage students, teachers, and their enrollments");
        subtitle.getStyleClass().add("subtitle");

        // Summary row (dynamic)
        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");

        // Filters
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

        // Table
        TableView<UserRow> table = AdminUI.buildUsersTable();
        table.getItems().clear(); // remove demo rows

        ObservableList<UserRow> rows = FXCollections.observableArrayList();
        FilteredList<UserRow> filtered = new FilteredList<>(rows, r -> true);
        table.setItems(filtered);

        Runnable applyFilter = () -> {
            String q = (search.getText() == null) ? "" : search.getText().trim().toLowerCase();
            String t = type.getValue();

            filtered.setPredicate(r -> {
                boolean matchText = q.isBlank()
                        || safe(r.userProperty().get()).contains(q)
                        || safe(r.typeProperty().get()).contains(q)
                        || safe(r.enrolledProperty().get()).contains(q);

                boolean matchType = true;
                if ("Student".equalsIgnoreCase(t)) matchType = "STUDENT".equalsIgnoreCase(r.typeProperty().get());
                if ("Teacher".equalsIgnoreCase(t)) matchType = "TEACHER".equalsIgnoreCase(r.typeProperty().get());
                if ("Admin".equalsIgnoreCase(t)) matchType = "ADMIN".equalsIgnoreCase(r.typeProperty().get());

                return matchText && matchType;
            });
        };

        search.textProperty().addListener((o, a, b) -> applyFilter.run());
        type.setOnAction(e -> applyFilter.run());

        Label loadError = new Label();
        loadError.getStyleClass().add("subtitle");
        loadError.setManaged(false);
        loadError.setVisible(false);

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);

        Runnable reload = () -> {
            loadError.setVisible(false);
            loadError.setManaged(false);

            new Thread(() -> {
                try {
                    AdminUsersResponseDto data = api.getAdminUsers();

                    Platform.runLater(() -> {
                        summary.getChildren().setAll(
                                AdminUI.smallSummaryCard("Students", String.valueOf(data.students), "🎓", "accent-green"),
                                AdminUI.smallSummaryCard("Teachers", String.valueOf(data.teachers), "👥", "accent-purple"),
                                AdminUI.smallSummaryCard("Admins", String.valueOf(data.admins), "🛡", "accent-orange")
                        );

                        rows.clear();
                        if (data.users != null) {
                            for (AdminUserDto u : data.users) {
                                // Your AdminUI table expects:
                                // User column is a single string; your old sample used "Name\nemail"
                                String userCell = (u.name == null ? "" : u.name) + "\n" + (u.email == null ? "" : u.email);
                                rows.add(new UserRow(
                                        userCell,
                                        u.role == null ? "" : u.role,
                                        u.enrolled == null ? "-" : u.enrolled
                                ));
                            }
                        }

                        applyFilter.run();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        loadError.setText("Failed to load users: " + e.getMessage());
                        loadError.setVisible(true);
                        loadError.setManaged(true);
                    });
                }
            }).start();
        };

        reload.run();

        content.getChildren().addAll(title, subtitle, summary, filters, loadError, table);

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
                "third",
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

    private static String safe(String s) { return s == null ? "" : s.toLowerCase(); }
}