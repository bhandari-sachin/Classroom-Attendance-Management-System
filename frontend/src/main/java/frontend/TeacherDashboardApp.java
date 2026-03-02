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
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import dto.AttendanceStats;
import model.CourseClass;
import service.AttendanceService;
import service.ClassService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeacherDashboardApp {

    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        ClassService classService = new ClassService(new ClassSQL());
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());
        UserService userService = new UserService(new UserSQL());

        List<CourseClass> classes = classService.getClassesByTeacher(teacherId);
        if (classes != null) {
            totalClasses = classes.size();
            int total = 0;
            int present = 0;
            int absent = 0;

            for (CourseClass c : classes) {
                try {
                    int enrolled = classService.getEnrollmentCount(c.getId());
                    total += enrolled;

                    // add present day stats later
                    AttendanceStats stats = attendanceService.getStatsForClass(c.getId());
                    if (stats != null) {
                        present += stats.getPresentCount();
                        absent += stats.getAbsentCount();
                    }
                } catch (Exception ex) {
                    System.err.println("Failed loading class data: " + ex.getMessage());
                }
            }

            totalStudents = total;
            presentToday = present;
            absentToday = absent;
        }

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

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

        stats.add(statCard("My Classes", totalClasses), 0, 0);
        stats.add(statCard("Total Students", totalStudents), 1, 0);
        stats.add(statCard("Present Today", presentToday), 0, 1);
        stats.add(statCard("Absent Today", absentToday), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        // My classes section
        Label classesTitle = new Label("My classes");
        classesTitle.getStyleClass().add("section-title");

        VBox classesBox = new VBox(8);
        classesBox.setAlignment(Pos.TOP_LEFT);
        classesBox.setMinHeight(170);
        classesBox.getStyleClass().add("classes-card");

        if (classes == null || classes.isEmpty()) {
            Label icon = new Label("📅");
            icon.getStyleClass().add("empty-icon");

            Label t = new Label("No classes Assigned");
            t.getStyleClass().add("empty-title");

            Label s = new Label("You haven’t been assigned any classes yet.");
            s.getStyleClass().add("empty-subtitle");

            classesBox.getChildren().addAll(icon, t, s);
        } else {
            for (CourseClass c : classes) {
                Label row = new Label(c.getName() + " — " + c.getClassCode());
                row.getStyleClass().add("class-row");
                classesBox.getChildren().add(row);
            }
        }

        page.getChildren().addAll(
                greeting,
                dateLabel,
                actionsRow,
                stats,
                new Separator(),
                classesTitle,
                classesBox
        );

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
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("stat-value");

        card.getChildren().addAll(l, v);
        return card;
    }
}