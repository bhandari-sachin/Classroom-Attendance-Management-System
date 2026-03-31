package frontend.admin;

import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import util.I18n;
import util.RtlUtil;

import java.util.List;
import java.util.Map;

public class AdminPages {

    // ===== Dashboard content =====
    public static Parent dashboardPage(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        VBox content = new VBox(18);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        RtlUtil.apply(content);

        Label title = new Label(I18n.t("admin.dashboard.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(I18n.t("admin.dashboard.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("grid");
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);
        RtlUtil.apply(statsGrid);

        Pane totalClassesCard = AdminUI.makeStatCard(
                I18n.t("admin.dashboard.stats.totalClasses"),
                I18n.t("common.status.loading"),
                "📘",
                "accent-purple"
        );
        Pane studentsCard = AdminUI.makeStatCard(
                I18n.t("admin.dashboard.stats.students"),
                I18n.t("common.status.loading"),
                "🎓",
                "accent-green"
        );
        Pane teachersCard = AdminUI.makeStatCard(
                I18n.t("admin.dashboard.stats.teachers"),
                I18n.t("common.status.loading"),
                "👥",
                "accent-orange"
        );
        Pane rateCard = AdminUI.makeStatCard(
                I18n.t("admin.dashboard.stats.monthlyRate"),
                I18n.t("common.status.loading"),
                "📈",
                "accent-green"
        );

        statsGrid.add(totalClassesCard, 0, 0);
        statsGrid.add(studentsCard, 1, 0);
        statsGrid.add(teachersCard, 0, 1);
        statsGrid.add(rateCard, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        statsGrid.getColumnConstraints().addAll(c1, c2);

        Label qaTitle = new Label(I18n.t("admin.dashboard.quickActions.title"));
        qaTitle.getStyleClass().add("section-title");

        HBox quickActions = new HBox(14);
        quickActions.getStyleClass().add("quick-actions");
        RtlUtil.apply(quickActions);

        Pane manageClasses = AdminUI.makeActionCard(
                I18n.t("admin.dashboard.quickActions.manageClasses"),
                I18n.t("admin.dashboard.quickActions.manageClasses.desc"),
                "📚",
                "qa-green"
        );
        manageClasses.setOnMouseClicked(e -> router.go("admin-classes"));

        Pane manageUsers = AdminUI.makeActionCard(
                I18n.t("admin.dashboard.quickActions.manageUsers"),
                I18n.t("admin.dashboard.quickActions.manageUsers.desc"),
                "👤",
                "qa-purple"
        );
        manageUsers.setOnMouseClicked(e -> router.go("admin-users"));

        Pane reports = AdminUI.makeActionCard(
                I18n.t("admin.dashboard.quickActions.reports"),
                I18n.t("admin.dashboard.quickActions.reports.desc"),
                "🧾",
                "qa-green"
        );
        reports.setOnMouseClicked(e -> router.go("admin-reports"));

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);

        Label rcTitle = new Label(I18n.t("admin.dashboard.recentClasses.title"));
        rcTitle.getStyleClass().add("section-title");

        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);
        RtlUtil.apply(recentGrid);

        ColumnConstraints r1 = new ColumnConstraints();
        r1.setHgrow(Priority.ALWAYS);
        r1.setFillWidth(true);

        ColumnConstraints r2 = new ColumnConstraints();
        r2.setHgrow(Priority.ALWAYS);
        r2.setFillWidth(true);

        recentGrid.getColumnConstraints().addAll(r1, r2);

        Label loadingClasses = new Label(I18n.t("admin.dashboard.recentClasses.loading"));
        loadingClasses.getStyleClass().add("subtitle");
        recentGrid.add(loadingClasses, 0, 0);

        content.getChildren().addAll(title, subtitle, statsGrid, qaTitle, quickActions, rcTitle, recentGrid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        RtlUtil.apply(scroll);

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);

        new Thread(() -> {
            try {
                Map<String, Object> stats = api.getAttendanceStats();
                Map<String, Object> users = api.getAdminUsersRaw();
                List<Map<String, Object>> classes = api.getAdminClassesRaw();

                int totalClasses = classes == null ? 0 : classes.size();
                int students = ((Number) users.getOrDefault("students", 0)).intValue();
                int teachers = ((Number) users.getOrDefault("teachers", 0)).intValue();

                double attendanceRate = 0.0;
                Object rateObj = stats.get("attendanceRate");
                if (rateObj instanceof Number n) {
                    attendanceRate = n.doubleValue();
                } else if (rateObj != null) {
                    try {
                        attendanceRate = Double.parseDouble(String.valueOf(rateObj));
                    } catch (Exception ignored) {
                    }
                }

                double finalAttendanceRate = attendanceRate;

                Platform.runLater(() -> {
                    setStatCardValue(totalClassesCard, String.valueOf(totalClasses));
                    setStatCardValue(studentsCard, String.valueOf(students));
                    setStatCardValue(teachersCard, String.valueOf(teachers));
                    setStatCardValue(rateCard, Math.round(finalAttendanceRate) + "%");

                    recentGrid.getChildren().clear();

                    if (classes == null || classes.isEmpty()) {
                        Label empty = new Label(I18n.t("admin.dashboard.recentClasses.none"));
                        empty.getStyleClass().add("subtitle");
                        recentGrid.add(empty, 0, 0);
                        return;
                    }

                    int max = Math.min(classes.size(), 4);
                    for (int i = 0; i < max; i++) {
                        Map<String, Object> c = classes.get(i);

                        String className = String.valueOf(
                                c.getOrDefault("name", I18n.t("teacher.dashboard.classes.unnamed"))
                        );
                        String code = String.valueOf(c.getOrDefault("classCode", "—"));
                        String teacherEmail = String.valueOf(c.getOrDefault("teacherEmail", "—"));

                        String semester = String.valueOf(c.getOrDefault("semester", ""));
                        String academicYear = String.valueOf(c.getOrDefault("academicYear", ""));
                        String schedule = (semester + " " + academicYear).trim();
                        if (schedule.isBlank()) {
                            schedule = I18n.t("admin.dashboard.recentClasses.noSchedule");
                        }

                        Pane card = AdminUI.makeClassCard(className, code, teacherEmail, schedule);
                        recentGrid.add(card, i % 2, i / 2);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    setStatCardValue(totalClassesCard, I18n.t("common.status.failed"));
                    setStatCardValue(studentsCard, I18n.t("common.status.failed"));
                    setStatCardValue(teachersCard, I18n.t("common.status.failed"));
                    setStatCardValue(rateCard, I18n.t("common.status.failed"));

                    recentGrid.getChildren().clear();
                    Label err = new Label(I18n.t("admin.dashboard.recentClasses.error"));
                    err.getStyleClass().add("subtitle");
                    recentGrid.add(err, 0, 0);
                });
            }
        }).start();

        return scroll;
    }

    private static void setStatCardValue(Pane card, String value) {
        if (card instanceof VBox vbox && !vbox.getChildren().isEmpty()) {
            var top = vbox.getChildren().get(0);
            if (top instanceof HBox topBox && !topBox.getChildren().isEmpty()) {
                var left = topBox.getChildren().get(0);
                if (left instanceof VBox leftBox && leftBox.getChildren().size() >= 2) {
                    var valueNode = leftBox.getChildren().get(1);
                    if (valueNode instanceof Label label) {
                        label.setText(value);
                    }
                }
            }
        }
    }
}