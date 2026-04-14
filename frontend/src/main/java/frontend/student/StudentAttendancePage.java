package frontend.student;

import frontend.AppLayout;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StudentAttendancePage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String studentName = resolveStudentName(state);

        VBox page = buildPageContainer();

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

        return AppLayout.wrapWithSidebar(
                studentName,
                helper.getMessage("student.panel.title"),
                helper.getMessage("student.nav.dashboard"),
                helper.getMessage("student.nav.markAttendance"),
                helper.getMessage("student.nav.myAttendance"),
                helper.getMessage("student.nav.email"),
                scroll,
                "third",
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

    private String resolveStudentName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("student.name.placeholder")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        return page;
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
                                    .replace("{path}", destination.getAbsolutePath())
                    ));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showError(
                            helper.getMessage("student.attendance.export.error")
                                    .replace("{error}", safeErrorMessage(ex))
                    ));
                }
            }).start();
        });

        return exportPdfBtn;
    }

    private HBox buildExportRow(Button exportPdfBtn) {
        HBox exportRow = new HBox(exportPdfBtn);
        exportRow.setAlignment(Pos.CENTER_RIGHT);
        return exportRow;
    }

    private Label createRateValueLabel() {
        Label label = new Label("0%");
        label.getStyleClass().add("rate-value");
        return label;
    }

    private Label createStatValueLabel() {
        return new Label("0");
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
        stats.getStyleClass().add("dash-stats");

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

    private String localizeAttendanceStatus(String status) {
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

    private static int num(Object value) {
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

    private static double dbl(Object value) {
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

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");
        icon.setFont(Font.font("Segoe UI Emoji", 18));

        Label title = new Label(helper.getMessage("student.attendance.records.empty.title"));
        title.getStyleClass().add("empty-title");

        Label subtitle = new Label(helper.getMessage("student.attendance.records.empty.subtitle"));
        subtitle.getStyleClass().add("empty-subtitle");

        box.getChildren().addAll(icon, title, subtitle);
        return box;
    }

    private HBox recordRow(String date, String status, String rawStatus) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("record-row");

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("record-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chip = new Label(status);
        chip.getStyleClass().add("record-chip");

        String normalized = rawStatus == null ? "" : rawStatus.trim().toUpperCase();
        switch (normalized) {
            case "PRESENT" -> chip.getStyleClass().add("record-chip-present");
            case "ABSENT" -> chip.getStyleClass().add("record-chip-absent");
            case "EXCUSED" -> chip.getStyleClass().add("record-chip-excused");
            default -> {
            }
        }

        row.getChildren().addAll(dateLabel, spacer, chip);
        return row;
    }

    private VBox rateCardWithValue(String label, Label valueLabel) {
        VBox card = new VBox(8);
        card.getStyleClass().add("rate-card");

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("rate-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label trend = new Label("↗");
        trend.getStyleClass().add("rate-trend");

        top.getChildren().addAll(labelNode, spacer, trend);
        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    private VBox smallStatCardWithValue(String label, Label valueLabel, String colorHex, String iconKey) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-stat-card");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        badge.setPrefSize(26, 26);
        badge.setMinSize(26, 26);
        badge.setMaxSize(26, 26);
        badge.setStyle("-fx-background-color: " + colorHex + ";-fx-background-radius: 8;");

        Node iconNode = makeBadgeIcon(iconKey);
        badge.getChildren().add(iconNode);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("mini-label");

        topRow.getChildren().addAll(badge, labelNode);

        valueLabel.getStyleClass().add("mini-value");

        HBox valueRow = new HBox(valueLabel);
        valueRow.setPadding(new Insets(0, 0, 0, 36));

        card.getChildren().addAll(topRow, valueRow);
        return card;
    }

    private Node makeBadgeIcon(String key) {
        Color stroke = Color.WHITE;

        switch (key) {
            case "check": {
                Line first = new Line(6, 14, 11, 18);
                Line second = new Line(11, 18, 20, 8);
                first.setStroke(stroke);
                second.setStroke(stroke);
                first.setStrokeWidth(2.6);
                second.setStrokeWidth(2.6);
                first.setStrokeLineCap(StrokeLineCap.ROUND);
                second.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(first, second);
            }
            case "x": {
                Line first = new Line(7, 7, 19, 19);
                Line second = new Line(19, 7, 7, 19);
                first.setStroke(stroke);
                second.setStroke(stroke);
                first.setStrokeWidth(2.6);
                second.setStrokeWidth(2.6);
                first.setStrokeLineCap(StrokeLineCap.ROUND);
                second.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(first, second);
            }
            case "clock": {
                Circle circle = new Circle(13, 13, 8);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(stroke);
                circle.setStrokeWidth(2.2);

                Line hour = new Line(13, 13, 13, 9);
                Line minute = new Line(13, 13, 17, 13);
                hour.setStroke(stroke);
                minute.setStroke(stroke);
                hour.setStrokeWidth(2.2);
                minute.setStrokeWidth(2.2);
                hour.setStrokeLineCap(StrokeLineCap.ROUND);
                minute.setStrokeLineCap(StrokeLineCap.ROUND);

                return new Group(circle, hour, minute);
            }
            case "calendar": {
                Rectangle body = new Rectangle(7, 8, 12, 12);
                body.setFill(Color.TRANSPARENT);
                body.setStroke(stroke);
                body.setStrokeWidth(2.0);
                body.setArcWidth(3);
                body.setArcHeight(3);

                Line top = new Line(7, 11, 19, 11);
                top.setStroke(stroke);
                top.setStrokeWidth(2.0);

                Line ring1 = new Line(10, 6, 10, 9);
                Line ring2 = new Line(16, 6, 16, 9);
                ring1.setStroke(stroke);
                ring2.setStroke(stroke);
                ring1.setStrokeWidth(2.0);
                ring2.setStrokeWidth(2.0);
                ring1.setStrokeLineCap(StrokeLineCap.ROUND);
                ring2.setStrokeLineCap(StrokeLineCap.ROUND);

                return new Group(body, top, ring1, ring2);
            }
            default: {
                Circle dot = new Circle(13, 13, 3);
                dot.setFill(stroke);
                return dot;
            }
        }
    }

    private String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "Unknown error";
        }
        return throwable.getMessage();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}