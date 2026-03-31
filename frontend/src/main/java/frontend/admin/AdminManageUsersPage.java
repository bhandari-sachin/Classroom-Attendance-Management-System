package frontend.admin;

import frontend.UserRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminUserDto;
import frontend.dto.AdminUsersResponseDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

public class AdminManageUsersPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        RtlUtil.apply(content);

        Label title = new Label(I18n.t("admin.users.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(I18n.t("admin.users.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        // Summary row (dynamic)
        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");
        RtlUtil.apply(summary);

        // Filters
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(filters);

        TextField search = new TextField();
        search.setPromptText(I18n.t("admin.users.search.placeholder"));
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);
        RtlUtil.apply(search);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                I18n.t("admin.users.filter.allTypes"),
                I18n.t("admin.users.filter.student"),
                I18n.t("admin.users.filter.teacher"),
                I18n.t("admin.users.filter.admin")
        );
        type.setValue(I18n.t("admin.users.filter.allTypes"));
        type.getStyleClass().add("filter-combo");
        RtlUtil.apply(type);

        filters.getChildren().addAll(search, type);

        // Table
        TableView<UserRow> table = AdminUI.buildUsersTable();
        table.getItems().clear();
        RtlUtil.apply(table);

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

                if (I18n.t("admin.users.filter.student").equals(t)) {
                    matchType = "STUDENT".equalsIgnoreCase(r.typeProperty().get());
                }
                if (I18n.t("admin.users.filter.teacher").equals(t)) {
                    matchType = "TEACHER".equalsIgnoreCase(r.typeProperty().get());
                }
                if (I18n.t("admin.users.filter.admin").equals(t)) {
                    matchType = "ADMIN".equalsIgnoreCase(r.typeProperty().get());
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
                                        I18n.t("admin.users.summary.students"),
                                        String.valueOf(data.students),
                                        "🎓",
                                        "accent-green"
                                ),
                                AdminUI.smallSummaryCard(
                                        I18n.t("admin.users.summary.teachers"),
                                        String.valueOf(data.teachers),
                                        "👥",
                                        "accent-purple"
                                ),
                                AdminUI.smallSummaryCard(
                                        I18n.t("admin.users.summary.admins"),
                                        String.valueOf(data.admins),
                                        "🛡",
                                        "accent-orange"
                                )
                        );

                        rows.clear();
                        if (data.users != null) {
                            for (AdminUserDto u : data.users) {
                                String userCell = (u.name == null ? "" : u.name) + "\n" + (u.email == null ? "" : u.email);
                                rows.add(new UserRow(
                                        userCell,
                                        u.role == null ? "" : localizeRole(u.role),
                                        u.enrolled == null ? "-" : u.enrolled
                                ));
                            }
                        }

                        applyFilter.run();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        loadError.setText(I18n.t("admin.users.loadError"));
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
        RtlUtil.apply(scroll);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                I18n.t("admin.dashboard.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("admin.classes.title"),
                I18n.t("admin.users.title"),
                I18n.t("admin.reports.title"),
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
                }, router
        );
    }

    private static String localizeRole(String role) {
        if (role == null) return "";
        return switch (role.toUpperCase()) {
            case "STUDENT" -> I18n.t("admin.users.filter.student");
            case "TEACHER" -> I18n.t("admin.users.filter.teacher");
            case "ADMIN" -> I18n.t("admin.users.filter.admin");
            default -> role;
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}