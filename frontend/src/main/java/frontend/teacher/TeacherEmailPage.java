package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? t("teacher.fallback.name", "Teacher")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label(t("teacher.email.title", "Student Emails"));
        title.getStyleClass().add("title");

        Label info = new Label(t("teacher.email.subtitle", "Select a class to view student email addresses"));
        info.getStyleClass().add("subtitle");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(t("teacher.email.class_select.placeholder", "Select a class"));
        classBox.setMaxWidth(360);

        Button refresh = new Button(t("teacher.email.button.refresh", "Refresh"));
        refresh.getStyleClass().addAll("pill", "pill-green");

        top.getChildren().addAll(classBox, refresh);

        TableView<StudentRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);

        TableColumn<StudentRow, String> colName = new TableColumn<>(t("teacher.email.table.column.student", "Student"));
        colName.setCellValueFactory(d -> d.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>(t("teacher.email.table.column.email", "Email"));
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, top, table);

        Runnable loadStudentsForSelectedClass = () -> {
            ClassItem selected = classBox.getValue();
            if (selected == null) {
                rows.clear();
                return;
            }

            rows.setAll(new StudentRow(-1L, t("teacher.email.loading.students", "Loading students..."), "-", "—"));

            new Thread(() -> {
                try {
                    List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selected.id);

                    Platform.runLater(() -> {
                        rows.clear();

                        for (var s : students) {
                            long studentId = Long.parseLong(String.valueOf(s.get("id")));
                            String fn = String.valueOf(s.get("firstName"));
                            String ln = String.valueOf(s.get("lastName"));
                            String email = String.valueOf(s.get("email"));

                            rows.add(new StudentRow(studentId, fn + " " + ln, email, "—"));
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        rows.clear();
                        new Alert(
                                Alert.AlertType.ERROR,
                                t("teacher.email.error.students", "Failed to load students: {reason}")
                                        .replace("{reason}", ex.getMessage()),
                                ButtonType.OK
                        ).showAndWait();
                    });
                }
            }).start();
        };

        new Thread(() -> {
            try {
                List<Map<String, Object>> list = api.getMyClasses(jwtStore, state);

                var items = list.stream().map(m -> {
                    long id = Long.parseLong(String.valueOf(m.get("id")));
                    String classCode = String.valueOf(m.get("classCode"));
                    String name = String.valueOf(m.get("name"));
                    return new ClassItem(id, classCode + " — " + name);
                }).toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(
                                Alert.AlertType.ERROR,
                                t("teacher.email.error.classes", "Failed to load classes: {reason}")
                                        .replace("{reason}", ex.getMessage()),
                                ButtonType.OK
                        ).showAndWait()
                );
            }
        }).start();

        classBox.setOnAction(e -> loadStudentsForSelectedClass.run());
        refresh.setOnAction(e -> loadStudentsForSelectedClass.run());

        return AppLayout.wrapWithSidebar(
                teacherName,
                t("teacher.sidebar.title", "Teacher Panel"),
                t("teacher.sidebar.menu.dashboard", "Dashboard"),
                t("teacher.sidebar.menu.take_attendance", "Take Attendance"),
                t("teacher.sidebar.menu.reports", "Reports"),
                t("teacher.sidebar.menu.email", "Email"),
                page,
                "fourth",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("teacher-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("teacher-take"); }
                    @Override public void goReports() { router.go("teacher-reports"); }
                    @Override public void goEmail() { router.go("teacher-email"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}