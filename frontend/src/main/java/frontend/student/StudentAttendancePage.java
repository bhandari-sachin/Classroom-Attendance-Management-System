package frontend.student;

import frontend.AppLayout;
import frontend.api.ReportApi;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import util.I18n;
import util.RtlUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StudentAttendancePage {

    private static final String BASE_URL =
            System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(I18n.t("student.attendance.title"));
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label(I18n.t("student.attendance.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");

        /* ================= FILTERS ================= */
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(filters);

        Label filterIcon = new Label("⏷");
        filterIcon.getStyleClass().add("filter-icon");

        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().addAll(I18n.t("student.attendance.filter.class.all"));
        classFilter.setValue(I18n.t("student.attendance.filter.class.all"));
        classFilter.getStyleClass().add("filter-combo");
        RtlUtil.apply(classFilter);

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll(
                I18n.t("student.attendance.filter.time.thisMonth"),
                I18n.t("student.attendance.filter.time.lastMonth"),
                I18n.t("student.attendance.filter.time.thisYear")
        );
        timeFilter.setValue(I18n.t("student.attendance.filter.time.thisMonth"));
        timeFilter.getStyleClass().add("filter-combo");
        RtlUtil.apply(timeFilter);

        filters.getChildren().addAll(filterIcon, classFilter, timeFilter);

        /* ================= EXPORT ================= */
        ReportApi reportApi = new ReportApi(BASE_URL);

        Button exportPdfBtn = new Button(I18n.t("common.export.pdf"));
        exportPdfBtn.getStyleClass().addAll("pill", "pill-blue");

        exportPdfBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(I18n.t("student.attendance.export.file.title"));
            fc.setInitialFileName(I18n.t("student.attendance.export.file.name"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File dest = fc.showSaveDialog(scene.getWindow());
            if (dest == null) return;

            new Thread(() -> {
                try {
                    reportApi.exportStudentPdf(jwtStore, state, dest.getAbsolutePath());
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.INFORMATION,
                            I18n.t("student.attendance.export.success").replace("{path}", dest.getAbsolutePath()),
                            ButtonType.OK
                    ).showAndWait());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> new Alert(
                            Alert.AlertType.ERROR,
                            I18n.t("student.attendance.export.error").replace("{error}", ex.getMessage()),
                            ButtonType.OK
                    ).showAndWait());
                }
            }).start();
        });

        /* ================= STATS ================= */
        HBox exportRow = new HBox(exportPdfBtn);
        exportRow.setAlignment(Pos.CENTER_RIGHT);
        RtlUtil.apply(exportRow);

        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        stats.getStyleClass().add("dash-stats");
        RtlUtil.apply(stats);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        Label rateValue = new Label("0%");
        rateValue.getStyleClass().add("rate-value");

        VBox rateCard = rateCardWithValue(I18n.t("student.attendance.stats.rate"), rateValue);

        Label presentValue = new Label("0");
        Label absentValue = new Label("0");
        Label excusedValue = new Label("0");
        Label totalDaysValue = new Label("0");

        VBox presentCard = smallStatCardWithValue(
                I18n.t("student.attendance.stats.present"),
                presentValue,
                "#3BAA66",
                "check"
        );
        VBox absentCard = smallStatCardWithValue(
                I18n.t("student.attendance.stats.absent"),
                absentValue,
                "#E05A5A",
                "x"
        );
        VBox excusedCard = smallStatCardWithValue(
                I18n.t("student.attendance.stats.excused"),
                excusedValue,
                "#E09A3B",
                "clock"
        );
        VBox totalDaysCard = smallStatCardWithValue(
                I18n.t("student.attendance.stats.totalDays"),
                totalDaysValue,
                "#BFC5CC",
                "calendar"
        );

        stats.add(rateCard, 0, 0);
        stats.add(presentCard, 1, 0);
        stats.add(absentCard, 0, 1);
        stats.add(excusedCard, 1, 1);
        stats.add(totalDaysCard, 0, 2);

        /* ================= RECORDS ================= */
        Label recordsTitle = new Label(I18n.t("student.attendance.records.title"));
        recordsTitle.getStyleClass().add("section-title");

        VBox recordsCard = new VBox(10);
        recordsCard.getStyleClass().add("records-card");
        recordsCard.setMinHeight(160);
        RtlUtil.apply(recordsCard);

        Label loadingRecords = new Label(I18n.t("student.attendance.records.loading"));
        loadingRecords.getStyleClass().add("empty-subtitle");
        recordsCard.setAlignment(Pos.CENTER);
        recordsCard.getChildren().add(loadingRecords);

        page.getChildren().addAll(
                title,
                subtitle,
                filters,
                exportRow,
                stats,
                new Separator(),
                recordsTitle,
                recordsCard
        );

        loadAttendance(jwtStore, state, rateValue, presentValue, absentValue, excusedValue, totalDaysValue, recordsCard);

        return AppLayout.wrapWithSidebar(
                studentName,
                I18n.t("student.panel.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("student.nav.markAttendance"),
                I18n.t("student.nav.myAttendance"),
                I18n.t("student.nav.email"),
                I18n.t("student.nav.logout"),
                page,
                "third",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("student-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("student-mark"); }
                    @Override public void goReports() { router.go("student-attendance"); }
                    @Override public void goEmail() { router.go("student-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                },
                router,
                I18n.isRtl()
        );
    }

    private void loadAttendance(JwtStore jwtStore,
                                AuthState state,
                                Label rateValue,
                                Label presentValue,
                                Label absentValue,
                                Label excusedValue,
                                Label totalDaysValue,
                                VBox recordsCard) {

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
                Platform.runLater(() -> {
                    int present = num(summary.get("presentCount"));
                    int absent = num(summary.get("absentCount"));
                    int excused = num(summary.get("excusedCount"));
                    int total = num(summary.get("totalDays"));

                    double rate = dbl(summary.get("attendanceRate"));
                    rateValue.setText(((int) Math.round(rate)) + "%");

                    presentValue.setText(String.valueOf(present));
                    absentValue.setText(String.valueOf(absent));
                    excusedValue.setText(String.valueOf(excused));
                    totalDaysValue.setText(String.valueOf(total));

                    recordsCard.getChildren().clear();
                    recordsCard.setAlignment(Pos.TOP_LEFT);

                    if (records == null || records.isEmpty()) {
                        VBox empty = emptyRecords();
                        recordsCard.setAlignment(Pos.CENTER);
                        recordsCard.getChildren().add(empty);
                        return;
                    }

                    for (Map<String, Object> r : records) {
                        String date = String.valueOf(r.getOrDefault("sessionDate", "—"));
                        String status = localizeStatus(String.valueOf(r.getOrDefault("status", "—")));
                        recordsCard.getChildren().add(recordRow(date, status));
                    }
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                Platform.runLater(() -> {
                    recordsCard.getChildren().clear();
                    recordsCard.setAlignment(Pos.CENTER);

                    Label err = new Label(
                            I18n.t("student.attendance.error.load")
                                    .replace("{error}", e == null ? "" : e.getMessage())
                    );
                    err.getStyleClass().add("empty-subtitle");
                    recordsCard.getChildren().add(err);
                });
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private static int num(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private static double dbl(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private VBox emptyRecords() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        RtlUtil.apply(box);

        Label recIcon = new Label("📅");
        recIcon.getStyleClass().add("empty-icon");
        recIcon.setFont(Font.font("Segoe UI Emoji", 18));

        Label recT = new Label(I18n.t("student.attendance.records.empty.title"));
        recT.getStyleClass().add("empty-title");

        Label recS = new Label(I18n.t("student.attendance.records.empty.subtitle"));
        recS.getStyleClass().add("empty-subtitle");

        box.getChildren().addAll(recIcon, recT, recS);
        return box;
    }

    private HBox recordRow(String date, String status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("record-row");
        RtlUtil.apply(row);

        Label d = new Label(date);
        d.getStyleClass().add("record-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chip = new Label(status);
        chip.getStyleClass().add("record-chip");

        row.getChildren().addAll(d, spacer, chip);
        return row;
    }

    private VBox rateCardWithValue(String label, Label valueLabel) {
        VBox card = new VBox(8);
        card.getStyleClass().add("rate-card");
        RtlUtil.apply(card);

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);
        RtlUtil.apply(top);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("rate-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label trend = new Label("↗");
        trend.getStyleClass().add("rate-trend");

        top.getChildren().addAll(lbl, spacer, trend);

        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    private VBox smallStatCardWithValue(String label, Label valueLabel, String colorHex, String iconKey) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-stat-card");
        RtlUtil.apply(card);

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(row1);

        StackPane badge = new StackPane();
        badge.setPrefSize(26, 26);
        badge.setMinSize(26, 26);
        badge.setMaxSize(26, 26);

        badge.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 8;"
        );

        Node iconNode = makeBadgeIcon(iconKey);
        badge.getChildren().add(iconNode);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("mini-label");

        row1.getChildren().addAll(badge, lbl);

        valueLabel.getStyleClass().add("mini-value");

        HBox row2 = new HBox(valueLabel);
        row2.setPadding(new Insets(0, 0, 0, 36));
        RtlUtil.apply(row2);

        card.getChildren().addAll(row1, row2);
        return card;
    }

    private static String localizeStatus(String rawStatus) {
        if (rawStatus == null) return "—";

        return switch (rawStatus.toUpperCase()) {
            case "PRESENT" -> I18n.t("common.attendance.present");
            case "ABSENT" -> I18n.t("common.attendance.absent");
            case "EXCUSED" -> I18n.t("common.attendance.excused");
            default -> rawStatus;
        };
    }

    private Node makeBadgeIcon(String key) {
        Color stroke = Color.WHITE;

        switch (key) {
            case "check": {
                Line l1 = new Line(6, 14, 11, 18);
                Line l2 = new Line(11, 18, 20, 8);
                l1.setStroke(stroke); l2.setStroke(stroke);
                l1.setStrokeWidth(2.6); l2.setStrokeWidth(2.6);
                l1.setStrokeLineCap(StrokeLineCap.ROUND);
                l2.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(l1, l2);
            }
            case "x": {
                Line a = new Line(7, 7, 19, 19);
                Line b = new Line(19, 7, 7, 19);
                a.setStroke(stroke); b.setStroke(stroke);
                a.setStrokeWidth(2.6); b.setStrokeWidth(2.6);
                a.setStrokeLineCap(StrokeLineCap.ROUND);
                b.setStrokeLineCap(StrokeLineCap.ROUND);
                return new Group(a, b);
            }
            case "clock": {
                Circle c = new Circle(13, 13, 8);
                c.setFill(Color.TRANSPARENT);
                c.setStroke(stroke);
                c.setStrokeWidth(2.2);

                Line h = new Line(13, 13, 13, 9);
                Line m = new Line(13, 13, 17, 13);
                h.setStroke(stroke); m.setStroke(stroke);
                h.setStrokeWidth(2.2); m.setStrokeWidth(2.2);
                h.setStrokeLineCap(StrokeLineCap.ROUND);
                m.setStrokeLineCap(StrokeLineCap.ROUND);

                return new Group(c, h, m);
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
                ring1.setStroke(stroke); ring2.setStroke(stroke);
                ring1.setStrokeWidth(2.0); ring2.setStrokeWidth(2.0);
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
}