package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

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
                ? I18n.t("teacher.fallback.name")
                : state.getName();

        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        RtlUtil.apply(page);

        Label title = new Label(I18n.t("teacher.email.title"));
        title.getStyleClass().add("title");

        Label info = new Label(I18n.t("teacher.email.subtitle"));
        info.getStyleClass().add("subtitle");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(top);

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(I18n.t("teacher.email.class_select.placeholder"));
        classBox.setMaxWidth(360);
        RtlUtil.apply(classBox);

        Button refresh = new Button(I18n.t("teacher.email.button.refresh"));
        refresh.getStyleClass().addAll("pill", "pill-green");

        top.getChildren().addAll(classBox, refresh);

        TableView<StudentRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(340);
        RtlUtil.apply(table);

        TableColumn<StudentRow, String> colName = new TableColumn<>(I18n.t("teacher.email.table.column.student"));
        colName.setCellValueFactory(d -> d.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>(I18n.t("teacher.email.table.column.email"));
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        table.getColumns().addAll(colName, colEmail);

        page.getChildren().addAll(title, info, top, table);

        Runnable loadStudentsForSelectedClass = () -> {
            ClassItem selected = classBox.getValue();
            if (selected == null) {
                rows.clear();
                return;
            }

            rows.setAll(new StudentRow(-1L, I18n.t("teacher.email.loading.students"), "-", "—"));

            new Thread(() -> {
                try {
                    List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selected.id);

                    Platform.runLater(() -> {
                        rows.clear();

                        for (var s : students) {
                            long studentId = Long.parseLong(String.valueOf(s.get("id")));
                            String fn = String.valueOf(s.getOrDefault("firstName", ""));
                            String ln = String.valueOf(s.getOrDefault("lastName", ""));
                            String email = String.valueOf(s.getOrDefault("email", ""));

                            rows.add(new StudentRow(studentId, (fn + " " + ln).trim(), email, "—"));
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        rows.clear();
                        new Alert(
                                Alert.AlertType.ERROR,
                                I18n.t("teacher.email.error.students").replace("{reason}", ex.getMessage()),
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
                    String classCode = String.valueOf(m.getOrDefault("classCode", "—"));
                    String name = String.valueOf(m.getOrDefault("name", I18n.t("teacher.dashboard.classes.unnamed")));
                    return new ClassItem(id, classCode + " — " + name);
                }).toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(
                                Alert.AlertType.ERROR,
                                I18n.t("teacher.email.error.classes").replace("{reason}", ex.getMessage()),
                                ButtonType.OK
                        ).showAndWait()
                );
            }
        }).start();

        classBox.setOnAction(e -> loadStudentsForSelectedClass.run());
        refresh.setOnAction(e -> loadStudentsForSelectedClass.run());

        return AppLayout.wrapWithSidebar(
                teacherName,
                I18n.t("teacher.sidebar.title"),
                I18n.t("teacher.sidebar.menu.dashboard"),
                I18n.t("teacher.sidebar.menu.take_attendance"),
                I18n.t("teacher.sidebar.menu.reports"),
                I18n.t("teacher.sidebar.menu.email"),
                I18n.t("teacher.sidebar.logout"),
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
                },
                router,
                I18n.isRtl()
        );
    }
}