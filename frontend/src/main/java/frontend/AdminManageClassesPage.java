package frontend;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.CourseClass;
import service.ClassService;

public class AdminManageClassesPage {

    ClassSQL classSQL = new ClassSQL();
    ClassService classService = new ClassService(classSQL);

    private void loadClasses(TableView<ClassRow> table) {
        table.getItems().clear();

        for (CourseClass c : classService.getAllClasses()) {
            int count = classService.getEnrollmentCount(c.getId());
            table.getItems().add(
                    new ClassRow(
                            c.getName(),
                            c.getClassCode(),
                            c.getTeacherName(),
                            c.getTeacherEmail(),
                            c.getSemester() + " " + c.getAcademicYear(),
                            count
                    )
            );
        }
    }

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

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
        loadClasses(table);

        content.getChildren().addAll(titleRow, search, section, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                "Admin Panel",
                "Dashboard",
                "Manage Classes",
                "Manage Users",
                "Attendance Reports",
                scroll,
                "second", // ✅ active = Manage Classes
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-users"); }
                    @Override public void goEmail() { router.go("admin-reports"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}