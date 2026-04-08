package frontend.teacher;

import frontend.AppLayout;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
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

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? t("teacher.fallback.name", "Teacher")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        Label greeting = new Label(
                t("teacher.dashboard.greeting", "Welcome, {name}").replace("{name}", teacherName)
        );
        greeting.getStyleClass().add("dash-title");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("dash-subtitle");

        HBox actionsRow = new HBox(14);
        actionsRow.getStyleClass().add("dash-actions");

        VBox takeCard = bigActionCard(
                t("teacher.dashboard.takeAttendance.title", "Take Attendance"),
                t("teacher.dashboard.takeAttendance.subtitle", "Start attendance for a class session"),
                "card-green",
                () -> router.go("teacher-take")
        );

        VBox reportCard = bigActionCard(
                t("teacher.dashboard.reports.title", "Reports"),
                t("teacher.dashboard.reports.subtitle", "View attendance reports and summaries"),
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

        VBox myClassesCard = statCard(t("teacher.dashboard.stats.classes", "Classes"), totalClasses);
        VBox totalStudentsCard = statCard(t("teacher.dashboard.stats.students", "Students"), totalStudents);
        VBox presentTodayCard = statCard(t("teacher.dashboard.stats.present", "Present Today"), presentToday);
        VBox absentTodayCard = statCard(t("teacher.dashboard.stats.absent", "Absent Today"), absentToday);

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

        Label classesTitle = new Label(t("teacher.dashboard.classes.title", "My Classes"));
        classesTitle.getStyleClass().add("section-title");

        VBox classesContainer = new VBox(10);
        classesContainer.getStyleClass().add("classes-card");
        classesContainer.setPadding(new Insets(16));
        classesContainer.setMinHeight(170);

        Label loading = new Label(t("teacher.dashboard.classes.loading", "Loading classes..."));
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
                                t("teacher.dashboard.error.stats", "Failed to load stats: {error}")
                                        .replace("{error}", ex.getMessage()),
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

                        Label t1 = new Label(t("teacher.dashboard.classes.empty.title", "No classes yet"));
                        t1.getStyleClass().add("empty-title");

                        Label s = new Label(t("teacher.dashboard.classes.empty.subtitle", "Your assigned classes will appear here"));
                        s.getStyleClass().add("empty-subtitle");

                        classesContainer.getChildren().addAll(icon, t1, s);
                        return;
                    }

                    classesContainer.setAlignment(Pos.TOP_LEFT);

                    for (Map<String, Object> c : classes) {
                        String classCode = String.valueOf(c.getOrDefault("classCode", "—"));
                        String name = String.valueOf(c.getOrDefault("name", t("teacher.dashboard.classes.unnamed", "Unnamed class")));
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
                            t("teacher.dashboard.error.classes", "Failed to load classes: {error}")
                                    .replace("{error}", ex.getMessage())
                    );
                    err.getStyleClass().add("empty-subtitle");
                    classesContainer.getChildren().add(err);
                });
            }
        }).start();

        return AppLayout.wrapWithSidebar(
                teacherName,
                t("teacher.sidebar.title", "Teacher Panel"),
                t("teacher.sidebar.menu.dashboard", "Dashboard"),
                t("teacher.sidebar.menu.take_attendance", "Take Attendance"),
                t("teacher.sidebar.menu.reports", "Reports"),
                t("teacher.sidebar.menu.email", "Email"),
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

        Label t1 = new Label(title);
        t1.getStyleClass().add("big-card-title");

        Label s = new Label(subtitle);
        s.getStyleClass().add("big-card-subtitle");

        card.getChildren().addAll(t1, s);
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
                t("teacher.dashboard.classes.meta", "{semester} • {year} • {count} students")
                        .replace("{semester}", semester)
                        .replace("{year}", academicYear)
                        .replace("{count}", studentsCount)
        );
        meta.getStyleClass().add("class-meta");

        box.getChildren().addAll(title, meta);
        return box;
    }

    private String resolveIcon(String label) {
        if (label.equals(t("teacher.dashboard.stats.classes", "Classes"))) return "📚";
        if (label.equals(t("teacher.dashboard.stats.students", "Students"))) return "👥";
        if (label.equals(t("teacher.dashboard.stats.present", "Present Today"))) return "✅";
        if (label.equals(t("teacher.dashboard.stats.absent", "Absent Today"))) return "❌";
        return "📊";
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}