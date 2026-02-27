package frontend;

import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import config.UserSQL;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.AttendanceService;
import service.ClassService;
import service.UserService;
import model.User;
import model.UserRole;
import model.CourseClass;
import dto.AttendanceStats;

import java.util.List;

public class AdminPages {

    // ===== Dashboard content =====
    public static Parent dashboardPage(Scene scene, String adminName) {
        VBox content = new VBox(18);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Manage your school's attendance system");
        subtitle.getStyleClass().add("subtitle");

        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("grid");
        statsGrid.setHgap(14);
        statsGrid.setVgap(14);

        // Use backend services to compute stats
        ClassService classService = new ClassService(new ClassSQL());
        UserService userService = new UserService(new UserSQL());
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());

        List<CourseClass> classes = classService.getAllClasses();
        List<User> users = userService.getAllUsers();

        int totalClasses = classes == null ? 0 : classes.size();
        long totalStudents = users == null ? 0 : users.stream().filter(u -> u.getRole() == UserRole.STUDENT).count();
        long totalTeachers = users == null ? 0 : users.stream().filter(u -> u.getRole() == UserRole.TEACHER).count();

        AttendanceStats monthStats = attendanceService.getStatsThisMonth();
        String monthlyRate = monthStats == null ? "0%" : String.format("%.1f%%", monthStats.getAttendanceRate());

        statsGrid.add(AdminUI.makeStatCard("Total Classes", String.valueOf(totalClasses), "📘", "accent-purple"), 0, 0);
        statsGrid.add(AdminUI.makeStatCard("Students", String.valueOf(totalStudents), "🎓", "accent-green"), 1, 0);
        statsGrid.add(AdminUI.makeStatCard("Teachers", String.valueOf(totalTeachers), "👥", "accent-orange"), 0, 1);
        statsGrid.add(AdminUI.makeStatCard("Monthly Rate", monthlyRate, "📈", "accent-green"), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        statsGrid.getColumnConstraints().addAll(c1, c2);

        Label qaTitle = new Label("Quick Actions");
        qaTitle.getStyleClass().add("section-title");

        HBox quickActions = new HBox(14);
        quickActions.getStyleClass().add("quick-actions");

        Pane manageClasses = AdminUI.makeActionCard("Manage Classes", "Add, edit or remove classes", "📚", "qa-green");
        manageClasses.setOnMouseClicked(e ->
                scene.setRoot(new AdminManageClassesPage(new ClassService(new ClassSQL())).build(scene, adminName))
        );

        Pane manageUsers = AdminUI.makeActionCard("Manage Users", "Student registration and details", "👤", "qa-purple");
        manageUsers.setOnMouseClicked(e ->
                scene.setRoot(new AdminManageUsersPage(new UserService(new UserSQL())).build(scene, adminName))
        );

        Pane reports = AdminUI.makeActionCard("Attendance Reports", "View comprehensive reports", "🧾", "qa-green");
        reports.setOnMouseClicked(e ->
                scene.setRoot(new AdminAttendanceReportsPage(new AttendanceService(new AttendanceSQL(), new SessionSQL()),
                        new ClassService(new ClassSQL())).build(scene, adminName))
        );

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);

        Label rcTitle = new Label("Recent classes");
        rcTitle.getStyleClass().add("section-title");

        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);

        // Populate recent classes dynamically (up to 4)
        if (classes != null) {
            for (int i = 0; i < Math.min(4, classes.size()); i++) {
                CourseClass cls = classes.get(i);
                String schedule = (cls.getSemester() == null ? "" : cls.getSemester()) + " " + (cls.getAcademicYear() == null ? "" : cls.getAcademicYear());
                int col = i % 2;
                int row = i / 2;
                recentGrid.add(AdminUI.makeClassCard(cls.getName(), cls.getClassCode(), cls.getTeacherEmail() == null ? "" : cls.getTeacherEmail(), schedule), col, row);
            }
        }

        ColumnConstraints r1 = new ColumnConstraints();
        r1.setHgrow(Priority.ALWAYS);
        r1.setFillWidth(true);

        ColumnConstraints r2 = new ColumnConstraints();
        r2.setHgrow(Priority.ALWAYS);
        r2.setFillWidth(true);

        recentGrid.getColumnConstraints().addAll(r1, r2);

        content.getChildren().addAll(title, subtitle, statsGrid, qaTitle, quickActions, rcTitle, recentGrid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return scroll;

    }
}
