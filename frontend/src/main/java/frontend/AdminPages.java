package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminPages {

    // dashboard
    public static Parent dashboard(AdminShell nav) {
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

        statsGrid.add(AdminUI.makeStatCard("Total Classes", "4", "📘", "accent-purple"), 0, 0);
        statsGrid.add(AdminUI.makeStatCard("Students", "0", "🎓", "accent-green"), 1, 0);
        statsGrid.add(AdminUI.makeStatCard("Teachers", "1", "👥", "accent-orange"), 0, 1);
        statsGrid.add(AdminUI.makeStatCard("Monthly Rate", "0%", "📈", "accent-green"), 1, 1);

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
        manageClasses.setOnMouseClicked(e -> nav.showManageClasses());

        Pane manageUsers = AdminUI.makeActionCard("Manage Users", "Student registration and details", "👤", "qa-purple");
        manageUsers.setOnMouseClicked(e -> nav.showManageUsers());

        Pane reports = AdminUI.makeActionCard("Attendance Reports", "View comprehensive reports", "🧾", "qa-green");
        reports.setOnMouseClicked(e -> System.out.println("TODO: Reports"));

        quickActions.getChildren().addAll(manageClasses, manageUsers, reports);

        Label rcTitle = new Label("Recent classes");
        rcTitle.getStyleClass().add("section-title");

        GridPane recentGrid = new GridPane();
        recentGrid.setHgap(14);
        recentGrid.setVgap(14);

        recentGrid.add(AdminUI.makeClassCard("Mathematics", "TX-09374", "teacher@example.com", "MWF 9:00 AM"), 0, 0);
        recentGrid.add(AdminUI.makeClassCard("Physics", "TX-09374", "teacher@example.com", "MWF 9:00 AM"), 1, 0);
        recentGrid.add(AdminUI.makeClassCard("Design Patterns", "SW-27366", "teacher@example.com", "MWF 9:00 AM"), 0, 1);
        recentGrid.add(AdminUI.makeClassCard("Software Project 1", "SW-64544", "teacher@example.com", "MWF 9:00 AM"), 1, 1);

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

    // manage classes
    public static Parent manageClasses(AdminShell nav) {
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(4);
        Label title = new Label("Manage Classes");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Create and manage classes");
        subtitle.getStyleClass().add("subtitle");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button("+   Add class");
        add.getStyleClass().add("primary-btn");

        titleRow.getChildren().addAll(titleCol, spacer, add);

        TextField search = new TextField();
        search.setPromptText("Search classes...");
        search.getStyleClass().add("search-field");

        Label section = new Label("Detailed Records");
        section.getStyleClass().add("section-title");

        TableView<ClassRow> table = AdminUI.buildClassesTable();

        content.getChildren().addAll(titleRow, search, section, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        return scroll;
    }
    public static Parent attendanceReports(AdminShell nav) {

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Comprehensive attendance analytics and reports");
        subtitle.getStyleClass().add("subtitle");

        /* ---------- Filters ---------- */
        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(8);

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll("All Classes", "Mathematics", "Physics");
        classFilter.setValue("All Classes");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("This Month", "Last Month", "This Year");
        timeFilter.setValue("This Month");

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search by name...");

        filters.add(new VBox(new Label("Class"), classFilter), 0, 0);
        filters.add(new VBox(new Label("Time Period"), timeFilter), 1, 0);
        filters.add(new VBox(new Label("Search Student"), studentSearch), 2, 0);

        /* ---------- Stats ---------- */
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        stats.add(AdminUI.makeStatCard("Overall Attendance Rate", "0%", "📈", "accent-green"), 0, 0);
        stats.add(AdminUI.makeStatCard("Present", "0", "🟢", "accent-green"), 1, 0);
        stats.add(AdminUI.makeStatCard("Absent", "0", "🔴", "accent-orange"), 0, 1);
        stats.add(AdminUI.makeStatCard("Excused", "0", "🟠", "accent-purple"), 1, 1);
        stats.add(AdminUI.makeStatCard("Total Records", "0", "📄", "accent-purple"), 0, 2);

        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.ALWAYS);
        c.setFillWidth(true);
        stats.getColumnConstraints().addAll(c, c);

        /* ---------- Class summary ---------- */
        Label summaryTitle = new Label("Class summary");
        summaryTitle.getStyleClass().add("section-title");

        Pane classSummary = AdminUI.makeClassCard(
                "Mathematics",
                "TX-09374",
                "2 present · 1 absent · 1 excused",
                "50%"
        );

        /* ---------- Table ---------- */
        Label recordsTitle = new Label("All Records");
        recordsTitle.getStyleClass().add("section-title");

        TableView<UserRow> table = AdminUI.buildUsersTable();

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                stats,
                summaryTitle,
                classSummary,
                recordsTitle,
                table
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return scroll;
    }


    // manage users
    public static Parent manageUsers(AdminShell nav) {
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Manage Users");
        title.getStyleClass().add("title");

        Label subtitle = new Label("View and manage students, teachers, and their enrollments");
        subtitle.getStyleClass().add("subtitle");

        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");
        summary.getChildren().addAll(
                AdminUI.smallSummaryCard("Students", "0", "🎓", "accent-green"),
                AdminUI.smallSummaryCard("Teachers", "1", "👥", "accent-purple"),
                AdminUI.smallSummaryCard("Admins", "1", "🛡", "accent-orange")
        );

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search by name or email...");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("All Types", "Student", "Teacher", "Admin");
        type.setValue("All Types");
        type.getStyleClass().add("filter-combo");

        filters.getChildren().addAll(search, type);

        TableView<UserRow> table = AdminUI.buildUsersTable();

        content.getChildren().addAll(title, subtitle, summary, filters, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        return scroll;
    }
}
