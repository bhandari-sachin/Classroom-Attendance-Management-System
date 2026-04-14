package frontend.admin;

import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class AdminPages {

    public static Parent dashboardPage(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        HelperClass helper = new HelperClass();

        VBox content = buildContentContainer();
        Label title = buildTitle(helper);
        Label subtitle = buildSubtitle(helper);

        Pane totalClassesCard = AdminUI.makeStatCard(
                helper.getMessage("admin.dashboard.stats.totalClasses"),
                helper.getMessage("common.status.loading"),
                "📘",
                "accent-purple"
        );

        Pane studentsCard = AdminUI.makeStatCard(
                helper.getMessage("admin.dashboard.stats.students"),
                helper.getMessage("common.status.loading"),
                "🎓",
                "accent-green"
        );

        Pane teachersCard = AdminUI.makeStatCard(
                helper.getMessage("admin.dashboard.stats.teachers"),
                helper.getMessage("common.status.loading"),
                "👥",
                "accent-orange"
        );

        Pane rateCard = AdminUI.makeStatCard(
                helper.getMessage("admin.dashboard.stats.monthlyRate"),
                helper.getMessage("common.status.loading"),
                "📈",
                "accent-green"
        );

        GridPane statsGrid = buildStatsGrid(totalClassesCard, studentsCard, teachersCard, rateCard);

        Label quickActionsTitle = buildQuickActionsTitle(helper);
        HBox quickActions = buildQuickActions(helper, router);

        Label recentClassesTitle = buildRecentClassesTitle(helper);
        GridPane recentGrid = buildRecentGrid(helper);

        content.getChildren().addAll(
                title,
                subtitle,
                statsGrid,
                quickActionsTitle,
                quickActions,
                recentClassesTitle,
                recentGrid
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);

        loadDashboardData(
                api,
                helper,
                totalClassesCard,
                studentsCard,
                teachersCard,
                rateCard,
                recentGrid
        );

        return scroll;
    }

    private static VBox buildContentContainer() {
        VBox content = new VBox(18);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        return content;
    }

    private static Label buildTitle(HelperClass helper) {
        Label title = new Label(helper.getMessage("admin.dashboard.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private static Label buildSubtitle(HelperClass helper) {
        Label subtitle = new Label(helper.getMessage("admin.dashboard.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private static GridPane buildStatsGrid(
            Pane totalClassesCard,
            Pane studentsCard,
            Pane teachersCard,
            Pane rateCard
    ) {
        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("grid");
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        firstColumn.setFillWidth(true);

        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.ALWAYS);
        secondColumn.setFillWidth(true);

        statsGrid.getColumnConstraints().addAll(firstColumn, secondColumn);

        statsGrid.add(totalClassesCard, 0, 0);
        statsGrid.add(studentsCard, 1, 0);
        statsGrid.add(teachersCard, 0, 1);
        statsGrid.add(rateCard, 1, 1);

        return statsGrid;
    }

    private static Label buildQuickActionsTitle(HelperClass helper) {
        Label quickActionsTitle = new Label(helper.getMessage("admin.dashboard.quickActions.title"));
        quickActionsTitle.getStyleClass().add("section-title");
        return quickActionsTitle;
    }

    private static HBox buildQuickActions(HelperClass helper, AppRouter router) {
        HBox quickActions = new HBox(14);
        quickActions.getStyleClass().add("quick-actions");

        Pane manageClasses = AdminUI.makeActionCard(
                helper.getMessage("admin.dashboard.quickActions.manageClasses"),
                helper.getMessage("admin.dashboard.quickActions.manageClasses.desc"),
                "📚",
                "qa-green"
        );
        manageClasses.setOnMouseClicked(e -> router.go("admin-classes"));

        Pane manageUsers = AdminUI.makeActionCard(
                helper.getMessage("admin.dashboard.quickActions.manageUsers"),
                helper.getMessage("admin.dashboard.quickActions.manageUsers.desc"),
                "👤",
                "qa-purple"
        );
        manageUsers.setOnMouseClicked(e -> router.go("admin-users"));

        Pane reports = AdminUI.makeActionCard(
                helper.getMessage("admin.dashboard.quickActions.reports"),
                helper.getMessage("admin.dashboard.quickActions.reports.desc"),
                "🧾",
                "qa-green"
        );
        reports.setOnMouseClicked(e -> router.go("admin-reports"));

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);
        return quickActions;
    }

    private static Label buildRecentClassesTitle(HelperClass helper) {
        Label recentClassesTitle = new Label(helper.getMessage("admin.dashboard.recentClasses.title"));
        recentClassesTitle.getStyleClass().add("section-title");
        return recentClassesTitle;
    }

    private static GridPane buildRecentGrid(HelperClass helper) {
        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        firstColumn.setFillWidth(true);

        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.ALWAYS);
        secondColumn.setFillWidth(true);

        recentGrid.getColumnConstraints().addAll(firstColumn, secondColumn);

        Label loadingClasses = new Label(helper.getMessage("admin.dashboard.recentClasses.loading"));
        loadingClasses.getStyleClass().add("subtitle");
        recentGrid.add(loadingClasses, 0, 0);

        return recentGrid;
    }

    private static void loadDashboardData(
            AdminApi api,
            HelperClass helper,
            Pane totalClassesCard,
            Pane studentsCard,
            Pane teachersCard,
            Pane rateCard,
            GridPane recentGrid
    ) {
        new Thread(() -> {
            try {
                Map<String, Object> stats = api.getAttendanceStats();
                Map<String, Object> users = api.getAdminUsersRaw();
                List<Map<String, Object>> classes = api.getAdminClassesRaw();

                int totalClasses = classes == null ? 0 : classes.size();
                int students = toInt(users.get("students"));
                int teachers = toInt(users.get("teachers"));
                double attendanceRate = toDouble(stats.get("attendanceRate"));

                Platform.runLater(() -> {
                    setStatCardValue(totalClassesCard, String.valueOf(totalClasses));
                    setStatCardValue(studentsCard, String.valueOf(students));
                    setStatCardValue(teachersCard, String.valueOf(teachers));
                    setStatCardValue(rateCard, Math.round(attendanceRate) + "%");

                    renderRecentClasses(helper, recentGrid, classes);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showDashboardLoadError(
                        helper,
                        totalClassesCard,
                        studentsCard,
                        teachersCard,
                        rateCard,
                        recentGrid,
                        ex.getMessage()
                ));
            }
        }).start();
    }

    private static void renderRecentClasses(
            HelperClass helper,
            GridPane recentGrid,
            List<Map<String, Object>> classes
    ) {
        recentGrid.getChildren().clear();

        if (classes == null || classes.isEmpty()) {
            Label empty = new Label(helper.getMessage("admin.dashboard.recentClasses.none"));
            empty.getStyleClass().add("subtitle");
            recentGrid.add(empty, 0, 0);
            return;
        }

        int max = Math.min(classes.size(), 4);

        for (int i = 0; i < max; i++) {
            Map<String, Object> classData = classes.get(i);

            String className = valueOr(classData.get("name"), "Unnamed class");
            String code = valueOr(classData.get("classCode"), "—");
            String teacherEmail = valueOr(classData.get("teacherEmail"), "—");
            String semester = valueOr(classData.get("semester"), "");
            String academicYear = valueOr(classData.get("academicYear"), "");

            String schedule = (semester + " " + academicYear).trim();
            if (schedule.isBlank()) {
                schedule = helper.getMessage("admin.dashboard.recentClasses.noSchedule");
            }

            Pane card = AdminUI.makeClassCard(className, code, teacherEmail, schedule);
            recentGrid.add(card, i % 2, i / 2);
        }
    }

    private static void showDashboardLoadError(
            HelperClass helper,
            Pane totalClassesCard,
            Pane studentsCard,
            Pane teachersCard,
            Pane rateCard,
            GridPane recentGrid,
            String errorMessage
    ) {
        setStatCardValue(totalClassesCard, helper.getMessage("common.status.failed"));
        setStatCardValue(studentsCard, helper.getMessage("common.status.failed"));
        setStatCardValue(teachersCard, helper.getMessage("common.status.failed"));
        setStatCardValue(rateCard, helper.getMessage("common.status.failed"));

        recentGrid.getChildren().clear();

        Label error = new Label(
                helper.getMessage("admin.dashboard.recentClasses.error") + " " + errorMessage
        );
        error.getStyleClass().add("subtitle");
        recentGrid.add(error, 0, 0);
    }

    private static void setStatCardValue(Pane card, String value) {
        if (card instanceof VBox vbox && !vbox.getChildren().isEmpty()) {
            Object top = vbox.getChildren().get(0);
            if (top instanceof HBox topBox && !topBox.getChildren().isEmpty()) {
                Object left = topBox.getChildren().get(0);
                if (left instanceof VBox leftBox && leftBox.getChildren().size() >= 2) {
                    Object valueNode = leftBox.getChildren().get(1);
                    if (valueNode instanceof Label label) {
                        label.setText(value);
                    }
                }
            }
        }
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return 0;
        }
    }

    private static double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private static String valueOr(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}