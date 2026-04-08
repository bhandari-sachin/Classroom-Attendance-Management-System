package frontend.student;

import frontend.AppLayout;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.Map;

public class StudentDashboardApp {

    private static final String BASE_URL = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String studentName = (state.getName() == null || state.getName().isBlank())
                ? t("student.name.placeholder", "Name")
                : state.getName();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        Label title = new Label(
                t("student.dashboard.title", "Welcome, {name}").replace("{name}", studentName)
        );
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label(t("student.dashboard.subtitle", "Track your attendance and manage your classes"));
        subtitle.getStyleClass().add("dash-subtitle");

        Button markAttendance = attendanceCard(router);

        Label presentValue = new Label("0");
        Label absentValue = new Label("0");
        Label excusedValue = new Label("0");
        Label rateValue = new Label("0%");

        GridPane stats = statsGrid(presentValue, absentValue, excusedValue, rateValue);

        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label(t("student.dashboard.classes.title", "My Classes"));
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button(t("student.dashboard.viewAll", "View All"));
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> router.go("student-attendance"));

        classesHeader.getChildren().addAll(classesTitle, spacer, viewAll);

        VBox classesCard = emptyClassesCard();

        page.getChildren().addAll(
                title,
                subtitle,
                markAttendance,
                stats,
                new Separator(),
                classesHeader,
                classesCard
        );

        loadStudentSummary(jwtStore, state, presentValue, absentValue, excusedValue, rateValue);

        return AppLayout.wrapWithSidebar(
                studentName,
                t("student.panel.title", "Student Panel"),
                t("student.nav.dashboard", "Dashboard"),
                t("student.nav.markAttendance", "Mark Attendance"),
                t("student.nav.myAttendance", "My Attendance"),
                t("student.nav.email", "Email"),
                page,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("student-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("student-mark"); }
                    @Override public void goReports() { router.go("student-attendance"); }
                    @Override public void goEmail() { router.go("student-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private void loadStudentSummary(
            JwtStore jwtStore,
            AuthState state,
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label rateValue
    ) {
        StudentAttendanceApi api = new StudentAttendanceApi(BASE_URL);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return api.getSummary(jwtStore, state);
            }

            @Override
            protected void succeeded() {
                Map<String, Object> s = getValue();
                int present = num(s.get("presentCount"));
                int absent = num(s.get("absentCount"));
                int excused = num(s.get("excusedCount"));
                double rate = dbl(s.get("attendanceRate"));

                Platform.runLater(() -> {
                    presentValue.setText(String.valueOf(present));
                    absentValue.setText(String.valueOf(absent));
                    excusedValue.setText(String.valueOf(excused));
                    rateValue.setText(((int) Math.round(rate)) + "%");
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                Platform.runLater(() -> rateValue.setText("—"));
                if (e != null) e.printStackTrace();
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private static int num(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    private static double dbl(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    private Button attendanceCard(AppRouter router) {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);

        Label big = new Label(t("student.dashboard.markAttendance.title", "Mark Attendance"));
        big.getStyleClass().add("attendance-title");

        Label small = new Label(t("student.dashboard.markAttendance.subtitle", "Scan the QR code to mark your attendance"));
        small.getStyleClass().add("attendance-subtitle");

        texts.getChildren().addAll(big, small);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("attendance-arrow");

        box.getChildren().addAll(icon, texts, spacer, arrow);

        btn.setGraphic(box);
        btn.setOnAction(e -> router.go("student-mark"));
        return btn;
    }

    private GridPane statsGrid(Label presentValue, Label absentValue, Label excusedValue, Label rateValue) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.getStyleClass().add("dash-stats");

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(statCardWithBadge(
                t("student.dashboard.stats.present", "Present"),
                presentValue,
                t("student.dashboard.stats.hint", "Updated from latest records"),
                "#3BAA66",
                "✓"
        ), 0, 0);

        grid.add(statCardWithBadge(
                t("student.dashboard.stats.absent", "Absent"),
                absentValue,
                t("student.dashboard.stats.hint", "Updated from latest records"),
                "#E05A5A",
                "✕"
        ), 1, 0);

        grid.add(statCardWithBadge(
                t("student.dashboard.stats.excused", "Excused"),
                excusedValue,
                t("student.dashboard.stats.hint", "Updated from latest records"),
                "#E09A3B",
                "⏱"
        ), 0, 1);

        grid.add(statCardWithBadge(
                t("student.attendance.stats.rate", "Attendance Rate"),
                rateValue,
                t("student.dashboard.stats.hint", "Updated from latest records"),
                "#5AA6E0",
                "%"
        ), 1, 1);

        return grid;
    }

    private VBox statCardWithBadge(String label, Label valueLabel, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.getStyleClass().addAll("dashboard-card", "student-stat-card");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("dashboard-card-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 10;");

        Label icon = new Label(iconChar);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(lbl, spacer, badge);

        valueLabel.getStyleClass().add("dashboard-card-value");

        Label small = new Label(hint);
        small.getStyleClass().add("dashboard-card-hint");

        card.getChildren().addAll(top, valueLabel, small);
        return card;
    }

    private VBox emptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);

        Label cal = new Label("📅");
        cal.getStyleClass().add("empty-icon");

        Label t = new Label(t("student.dashboard.classes.empty.title", "No classes yet"));
        t.getStyleClass().add("empty-title");

        Label s = new Label(t("student.dashboard.classes.empty.subtitle", "Your enrolled classes will appear here"));
        s.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(cal, t, s);
        return card;
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}