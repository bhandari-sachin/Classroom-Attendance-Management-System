package frontend;

import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminAttendanceReportsPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Comprehensive attendance analytics and reports");
        subtitle.getStyleClass().add("subtitle");

        // ================= FILTERS =================
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

        // ================= STATS =================
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.ALWAYS);
        c.setFillWidth(true);
        stats.getColumnConstraints().addAll(c, c);

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);

        Runnable loadStats = () -> {
            try {
                error.setVisible(false);
                error.setManaged(false);

                // 🔹 For now backend ignores filters (OK)
                String json = api.getAttendanceStatsJson();

                int present = extractInt(json, "presentCount");
                int absent  = extractInt(json, "absentCount");
                int excused = extractInt(json, "excusedCount");
                int total   = extractInt(json, "totalRecords");

                double rate = total == 0 ? 0.0 : (present * 100.0) / total;

                stats.getChildren().clear();
                stats.add(AdminUI.makeStatCard("Overall Attendance Rate", String.format("%.1f%%", rate), "📈", "accent-green"), 0, 0);
                stats.add(AdminUI.makeStatCard("Present", String.valueOf(present), "🟢", "accent-green"), 1, 0);
                stats.add(AdminUI.makeStatCard("Absent", String.valueOf(absent), "🔴", "accent-orange"), 0, 1);
                stats.add(AdminUI.makeStatCard("Excused", String.valueOf(excused), "🟠", "accent-purple"), 1, 1);
                stats.add(AdminUI.makeStatCard("Total Records", String.valueOf(total), "📄", "accent-purple"), 0, 2);

            } catch (Exception e) {
                e.printStackTrace();
                error.setText("Failed to load attendance stats.");
                error.setVisible(true);
                error.setManaged(true);
            }
        };

        // Reload stats when filters change (backend-ready)
        classFilter.setOnAction(e -> loadStats.run());
        timeFilter.setOnAction(e -> loadStats.run());
        studentSearch.textProperty().addListener((obs, o, n) -> loadStats.run());

        loadStats.run();

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                error,
                stats
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

    private static int extractInt(String json, String key) {
        String needle = "\"" + key + "\":";
        int i = json.indexOf(needle);
        if (i < 0) return 0;
        int start = i + needle.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}