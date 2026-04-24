package frontend.student;

import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentDashboardApp {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private static final String UNKNOWN_ERROR = "Unknown error";

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardApp.class.getName());

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        logIfSceneMissing(scene);

        String studentName = StudentPageSupport.resolveStudentName(state, helper);

        VBox page = StudentPageSupport.buildPageContainer();

        Label title = buildTitle(studentName);
        Label subtitle = buildSubtitle();

        javafx.scene.control.Button markAttendanceCard = buildAttendanceCard(router);

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

        return StudentPageSupport.wrapWithSidebar(
                studentName,
                helper,
                scroll,
                "dashboard",
                router,
                jwtStore
        );
    }

    private void logIfSceneMissing(Scene scene) {
        if (scene == null) {
            LOGGER.fine("StudentDashboardApp.build called with a null scene.");
        }
    }

    private String statsHint() {
        return helper.getMessage("student.dashboard.stats.hint");
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

    private javafx.scene.control.Button buildAttendanceCard(AppRouter router) {
        javafx.scene.control.Button button = new javafx.scene.control.Button();
        button.getStyleClass().add("attendance-card");
        button.setMaxWidth(Double.MAX_VALUE);

        HBox content = new HBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        StackPane icon = new StackPane(new Label("✓"));
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(4);

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
                        statsHint(),
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
                        statsHint(),
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
                        statsHint(),
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
                        statsHint(),
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
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label classesTitle = new Label(helper.getMessage("student.dashboard.classes.title"));
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        javafx.scene.control.Button viewAll = new javafx.scene.control.Button(helper.getMessage("student.dashboard.viewAll"));
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> router.go("student-attendance"));

        header.getChildren().addAll(classesTitle, spacer, viewAll);
        return header;
    }

    private VBox buildEmptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);
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
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox(8);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox texts = new VBox(2);
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-label");

        Label hintNode = new Label(hint);
        hintNode.getStyleClass().add("stat-hint");

        texts.getChildren().addAll(labelNode, hintNode);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane(new Label(iconChar));
        badge.getStyleClass().add("stat-badge");
        badge.setStyle("-fx-background-color: " + colorHex + ";");

        top.getChildren().addAll(texts, spacer, badge);

        valueLabel.getStyleClass().add("stat-value");

        card.getChildren().addAll(top, valueLabel);
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

        new Thread(() -> {
            try {
                Map<String, Object> summary = api.getSummary(jwtStore, state);

                Platform.runLater(() -> {
                    presentValue.setText(resolveSummaryValue(summary, "presentCount", false));
                    absentValue.setText(resolveSummaryValue(summary, "absentCount", false));
                    excusedValue.setText(resolveSummaryValue(summary, "excusedCount", false));
                    rateValue.setText(resolveSummaryValue(summary, "attendanceRate", true));
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading student dashboard summary was interrupted.", ex);
                Platform.runLater(() -> resetSummaryValues(
                        presentValue,
                        absentValue,
                        excusedValue,
                        rateValue
                ));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load student dashboard summary.", ex);
                Platform.runLater(() -> resetSummaryValues(
                        presentValue,
                        absentValue,
                        excusedValue,
                        rateValue
                ));
            }
        }).start();
    }

    private void resetSummaryValues(
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label rateValue
    ) {
        presentValue.setText("0");
        absentValue.setText("0");
        excusedValue.setText("0");
        rateValue.setText("0%");
    }

    String resolveSummaryValue(Map<String, Object> summary, String key, boolean percentage) {
        Object value = summary == null ? null : summary.get(key);

        if (percentage) {
            return pct(value);
        }

        return String.valueOf(num(value));
    }

    static int num(Object value) {
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

    static String pct(Object value) {
        if (value == null) {
            return "0%";
        }
        if (value instanceof Number number) {
            return Math.round(number.doubleValue()) + "%";
        }
        try {
            return Math.round(Double.parseDouble(String.valueOf(value))) + "%";
        } catch (Exception ex) {
            return "0%";
        }
    }

    String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return UNKNOWN_ERROR;
        }
        return throwable.getMessage();
    }
}