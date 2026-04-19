package frontend.admin;

import frontend.ClassRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AdminManageClassesPage {

    private static final Logger LOGGER =
            Logger.getLogger(AdminManageClassesPage.class.getName());

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String adminName = AdminPageSupport.resolveAdminName(state, helper);

        VBox content = AdminPageSupport.buildContentContainer();

        HBox titleRow = buildTitleRow();
        TextField searchField = buildSearchField();
        Label loadError = buildLoadErrorLabel();
        Label sectionTitle = buildSectionTitle();

        TableView<ClassRow> table = AdminUI.buildClassesTable();
        table.getItems().clear();

        ObservableList<ClassRow> rows = FXCollections.observableArrayList();
        FilteredList<ClassRow> filteredRows = new FilteredList<>(rows, row -> true);
        table.setItems(filteredRows);

        Button enrollButton = getEnrollButton(titleRow);
        Button addButton = getAddButton(titleRow);

        enrollButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                applySearchFilter(filteredRows, newValue)
        );

        AdminApi adminApi = new AdminApi("http://localhost:8081", jwtStore);

        Runnable reload = () -> loadClasses(adminApi, rows, loadError);

        reload.run();

        enrollButton.setOnAction(event -> {
            ClassRow selectedClass = table.getSelectionModel().getSelectedItem();
            if (selectedClass != null) {
                openEnrollStudentsDialog(adminApi, selectedClass, reload);
            }
        });

        addButton.setOnAction(event -> openAddClassDialog(adminApi, reload));

        content.getChildren().addAll(titleRow, searchField, loadError, sectionTitle, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminPageSupport.wrapWithSidebar(
                adminName,
                helper,
                scroll,
                "second",
                router,
                jwtStore
        );
    }

    private HBox buildTitleRow() {
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleColumn = new VBox(4);

        Label title = new Label(helper.getMessage("admin.classes.title"));
        title.getStyleClass().add("title");

        Label subtitle = new Label(helper.getMessage("admin.classes.subtitle"));
        subtitle.getStyleClass().add("subtitle");

        titleColumn.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button enrollButton = new Button(helper.getMessage("admin.classes.button.enroll"));
        enrollButton.getStyleClass().add("secondary-btn");

        Button addButton = new Button("+   " + helper.getMessage("admin.classes.button.add"));
        addButton.getStyleClass().add("primary-btn");

        titleRow.getChildren().addAll(titleColumn, spacer, enrollButton, addButton);
        return titleRow;
    }

    private Button getEnrollButton(HBox titleRow) {
        return (Button) titleRow.getChildren().get(titleRow.getChildren().size() - 2);
    }

    private Button getAddButton(HBox titleRow) {
        return (Button) titleRow.getChildren().get(titleRow.getChildren().size() - 1);
    }

    private TextField buildSearchField() {
        TextField search = new TextField();
        search.setPromptText(helper.getMessage("admin.classes.search.placeholder"));
        search.getStyleClass().add("search-field");
        return search;
    }

    private Label buildLoadErrorLabel() {
        Label loadError = new Label();
        loadError.getStyleClass().add("subtitle");
        loadError.setManaged(false);
        loadError.setVisible(false);
        return loadError;
    }

    private Label buildSectionTitle() {
        Label section = new Label(helper.getMessage("admin.classes.section.detailed"));
        section.getStyleClass().add("section-title");
        return section;
    }

    void applySearchFilter(FilteredList<ClassRow> filteredRows, String query) {
        String searchValue = query == null ? "" : query.trim().toLowerCase();

        filteredRows.setPredicate(row -> {
            if (row == null) {
                return false;
            }
            if (searchValue.isBlank()) {
                return true;
            }

            return safe(row.getClassName()).contains(searchValue)
                    || safe(row.getCode()).contains(searchValue)
                    || safe(row.getTeacher()).contains(searchValue)
                    || safe(row.getSchedule()).contains(searchValue);
        });
    }

    private void loadClasses(AdminApi adminApi, ObservableList<ClassRow> rows, Label loadError) {
        hideLabel(loadError);

        new Thread(() -> {
            try {
                List<AdminClassDto> classes = adminApi.getAdminClasses();
                List<ClassRow> mappedRows = classes.stream()
                        .map(this::mapClassRow)
                        .collect(Collectors.toList());

                Platform.runLater(() -> rows.setAll(mappedRows));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load admin classes.", ex);
                Platform.runLater(() -> showLabel(
                        loadError,
                        helper.getMessage("admin.classes.dialog.loadStudents.failed")
                                + " "
                                + (ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                ));
            }
        }).start();
    }

    ClassRow mapClassRow(AdminClassDto classDto) {
        String schedule = joinNonEmpty(classDto.getSemester(), classDto.getAcademicYear());

        return new ClassRow(
                nullToEmpty(classDto.getName()),
                nullToEmpty(classDto.getClassCode()),
                nullToEmpty(classDto.getTeacherEmail()),
                schedule,
                String.valueOf(classDto.getStudents())
        );
    }

    private void openAddClassDialog(AdminApi adminApi, Runnable reload) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(helper.getMessage("admin.classes.dialog.add.title"));

        ButtonType createButtonType = new ButtonType(
                helper.getMessage("admin.classes.dialog.add.create"),
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane form = buildAddClassForm();
        dialog.getDialogPane().setContent(form);

        TextField classCodeField = buildAddFormField("e.g. TX-09374");
        TextField nameField = buildAddFormField("e.g. Mathematics");
        TextField teacherEmailField = buildAddFormField("teacher@example.com");
        TextField semesterField = buildAddFormField("e.g. Spring");
        TextField academicYearField = buildAddFormField("e.g. 2025/2026");
        TextField maxCapacityField = buildAddFormField("e.g. 30");

        form.addRow(0, new Label(helper.getMessage("admin.classes.dialog.add.classCode")), classCodeField);
        form.addRow(1, new Label(helper.getMessage("admin.classes.dialog.add.name")), nameField);
        form.addRow(2, new Label(helper.getMessage("admin.classes.dialog.add.teacherEmail")), teacherEmailField);
        form.addRow(3, new Label(helper.getMessage("admin.classes.dialog.add.semester")), semesterField);
        form.addRow(4, new Label(helper.getMessage("admin.classes.dialog.add.academicYear")), academicYearField);
        form.addRow(5, new Label(helper.getMessage("admin.classes.dialog.add.maxCapacity")), maxCapacityField);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        updateCreateButtonState(createButton, classCodeField, nameField, teacherEmailField);

        classCodeField.textProperty().addListener((obs, oldValue, newValue) ->
                updateCreateButtonState(createButton, classCodeField, nameField, teacherEmailField));
        nameField.textProperty().addListener((obs, oldValue, newValue) ->
                updateCreateButtonState(createButton, classCodeField, nameField, teacherEmailField));
        teacherEmailField.textProperty().addListener((obs, oldValue, newValue) ->
                updateCreateButtonState(createButton, classCodeField, nameField, teacherEmailField));

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType != createButtonType) {
                return;
            }

            String classCode = classCodeField.getText().trim();
            String name = nameField.getText().trim();
            String teacherEmail = teacherEmailField.getText().trim();
            String semester = semesterField.getText().trim();
            String academicYear = academicYearField.getText().trim();
            Integer maxCapacity = parseInteger(maxCapacityField.getText().trim());

            createClass(
                    adminApi,
                    classCode,
                    name,
                    teacherEmail,
                    semester,
                    academicYear,
                    maxCapacity,
                    reload
            );
        });
    }

    private void updateCreateButtonState(
            Button createButton,
            TextField classCodeField,
            TextField nameField,
            TextField teacherEmailField
    ) {
        boolean disabled =
                classCodeField.getText().trim().isBlank()
                        || nameField.getText().trim().isBlank()
                        || teacherEmailField.getText().trim().isBlank();

        createButton.setDisable(disabled);
    }

    private GridPane buildAddClassForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        return form;
    }

    private TextField buildAddFormField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        return field;
    }

    private void createClass(
            AdminApi adminApi,
            String classCode,
            String name,
            String teacherEmail,
            String semester,
            String academicYear,
            Integer maxCapacity,
            Runnable reload
    ) {
        new Thread(() -> {
            try {
                adminApi.createClass(classCode, name, teacherEmail, semester, academicYear, maxCapacity);
                Platform.runLater(reload);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to create class.", ex);
                Platform.runLater(() -> showError(
                        helper.getMessage("admin.classes.dialog.add.error")
                                + ":\n"
                                + (ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                ));
            }
        }).start();
    }

    private void openEnrollStudentsDialog(AdminApi adminApi, ClassRow selectedClass, Runnable reload) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(helper.getMessage("admin.classes.dialog.enroll.title"));
        dialog.getDialogPane().setPrefWidth(560);

        ButtonType enrollButtonType = new ButtonType(
                helper.getMessage("admin.classes.button.enroll"),
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(enrollButtonType, ButtonType.CANCEL);

        VBox root = new VBox(12);
        root.setPadding(new Insets(12));

        Label classInfo = new Label(
                "Class: " + nullToEmpty(selectedClass.getClassName())
                        + " (" + nullToEmpty(selectedClass.getCode()) + ")"
        );
        classInfo.getStyleClass().add("section-title");

        TextField searchStudents = new TextField();
        searchStudents.setPromptText(helper.getMessage("admin.classes.dialog.enroll.search.placeholder"));

        Label statusLabel = new Label(helper.getMessage("admin.classes.dialog.loadStudents.loading"));
        statusLabel.getStyleClass().add("subtitle");

        ListView<AdminStudentDto> listView = buildStudentListView();
        Label selectedCount = buildSelectedCountLabel();

        ObservableList<AdminStudentDto> studentRows = FXCollections.observableArrayList();
        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(studentRows, student -> true);
        listView.setItems(filteredStudents);

        listView.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<AdminStudentDto>) change ->
                        selectedCount.setText("Selected: " + listView.getSelectionModel().getSelectedItems().size())
        );

        searchStudents.textProperty().addListener((obs, oldValue, newValue) ->
                applyStudentFilter(filteredStudents, newValue)
        );

        root.getChildren().addAll(classInfo, searchStudents, statusLabel, listView, selectedCount);
        dialog.getDialogPane().setContent(root);

        Button enrollButton = (Button) dialog.getDialogPane().lookupButton(enrollButtonType);
        enrollButton.disableProperty().bind(
                listView.getSelectionModel().selectedItemProperty().isNull()
        );

        loadAvailableStudents(adminApi, selectedClass, studentRows, statusLabel);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType != enrollButtonType) {
                return;
            }

            enrollSelectedStudents(adminApi, selectedClass, listView, statusLabel, dialog, reload);
        });
    }

    private ListView<AdminStudentDto> buildStudentListView() {
        ListView<AdminStudentDto> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setPrefHeight(320);

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(AdminStudentDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                String fullName = (nullToEmpty(item.getFirstName()) + " " + nullToEmpty(item.getLastName())).trim();
                setText(fullName + "  |  " + nullToEmpty(item.getEmail()) + "  |  " + nullToEmpty(item.getStudentCode()));
            }
        });

        return listView;
    }

    private Label buildSelectedCountLabel() {
        Label selectedCount = new Label(helper.getMessage("admin.classes.dialog.enroll.selectedCount"));
        selectedCount.getStyleClass().add("subtitle");
        return selectedCount;
    }

    void applyStudentFilter(FilteredList<AdminStudentDto> filteredStudents, String query) {
        String searchValue = query == null ? "" : query.trim().toLowerCase();

        filteredStudents.setPredicate(student -> {
            if (student == null) {
                return false;
            }
            if (searchValue.isBlank()) {
                return true;
            }

            String fullName = (nullToEmpty(student.getFirstName()) + " " + nullToEmpty(student.getLastName())).toLowerCase();

            return fullName.contains(searchValue)
                    || safe(student.getEmail()).contains(searchValue)
                    || safe(student.getStudentCode()).contains(searchValue);
        });
    }

    private void loadAvailableStudents(
            AdminApi adminApi,
            ClassRow selectedClass,
            ObservableList<AdminStudentDto> studentRows,
            Label statusLabel
    ) {
        new Thread(() -> {
            try {
                List<AdminStudentDto> students = adminApi.getAllStudentsNotInClass(selectedClass.getCode());

                Platform.runLater(() -> {
                    studentRows.setAll(students);
                    statusLabel.setText(
                            students.isEmpty()
                                    ? helper.getMessage("admin.classes.dialog.loadStudents.empty")
                                    : "Select one or more students to enroll."
                    );
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load available students for class enrollment.", ex);
                Platform.runLater(() ->
                        statusLabel.setText(
                                helper.getMessage("admin.classes.dialog.loadStudents.failed")
                                        + " "
                                        + (ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                        )
                );
            }
        }).start();
    }

    private void enrollSelectedStudents(
            AdminApi adminApi,
            ClassRow selectedClass,
            ListView<AdminStudentDto> listView,
            Label statusLabel,
            Dialog<ButtonType> dialog,
            Runnable reload
    ) {
        List<AdminStudentDto> selectedStudents =
                new ArrayList<>(listView.getSelectionModel().getSelectedItems());

        List<String> studentEmails = selectedStudents.stream()
                .map(student -> student.getEmail())
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toList());

        if (studentEmails.isEmpty()) {
            showWarning(helper.getMessage("admin.classes.dialog.enroll.noneSelected"));
            return;
        }

        statusLabel.setText("Enrolling students...");

        new Thread(() -> {
            try {
                adminApi.enrollStudentsToClass(selectedClass.getCode(), studentEmails);

                Platform.runLater(() -> {
                    dialog.close();
                    showInfo(helper.getMessage("admin.classes.dialog.enroll.success"));
                    reload.run();
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to enroll students into class.", ex);
                Platform.runLater(() -> {
                    String errorMessage = ex.getMessage() == null ? "Unknown error" : ex.getMessage();
                    statusLabel.setText(
                            helper.getMessage("admin.classes.dialog.enroll.failure") + " " + errorMessage
                    );
                    showError(
                            helper.getMessage("admin.classes.dialog.enroll.failure") + "\n" + errorMessage
                    );
                });
            }
        }).start();
    }

    Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private void showLabel(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    static String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    static String joinNonEmpty(String first, String second) {
        if ((first == null || first.isBlank()) && (second == null || second.isBlank())) {
            return "";
        }
        if (first == null || first.isBlank()) {
            return second;
        }
        if (second == null || second.isBlank()) {
            return first;
        }
        return first + " " + second;
    }
}