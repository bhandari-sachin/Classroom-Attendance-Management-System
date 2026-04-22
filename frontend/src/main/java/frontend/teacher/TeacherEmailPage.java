package frontend.teacher;

import frontend.ui.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherEmailPage {

    private static final Logger LOGGER =
            Logger.getLogger(TeacherEmailPage.class.getName());

    static class ClassItem {
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
        String teacherName = TeacherPageSupport.resolveTeacherName(state, helper);

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = TeacherPageSupport.buildPageContainer();

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

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return TeacherPageSupport.wrapWithSidebar(
                teacherName,
                helper,
                scroll,
                "fourth",
                router,
                jwtStore
        );
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
                LOGGER.log(Level.SEVERE, "Failed to load classes for teacher email page.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.email.error.classes")
                                .replace("{reason}", ex.getMessage() == null ? "Unknown error" : ex.getMessage()))
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

        rows.setAll(new StudentRow(
                -1L,
                helper.getMessage("teacher.email.loading.students"),
                "-",
                "—"
        ));

        new Thread(() -> {
            try {
                List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selectedClass.id);
                List<StudentRow> mappedRows = students.stream()
                        .map(this::mapStudentRow)
                        .toList();

                Platform.runLater(() -> rows.setAll(mappedRows));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load students for selected class on teacher email page.", ex);
                Platform.runLater(() -> {
                    rows.clear();
                    showError(helper.getMessage("teacher.email.error.students")
                            .replace("{reason}", ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
                });
            }
        }).start();
    }

    ClassItem mapClassItem(Map<String, Object> classData) {
        long id = Long.parseLong(String.valueOf(classData.get("id")));
        String classCode = String.valueOf(classData.get("classCode"));
        String name = String.valueOf(classData.get("name"));
        return new ClassItem(id, classCode + " — " + name);
    }

    StudentRow mapStudentRow(Map<String, Object> studentData) {
        long studentId = Long.parseLong(String.valueOf(studentData.get("id")));
        String firstName = String.valueOf(studentData.get("firstName"));
        String lastName = String.valueOf(studentData.get("lastName"));
        String email = String.valueOf(studentData.get("email"));

        return new StudentRow(
                studentId,
                (firstName + " " + lastName).trim(),
                email,
                "—"
        );
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}