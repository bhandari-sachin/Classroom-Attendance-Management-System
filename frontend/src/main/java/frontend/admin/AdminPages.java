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
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class AdminPages {

    // ===== Dashboard content =====
    public static Parent dashboardPage(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        VBox content = new VBox(18);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Manage your school's attendance system");
        subtitle.getStyleClass().add("subtitle");

        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("grid");
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);

        Pane totalClassesCard = AdminUI.makeStatCard("Total Classes", "Loading...", "📘", "accent-purple");
        Pane studentsCard = AdminUI.makeStatCard("Students", "Loading...", "🎓", "accent-green");
        Pane teachersCard = AdminUI.makeStatCard("Teachers", "Loading...", "👥", "accent-orange");
        Pane rateCard = AdminUI.makeStatCard("Monthly Rate", "Loading...", "📈", "accent-green");

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

        Label qaTitle = new Label("Quick Actions");
        qaTitle.getStyleClass().add("section-title");

        HBox quickActions = new HBox(14);
        quickActions.getStyleClass().add("quick-actions");

        Pane manageClasses = AdminUI.makeActionCard("Manage Classes", "Add, edit or remove classes", "📚", "qa-green");
        manageClasses.setOnMouseClicked(e -> router.go("admin-classes"));

        Pane manageUsers = AdminUI.makeActionCard("Manage Users", "Student registration and details", "👤", "qa-purple");
        manageUsers.setOnMouseClicked(e -> router.go("admin-users"));

        Pane reports = AdminUI.makeActionCard("Attendance Reports", "View comprehensive reports", "🧾", "qa-green");
        reports.setOnMouseClicked(e -> router.go("admin-reports"));

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);

        Label rcTitle = new Label("Recent classes");
        rcTitle.getStyleClass().add("section-title");

        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);

        ColumnConstraints r1 = new ColumnConstraints();
        r1.setHgrow(Priority.ALWAYS);
        r1.setFillWidth(true);

        ColumnConstraints r2 = new ColumnConstraints();
        r2.setHgrow(Priority.ALWAYS);
        r2.setFillWidth(true);

        recentGrid.getColumnConstraints().addAll(r1, r2);

        Label loadingClasses = new Label("Loading classes...");
        loadingClasses.getStyleClass().add("subtitle");
        recentGrid.add(loadingClasses, 0, 0);

        content.getChildren().addAll(title, subtitle, statsGrid, qaTitle, quickActions, rcTitle, recentGrid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

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
                        Label empty = new Label("No classes found");
                        empty.getStyleClass().add("subtitle");
                        recentGrid.add(empty, 0, 0);
                        return;
                    }

                    int max = Math.min(classes.size(), 4);
                    for (int i = 0; i < max; i++) {
                        Map<String, Object> c = classes.get(i);

                        String className = String.valueOf(c.getOrDefault("name", "Unnamed class"));
                        String code = String.valueOf(c.getOrDefault("classCode", "—"));
                        String teacherEmail = String.valueOf(c.getOrDefault("teacherEmail", "—"));

                        String semester = String.valueOf(c.getOrDefault("semester", ""));
                        String academicYear = String.valueOf(c.getOrDefault("academicYear", ""));
                        String schedule = (semester + " " + academicYear).trim();
                        if (schedule.isBlank()) {
                            schedule = "No schedule";
                        }

                        Pane card = AdminUI.makeClassCard(className, code, teacherEmail, schedule);
                        recentGrid.add(card, i % 2, i / 2);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    setStatCardValue(totalClassesCard, "Error");
                    setStatCardValue(studentsCard, "Error");
                    setStatCardValue(teachersCard, "Error");
                    setStatCardValue(rateCard, "Error");

                    recentGrid.getChildren().clear();
                    Label err = new Label("Failed to load dashboard data: " + e.getMessage());
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