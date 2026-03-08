package frontend.student;

import frontend.AppLayout;
import frontend.api.StudentAttendanceApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import dto.AttendanceStats;
=======
import javafx.application.Platform;
import javafx.concurrent.Task;
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import model.CourseClass;
import service.AttendanceService;
import service.ClassService;

import java.util.List;

import java.util.List;
import java.util.Map;

public class StudentAttendancePage {

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
    private final Label presentValueLabel = new Label("0");
    private final Label absentValueLabel = new Label("0");
    private final Label excusedValueLabel = new Label("0");
    private final Label totalDaysValueLabel = new Label("0");
    private final Label rateValueLabel = new Label("0%");

    private final ClassService classService = new ClassService(new ClassSQL());
    private final AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());
=======
    // backend base url (same port as BackendMain)
    private static final String BASE_URL = "http://localhost:8081";
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String studentName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        Long studentId = state.getUserId();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        Label title = new Label("My Attendance");
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label("View your attendance history and statistics");
        subtitle.getStyleClass().add("dash-subtitle");

        /* ================= FILTERS ================= */
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterIcon = new Label("⏷");
        filterIcon.getStyleClass().add("filter-icon");

        ComboBox<Object> classFilter = new ComboBox<>();
        classFilter.getItems().add("All Classes");
        classFilter.setValue("All Classes");
        classFilter.getStyleClass().add("filter-combo");

        try {
            List<CourseClass> classes = classService.getClassesForStudent(studentId);
            if (classes != null) {
                for (CourseClass c : classes) classFilter.getItems().add(c);
            }
        } catch (Exception ex) {
            System.err.println("Failed loading student classes: " + ex.getMessage());
        }

        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("This Month", "Last Month", "This Year");
        timeFilter.setValue("This Month");
        timeFilter.getStyleClass().add("filter-combo");

        filters.getChildren().addAll(filterIcon, classFilter, timeFilter);

        /* ================= STATS ================= */
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

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
        VBox rateCard = rateCard("Attendance Rate", rateValueLabel);

        VBox presentCard = smallStatCard("Present", presentValueLabel, "#3BAA66", "check");
        VBox absentCard  = smallStatCard("Absent",  absentValueLabel,  "#E05A5A", "x");
        VBox excusedCard = smallStatCard("Excused", excusedValueLabel, "#E09A3B", "clock");
        VBox totalDaysCard = smallStatCard("Total Days", totalDaysValueLabel, "#BFC5CC", "calendar");
=======
        // Labels we can update after API call
        Label rateValue = new Label("0%");
        rateValue.getStyleClass().add("rate-value");

        VBox rateCard = rateCardWithValue("Attendance Rate", rateValue);

        Label presentValue = new Label("0");
        Label absentValue = new Label("0");
        Label excusedValue = new Label("0");
        Label totalDaysValue = new Label("0");

        VBox presentCard = smallStatCardWithValue("Present", presentValue, "#3BAA66", "check");
        VBox absentCard  = smallStatCardWithValue("Absent",  absentValue,  "#E05A5A", "x");
        VBox excusedCard = smallStatCardWithValue("Excused", excusedValue, "#E09A3B", "clock");
        VBox totalDaysCard = smallStatCardWithValue("Total Days", totalDaysValue, "#BFC5CC", "calendar");
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java

        stats.add(rateCard, 0, 0);
        stats.add(presentCard, 1, 0);
        stats.add(absentCard, 0, 1);
        stats.add(excusedCard, 1, 1);
        stats.add(totalDaysCard, 0, 2);

        /* ================= RECORDS ================= */
        Label recordsTitle = new Label("Attendance Records");
        recordsTitle.getStyleClass().add("section-title");

        VBox recordsCard = new VBox(10);
        recordsCard.getStyleClass().add("records-card");
        recordsCard.setMinHeight(160);

        Label loadingRecords = new Label("Loading records…");
        loadingRecords.getStyleClass().add("empty-subtitle");
        recordsCard.setAlignment(Pos.CENTER);
        recordsCard.getChildren().add(loadingRecords);

        page.getChildren().addAll(
                title,
                subtitle,
                filters,
                stats,
                new Separator(),
                recordsTitle,
                recordsCard
        );

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
        updateStatsForSelection(classFilter.getValue(), timeFilter.getValue(), studentId);

        classFilter.setOnAction(e -> updateStatsForSelection(classFilter.getValue(), timeFilter.getValue(), studentId));
        timeFilter.setOnAction(e -> updateStatsForSelection(classFilter.getValue(), timeFilter.getValue(), studentId));
