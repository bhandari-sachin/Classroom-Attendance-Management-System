package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import static javafx.application.Application.launch;

public class StudentDashboardApp {

    // Dummy data (replace later from backend)
    private int presentCount = 0;
    private int absentCount = 0;
    private int excusedCount = 0;
    private double attendanceRate = 0.0; // 0.0 -> 0%

    public Parent build(Scene scene, String studentName) {

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Header
        Label title = new Label("Welcome back, " + studentName + "!");
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label("Here’s your attendance overview for this month");
        subtitle.getStyleClass().add("dash-subtitle");

        // Action card (Mark attendance)
        Button markAttendance = attendanceCard(scene, studentName);

        // Stats grid (2x2)
        GridPane stats = statsGrid();

        // Classes header row
        HBox classesHeader = new HBox(10);
        classesHeader.setAlignment(Pos.CENTER_LEFT);

        Label classesTitle = new Label("Your classes");
        classesTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Attendance  →");
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e ->
                scene.setRoot(new StudentAttendancePage().build(scene, studentName))
        );

        classesHeader.getChildren().addAll(classesTitle, spacer, viewAll);

        VBox classesCard = emptyClassesCard();

        page.getChildren().addAll(
                title,
                subtitle,
                markAttendance,
                stats,
                new Separator(),
                classesHeader,
                classesCard
        );

        // Wrap in shared sidebar layout
        return AppLayout.wrapWithSidebar(
                studentName,
                page,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() {
                        scene.setRoot(build(scene, studentName));
                    }

                    @Override public void goTakeAttendance() {
                        // Map "Take Attendance" menu item to "Mark Attendance" for students
                        scene.setRoot(new StudentMarkAttendancePage().build(scene, studentName));
                    }

                    @Override public void goReports() {
                        // Map "Reports" menu item to Student Attendance overview
                        scene.setRoot(new StudentAttendancePage().build(scene, studentName));
                    }

                    @Override public void goEmail() {
                        // Optional: student email/contact page
                        scene.setRoot(new StudentEmailPage().build(scene, studentName));
                    }

                    @Override public void logout() {
                        System.out.println("TODO: Student Logout");
                    }
                }
        );
    }

    // ===== UI blocks / helpers =====

    private Button attendanceCard(Scene scene, String studentName) {
        Button btn = new Button();
        btn.getStyleClass().add("attendance-card");
        btn.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⌁");
        icon.getStyleClass().add("attendance-icon");

        VBox texts = new VBox(2);
        Label big = new Label("Mark Attendance");
        big.getStyleClass().add("attendance-title");

        Label small = new Label("Scan the QR code to check in");
        small.getStyleClass().add("attendance-subtitle");

        texts.getChildren().addAll(big, small);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("attendance-arrow");

        box.getChildren().addAll(icon, texts, spacer, arrow);
        btn.setGraphic(box);

        btn.setOnAction(e ->
                scene.setRoot(new StudentMarkAttendancePage().build(scene, studentName))
        );

        return btn;
    }

    private GridPane statsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.getStyleClass().add("dash-stats");

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(statCardWithBadge("Present", String.valueOf(presentCount), "This month", "#3BAA66", "✓"), 0, 0);
        grid.add(statCardWithBadge("Absent", String.valueOf(absentCount), "This month", "#E05A5A", "✕"), 1, 0);
        grid.add(statCardWithBadge("Excused", String.valueOf(excusedCount), "This month", "#E09A3B", "⏱"), 0, 1);
        grid.add(statCardWithBadge("Rate", (int) (attendanceRate * 100) + "%", "This month", "#5AA6E0", "%"), 1, 1);

        return grid;
    }

    private VBox statCardWithBadge(String label, String value, String hint, String colorHex, String iconChar) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #ECEFF2;" +
                        "-fx-border-radius: 12;"
        );

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #4B5563;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane();
        badge.setMinSize(28, 28);
        badge.setMaxSize(28, 28);
        badge.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 10;"
        );

        Label icon = new Label(iconChar);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);

        top.getChildren().addAll(lbl, spacer, badge);

        Label big = new Label(value);
        big.setStyle("-fx-font-size: 28px; -fx-font-weight: 900;");

        Label small = new Label(hint);
        small.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        card.getChildren().addAll(top, big, small);
        return card;
    }

    private VBox emptyClassesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("classes-card");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(160);

        Label cal = new Label("📅");
        cal.getStyleClass().add("empty-icon");

        Label t = new Label("No classes yet");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been enrolled in any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        card.getChildren().addAll(cal, t, s);
        return card;
    }

}
