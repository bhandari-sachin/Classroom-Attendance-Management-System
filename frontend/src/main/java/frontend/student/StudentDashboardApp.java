package frontend.student;

import frontend.AppLayout;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class StudentDashboardApp {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String studentName = resolveStudentName(state);

        VBox page = buildPageContainer();

        Label title = buildTitle(studentName);
        Label subtitle = buildSubtitle();

        Button markAttendanceCard = buildAttendanceCard(router);

        Label presentValue = createStatValueLabel();
        Label absentValue = createStatValueLabel();
        Label excusedValue = createStatValueLabel();
        Label rateValue = createRateValueLabel();

        GridPane statsGrid = buildStatsGrid(
                presentValue,
                absentValue,
                excusedValue,
                rateValue
        );

        HBox classesHeader = buildClassesHeader(router);
        VBox classesCard = buildEmptyClassesCard();

        page.getChildren().addAll(
                title,
                subtitle,
                markAttendanceCard,
                statsGrid,
                new Separator(),
                classesHeader,
                classesCard
        );

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        loadStudentSummary(
                jwtStore,
                state,
                presentValue,
                absentValue,
                excusedValue,
                rateValue
        );

        return AppLayout.wrapWithSidebar(
                studentName,
                helper.getMessage("student.panel.title"),
                helper.getMessage("student.nav.dashboard"),
                helper.getMessage("student.nav.markAttendance"),
                helper.getMessage("student.nav.myAttendance"),
                helper.getMessage("student.nav.email"),
                scroll,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("student-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("student-mark");
                    }

                    @Override
                    public void goReports() {
                        router.go("student-attendance");
                    }

                    @Override
                    public void goEmail() {
                        router.go("student-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveStudentName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("student.name.placeholder")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        return page;
    }

    private Label buildTitle(String studentName) {
        Label title = new Label(
                helper.getMessage("student.dashboard.title").replace("{name}", studentName)
        );
        title.getStyleClass().add("dash-title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("student.dashboard.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");
        return subtitle;
    }

    private Label createStatValueLabel() {
        return new Label("0");
    }

    private Label createRateValueLabel() {
        return new Label("0%");
    }

    private Button buildAttendanceCard(AppRouter router) {
        Button button = new Button();
        button.getStyleClass().add("attendance-card");
        button.setMaxWidth(Double.MAX_VALUE);

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);

        Label big = new Label(helper.getMessage("student.dashboard.markAttendance.title"));
        big.getStyleClass().add("attendance-title");

        Label small = new Label(helper.getMessage("student.dashboard.markAttendance.subtitle"));
        small.getStyleClass().add("attendance-subtitle");

        texts.getChildren().addAll(big, small);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("attendance-arrow");

        content.getChildren().addAll(icon, texts, spacer, arrow);

        button.setGraphic(content);
        button.setOnAction(e -> router.go("student-mark"));

        return button;
    }

    private GridPane buildStatsGrid(
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label rateValue
    ) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.getStyleClass().add("dash-stats");

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        firstColumn.setFillWidth(true);

        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.ALWAYS);
        secondColumn.setFillWidth(true);

        grid.getColumnConstraints().addAll(firstColumn, secondColumn);

        grid.add(
                statCardWithBadge(
                        helper.getMessage("student.dashboard.stats.present"),
                        presentValue,
                        helper.getMessage("student.dashboard.stats.hint"),
                        "#3BAA66",
                        "✓"
                ),
                0,
                0
        );

        grid.add(
                statCardWithBadge(
                        helper.getMessage("student.dashboard.stats.absent"),
                        absentValue,
                        helper.getMessage("student.dashboard.stats.hint"),
                        "#E05A5A",
                        "✕"
                ),
                1,
                0
        );

        grid.add(
                statCardWithBadge(
                        helper.getMessage("student.dashboard.stats.excused"),
                        excusedValue,
                        helper.getMessage("student.dashboard.stats.hint"),
                        "#E09A3B",
                        "⏱"
                ),
                0,
                1
        );

        grid.add(
                statCardWithBadge(
                        helper.getMessage("student.dashboard.rate"),
                        rateValue,
                        helper.getMessage("student.dashboard.stats.hint"),
                        "#5AA6E0",
                        "%"
                ),
                1,
                1
        );

        return grid;
    }

    private HBox buildClassesHeader(AppRouter router) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label(helper.getMessage("student.dashboard.classes.title"));
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button(helper.getMessage("student.dashboard.viewAll"));
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> router.go("student-attendance"));

        header.getChildren().addAll(classesTitle, spacer, viewAll);
        return header;
    }

    private VBox buildEmptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");

        Label title = new Label(helper.getMessage("student.dashboard.classes.empty"));
        title.getStyleClass().add("empty-title");

        Label subtitle = new Label(helper.getMessage("student.dashboard.classes.empty.subtitle"));
        subtitle.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(icon, title, subtitle);
        return card;
    }

    private VBox statCardWithBadge(
            String label,
            Label valueLabel,
            String hint,
            String colorHex,
            String iconChar
    ) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.getStyleClass().addAll("dashboard-card", "student-stat-card");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("dashboard-card-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 10;");

        Label icon = new Label(iconChar);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(labelNode, spacer, badge);

        valueLabel.getStyleClass().add("dashboard-card-value");

        Label hintLabel = new Label(hint);
        hintLabel.getStyleClass().add("dashboard-card-hint");

        card.getChildren().addAll(top, valueLabel, hintLabel);
        return card;
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
                Map<String, Object> summary = getValue();
                int present = num(summary.get("presentCount"));
                int absent = num(summary.get("absentCount"));
                int excused = num(summary.get("excusedCount"));
                double rate = dbl(summary.get("attendanceRate"));

                Platform.runLater(() -> {
                    presentValue.setText(String.valueOf(present));
                    absentValue.setText(String.valueOf(absent));
                    excusedValue.setText(String.valueOf(excused));
                    rateValue.setText(Math.round(rate) + "%");
                });
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                Platform.runLater(() -> rateValue.setText("—"));
                if (exception != null) {
                    exception.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private static int num(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private static double dbl(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }
}