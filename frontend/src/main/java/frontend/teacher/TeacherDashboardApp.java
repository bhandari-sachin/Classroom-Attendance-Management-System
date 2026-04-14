package frontend.teacher;

import frontend.AppLayout;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TeacherDashboardApp {

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = resolveTeacherName(state);

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = buildPageContainer();

        Label greeting = buildGreetingLabel(teacherName);
        Label dateLabel = buildDateLabel();

        HBox actionsRow = buildActionsRow(router);

        VBox myClassesCard = statCard(helper.getMessage("teacher.dashboard.stats.classes"), 0);
        VBox totalStudentsCard = statCard(helper.getMessage("teacher.dashboard.stats.students"), 0);
        VBox presentTodayCard = statCard(helper.getMessage("teacher.dashboard.stats.present"), 0);
        VBox absentTodayCard = statCard(helper.getMessage("teacher.dashboard.stats.absent"), 0);

        GridPane statsGrid = buildStatsGrid(
                myClassesCard,
                totalStudentsCard,
                presentTodayCard,
                absentTodayCard
        );

        Label classesTitle = buildClassesTitle();
        VBox classesContainer = buildClassesContainer();

        page.getChildren().addAll(
                greeting,
                dateLabel,
                actionsRow,
                statsGrid,
                new Separator(),
                classesTitle,
                classesContainer
        );

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        loadDashboardStats(
                api,
                jwtStore,
                state,
                myClassesCard,
                totalStudentsCard,
                presentTodayCard,
                absentTodayCard
        );

        loadTeacherClasses(api, jwtStore, state, classesContainer);

        return AppLayout.wrapWithSidebar(
                teacherName,
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email"),
                scroll,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("teacher-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("teacher-take");
                    }

                    @Override
                    public void goReports() {
                        router.go("teacher-reports");
                    }

                    @Override
                    public void goEmail() {
                        router.go("teacher-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveTeacherName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        return page;
    }

    private Label buildGreetingLabel(String teacherName) {
        Label greeting = new Label(
                helper.getMessage("teacher.dashboard.greeting").replace("{name}", teacherName)
        );
        greeting.getStyleClass().add("dash-title");
        return greeting;
    }

    private Label buildDateLabel() {
        String formattedDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("dash-subtitle");
        return dateLabel;
    }

    private HBox buildActionsRow(AppRouter router) {
        HBox actionsRow = new HBox(14);
        actionsRow.getStyleClass().add("dash-actions");

        VBox takeAttendanceCard = bigActionCard(
                helper.getMessage("teacher.dashboard.takeAttendance.title"),
                helper.getMessage("teacher.dashboard.takeAttendance.subtitle"),
                "card-green",
                () -> router.go("teacher-take")
        );

        VBox reportsCard = bigActionCard(
                helper.getMessage("teacher.dashboard.reports.title"),
                helper.getMessage("teacher.dashboard.reports.subtitle"),
                "card-purple",
                () -> router.go("teacher-reports")
        );

        HBox.setHgrow(takeAttendanceCard, Priority.ALWAYS);
        HBox.setHgrow(reportsCard, Priority.ALWAYS);

        actionsRow.getChildren().addAll(takeAttendanceCard, reportsCard);
        return actionsRow;
    }

    private GridPane buildStatsGrid(
            VBox myClassesCard,
            VBox totalStudentsCard,
            VBox presentTodayCard,
            VBox absentTodayCard
    ) {
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);
        statsGrid.getStyleClass().add("dash-stats");

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        firstColumn.setFillWidth(true);

        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.ALWAYS);
        secondColumn.setFillWidth(true);

        statsGrid.getColumnConstraints().addAll(firstColumn, secondColumn);

        statsGrid.add(myClassesCard, 0, 0);
        statsGrid.add(totalStudentsCard, 1, 0);
        statsGrid.add(presentTodayCard, 0, 1);
        statsGrid.add(absentTodayCard, 1, 1);

        return statsGrid;
    }

    private Label buildClassesTitle() {
        Label classesTitle = new Label(helper.getMessage("teacher.dashboard.classes.title"));
        classesTitle.getStyleClass().add("section-title");
        return classesTitle;
    }

    private VBox buildClassesContainer() {
        VBox classesContainer = new VBox(10);
        classesContainer.getStyleClass().add("classes-card");
        classesContainer.setPadding(new Insets(16));
        classesContainer.setMinHeight(170);
        classesContainer.setAlignment(Pos.CENTER);

        Label loading = new Label(helper.getMessage("teacher.dashboard.classes.loading"));
        loading.getStyleClass().add("empty-subtitle");

        classesContainer.getChildren().add(loading);
        return classesContainer;
    }

    private void loadDashboardStats(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            VBox myClassesCard,
            VBox totalStudentsCard,
            VBox presentTodayCard,
            VBox absentTodayCard
    ) {
        new Thread(() -> {
            try {
                Map<String, Object> response = api.getDashboardStats(jwtStore, state);

                int totalClasses = toInt(response.get("totalClasses"));
                int totalStudents = toInt(response.get("totalStudents"));
                int presentToday = toInt(response.get("presentToday"));
                int absentToday = toInt(response.get("absentToday"));

                Platform.runLater(() -> {
                    setStatValue(myClassesCard, totalClasses);
                    setStatValue(totalStudentsCard, totalStudents);
                    setStatValue(presentTodayCard, presentToday);
                    setStatValue(absentTodayCard, absentToday);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError(
                                helper.getMessage("teacher.dashboard.error.stats")
                                        .replace("{error}", ex.getMessage())
                        )
                );
            }
        }).start();
    }

    private void loadTeacherClasses(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            VBox classesContainer
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = api.getMyClasses(jwtStore, state);
                Platform.runLater(() -> renderTeacherClasses(classesContainer, classes));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showClassesError(classesContainer, ex.getMessage()));
            }
        }).start();
    }

    private void renderTeacherClasses(VBox classesContainer, List<Map<String, Object>> classes) {
        classesContainer.getChildren().clear();

        if (classes == null || classes.isEmpty()) {
            showEmptyClasses(classesContainer);
            return;
        }

        classesContainer.setAlignment(Pos.TOP_LEFT);

        for (Map<String, Object> classData : classes) {
            String classCode = valueOr(classData.get("classCode"), "—");
            String name = valueOr(
                    classData.get("name"),
                    helper.getMessage("teacher.dashboard.classes.unnamed")
            );
            String semester = valueOr(classData.get("semester"), "—");
            String academicYear = valueOr(classData.get("academicYear"), "—");
            String studentsCount = valueOr(classData.get("studentsCount"), "0");

            classesContainer.getChildren().add(
                    classRow(classCode, name, semester, academicYear, studentsCount)
            );
        }
    }

    private void showEmptyClasses(VBox classesContainer) {
        classesContainer.setAlignment(Pos.CENTER);

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");

        Label title = new Label(helper.getMessage("teacher.dashboard.classes.empty.title"));
        title.getStyleClass().add("empty-title");

        Label subtitle = new Label(helper.getMessage("teacher.dashboard.classes.empty.subtitle"));
        subtitle.getStyleClass().add("empty-subtitle");

        classesContainer.getChildren().addAll(icon, title, subtitle);
    }

    private void showClassesError(VBox classesContainer, String errorMessage) {
        classesContainer.getChildren().clear();
        classesContainer.setAlignment(Pos.CENTER);

        Label error = new Label(
                helper.getMessage("teacher.dashboard.error.classes")
                        .replace("{error}", errorMessage)
        );
        error.getStyleClass().add("empty-subtitle");

        classesContainer.getChildren().add(error);
    }

    private void setStatValue(VBox statCard, int value) {
        if (statCard.getChildren().size() >= 2 && statCard.getChildren().get(1) instanceof Label valueLabel) {
            valueLabel.setText(String.valueOf(value));
        }
    }

    private VBox bigActionCard(String title, String subtitle, String styleClass, Runnable action) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("big-card", styleClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("big-card-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("big-card-subtitle");

        card.getChildren().addAll(titleLabel, subtitleLabel);
        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    private VBox statCard(String label, int value) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stat-card", "dashboard-card");
        card.setPadding(new Insets(16));

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(resolveIcon(label));
        icon.setStyle("-fx-font-size: 18px;");

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-label");

        top.getChildren().addAll(icon, labelNode);

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("stat-value");

        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    private VBox classRow(
            String classCode,
            String name,
            String semester,
            String academicYear,
            String studentsCount
    ) {
        VBox box = new VBox(4);
        box.getStyleClass().add("class-item");
        box.setPadding(new Insets(12));

        Label title = new Label(classCode + " — " + name);
        title.getStyleClass().add("class-title");

        Label meta = new Label(
                helper.getMessage("teacher.dashboard.classes.meta")
                        .replace("{semester}", semester)
                        .replace("{year}", academicYear)
                        .replace("{count}", studentsCount)
        );
        meta.getStyleClass().add("class-meta");

        box.getChildren().addAll(title, meta);
        return box;
    }

    private String resolveIcon(String label) {
        if (label.equals(helper.getMessage("teacher.dashboard.stats.classes"))) {
            return "📚";
        }
        if (label.equals(helper.getMessage("teacher.dashboard.stats.students"))) {
            return "👥";
        }
        if (label.equals(helper.getMessage("teacher.dashboard.stats.present"))) {
            return "✅";
        }
        if (label.equals(helper.getMessage("teacher.dashboard.stats.absent"))) {
            return "❌";
        }
        return "📊";
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
        } catch (Exception e) {
            return 0;
        }
    }

    private static String valueOr(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}