package frontend;

import config.ClassSQL;
import config.UserSQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.AttendanceService;
import service.ClassService;
import dto.AttendanceStats;
import service.UserService;
import model.User;
import model.CourseClass;

import java.util.List;

public class AdminAttendanceReportsPage {
    private final AttendanceService attendanceService;
    private final ClassService classService;

    public AdminAttendanceReportsPage(AttendanceService attendanceService, ClassService classService) {
        this.attendanceService = attendanceService;
        this.classService = classService;
    }

    public Parent build(Scene scene, String adminName) {

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Comprehensive attendance analytics and reports");
        subtitle.getStyleClass().add("subtitle");

        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(8);

        // Keep a list of classes to map selection index to class id
        List<CourseClass> classes = classService.getAllClasses();

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll("All Classes");
        if (classes != null) {
            classes.forEach(c -> classFilter.getItems().add(c.getName()));
        }
        classFilter.setValue("All Classes");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("This Month", "Last Month", "This Year");
        timeFilter.setValue("This Month");

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search by name...");

        filters.add(new VBox(new Label("Class"), classFilter), 0, 0);
        filters.add(new VBox(new Label("Time Period"), timeFilter), 1, 0);
        filters.add(new VBox(new Label("Search Student"), studentSearch), 2, 0);

        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.ALWAYS);
        c.setFillWidth(true);
        stats.getColumnConstraints().addAll(c, c);

        Label summaryTitle = new Label("Class summary");
        summaryTitle.getStyleClass().add("section-title");

        // placeholder container for class summary card (mutable via array)
        final Pane[] classSummaryHolder = new Pane[] { new StackPane() };

        Label recordsTitle = new Label("All Records");
        recordsTitle.getStyleClass().add("section-title");

        TableView<UserRow> table = AdminUI.buildUsersTable();

        // populate users table from backend
        UserService userService = new UserService(new UserSQL());
        ObservableList<UserRow> usersList = FXCollections.observableArrayList();
        List<User> users = userService.getAllUsers();
        if (users != null) {
            for (User u : users) {
                usersList.add(new UserRow(u.getName(), u.getEmail(), u.getRole(), String.valueOf(userService.getEnrolledClasses(u.getId()))));
            }
        }
        table.setItems(usersList);

        // helper to update stats and class summary
        Runnable updateStats = () -> {
            stats.getChildren().clear();
            AttendanceStats statsData;
            int classIndex = classFilter.getSelectionModel().getSelectedIndex();
            boolean allClasses = classIndex <= 0; // 0 == All Classes

            if (allClasses) {
                switch (timeFilter.getValue()) {
                    case "Last Month" -> statsData = attendanceService.getStatsLastMonth();
                    case "This Year" -> statsData = attendanceService.getStatsThisYear();
                    default -> statsData = attendanceService.getStatsThisMonth();
                }
            } else {
                if (classes == null || classIndex - 1 < 0 || classIndex - 1 >= classes.size()) {
                    statsData = new AttendanceStats(0, 0, 0, 0);
                } else {
                    CourseClass selected = classes.get(classIndex - 1);
                    statsData = attendanceService.getStatsForClass(selected.getId());
                }
            }

            if (statsData == null) {
                statsData = new AttendanceStats(0, 0, 0, 0);
            }

            stats.add(AdminUI.makeStatCard("Overall Attendance Rate", String.format("%.1f%%", statsData.getAttendanceRate()), "📈", "accent-green"), 0, 0);
            stats.add(AdminUI.makeStatCard("Present", String.valueOf(statsData.getPresentCount()), "🟢", "accent-green"), 1, 0);
            stats.add(AdminUI.makeStatCard("Absent", String.valueOf(statsData.getAbsentCount()), "🔴", "accent-orange"), 0, 1);
            stats.add(AdminUI.makeStatCard("Excused", String.valueOf(statsData.getExcusedCount()), "🟠", "accent-purple"), 1, 1);
            stats.add(AdminUI.makeStatCard("Total Records", String.valueOf(statsData.getTotalRecords()), "📄", "accent-purple"), 0, 2);

            // update class summary card
            if (!allClasses && classes != null && classIndex - 1 >= 0 && classIndex - 1 < classes.size()) {
                CourseClass selected = classes.get(classIndex - 1);
                String summary = String.format("%d present · %d absent · %d excused", statsData.getPresentCount(), statsData.getAbsentCount(), statsData.getExcusedCount());
                Pane newCard = AdminUI.makeClassCard(selected.getName(), selected.getClassCode(), selected.getTeacherEmail() == null ? "" : selected.getTeacherEmail(), summary);
                classSummaryHolder[0] = newCard;
            } else {
                classSummaryHolder[0] = AdminUI.makeClassCard("All classes summary", "", "", "");
            }
        };

        // initial stats
        updateStats.run();

        timeFilter.setOnAction(e -> updateStats.run());
        classFilter.setOnAction(e -> updateStats.run());

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                stats,
                summaryTitle,
                classSummaryHolder[0],
                recordsTitle,
                table
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                "Admin Panel",
                "Dashboard",
                "Manage Classes",
                "Manage Users",
                "Attendance Reports",
                scroll,
                "email", // activeKey for Attendance Reports (4th item)
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new AdminDashboardApp().build(scene, adminName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new AdminManageClassesPage(new ClassService(new ClassSQL())).build(scene, adminName)); }
                    @Override public void goReports() { scene.setRoot(new AdminManageUsersPage(new UserService(new UserSQL())).build(scene, adminName)); }
                    @Override public void goEmail() { scene.setRoot(build(scene, adminName)); }
                    @Override public void logout() { System.out.println("TODO: Admin Logout"); }
                }
        );

    }
}
