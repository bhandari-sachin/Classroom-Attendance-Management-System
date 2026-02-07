package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TeacherDashboardApp {

    // Dummy data (replace later from backend)
    private int totalClasses = 0;
    private int totalStudents = 0;
    private int presentToday = 0;
    private int absentToday = 0;

    public Parent build(Scene scene, String teacherName) {

        // You can auto-fill totalStudents from DataStore if you want:
        // totalStudents = DataStore.getStudents().size();

        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");

        // Header
        Label greeting = new Label("Good afternoon, " + teacherName + "!");
        greeting.getStyleClass().add("dash-title");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("dash-subtitle");

        // Action cards row
        HBox actionsRow = new HBox(14);
        actionsRow.getStyleClass().add("dash-actions");

        VBox takeCard = bigActionCard(
                "Take Attendance",
                "Generate QR code for your class",
                "card-green",
                () -> scene.setRoot(new TeacherTakeAttendancePage().build(scene, teacherName))
        );

        VBox reportCard = bigActionCard(
                "View Reports",
                "Class Attendance Reports",
                "card-purple",
                () -> scene.setRoot(new TeacherReportsPage().build(scene, teacherName))
        );

        HBox.setHgrow(takeCard, Priority.ALWAYS);
        HBox.setHgrow(reportCard, Priority.ALWAYS);
        actionsRow.getChildren().addAll(takeCard, reportCard);

        // Stats grid (2x2)
        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        stats.getStyleClass().add("dash-stats");

        stats.add(statCard("My Classes", totalClasses), 0, 0);
        stats.add(statCard("Total Students", totalStudents), 1, 0);
        stats.add(statCard("Present Today", presentToday), 0, 1);
        stats.add(statCard("Absent Today", absentToday), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setFillWidth(true);

        stats.getColumnConstraints().addAll(c1, c2);

        // My classes section
        Label classesTitle = new Label("My classes");
        classesTitle.getStyleClass().add("section-title");

        VBox emptyClasses = new VBox(8);
        emptyClasses.setAlignment(Pos.CENTER);
        emptyClasses.setMinHeight(170);
        emptyClasses.getStyleClass().add("classes-card");

        Label icon = new Label("📅");
        icon.getStyleClass().add("empty-icon");

        Label t = new Label("No classes Assigned");
        t.getStyleClass().add("empty-title");

        Label s = new Label("You haven’t been assigned any classes yet.");
        s.getStyleClass().add("empty-subtitle");

        emptyClasses.getChildren().addAll(icon, t, s);

        page.getChildren().addAll(
                greeting,
                dateLabel,
                actionsRow,
                stats,
                new Separator(),
                classesTitle,
                emptyClasses
        );

        return AppLayout.wrapWithSidebar(
                teacherName,
                page,
                "dashboard",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(build(scene, teacherName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new TeacherTakeAttendancePage().build(scene, teacherName)); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, teacherName)); }
                    @Override public void goEmail() { scene.setRoot(new TeacherEmailPage().build(scene, teacherName)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }

    // ===== Helpers MUST be inside the class =====

    private VBox bigActionCard(String title, String subtitle, String styleClass, Runnable action) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("big-card", styleClass);

        Label t = new Label(title);
        t.getStyleClass().add("big-card-title");

        Label s = new Label(subtitle);
        s.getStyleClass().add("big-card-subtitle");

        card.getChildren().addAll(t, s);
        card.setOnMouseClicked(e -> action.run());
        return card;
    }

    private VBox statCard(String label, int value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("stat-value");

        card.getChildren().addAll(l, v);
        return card;
    }
}
