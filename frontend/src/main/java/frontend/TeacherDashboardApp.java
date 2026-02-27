package frontend;

import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import config.UserSQL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import dto.AttendanceStats;
import model.CourseClass;
import model.User;
import service.AttendanceService;
import service.ClassService;
import service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeacherDashboardApp {

    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    public Parent build(Scene scene, String teacherName) {

        ClassService classService = new ClassService(new ClassSQL());
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());
        UserService userService = new UserService(new UserSQL());

        Long teacherId = 3L; // temporary until auth is added
        String displayName = teacherName;

        try {
            List<User> all = userService.getAllUsers();
            if (all != null) {
                for (User u : all) {
                    if (u.getId() != null && u.getId().equals(teacherId)) {
                        String first = u.getFirstName() == null ? "" : u.getFirstName();
                        String last = u.getLastName() == null ? "" : u.getLastName();
                        String combined = (first + " " + last).trim();
                        if (!combined.isEmpty()) displayName = combined;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to fetch user display name: " + ex.getMessage());
        }

        final String displayNameFinal = displayName;

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

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Header
        Label greeting = new Label("Good afternoon, " + displayNameFinal + "!");
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
                () -> scene.setRoot(new TeacherTakeAttendancePage().build(scene, displayNameFinal))
        );

        VBox reportCard = bigActionCard(
                "View Reports",
                "Class Attendance Reports",
                "card-purple",
                () -> scene.setRoot(new TeacherReportsPage().build(scene, displayNameFinal))
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
                displayNameFinal,
                "Student Panel", "Dashboard", "Mark Attendance", "My Attendance", "Contact", page,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(build(scene, displayNameFinal)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new TeacherTakeAttendancePage().build(scene, displayNameFinal)); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, displayNameFinal)); }
                    @Override public void goEmail() { scene.setRoot(new TeacherEmailPage().build(scene, displayNameFinal)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }

    // ===== Helpers MUST be inside the class =====

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
