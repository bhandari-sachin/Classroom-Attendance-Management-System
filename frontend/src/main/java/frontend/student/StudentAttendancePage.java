package frontend.student;

import frontend.api.ReportApi;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentAttendancePage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private static final Logger LOGGER = Logger.getLogger(StudentAttendancePage.class.getName());

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String studentName = StudentPageSupport.resolveStudentName(state, helper);

        VBox page = StudentPageSupport.buildPageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();

        HBox filters = buildFilters();

        Button exportPdfBtn = buildExportPdfButton(scene, jwtStore, state);
        HBox exportRow = buildExportRow(exportPdfBtn);

        Label rateValue = createRateValueLabel();
        Label presentValue = createStatValueLabel();
        Label absentValue = createStatValueLabel();
        Label excusedValue = createStatValueLabel();
        Label totalDaysValue = createStatValueLabel();

        GridPane statsGrid = buildStatsGrid(
                rateValue,
                presentValue,
                absentValue,
                excusedValue,
                totalDaysValue
        );

        Label recordsTitle = buildRecordsTitle();
        VBox recordsCard = buildRecordsCard();

        page.getChildren().addAll(
                title,
                subtitle,
                filters,
                exportRow,
                statsGrid,
                new Separator(),
                recordsTitle,
                recordsCard
        );

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        loadAttendance(
                jwtStore,
                state,
                rateValue,
                presentValue,
                absentValue,
                excusedValue,
                totalDaysValue,
                recordsCard
        );

        return StudentPageSupport.wrapWithSidebar(
                studentName,
                helper,
                scroll,
                "third",
                router,
                jwtStore
        );
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("student.attendance.title"));
        title.getStyleClass().add("dash-title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("student.attendance.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");
        return subtitle;
    }

    private HBox buildFilters() {
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterIcon = new Label("⏷");
        filterIcon.getStyleClass().add("filter-icon");

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll(
                helper.getMessage("student.attendance.filter.all"),
                "OOP1",
                "Databases",
                "Web Dev"
        );
        classFilter.setValue(helper.getMessage("student.attendance.filter.all"));
        classFilter.getStyleClass().add("filter-combo");

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll(
                helper.getMessage("student.attendance.filter.time.thisMonth"),
                helper.getMessage("student.attendance.filter.lastMonth"),
                helper.getMessage("student.attendance.filter.thisYear")
        );
        timeFilter.setValue(helper.getMessage("student.attendance.filter.time.thisMonth"));
        timeFilter.getStyleClass().add("filter-combo");

        filters.getChildren().addAll(filterIcon, classFilter, timeFilter);
        return filters;
    }

    private Button buildExportPdfButton(Scene scene, JwtStore jwtStore, AuthState state) {
        ReportApi reportApi = new ReportApi(BASE_URL);

        Button exportPdfBtn = new Button(helper.getMessage("common.export.pdf"));
        exportPdfBtn.getStyleClass().addAll("pill", "pill-blue");

        exportPdfBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(helper.getMessage("student.attendance.export.file.title"));
            chooser.setInitialFileName(helper.getMessage("student.attendance.export.file.name"));
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File destination = chooser.showSaveDialog(scene.getWindow());
            if (destination == null) {
                return;
            }

            new Thread(() -> {
                try {
                    reportApi.exportStudentPdf(jwtStore, state, destination.getAbsolutePath());
                    Platform.runLater(() -> showInfo(
                            helper.getMessage("student.attendance.export.success")
                    ));
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to export student attendance PDF.", ex);
                    Platform.runLater(() -> showError(
                            helper.getMessage("student.attendance.export.error")
                                    .replace("{error}", ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                    ));
                }
            }).start();
        });

        return exportPdfBtn;
    }

    private HBox buildExportRow(Button exportPdfBtn) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, spacer, exportPdfBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label createRateValueLabel() {
        Label label = new Label("0%");
        label.getStyleClass().add("rate-value");
        return label;
    }

    private Label createStatValueLabel() {
        Label label = new Label("0");
        label.getStyleClass().add("stat-number");
        return label;
    }

    private GridPane buildStatsGrid(
            Label rateValue,
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label totalDaysValue
    ) {
        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        VBox rateCard = rateCardWithValue(
                helper.getMessage("student.attendance.stats.rate"),
                rateValue
        );
        VBox presentCard = smallStatCardWithValue(
                helper.getMessage("student.attendance.stats.present"),
                presentValue,
                "#3BAA66",
                "check"
        );
        VBox absentCard = smallStatCardWithValue(
                helper.getMessage("student.attendance.stats.absent"),
                absentValue,
                "#E05A5A",
                "x"
        );
        VBox excusedCard = smallStatCardWithValue(
                helper.getMessage("student.attendance.stats.excused"),
                excusedValue,
                "#E09A3B",
                "clock"
        );
        VBox totalDaysCard = smallStatCardWithValue(
                helper.getMessage("student.attendance.stats.totalDays"),
                totalDaysValue,
                "#BFC5CC",
                "calendar"
        );

        stats.add(rateCard, 0, 0);
        stats.add(presentCard, 1, 0);
        stats.add(absentCard, 0, 1);
        stats.add(excusedCard, 1, 1);
        stats.add(totalDaysCard, 0, 2);

        return stats;
    }

    private VBox rateCardWithValue(String titleText, Label rateValue) {
        VBox card = new VBox(12);
        card.getStyleClass().add("rate-card");
        card.setPadding(new Insets(18));

        Label title = new Label(titleText);
        title.getStyleClass().add("stat-title");

        StackPane gaugeWrap = new StackPane(buildRateGraphic(), rateValue);
        gaugeWrap.setAlignment(Pos.CENTER);
        gaugeWrap.setMinHeight(120);

        card.getChildren().addAll(title, gaugeWrap);
        return card;
    }

    private Node buildRateGraphic() {
        Circle track = new Circle(42);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#E5E7EB"));
        track.setStrokeWidth(8);

        Line needle = new Line(0, 0, 26, -18);
        needle.setStroke(Color.web("#3B82F6"));
        needle.setStrokeWidth(4);
        needle.setStrokeLineCap(StrokeLineCap.ROUND);

        Circle center = new Circle(5, Color.web("#1F2937"));

        return new Group(track, needle, center);
    }

    private VBox smallStatCardWithValue(
            String titleText,
            Label valueLabel,
            String colorHex,
            String iconName
    ) {
        VBox card = new VBox(8);
        card.getStyleClass().add("small-stat-card");
        card.setPadding(new Insets(18));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.getStyleClass().add("stat-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 10;");

        Label icon = new Label(resolveStatIcon(iconName));
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(title, spacer, badge);

        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    String resolveStatIcon(String iconName) {
        return switch (iconName) {
            case "check" -> "✓";
            case "x" -> "✕";
            case "clock" -> "⏱";
            case "calendar" -> "📅";
            default -> "•";
        };
    }

    private Label buildRecordsTitle() {
        Label recordsTitle = new Label(helper.getMessage("student.attendance.records.title"));
        recordsTitle.getStyleClass().add("section-title");
        return recordsTitle;
    }

    private VBox buildRecordsCard() {
        VBox recordsCard = new VBox(10);
        recordsCard.getStyleClass().add("records-card");
        recordsCard.setMinHeight(160);
        recordsCard.setAlignment(Pos.CENTER);

        Label loadingRecords = new Label(helper.getMessage("student.attendance.records.loading"));
        loadingRecords.getStyleClass().add("empty-subtitle");
        recordsCard.getChildren().add(loadingRecords);

        return recordsCard;
    }

    private void loadAttendance(
            JwtStore jwtStore,
            AuthState state,
            Label rateValue,
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label totalDaysValue,
            VBox recordsCard
    ) {
        StudentAttendanceApi api = new StudentAttendanceApi(BASE_URL);

        Task<Void> task = new Task<>() {
            Map<String, Object> summary;
            List<Map<String, Object>> records;

            @Override
            protected Void call() throws Exception {
                summary = api.getSummary(jwtStore, state);
                records = api.getRecords(jwtStore, state);
                return null;
            }

            @Override
            protected void succeeded() {
                updateAttendanceUi(
                        summary,
                        records,
                        rateValue,
                        presentValue,
                        absentValue,
                        excusedValue,
                        totalDaysValue,
                        recordsCard
                );
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                Platform.runLater(() -> showRecordsError(recordsCard, exception));
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateAttendanceUi(
            Map<String, Object> summary,
            List<Map<String, Object>> records,
            Label rateValue,
            Label presentValue,
            Label absentValue,
            Label excusedValue,
            Label totalDaysValue,
            VBox recordsCard
    ) {
        Platform.runLater(() -> {
            int present = num(summary.get("presentCount"));
            int absent = num(summary.get("absentCount"));
            int excused = num(summary.get("excusedCount"));
            int totalDays = num(summary.get("totalDays"));
            double rate = dbl(summary.get("attendanceRate"));

            rateValue.setText(Math.round(rate) + "%");
            presentValue.setText(String.valueOf(present));
            absentValue.setText(String.valueOf(absent));
            excusedValue.setText(String.valueOf(excused));
            totalDaysValue.setText(String.valueOf(totalDays));

            renderRecords(recordsCard, records);
        });
    }

    private void renderRecords(VBox recordsCard, List<Map<String, Object>> records) {
        recordsCard.getChildren().clear();

        if (records == null || records.isEmpty()) {
            recordsCard.setAlignment(Pos.CENTER);
            recordsCard.getChildren().add(emptyRecords());
            return;
        }

        recordsCard.setAlignment(Pos.TOP_LEFT);

        for (Map<String, Object> record : records) {
            String date = String.valueOf(record.getOrDefault("sessionDate", "—"));
            String rawStatus = String.valueOf(record.getOrDefault("status", "—"));
            String localizedStatus = localizeAttendanceStatus(rawStatus);
            recordsCard.getChildren().add(recordRow(date, localizedStatus, rawStatus));
        }
    }

    private HBox recordRow(String date, String localizedStatus, String rawStatus) {
        HBox row = new HBox(12);
        row.getStyleClass().add("record-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("record-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(localizedStatus);
        statusLabel.getStyleClass().add("record-status");

        String normalized = rawStatus == null ? "" : rawStatus.trim().toUpperCase();
        switch (normalized) {
            case "PRESENT" -> statusLabel.getStyleClass().add("status-present");
            case "ABSENT" -> statusLabel.getStyleClass().add("status-absent");
            case "EXCUSED" -> statusLabel.getStyleClass().add("status-excused");
            default -> {
            }
        }

        row.getChildren().addAll(dateLabel, spacer, statusLabel);
        return row;
    }

    private void showRecordsError(VBox recordsCard, Throwable exception) {
        recordsCard.getChildren().clear();
        recordsCard.setAlignment(Pos.CENTER);

        Label error = new Label(
                helper.getMessage("student.attendance.error.load")
                        .replace("{error}", safeErrorMessage(exception))
        );
        error.getStyleClass().add("empty-subtitle");
        recordsCard.getChildren().add(error);
    }

    String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank()) {
            return "—";
        }

        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> helper.getMessage("student.attendance.stats.present");
            case "ABSENT" -> helper.getMessage("student.attendance.stats.absent");
            case "EXCUSED" -> helper.getMessage("student.attendance.stats.excused");
            default -> status;
        };
    }

    static int num(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    static double dbl(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private VBox emptyRecords() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Label empty = new Label(helper.getMessage("student.attendance.records.empty"));
        empty.getStyleClass().add("empty-subtitle");

        box.getChildren().add(empty);
        return box;
    }

    String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "Unknown error";
        }
        return throwable.getMessage();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
}