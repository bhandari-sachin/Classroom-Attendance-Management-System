package frontend.admin;

import frontend.UserRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminUsersResponseDto;
import frontend.dto.AdminUserDto;
import frontend.i18n.FrontendI18n;
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

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? t("teacher.fallback.name", "Name")
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label(t("admin.users.title", "Manage Users"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(t("admin.users.subtitle", "View and manage students, teachers, and their enrollments"));
        subtitle.getStyleClass().add("subtitle");

        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText(t("admin.users.search.placeholder", "Search by name or email..."));
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        String allTypesLabel = t("admin.users.filter.allTypes", "All Types");
        String studentLabel = t("admin.users.filter.student", "Student");
        String teacherLabel = t("admin.users.filter.teacher", "Teacher");
        String adminLabel = t("admin.users.filter.admin", "Admin");

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
                                AdminUI.smallSummaryCard(
                                        t("admin.users.summary.students", "Students"),
                                        String.valueOf(data.students),
                                        "🎓",
                                        "accent-green"
                                ),
                                AdminUI.smallSummaryCard(
                                        t("admin.users.summary.teachers", "Teachers"),
                                        String.valueOf(data.teachers),
                                        "👥",
                                        "accent-purple"
                                ),
                                AdminUI.smallSummaryCard(
                                        t("admin.users.summary.admins", "Admins"),
                                        String.valueOf(data.admins),
                                        "🛡",
                                        "accent-orange"
                                )
                        );

                        rows.clear();
                        if (data.users != null) {
                            for (AdminUserDto u : data.users) {
                                String userCell = (u.name == null ? "" : u.name) + "\n" + (u.email == null ? "" : u.email);

                                String localizedRole = localizeRole(u.role);
                                String enrolledText = localizeEnrolled(u.enrolled);

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
                        loadError.setText(t("admin.users.loadError", "Failed to load users:") + " " + e.getMessage());
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
                t("admin.panel", "Admin Panel"),
                t("admin.dashboard.title", "Dashboard"),
                t("admin.classes.title", "Manage Classes"),
                t("admin.reports.title", "Attendance Reports"),
                t("admin.users.title", "Manage Users"),
                scroll,
                "fourth",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-reports"); }
                    @Override public void goEmail() { router.go("admin-users"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String localizeRole(String role) {
        if (role == null) return "";

        return switch (role.trim().toUpperCase()) {
            case "STUDENT" -> t("admin.users.filter.student", "Student");
            case "TEACHER" -> t("admin.users.filter.teacher", "Teacher");
            case "ADMIN" -> t("admin.users.filter.admin", "Admin");
            default -> role;
        };
    }

    private String localizeEnrolled(String enrolled) {
        if (enrolled == null || enrolled.isBlank()) {
            return t("common.status.noData", "No data");
        }
        return enrolled;
    }

    private static String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}