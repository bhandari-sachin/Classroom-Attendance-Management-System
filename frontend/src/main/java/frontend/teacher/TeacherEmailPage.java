package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class TeacherEmailPage {

    private static class ClassItem {
        final long id;
        final String label;

        ClassItem(long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();
    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String teacherName = resolveTeacherName(state);

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = buildPageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();

        ComboBox<ClassItem> classBox = buildClassBox();
        Button refreshButton = buildRefreshButton();

        HBox topRow = buildTopRow(classBox, refreshButton);
        TableView<StudentRow> table = buildStudentTable();

        page.getChildren().addAll(title, subtitle, topRow, table);

        Runnable loadStudentsForSelectedClass = () -> loadStudentsForSelectedClass(
                api,
                jwtStore,
                state,
                classBox
        );

        refreshButton.setOnAction(e -> loadStudentsForSelectedClass.run());
        classBox.setOnAction(e -> loadStudentsForSelectedClass.run());

        loadClasses(api, jwtStore, state, classBox);

        return AppLayout.wrapWithSidebar(
                teacherName,
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email"),
                page,
                "fourth",
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("teacher-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("teacher-take");
                    }

                    @Override
                    public void goReports() {
                        router.go("teacher-reports");
                    }

                    @Override
                    public void goEmail() {
                        router.go("teacher-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveTeacherName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();
    }

    private VBox buildPageContainer() {
        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("teacher.email.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("teacher.email.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private ComboBox<ClassItem> buildClassBox() {
        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(helper.getMessage("teacher.email.class_select.placeholder"));
        classBox.setMaxWidth(360);
        return classBox;
    }

    private Button buildRefreshButton() {
        Button refresh = new Button(helper.getMessage("teacher.email.button.refresh"));
        refresh.getStyleClass().addAll("pill", "pill-green");
        return refresh;
    }

    private HBox buildTopRow(ComboBox<ClassItem> classBox, Button refreshButton) {
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(classBox, refreshButton);
        return top;
    }

    private TableView<StudentRow> buildStudentTable() {
        TableView<StudentRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);

        TableColumn<StudentRow, String> nameColumn =
                new TableColumn<>(helper.getMessage("teacher.email.table.column.student"));
        nameColumn.setCellValueFactory(data -> data.getValue().studentNameProperty());

        TableColumn<StudentRow, String> emailColumn =
                new TableColumn<>(helper.getMessage("teacher.email.table.column.email"));
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        table.getColumns().addAll(nameColumn, emailColumn);
        return table;
    }

    private void loadClasses(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> classList = api.getMyClasses(jwtStore, state);
                List<ClassItem> items = classList.stream()
                        .map(this::mapClassItem)
                        .toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.email.error.classes").replace("{reason}", ex.getMessage()))
                );
            }
        }).start();
    }

    private void loadStudentsForSelectedClass(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox
    ) {
        ClassItem selectedClass = classBox.getValue();
        if (selectedClass == null) {
            rows.clear();
            return;
        }

        rows.setAll(new StudentRow(-1L, helper.getMessage("teacher.email.loading.students"), "-", "—"));

        new Thread(() -> {
            try {
                List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selectedClass.id);
                List<StudentRow> mappedRows = students.stream()
                        .map(this::mapStudentRow)
                        .toList();

                Platform.runLater(() -> rows.setAll(mappedRows));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    rows.clear();
                    showError(helper.getMessage("teacher.email.error.students").replace("{reason}", ex.getMessage()));
                });
            }
        }).start();
    }

    private ClassItem mapClassItem(Map<String, Object> classData) {
        long id = Long.parseLong(String.valueOf(classData.get("id")));
        String classCode = String.valueOf(classData.get("classCode"));
        String name = String.valueOf(classData.get("name"));
        return new ClassItem(id, classCode + " — " + name);
    }

    private StudentRow mapStudentRow(Map<String, Object> studentData) {
        long studentId = Long.parseLong(String.valueOf(studentData.get("id")));
        String firstName = String.valueOf(studentData.get("firstName"));
        String lastName = String.valueOf(studentData.get("lastName"));
        String email = String.valueOf(studentData.get("email"));
        return new StudentRow(studentId, firstName + " " + lastName, email, "—");
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}