=======
        // Load from backend (NOT on UI thread)
        loadAttendance(jwtStore, state, rateValue, presentValue, absentValue, excusedValue, totalDaysValue, recordsCard);
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java

        return AppLayout.wrapWithSidebar(
                studentName,
                "Student Panel",
                "Dashboard",
                "Mark Attendance",
                "My Attendance",
                "Email",
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
                }
        );
    }

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
    private void updateStatsForSelection(Object selectedClassObj, String timeFilterValue, Long studentId) {
        Long classId = null;
        if (selectedClassObj instanceof CourseClass) classId = ((CourseClass) selectedClassObj).getId();

        AttendanceStats stats = null;
        try {
            if (classId == null) {
                switch (timeFilterValue) {
                    case "Last Month" -> stats = attendanceService.getStudentOverallLastMonth(studentId);
                    case "This Year" -> stats = attendanceService.getStudentOverallThisYear(studentId);
                    default -> stats = attendanceService.getStudentOverallThisMonth(studentId);
                }
            } else {
                switch (timeFilterValue) {
                    case "Last Month" -> stats = attendanceService.getStudentStatsLastMonth(studentId, classId);
                    case "This Year" -> stats = attendanceService.getStudentStatsThisYear(studentId, classId);
                    default -> stats = attendanceService.getStudentStatsThisMonth(studentId, classId);
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to load attendance stats: " + ex.getMessage());
        }

        if (stats == null) stats = new AttendanceStats(0,0,0,0);

        presentValueLabel.setText(String.valueOf(stats.getPresentCount()));
        absentValueLabel.setText(String.valueOf(stats.getAbsentCount()));
        excusedValueLabel.setText(String.valueOf(stats.getExcusedCount()));
        totalDaysValueLabel.setText(String.valueOf(stats.getTotalRecords()));
        rateValueLabel.setText(String.format("%.0f%%", stats.getAttendanceRate()));
    }

    /* ================= COMPONENTS ================= */

    private VBox rateCard(String label, Label valueLabel) {
=======
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
                    // summary
                    int present = num(summary.get("presentCount"));
                    int absent  = num(summary.get("absentCount"));
                    int excused = num(summary.get("excusedCount"));
                    int total   = num(summary.get("totalDays"));

                    double rate = dbl(summary.get("attendanceRate")); // already percent in backend example
                    // if backend returns 0..100:
                    rateValue.setText(((int) Math.round(rate)) + "%");

                    presentValue.setText(String.valueOf(present));
                    absentValue.setText(String.valueOf(absent));
                    excusedValue.setText(String.valueOf(excused));
                    totalDaysValue.setText(String.valueOf(total));

                    // records
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
                        String status = String.valueOf(r.getOrDefault("status", "—"));
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

                    Label err = new Label("Failed to load attendance: " + (e == null ? "" : e.getMessage()));
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

    /* ================= UI helpers ================= */

    private VBox emptyRecords() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Label recIcon = new Label("📅");
        recIcon.getStyleClass().add("empty-icon");
        recIcon.setFont(Font.font("Segoe UI Emoji", 18));

        Label recT = new Label("No Records Found");
        recT.getStyleClass().add("empty-title");

        Label recS = new Label("No attendance records for the selected filters");
        recS.getStyleClass().add("empty-subtitle");

        box.getChildren().addAll(recIcon, recT, recS);
        return box;
    }

    private HBox recordRow(String date, String status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("record-row");

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
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java
        VBox card = new VBox(8);
        card.getStyleClass().add("rate-card");

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("rate-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label trend = new Label("↗");
        trend.getStyleClass().add("rate-trend");

        top.getChildren().addAll(lbl, spacer, trend);

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
        valueLabel.getStyleClass().add("rate-value");

=======
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java
        card.getChildren().addAll(top, valueLabel);
        return card;
    }

<<<<<<< HEAD:frontend/src/main/java/frontend/StudentAttendancePage.java
    private VBox smallStatCard(String label, Label valueLabel, String colorHex, String iconKey) {
=======
    private VBox smallStatCardWithValue(String label, Label valueLabel, String colorHex, String iconKey) {
>>>>>>> origin/admin-api:frontend/src/main/java/frontend/student/StudentAttendancePage.java
        VBox card = new VBox(6);
        card.getStyleClass().add("mini-stat-card");

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

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

        card.getChildren().addAll(row1, row2);
        return card;
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