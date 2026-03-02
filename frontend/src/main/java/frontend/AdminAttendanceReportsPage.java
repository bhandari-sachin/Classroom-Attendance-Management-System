package frontend;

import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import config.UserSQL;
import dto.AttendanceStats;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.CourseClass;
import model.User;
import service.AttendanceService;
import service.ClassService;
import service.UserService;

import java.util.List;

public class AdminAttendanceReportsPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        AttendanceSQL attendanceSQL = new AttendanceSQL();
        SessionSQL sessionSQL = new SessionSQL();
        ClassSQL classSQL = new ClassSQL();
        AttendanceService attendanceService = new AttendanceService(attendanceSQL, sessionSQL);
        ClassService classService = new ClassService(classSQL);

        String adminName = (state.getName() == null || state.getName().isBlank()) ? "Name" : state.getName();

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

        // Keep a list of classes to map selection to class id
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

        StackPane classSummaryContainer = new StackPane();
        Pane defaultCard = AdminUI.makeClassCard("All classes summary", "", "", "Loading...");
        classSummaryContainer.getChildren().setAll(defaultCard);
        classSummaryContainer.setMinHeight(110);
        classSummaryContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(classSummaryContainer, Priority.NEVER);

        Label recordsTitle = new Label("All Records");
        recordsTitle.getStyleClass().add("section-title");

        TableView<UserRow> table = AdminUI.buildUsersTable();

        // populate users table from backend
        UserService userService = new UserService(new UserSQL());
        ObservableList<UserRow> usersList = FXCollections.observableArrayList();
        List<User> users = userService.getAllUsers();
        if (users != null) {
            for (User u : users) {
                usersList.add(new UserRow(u.getName(), u.getEmail(), u.getUserType(), String.valueOf(userService.getEnrolledClasses(u.getId()))));
            }
        }
        table.setItems(usersList);

        // helper to update stats and class summary
        Runnable updateStats = () -> {
            stats.getChildren().clear();
            AttendanceStats statsData;
            String selectedValue = classFilter.getValue();
            boolean allClasses = "All Classes".equals(selectedValue);

            CourseClass selectedClass = null;
            if (!allClasses && classes != null) {
                for (CourseClass cls : classes) {
                    if (cls.getName().equals(selectedValue)) { selectedClass = cls; break; }
                }
            }

            if (selectedClass == null) {
                switch (timeFilter.getValue()) {
                    case "Last Month" -> statsData = attendanceService.getStatsLastMonth();
                    case "This Year" -> statsData = attendanceService.getStatsThisYear();
                    default -> statsData = attendanceService.getStatsThisMonth();
                }

                if (classes != null && !classes.isEmpty()) {
                    FlowPane cards = new FlowPane();
                    cards.setHgap(12);
                    cards.setVgap(12);
                    cards.setPrefWrapLength(800);

                    for (CourseClass cls : classes) {
                        AttendanceStats s;
                        switch (timeFilter.getValue()) {
                            case "Last Month" -> s = attendanceService.getClassStatsLastMonth(cls.getId());
                            case "This Year" -> s = attendanceService.getClassStatsThisYear(cls.getId());
                            default -> s = attendanceService.getClassStatsThisMonth(cls.getId());
                        }
                        if (s == null) s = new AttendanceStats(0,0,0,0);
                        String summary = String.format("%d present · %d absent · %d excused", s.getPresentCount(), s.getAbsentCount(), s.getExcusedCount());
                        Pane card = AdminUI.makeClassCard(cls.getName(), cls.getClassCode(), cls.getTeacherEmail() == null ? "" : cls.getTeacherEmail(), summary);
                        cards.getChildren().add(card);
                    }

                    classSummaryContainer.getChildren().setAll(cards);
                } else {
                    // fallback aggregated card
                    String summary = String.format("%d present · %d absent · %d excused", statsData.getPresentCount(), statsData.getAbsentCount(), statsData.getExcusedCount());
                    classSummaryContainer.getChildren().setAll(AdminUI.makeClassCard("All classes summary", "", "", summary));
                }

            } else {
                switch (timeFilter.getValue()) {
                    case "Last Month" -> statsData = attendanceService.getClassStatsLastMonth(selectedClass.getId());
                    case "This Year" -> statsData = attendanceService.getClassStatsThisYear(selectedClass.getId());
                    default -> statsData = attendanceService.getClassStatsThisMonth(selectedClass.getId());
                }

                if (statsData == null) statsData = new AttendanceStats(0,0,0,0);

                stats.add(AdminUI.makeStatCard("Overall Attendance Rate", String.format("%.1f%%", statsData.getAttendanceRate()), "📈", "accent-green"), 0, 0);
                stats.add(AdminUI.makeStatCard("Present", String.valueOf(statsData.getPresentCount()), "🟢", "accent-green"), 1, 0);
                stats.add(AdminUI.makeStatCard("Absent", String.valueOf(statsData.getAbsentCount()), "🔴", "accent-orange"), 0, 1);
                stats.add(AdminUI.makeStatCard("Excused", String.valueOf(statsData.getExcusedCount()), "🟠", "accent-purple"), 1, 1);
                stats.add(AdminUI.makeStatCard("Total Records", String.valueOf(statsData.getTotalRecords()), "📄", "accent-purple"), 0, 2);

                String summary = String.format("%d present · %d absent · %d excused", statsData.getPresentCount(), statsData.getAbsentCount(), statsData.getExcusedCount());
                Pane newCard = AdminUI.makeClassCard(selectedClass.getName(), selectedClass.getClassCode(), selectedClass.getTeacherEmail() == null ? "" : selectedClass.getTeacherEmail(), summary);
                classSummaryContainer.getChildren().setAll(newCard);
            }

            if (selectedClass == null) {
                AttendanceStats agg;
                switch (timeFilter.getValue()) {
                    case "Last Month" -> agg = attendanceService.getStatsLastMonth();
                    case "This Year" -> agg = attendanceService.getStatsThisYear();
                    default -> agg = attendanceService.getStatsThisMonth();
                }
                if (agg == null) agg = new AttendanceStats(0,0,0,0);
                stats.add(AdminUI.makeStatCard("Overall Attendance Rate", String.format("%.1f%%", agg.getAttendanceRate()), "📈", "accent-green"), 0, 0);
                stats.add(AdminUI.makeStatCard("Present", String.valueOf(agg.getPresentCount()), "🟢", "accent-green"), 1, 0);
                stats.add(AdminUI.makeStatCard("Absent", String.valueOf(agg.getAbsentCount()), "🔴", "accent-orange"), 0, 1);
                stats.add(AdminUI.makeStatCard("Excused", String.valueOf(agg.getExcusedCount()), "🟠", "accent-purple"), 1, 1);
                stats.add(AdminUI.makeStatCard("Total Records", String.valueOf(agg.getTotalRecords()), "📄", "accent-purple"), 0, 2);
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
                classSummaryContainer,
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
                "Attendance Reports",
                "Manage Users",
                scroll,
                "third",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-reports"); }
                    @Override public void goEmail() { router.go("admin-users"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}