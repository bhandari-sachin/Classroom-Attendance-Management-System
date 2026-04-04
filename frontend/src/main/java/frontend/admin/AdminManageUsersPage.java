package frontend.admin;

import frontend.UserRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminUsersResponseDto;
import frontend.dto.AdminUserDto;
import frontend.ui.HelperClass;
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
        HelperClass helper = new HelperClass();

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label(helper.getMessage("admin.users.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(helper.getMessage("admin.users.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        // Summary row (dynamic)
        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");

        // Filters
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText(helper.getMessage("admin.users.search.placeholder"));
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        String allTypesLabel = helper.getMessage("admin.users.filter.allTypes");
        String studentLabel = helper.getMessage("admin.users.filter.student");
        String teacherLabel = helper.getMessage("admin.users.filter.teacher");
        String adminLabel = helper.getMessage("admin.users.filter.admin");

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                allTypesLabel,
                studentLabel,
                teacherLabel,
                adminLabel
        );
        type.setValue(allTypesLabel);
        type.getStyleClass().add("filter-combo");

        filters.getChildren().addAll(search, type);

        // Table
        TableView<UserRow> table = AdminUI.buildUsersTable();
        table.getItems().clear();

        ObservableList<UserRow> rows = FXCollections.observableArrayList();
        FilteredList<UserRow> filtered = new FilteredList<>(rows, r -> true);
        table.setItems(filtered);

        Runnable applyFilter = () -> {
            String q = (search.getText() == null) ? "" : search.getText().trim().toLowerCase();
            String selectedType = type.getValue();

            filtered.setPredicate(r -> {
                boolean matchText = q.isBlank()
                        || safe(r.userProperty().get()).contains(q)
                        || safe(r.typeProperty().get()).contains(q)
                        || safe(r.enrolledProperty().get()).contains(q);

                boolean matchType = true;

                if (studentLabel.equalsIgnoreCase(selectedType)) {
                    matchType = studentLabel.equalsIgnoreCase(r.typeProperty().get());
                } else if (teacherLabel.equalsIgnoreCase(selectedType)) {
                    matchType = teacherLabel.equalsIgnoreCase(r.typeProperty().get());
                } else if (adminLabel.equalsIgnoreCase(selectedType)) {
                    matchType = adminLabel.equalsIgnoreCase(r.typeProperty().get());
                }

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
                                AdminUI.smallSummaryCard(helper.getMessage("admin.users.summary.students"), String.valueOf(data.students), "🎓", "accent-green"),
                                AdminUI.smallSummaryCard(helper.getMessage("admin.users.summary.teachers"), String.valueOf(data.teachers), "👥", "accent-purple"),
                                AdminUI.smallSummaryCard(helper.getMessage("admin.users.summary.admins"), String.valueOf(data.admins), "🛡", "accent-orange")
                        );

                        rows.clear();
                        if (data.users != null) {
                            for (AdminUserDto u : data.users) {
                                String userCell = (u.name == null ? "" : u.name) + "\n" + (u.email == null ? "" : u.email);

                                String localizedRole = localizeRole(u.role, helper);
                                String enrolledText = localizeEnrolled(u.enrolled, helper);

                                rows.add(new UserRow(
                                        userCell,
                                        localizedRole,
                                        enrolledText
                                ));
                            }
                        }

                        applyFilter.run();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        loadError.setText(helper.getMessage("admin.users.loadError") + " " + e.getMessage());
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
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("admin.dashboard.title"),
                helper.getMessage("admin.classes.title"),
                helper.getMessage("admin.users.title"),
                helper.getMessage("admin.reports.title"),
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

    private String localizeRole(String role, HelperClass helper) {
        if (role == null) return "";

        return switch (role.trim().toUpperCase()) {
            case "STUDENT" -> helper.getMessage("admin.users.filter.student");
            case "TEACHER" -> helper.getMessage("admin.users.filter.teacher");
            case "ADMIN" -> helper.getMessage("admin.users.filter.admin");
            default -> role;
        };
    }

    private String localizeEnrolled(String enrolled, HelperClass helper) {
        if (enrolled == null || enrolled.isBlank()) {
            return helper.getMessage("common.status.noData");
        }
        return enrolled;
    }

    private static String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}