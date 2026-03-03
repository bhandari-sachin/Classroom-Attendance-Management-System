package frontend.teacher;

import frontend.AppLayout;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import java.util.Map;

public class TeacherDashboardApp {

    // values shown in UI (defaults until backend loads)
    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        TeacherApi api = new TeacherApi("http://localhost:8081");

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Header
        Label greeting = new Label("Good afternoon, " + teacherName + "!");
        greeting.getStyleClass().add("dash-title");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("dash-subtitle");

        // Action cards row
        HBox actionsRow = new HBox(14);
        actionsRow.getStyleClass().add("dash-actions");

        VBox takeCard = bigActionCard(
                "Take Attendance",
                "Generate QR code for your class",
                "card-green",
                () -> router.go("teacher-take")
        );

        VBox reportCard = bigActionCard(
                "View Reports",
                "Class Attendance Reports",
                "card-purple",
                () -> router.go("teacher-reports")
        );

        HBox.setHgrow(takeCard, Priority.ALWAYS);
        HBox.setHgrow(reportCard, Priority.ALWAYS);
        actionsRow.getChildren().addAll(takeCard, reportCard);

        // Stats grid (2x2)
        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        stats.getStyleClass().add("dash-stats");

        VBox myClassesCard = statCard("My Classes", totalClasses);
        VBox totalStudentsCard = statCard("Total Students", totalStudents);
        VBox presentTodayCard = statCard("Present Today", presentToday);
        VBox absentTodayCard = statCard("Absent Today", absentToday);

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

        // My classes section (empty state)
        Label classesTitle = new Label("My classes");
        classesTitle.getStyleClass().add("section-title");

        VBox emptyClasses = new VBox(8);
        emptyClasses.setAlignment(Pos.CENTER);
        emptyClasses.setMinHeight(170);
        emptyClasses.getStyleClass().add("classes-card");

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");

        Label t = new Label("No classes Assigned");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been assigned any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        emptyClasses.getChildren().addAll(icon, t, s);

        page.getChildren().addAll(
                greeting,
                dateLabel,
                actionsRow,
                stats,
                new Separator(),
                classesTitle,
                emptyClasses
        );

        // ✅ Load stats from backend (async)
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
                        new Alert(Alert.AlertType.ERROR,
                                "Failed to load dashboard stats: " + ex.getMessage(),
                                ButtonType.OK
                        ).showAndWait()
                );
            }
        }).start();

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Teacher Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
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
        // statCard children: [topRow, valueLabel]
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
        card.getStyleClass().add("stat-card");
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

    private String resolveIcon(String label) {
        return switch (label) {
            case "My Classes" -> "📚";
            case "Total Students" -> "👥";
            case "Present Today" -> "✅";
            case "Absent Today" -> "❌";
            default -> "📊";
        };
    }
}