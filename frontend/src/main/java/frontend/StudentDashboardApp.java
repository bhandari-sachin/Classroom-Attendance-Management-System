package frontend;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import dto.AttendanceStats;
import model.CourseClass;
import service.AttendanceService;
import service.ClassService;

import java.util.List;

public class StudentDashboardApp {

    private int presentCount = 0;
    private int absentCount = 0;
    private int excusedCount = 0;
    private double attendanceRate = 0.0; // 0.0 -> 0%

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        ClassService classService = new ClassService(new ClassSQL());
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());
        Long studentId = state.getUserId();

        // compute stats for this month
        AttendanceStats s = attendanceService.getStudentOverallThisMonth(studentId);
        if (s != null) {
            presentCount = s.getPresentCount();
            absentCount = s.getAbsentCount();
            excusedCount = s.getExcusedCount();
            attendanceRate = s.getAttendanceRate() / 100.0;
        }

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Header
        Label title = new Label("Welcome back, " + studentName + "!");
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label("Here’s your attendance overview for this month");
        subtitle.getStyleClass().add("dash-subtitle");

        // Action card (Mark attendance)
        Button markAttendance = attendanceCard(router);

        // Stats grid (2x2)
        GridPane stats = statsGrid();

        // Classes header row
        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label("Your classes");
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Attendance  →");
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> router.go("student-attendance"));

        classesHeader.getChildren().addAll(classesTitle, spacer, viewAll);

        VBox classesCard;
        try {
            List<CourseClass> classes = classService.getClassesForStudent(studentId);
            if (classes == null || classes.isEmpty()) {
                classesCard = emptyClassesCard();
            } else {
                VBox list = new VBox(6);
                list.getStyleClass().add("classes-card");
                for (CourseClass c : classes) {
                    Label row = new Label(c.getName() + " — " + c.getClassCode());
                    row.getStyleClass().add("class-row");
                    list.getChildren().add(row);
                }
                classesCard = list;
            }
        } catch (Exception ex) {
            classesCard = emptyClassesCard();
            System.err.println("Failed loading student classes: " + ex.getMessage());
        }

        page.getChildren().addAll(
                title,
                subtitle,
                markAttendance,
                stats,
                new Separator(),
                classesHeader,
                classesCard
        );

        return AppLayout.wrapWithSidebar(
                studentName,
                "Student Panel",
                "Dashboard",
                "Mark Attendance",
                "My Attendance",
                "Email",
                page,
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

    // ===== UI blocks / helpers =====

    private Button attendanceCard(AppRouter router) {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);
        Label big = new Label("Mark Attendance");
        big.getStyleClass().add("attendance-title");

        Label small = new Label("Scan the QR code to check in");
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

    private GridPane statsGrid() {
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

        grid.add(statCardWithBadge("Present", String.valueOf(presentCount), "This month", "#3BAA66", "✓"), 0, 0);
        grid.add(statCardWithBadge("Absent", String.valueOf(absentCount), "This month", "#E05A5A", "✕"), 1, 0);
        grid.add(statCardWithBadge("Excused", String.valueOf(excusedCount), "This month", "#E09A3B", "⏱"), 0, 1);
        grid.add(statCardWithBadge("Rate", (int) (attendanceRate * 100) + "%", "This month", "#5AA6E0", "%"), 1, 1);

        return grid;
    }

    private VBox statCardWithBadge(String label, String value, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #ECEFF2;" +
                        "-fx-border-radius: 12;"
        );

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

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

        Label big = new Label(value);
        big.setStyle("-fx-font-size: 28px; -fx-font-weight: 900;");

        Label small = new Label(hint);
        small.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        card.getChildren().addAll(top, big, small);
        return card;
    }

    private VBox emptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);

        Label cal = new Label("📅");
        cal.getStyleClass().add("empty-icon");

        Label t = new Label("No classes yet");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been enrolled in any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(cal, t, s);
        return card;
    }
}