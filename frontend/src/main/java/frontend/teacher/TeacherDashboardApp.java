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
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TeacherDashboardApp {

    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        Label greeting = new Label(
                helper.getMessage("teacher.dashboard.greeting").replace("{name}", teacherName)
        );
        greeting.getStyleClass().add("dash-title");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("dash-subtitle");

        HBox actionsRow = new HBox(14);
        actionsRow.getStyleClass().add("dash-actions");

        VBox takeCard = bigActionCard(
                helper.getMessage("teacher.dashboard.takeAttendance.title"),
                helper.getMessage("teacher.dashboard.takeAttendance.subtitle"),
                "card-green",
                () -> router.go("teacher-take")
        );

        VBox reportCard = bigActionCard(
                helper.getMessage("teacher.dashboard.reports.title"),
                helper.getMessage("teacher.dashboard.reports.subtitle"),
                "card-purple",
                () -> router.go("teacher-reports")
        );

        HBox.setHgrow(takeCard, Priority.ALWAYS);
        HBox.setHgrow(reportCard, Priority.ALWAYS);
        actionsRow.getChildren().addAll(takeCard, reportCard);

        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        stats.getStyleClass().add("dash-stats");

        VBox myClassesCard = statCard(helper.getMessage("teacher.dashboard.stats.classes"), totalClasses);
        VBox totalStudentsCard = statCard(helper.getMessage("teacher.dashboard.stats.students"), totalStudents);
        VBox presentTodayCard = statCard(helper.getMessage("teacher.dashboard.stats.present"), presentToday);
        VBox absentTodayCard = statCard(helper.getMessage("teacher.dashboard.stats.absent"), absentToday);

        stats.add(myClassesCard, 0, 0);
        stats.add(totalStudentsCard, 1, 0);
        stats.add(presentTodayCard, 0, 1);
        stats.add(absentTodayCard, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        Label classesTitle = new Label(helper.getMessage("teacher.dashboard.classes.title"));
        classesTitle.getStyleClass().add("section-title");

        VBox classesContainer = new VBox(10);
        classesContainer.getStyleClass().add("classes-card");
        classesContainer.setPadding(new Insets(16));
        classesContainer.setMinHeight(170);

        Label loading = new Label(helper.getMessage("teacher.dashboard.classes.loading"));
        loading.getStyleClass().add("empty-subtitle");
        classesContainer.setAlignment(Pos.CENTER);
        classesContainer.getChildren().add(loading);

        page.getChildren().addAll(
                greeting,
                dateLabel,
                actionsRow,
                stats,
                new Separator(),
                classesTitle,
                classesContainer
        );

        new Thread(() -> {
            try {
                Map<String, Object> res = api.getDashboardStats(jwtStore, state);
                int tc = ((Number) res.getOrDefault("totalClasses", 0)).intValue();
                int ts = ((Number) res.getOrDefault("totalStudents", 0)).intValue();
                int pt = ((Number) res.getOrDefault("presentToday", 0)).intValue();
                int at = ((Number) res.getOrDefault("absentToday", 0)).intValue();

                Platform.runLater(() -> {
                    setStatValue(myClassesCard, tc);
                    setStatValue(totalStudentsCard, ts);
                    setStatValue(presentTodayCard, pt);
                    setStatValue(absentTodayCard, at);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(
                                Alert.AlertType.ERROR,
                                helper.getMessage("teacher.dashboard.error.stats").replace("{error}", ex.getMessage()),
                                ButtonType.OK
                        ).showAndWait()
                );
            }
        }).start();

        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = api.getMyClasses(jwtStore, state);

                Platform.runLater(() -> {
                    classesContainer.getChildren().clear();

                    if (classes == null || classes.isEmpty()) {
                        classesContainer.setAlignment(Pos.CENTER);

                        Label icon = new Label("📅");
                        icon.getStyleClass().add("empty-icon");

                        Label t = new Label(helper.getMessage("teacher.dashboard.classes.empty.title"));
                        t.getStyleClass().add("empty-title");

                        Label s = new Label(helper.getMessage("teacher.dashboard.classes.empty.subtitle"));
                        s.getStyleClass().add("empty-subtitle");

                        classesContainer.getChildren().addAll(icon, t, s);
                        return;
                    }

                    classesContainer.setAlignment(Pos.TOP_LEFT);

                    for (Map<String, Object> c : classes) {
                        String classCode = String.valueOf(c.getOrDefault("classCode", "—"));
                        String name = String.valueOf(c.getOrDefault("name", helper.getMessage("teacher.dashboard.classes.unnamed")));
                        String semester = String.valueOf(c.getOrDefault("semester", "—"));
                        String academicYear = String.valueOf(c.getOrDefault("academicYear", "—"));
                        String studentsCount = String.valueOf(c.getOrDefault("studentsCount", "0"));

                        classesContainer.getChildren().add(classRow(
                                classCode,
                                name,
                                semester,
                                academicYear,
                                studentsCount
                        ));
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    classesContainer.getChildren().clear();
                    classesContainer.setAlignment(Pos.CENTER);

                    Label err = new Label(
                            helper.getMessage("teacher.dashboard.error.classes").replace("{error}", ex.getMessage())
                    );
                    err.getStyleClass().add("empty-subtitle");
                    classesContainer.getChildren().add(err);
                });
            }
        }).start();

        return AppLayout.wrapWithSidebar(
                teacherName,
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email"),
                page,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("teacher-take"); }
                    @Override public void goReports() { router.go("teacher-reports"); }
                    @Override public void goEmail() { router.go("teacher-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private void setStatValue(VBox statCard, int value) {
        if (statCard.getChildren().size() >= 2 && statCard.getChildren().get(1) instanceof Label v) {
            v.setText(String.valueOf(value));
        }
    }

    private VBox bigActionCard(String title, String subtitle, String styleClass, Runnable action) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("big-card", styleClass);

        Label t = new Label(title);
        t.getStyleClass().add("big-card-title");

        Label s = new Label(subtitle);
        s.getStyleClass().add("big-card-subtitle");

        card.getChildren().addAll(t, s);
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

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        top.getChildren().addAll(icon, l);

        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("stat-value");

        card.getChildren().addAll(top, v);
        return card;
    }

    private VBox classRow(String classCode, String name, String semester, String academicYear, String studentsCount) {
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
        if (label.equals(helper.getMessage("teacher.dashboard.stats.classes"))) return "📚";
        if (label.equals(helper.getMessage("teacher.dashboard.stats.students"))) return "👥";
        if (label.equals(helper.getMessage("teacher.dashboard.stats.present"))) return "✅";
        if (label.equals(helper.getMessage("teacher.dashboard.stats.absent"))) return "❌";
        return "📊";
    }
}