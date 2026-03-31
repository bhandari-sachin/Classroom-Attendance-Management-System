package frontend.student;

import frontend.AppLayout;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

import java.util.Map;

public class StudentDashboardApp {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(
                I18n.t("student.dashboard.title").replace("{name}", studentName)
        );
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label(I18n.t("student.dashboard.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");

        Button markAttendance = attendanceCard(router);

        Label presentValue = new Label("0");
        Label absentValue = new Label("0");
        Label excusedValue = new Label("0");
        Label rateValue = new Label("0%");

        GridPane stats = statsGrid(presentValue, absentValue, excusedValue, rateValue);

        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(classesHeader);

        Label classesTitle = new Label(I18n.t("student.dashboard.classes.title"));
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button(I18n.t("student.dashboard.viewAll"));
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
                I18n.t("student.panel.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("student.nav.markAttendance"),
                I18n.t("student.nav.myAttendance"),
                I18n.t("student.nav.email"),
                I18n.t("student.nav.logout"),
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
                },
                router,
                I18n.isRtl()
        );
    }

    private void loadStudentSummary(JwtStore jwtStore,
                                    AuthState state,
                                    Label presentValue,
                                    Label absentValue,
                                    Label excusedValue,
                                    Label rateValue) {

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
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private static double dbl(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private Button attendanceCard(AppRouter router) {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(box);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);
        RtlUtil.apply(texts);

        Label big = new Label(I18n.t("student.dashboard.markAttendance.title"));
        big.getStyleClass().add("attendance-title");

        Label small = new Label(I18n.t("student.dashboard.markAttendance.subtitle"));
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
        RtlUtil.apply(grid);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        grid.getColumnConstraints().addAll(c1, c2);

        String monthHint = I18n.t("student.dashboard.stats.hint");

        grid.add(statCardWithBadge(I18n.t("student.dashboard.stats.present"), presentValue, monthHint, "#3BAA66", "✓"), 0, 0);
        grid.add(statCardWithBadge(I18n.t("student.dashboard.stats.absent"), absentValue, monthHint, "#E05A5A", "✕"), 1, 0);
        grid.add(statCardWithBadge(I18n.t("student.dashboard.stats.excused"), excusedValue, monthHint, "#E09A3B", "⏱"), 0, 1);
        grid.add(statCardWithBadge(I18n.t("student.dashboard.stats.rate"), rateValue, monthHint, "#5AA6E0", "%"), 1, 1);

        return grid;
    }

    private VBox statCardWithBadge(String label, Label valueLabel, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #ECEFF2;" +
                        "-fx-border-radius: 12;"
        );
        RtlUtil.apply(card);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(top);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #4B5563;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 10;"
        );

        Label icon = new Label(iconChar);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(lbl, spacer, badge);

        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900;");

        Label small = new Label(hint);
        small.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        card.getChildren().addAll(top, valueLabel, small);
        return card;
    }

    private VBox emptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);
        RtlUtil.apply(card);

        Label cal = new Label("📅");
        cal.getStyleClass().add("empty-icon");

        Label t = new Label(I18n.t("student.dashboard.classes.empty.title"));
        t.getStyleClass().add("empty-title");

        Label s = new Label(I18n.t("student.dashboard.classes.empty.subtitle"));
        s.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(cal, t, s);
        return card;
    }
}