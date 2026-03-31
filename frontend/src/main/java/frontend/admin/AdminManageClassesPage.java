package frontend.admin;

import frontend.ClassRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.I18n;
import util.RtlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminManageClassesPage {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        RtlUtil.apply(content);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(titleRow);

        VBox titleCol = new VBox(4);
        RtlUtil.apply(titleCol);

        Label title = new Label(I18n.t("admin.classes.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(I18n.t("admin.classes.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button enrollBtn = new Button(I18n.t("admin.classes.button.enroll"));
        enrollBtn.getStyleClass().add("secondary-btn");

        Button add = new Button(I18n.t("admin.classes.button.add"));
        add.getStyleClass().add("primary-btn");

        titleRow.getChildren().addAll(titleCol, spacer, enrollBtn, add);

        TextField search = new TextField();
        search.setPromptText(I18n.t("admin.classes.search.placeholder"));
        search.getStyleClass().add("search-field");
        RtlUtil.apply(search);

        Label section = new Label(I18n.t("admin.classes.section.detailed"));
        section.getStyleClass().add("section-title");

        TableView<ClassRow> table = AdminUI.buildClassesTable();
        table.getItems().clear();
        RtlUtil.apply(table);

        ObservableList<ClassRow> rows = FXCollections.observableArrayList();
        FilteredList<ClassRow> filtered = new FilteredList<>(rows, r -> true);
        table.setItems(filtered);

        enrollBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        search.textProperty().addListener((obs, oldV, q) -> {
            String s = q == null ? "" : q.trim().toLowerCase();
            filtered.setPredicate(r -> {
                if (s.isBlank()) return true;
                return safe(r.getClassName()).contains(s)
                        || safe(r.codeProperty().get()).contains(s)
                        || safe(r.teacherProperty().get()).contains(s)
                        || safe(r.scheduleProperty().get()).contains(s);
            });
        });

        AdminApi api = new AdminApi("http://localhost:8081", jwtStore);

        Label loadError = new Label();
        loadError.getStyleClass().add("subtitle");
        loadError.setManaged(false);
        loadError.setVisible(false);

        Runnable reload = () -> {
            loadError.setVisible(false);
            loadError.setManaged(false);

            new Thread(() -> {
                try {
                    List<AdminClassDto> list = api.getAdminClasses();

                    Platform.runLater(() -> {
                        rows.clear();
                        for (AdminClassDto c : list) {
                            String schedule = joinNonEmpty(c.semester, c.academicYear);
                            rows.add(new ClassRow(
                                    nullToEmpty(c.name),
                                    nullToEmpty(c.classCode),
                                    nullToEmpty(c.teacherEmail),
                                    schedule,
                                    String.valueOf(c.students)
                            ));
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        loadError.setText(I18n.t("admin.classes.dialog.add.error"));
                        loadError.setVisible(true);
                        loadError.setManaged(true);
                    });
                }
            }).start();
        };

        reload.run();

        enrollBtn.setOnAction(e -> {
            ClassRow selectedClass = table.getSelectionModel().getSelectedItem();
            if (selectedClass == null) return;

            openEnrollStudentsDialog(api, selectedClass, reload);
        });

        add.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(I18n.t("admin.classes.dialog.add.title"));

            ButtonType createBtn = new ButtonType(
                    I18n.t("admin.classes.dialog.add.create"),
                    ButtonBar.ButtonData.OK_DONE
            );
            ButtonType cancelBtn = new ButtonType(
                    I18n.t("admin.classes.dialog.add.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE
            );
            dialog.getDialogPane().getButtonTypes().addAll(createBtn, cancelBtn);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.setPadding(new Insets(10));
            RtlUtil.apply(form);

            TextField classCode = new TextField();
            classCode.setPromptText(I18n.t("admin.classes.dialog.add.classCode"));

            TextField nameField = new TextField();
            nameField.setPromptText(I18n.t("admin.classes.dialog.add.name"));

            TextField teacherEmail = new TextField();
            teacherEmail.setPromptText(I18n.t("admin.classes.dialog.add.teacherEmail"));

            TextField semester = new TextField();
            semester.setPromptText(I18n.t("admin.classes.dialog.add.semester"));

            TextField academicYear = new TextField();
            academicYear.setPromptText(I18n.t("admin.classes.dialog.add.academicYear"));

            TextField maxCapacity = new TextField();
            maxCapacity.setPromptText(I18n.t("admin.classes.dialog.add.maxCapacity"));

            form.addRow(0, new Label(I18n.t("admin.classes.dialog.add.classCode")), classCode);
            form.addRow(1, new Label(I18n.t("admin.classes.dialog.add.name")), nameField);
            form.addRow(2, new Label(I18n.t("admin.classes.dialog.add.teacherEmail")), teacherEmail);
            form.addRow(3, new Label(I18n.t("admin.classes.dialog.add.semester")), semester);
            form.addRow(4, new Label(I18n.t("admin.classes.dialog.add.academicYear")), academicYear);
            form.addRow(5, new Label(I18n.t("admin.classes.dialog.add.maxCapacity")), maxCapacity);

            dialog.getDialogPane().setContent(form);
            RtlUtil.apply(dialog.getDialogPane());

            Node okNode = dialog.getDialogPane().lookupButton(createBtn);
            okNode.disableProperty().bind(
                    Bindings.createBooleanBinding(() ->
                                    classCode.getText().trim().isBlank()
                                            || nameField.getText().trim().isBlank()
                                            || teacherEmail.getText().trim().isBlank(),
                            classCode.textProperty(), nameField.textProperty(), teacherEmail.textProperty())
            );

            dialog.showAndWait().ifPresent(bt -> {
                if (bt != createBtn) return;

                String cc = classCode.getText().trim();
                String nm = nameField.getText().trim();
                String te = teacherEmail.getText().trim();
                String sem = semester.getText().trim();
                String ay = academicYear.getText().trim();

                Integer cap = null;
                try {
                    String capRaw = maxCapacity.getText().trim();
                    if (!capRaw.isBlank()) cap = Integer.parseInt(capRaw);
                } catch (Exception ignore) {
                }

                Integer finalCap = cap;
                new Thread(() -> {
                    try {
                        api.createClass(cc, nm, te, sem, ay, finalCap);
                        Platform.runLater(reload);
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                        Platform.runLater(() -> {
                            Alert a = new Alert(
                                    Alert.AlertType.ERROR,
                                    I18n.t("admin.classes.dialog.add.error") + "\n" + ex2.getMessage(),
                                    ButtonType.OK
                            );
                            a.showAndWait();
                        });
                    }
                }).start();
            });
        });

        content.getChildren().addAll(titleRow, search, loadError, section, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        RtlUtil.apply(scroll);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                I18n.t("admin.dashboard.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("admin.classes.title"),
                I18n.t("admin.users.title"),
                I18n.t("admin.reports.title"),
                scroll,
                "second",
                new AdminAppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("admin-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("admin-classes");
                    }

                    @Override
                    public void goReports() {
                        router.go("admin-users");
                    }

                    @Override
                    public void goEmail() {
                        router.go("admin-reports");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }, router
        );
    }

    private void openEnrollStudentsDialog(AdminApi api, ClassRow selectedClass, Runnable reload) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(I18n.t("admin.classes.dialog.enroll.title"));
        dialog.getDialogPane().setPrefWidth(560);

        ButtonType enrollType = new ButtonType(
                I18n.t("admin.classes.button.enroll"),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelBtn = new ButtonType(
                I18n.t("common.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialog.getDialogPane().getButtonTypes().addAll(enrollType, cancelBtn);

        VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        RtlUtil.apply(root);

        Label classInfo = new Label(
                I18n.t("admin.reports.filter.class") + ": "
                        + nullToEmpty(selectedClass.getClassName())
                        + " ("
                        + nullToEmpty(selectedClass.codeProperty().get())
                        + ")"
        );
        classInfo.getStyleClass().add("section-title");

        TextField searchStudents = new TextField();
        searchStudents.setPromptText(I18n.t("admin.classes.dialog.enroll.search.placeholder"));
        RtlUtil.apply(searchStudents);

        Label status = new Label(I18n.t("admin.classes.dialog.loadStudents.loading"));
        status.getStyleClass().add("subtitle");

        ListView<AdminStudentDto> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setPrefHeight(320);
        RtlUtil.apply(listView);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AdminStudentDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String fullName = (nullToEmpty(item.firstName) + " " + nullToEmpty(item.lastName)).trim();
                    setText(fullName + "  |  " + nullToEmpty(item.email) + "  |  " + nullToEmpty(item.studentCode));
                }
            }
        });

        Label selectedCount = new Label(formatSelectedCount(0));
        selectedCount.getStyleClass().add("subtitle");

        ObservableList<AdminStudentDto> studentRows = FXCollections.observableArrayList();
        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(studentRows, s -> true);
        listView.setItems(filteredStudents);

        listView.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<AdminStudentDto>) c ->
                selectedCount.setText(formatSelectedCount(listView.getSelectionModel().getSelectedItems().size()))
        );

        searchStudents.textProperty().addListener((obs, oldV, q) -> {
            String s = q == null ? "" : q.trim().toLowerCase();
            filteredStudents.setPredicate(student -> {
                if (student == null) return false;
                if (s.isBlank()) return true;

                String fullName = (nullToEmpty(student.firstName) + " " + nullToEmpty(student.lastName)).toLowerCase();
                return fullName.contains(s)
                        || safe(student.email).contains(s)
                        || safe(student.studentCode).contains(s);
            });
        });

        root.getChildren().addAll(classInfo, searchStudents, status, listView, selectedCount);
        dialog.getDialogPane().setContent(root);
        RtlUtil.apply(dialog.getDialogPane());

        Node enrollNode = dialog.getDialogPane().lookupButton(enrollType);
        enrollNode.disableProperty().bind(
                Bindings.size(listView.getSelectionModel().getSelectedItems()).isEqualTo(0)
        );

        new Thread(() -> {
            try {
                List<AdminStudentDto> students = api.getAllStudentsNotInClass(selectedClass.codeProperty().get());

                Platform.runLater(() -> {
                    studentRows.setAll(students);
                    status.setText(students.isEmpty()
                            ? I18n.t("admin.classes.dialog.loadStudents.empty")
                            : I18n.t("admin.classes.dialog.enroll.title"));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> status.setText(I18n.t("admin.classes.dialog.loadStudents.failed")));
            }
        }).start();

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != enrollType) return;

            List<AdminStudentDto> selectedStudents = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            List<String> studentEmails = selectedStudents.stream()
                    .map(s -> s.email)
                    .filter(v -> v != null && !v.isBlank())
                    .collect(Collectors.toList());

            if (studentEmails.isEmpty()) {
                Alert a = new Alert(
                        Alert.AlertType.WARNING,
                        I18n.t("admin.classes.dialog.enroll.noneSelected"),
                        ButtonType.OK
                );
                a.showAndWait();
                return;
            }

            status.setText(I18n.t("common.status.loading"));

            new Thread(() -> {
                try {
                    api.enrollStudentsToClass(selectedClass.codeProperty().get(), studentEmails);

                    Platform.runLater(() -> {
                        dialog.close();

                        Alert ok = new Alert(
                                Alert.AlertType.INFORMATION,
                                I18n.t("admin.classes.dialog.enroll.success"),
                                ButtonType.OK
                        );
                        ok.showAndWait();
                        reload.run();
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        status.setText(I18n.t("admin.classes.dialog.enroll.failure"));
                        Alert err = new Alert(
                                Alert.AlertType.ERROR,
                                I18n.t("admin.classes.dialog.enroll.failure") + "\n" + ex.getMessage(),
                                ButtonType.OK
                        );
                        err.showAndWait();
                    });
                }
            }).start();
        });
    }

    private static String formatSelectedCount(int count) {
        return I18n.t("admin.classes.dialog.enroll.selectedCount").replace("0", String.valueOf(count));
    }

    private static String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String joinNonEmpty(String a, String b) {
        a = nullToEmpty(a).trim();
        b = nullToEmpty(b).trim();
        if (a.isBlank() && b.isBlank()) return "";
        if (a.isBlank()) return b;
        if (b.isBlank()) return a;
        return a + " · " + b;
    }
}