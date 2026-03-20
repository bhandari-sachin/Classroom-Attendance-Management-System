package frontend.admin;

import frontend.ReportRow;
import frontend.api.AdminApi;
import frontend.api.ReportApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminAttendanceReportsPage {

    private static class ClassItem {
        final Long id;
        final String label;

        ClassItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

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

        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(8);

        ComboBox<ClassItem> classFilter = new ComboBox<>();
        classFilter.setPromptText("Loading classes...");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("ALL", "THIS_MONTH", "LAST_MONTH", "THIS_YEAR");
        timeFilter.setValue("THIS_MONTH");

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search by name...");

        filters.add(new VBox(new Label("Class"), classFilter), 0, 0);
        filters.add(new VBox(new Label("Time Period"), timeFilter), 1, 0);
        filters.add(new VBox(new Label("Search Student"), studentSearch), 2, 0);

        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        Label tableTitle = new Label("Filtered Records");
        tableTitle.getStyleClass().add("section-title");

        TableView<ReportRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ReportRow, String> cStudent = new TableColumn<>("Student");
        cStudent.setCellValueFactory(d -> d.getValue().studentProperty());

        TableColumn<ReportRow, String> cDate = new TableColumn<>("Session Date");
        cDate.setCellValueFactory(d -> d.getValue().dateProperty());

        TableColumn<ReportRow, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(cStudent, cDate, cStatus);

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);
        ReportApi reportApi = new ReportApi(System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081"));

        MenuButton exportBtn = new MenuButton("⬇ Export");
        exportBtn.getStyleClass().addAll("pill", "pill-blue");

        MenuItem exportPdf = new MenuItem("Export as PDF");
        MenuItem exportCsv = new MenuItem("Export as CSV");
        exportBtn.getItems().addAll(exportPdf, exportCsv);

        exportPdf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save PDF Report");
            fc.setInitialFileName("admin-report.pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;
            new Thread(() -> {
                try {
                    reportApi.exportAdminReport(jwtStore, state, "pdf", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "PDF saved:\n" + dest.getAbsolutePath(), ButtonType.OK).showAndWait());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage(), ButtonType.OK).showAndWait());
                }
            }).start();
        });

        exportCsv.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save CSV Report");
            fc.setInitialFileName("admin-report.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;
            new Thread(() -> {
                try {
                    reportApi.exportAdminReport(jwtStore, state, "csv", dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "CSV saved:\n" + dest.getAbsolutePath(), ButtonType.OK).showAndWait());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage(), ButtonType.OK).showAndWait());
                }
            }).start();
        });

        Runnable loadReport = () -> {
            ClassItem selectedClass = classFilter.getValue();
            if (selectedClass == null || selectedClass.id == null) {
                stats.getChildren().clear();
                table.getItems().clear();
                return;
            }

            String period = timeFilter.getValue();
            String search = studentSearch.getText();

            new Thread(() -> {
                try {
                    error.setVisible(false);
                    error.setManaged(false);

                    List<Map<String, Object>> rows = api.getAttendanceReport(selectedClass.id, period, search);

                    int present = 0;
                    int absent = 0;
                    int excused = 0;

                    List<ReportRow> mappedRows = new ArrayList<>();

                    for (Map<String, Object> r : rows) {
                        String firstName = String.valueOf(r.getOrDefault("firstName", ""));
                        String lastName = String.valueOf(r.getOrDefault("lastName", ""));
                        String studentName = (firstName + " " + lastName).trim();

                        String date = String.valueOf(r.getOrDefault("sessionDate", "—"));
                        String status = String.valueOf(r.getOrDefault("status", "—"));

                        mappedRows.add(new ReportRow(studentName, date, status));

                        switch (status.toUpperCase()) {
                            case "PRESENT" -> present++;
                            case "ABSENT" -> absent++;
                            case "EXCUSED" -> excused++;
                        }
                    }

                    int total = rows.size();
                    double rate = total == 0 ? 0.0 : (present * 100.0) / total;

                    final int finalPresent = present;
                    final int finalAbsent = absent;
                    final int finalExcused = excused;
                    final int finalTotal = total;
                    final double finalRate = rate;
                    final List<ReportRow> finalMappedRows = new ArrayList<>(mappedRows);

                    Platform.runLater(() -> {
                        stats.getChildren().clear();
                        stats.add(AdminUI.makeStatCard("Overall Attendance Rate", String.format("%.1f%%", finalRate), "📈", "accent-green"), 0, 0);
                        stats.add(AdminUI.makeStatCard("Present", String.valueOf(finalPresent), "🟢", "accent-green"), 1, 0);
                        stats.add(AdminUI.makeStatCard("Absent", String.valueOf(finalAbsent), "🔴", "accent-orange"), 0, 1);
                        stats.add(AdminUI.makeStatCard("Excused", String.valueOf(finalExcused), "🟠", "accent-purple"), 1, 1);
                        stats.add(AdminUI.makeStatCard("Total Records", String.valueOf(finalTotal), "📄", "accent-purple"), 0, 2);

                        table.getItems().setAll(finalMappedRows);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        error.setText("Failed to load attendance report.");
                        error.setVisible(true);
                        error.setManaged(true);
                    });
                }
            }).start();
        };

        classFilter.setOnAction(e -> loadReport.run());
        timeFilter.setOnAction(e -> loadReport.run());
        studentSearch.textProperty().addListener((obs, oldV, newV) -> loadReport.run());

        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = api.getAdminClassesRaw();

                List<ClassItem> items = new ArrayList<>();
                for (Map<String, Object> c : classes) {
                    Long id = ((Number) c.get("id")).longValue();
                    String label = c.getOrDefault("classCode", "—") + " — " + c.getOrDefault("name", "Unnamed");
                    items.add(new ClassItem(id, label));
                }

                Platform.runLater(() -> {
                    classFilter.getItems().setAll(items);
                    if (!items.isEmpty()) {
                        classFilter.setValue(items.get(0));
                        loadReport.run();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    error.setText("Failed to load classes.");
                    error.setVisible(true);
                    error.setManaged(true);
                });
            }
        }).start();

        content.getChildren().addAll(
                title,
                subtitle,
                filters,
                exportBtn,
                error,
                stats,
                tableTitle,
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