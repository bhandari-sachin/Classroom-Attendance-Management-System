package frontend.teacher;

import frontend.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.util.QRCodeImageUtil;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherTakeAttendancePage {

    private static final Logger LOGGER =
            Logger.getLogger(TeacherTakeAttendancePage.class.getName());

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

        final long[] currentSessionId = {-1L};

        VBox page = TeacherPageSupport.buildAttendancePageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();
        Label selectClassLabel = buildSelectClassLabel();

        ComboBox<ClassItem> classBox = buildClassBox();

        ImageView qrImageView = buildQrImageView();
        Label manualCode = buildManualCodeLabel();
        Button generateButton = buildGenerateButton();

        VBox qrCard = buildQrCard(qrImageView, manualCode, generateButton);

        Label studentsTitle = buildStudentsTitle();
        studentsTitle.textProperty().bind(
                Bindings.createStringBinding(
                        () -> helper.getMessage("teacher.attendance.students.title")
                                .replace("{count}", String.valueOf(rows.size())),
                        rows
                )
        );

        Button markAllPresentButton = buildMarkAllPresentButton();
        HBox studentsHeader = buildStudentsHeader(studentsTitle, markAllPresentButton);

        TableView<StudentRow> table = buildStudentsTable(
                api,
                jwtStore,
                state,
                currentSessionId
        );

        page.getChildren().addAll(
                title,
                subtitle,
                selectClassLabel,
                classBox,
                qrCard,
                studentsHeader,
                table
        );

        generateButton.setOnAction(e -> handleGenerateSession(
                api,
                jwtStore,
                state,
                classBox,
                generateButton,
                manualCode,
                qrImageView,
                currentSessionId
        ));

        markAllPresentButton.setOnAction(e -> handleMarkAllPresent(
                api,
                jwtStore,
                state,
                markAllPresentButton,
                currentSessionId
        ));

        classBox.setOnAction(e -> handleClassSelection(
                api,
                jwtStore,
                state,
                classBox,
                manualCode,
                qrImageView,
                currentSessionId
        ));

        loadClasses(api, jwtStore, state, classBox);

        return TeacherPageSupport.wrapWithSidebar(
                teacherName,
                helper,
                page,
                "second",
                router,
                jwtStore
        );
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("teacher.attendance.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("teacher.attendance.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private Label buildSelectClassLabel() {
        Label label = new Label(helper.getMessage("teacher.attendance.selectClass"));
        label.getStyleClass().add("section-title");
        return label;
    }

    private ComboBox<ClassItem> buildClassBox() {
        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText(helper.getMessage("teacher.attendance.class.prompt"));
        classBox.setMaxWidth(320);
        return classBox;
    }

    private ImageView buildQrImageView() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private Label buildManualCodeLabel() {
        Label manualCode = new Label("—");
        manualCode.getStyleClass().add("small-subtitle");
        return manualCode;
    }

    private Button buildGenerateButton() {
        Button generate = new Button(helper.getMessage("teacher.attendance.generate"));
        generate.getStyleClass().addAll("pill", "pill-green");
        generate.setMaxWidth(Double.MAX_VALUE);
        return generate;
    }

    private VBox buildQrCard(ImageView qrImageView, Label manualCode, Button generateButton) {
        VBox qrCard = new VBox(12);
        qrCard.getStyleClass().add("card");
        qrCard.setPadding(new Insets(16));

        Label qrTitle = new Label(helper.getMessage("teacher.attendance.qr.title"));
        qrTitle.getStyleClass().add("section-title");

        StackPane qrArea = new StackPane(qrImageView);
        qrArea.setAlignment(Pos.CENTER);
        qrArea.setPrefHeight(180);
        qrArea.setPrefWidth(180);
        qrArea.getStyleClass().add("qr-area");

        Label manualTitle = new Label(helper.getMessage("teacher.attendance.manual.title"));
        manualTitle.getStyleClass().add("small-title");

        VBox manualBox = new VBox(4, manualTitle, manualCode);
        manualBox.setAlignment(Pos.CENTER);
        manualBox.getStyleClass().add("manual-box");

        qrCard.getChildren().addAll(qrTitle, qrArea, manualBox, generateButton);
        return qrCard;
    }

    private Label buildStudentsTitle() {
        Label studentsTitle = new Label();
        studentsTitle.getStyleClass().add("section-title");
        return studentsTitle;
    }

    private Button buildMarkAllPresentButton() {
        Button button = new Button(helper.getMessage("teacher.attendance.markAll"));
        button.getStyleClass().addAll("pill", "pill-green");
        return button;
    }

    private HBox buildStudentsHeader(Label studentsTitle, Button markAllPresentButton) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, studentsTitle, spacer, markAllPresentButton);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private TableView<StudentRow> buildStudentsTable(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            long[] currentSessionId
    ) {
        TableView<StudentRow> table = new TableView<>(rows);
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(36);
        table.setPrefHeight(300);

        TableColumn<StudentRow, String> colName =
                new TableColumn<>(helper.getMessage("teacher.attendance.table.student"));
        colName.setCellValueFactory(data -> data.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail =
                new TableColumn<>(helper.getMessage("teacher.attendance.table.email"));
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<StudentRow, String> colStatus =
                new TableColumn<>(helper.getMessage("teacher.attendance.table.status"));
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(localizeAttendanceStatus(item));
            }
        });

        TableColumn<StudentRow, Void> colActions = buildActionsColumn(
                api,
                jwtStore,
                state,
                table,
                currentSessionId
        );

        table.getColumns().setAll(colName, colEmail, colStatus, colActions);
        return table;
    }

    private TableColumn<StudentRow, Void> buildActionsColumn(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            TableView<StudentRow> table,
            long[] currentSessionId
    ) {
        TableColumn<StudentRow, Void> colActions =
                new TableColumn<>(helper.getMessage("teacher.attendance.actions"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button presentBtn = createActionButton("✓");
            private final Button absentBtn = createActionButton("✕");
            private final Button excusedBtn = createActionButton("◔");
            private final HBox box = new HBox(8, presentBtn, absentBtn, excusedBtn);

            {
                box.setAlignment(Pos.CENTER);

                presentBtn.setOnAction(e -> updateAttendance("PRESENT"));
                absentBtn.setOnAction(e -> updateAttendance("ABSENT"));
                excusedBtn.setOnAction(e -> updateAttendance("EXCUSED"));
            }

            private void updateAttendance(String status) {
                StudentRow row = getTableView().getItems().get(getIndex());

                if (currentSessionId[0] <= 0) {
                    showWarning(helper.getMessage("teacher.attendance.generateSessionFirst"));
                    return;
                }

                setButtonsDisabled(true);

                new Thread(() -> {
                    try {
                        api.markAttendance(jwtStore, state, row.getStudentId(), currentSessionId[0], status);
                        Platform.runLater(() -> {
                            row.setStatus(status);
                            applyStatusStyles(status);
                            setButtonsDisabled(false);
                            table.refresh();
                        });
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to update attendance status.", ex);
                        Platform.runLater(() -> {
                            setButtonsDisabled(false);
                            showError(helper.getMessage("teacher.attendance.updateFailed") + " "
                                    + (ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
                        });
                    }
                }).start();
            }

            private void applyStatusStyles(String currentStatus) {
                presentBtn.getStyleClass().setAll("action-btn");
                absentBtn.getStyleClass().setAll("action-btn");
                excusedBtn.getStyleClass().setAll("action-btn");

                if ("PRESENT".equalsIgnoreCase(currentStatus)) {
                    presentBtn.getStyleClass().add("present-btn");
                    absentBtn.getStyleClass().add("action-btn-outline");
                    excusedBtn.getStyleClass().add("action-btn-outline");
                } else if ("ABSENT".equalsIgnoreCase(currentStatus)) {
                    presentBtn.getStyleClass().add("action-btn-outline");
                    absentBtn.getStyleClass().add("absent-btn");
                    excusedBtn.getStyleClass().add("action-btn-outline");
                } else if ("EXCUSED".equalsIgnoreCase(currentStatus)) {
                    presentBtn.getStyleClass().add("action-btn-outline");
                    absentBtn.getStyleClass().add("action-btn-outline");
                    excusedBtn.getStyleClass().add("excused-btn");
                } else {
                    presentBtn.getStyleClass().add("action-btn-outline");
                    absentBtn.getStyleClass().add("action-btn-outline");
                    excusedBtn.getStyleClass().add("action-btn-outline");
                }
            }

            private void setButtonsDisabled(boolean disabled) {
                presentBtn.setDisable(disabled);
                absentBtn.setDisable(disabled);
                excusedBtn.setDisable(disabled);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                StudentRow row = getTableView().getItems().get(getIndex());
                applyStatusStyles(row.getStatus());
                setGraphic(box);
            }
        });

        return colActions;
    }

    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.setMinSize(30, 30);
        button.setPrefSize(30, 30);
        return button;
    }

    private void handleGenerateSession(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox,
            Button generateButton,
            Label manualCode,
            ImageView qrImageView,
            long[] currentSessionId
    ) {
        ClassItem selected = classBox.getValue();
        if (selected == null) {
            showWarning(helper.getMessage("teacher.attendance.selectClassFirst"));
            return;
        }

        generateButton.setDisable(true);
        manualCode.setText(helper.getMessage("teacher.attendance.generating"));
        qrImageView.setImage(null);
        currentSessionId[0] = -1L;

        new Thread(() -> {
            try {
                Map<String, Object> response = api.createSession(jwtStore, state, selected.id);
                String code = api.extractCode(response);

                Number sessionIdNumber = (Number) response.get("sessionId");
                if (sessionIdNumber != null) {
                    currentSessionId[0] = sessionIdNumber.longValue();
                }

                Platform.runLater(() -> {
                    manualCode.setText(code == null || code.isBlank() ? "—" : code);

                    if (code != null && !code.isBlank()) {
                        try {
                            BufferedImage bufferedImage =
                                    QRCodeImageUtil.generateQRCodeImage(code, 180, 180);
                            qrImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                        } catch (Exception qrException) {
                            LOGGER.log(Level.SEVERE, "Failed to generate QR image.", qrException);
                        }
                    }

                    generateButton.setDisable(false);
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to generate attendance session.", ex);
                Platform.runLater(() -> {
                    manualCode.setText("—");
                    qrImageView.setImage(null);
                    generateButton.setDisable(false);
                    showError(helper.getMessage("teacher.attendance.generateFailed") + " "
                            + (ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
                });
            }
        }).start();
    }

    private void handleMarkAllPresent(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            Button markAllPresentButton,
            long[] currentSessionId
    ) {
        if (currentSessionId[0] <= 0) {
            showWarning(helper.getMessage("teacher.attendance.generateSessionFirst"));
            return;
        }

        if (rows.isEmpty()) {
            return;
        }

        markAllPresentButton.setDisable(true);

        new Thread(() -> {
            try {
                for (StudentRow row : rows) {
                    api.markAttendance(jwtStore, state, row.getStudentId(), currentSessionId[0], "PRESENT");
                }

                Platform.runLater(() -> {
                    for (StudentRow row : rows) {
                        row.setStatus("PRESENT");
                    }
                    markAllPresentButton.setDisable(false);
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to mark all students present.", ex);
                Platform.runLater(() -> {
                    markAllPresentButton.setDisable(false);
                    showError(helper.getMessage("teacher.attendance.markAllFailed") + " "
                            + (ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
                });
            }
        }).start();
    }

    private void handleClassSelection(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox,
            Label manualCode,
            ImageView qrImageView,
            long[] currentSessionId
    ) {
        ClassItem selected = classBox.getValue();
        if (selected == null) {
            return;
        }

        currentSessionId[0] = -1L;
        manualCode.setText("—");
        qrImageView.setImage(null);
        rows.clear();
        rows.add(new StudentRow(-1L, helper.getMessage("teacher.attendance.loading.students"), "-", "—"));

        new Thread(() -> {
            try {
                List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selected.id);

                Platform.runLater(() -> {
                    rows.clear();
                    for (Map<String, Object> student : students) {
                        rows.add(mapStudentRow(student));
                    }
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load students for selected class.", ex);
                Platform.runLater(() -> {
                    rows.clear();
                    showError(helper.getMessage("teacher.attendance.error.loadStudents") + " "
                            + (ex.getMessage() == null ? "Unknown error" : ex.getMessage()));
                });
            }
        }).start();
    }

    StudentRow mapStudentRow(Map<String, Object> student) {
        long studentId = Long.parseLong(String.valueOf(student.get("id")));
        String firstName = String.valueOf(student.get("firstName"));
        String lastName = String.valueOf(student.get("lastName"));
        String email = String.valueOf(student.get("email"));

        StudentRow row = new StudentRow(studentId, firstName + " " + lastName, email, "—");
        row.setExcuseReason("");
        return row;
    }

    private void loadClasses(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox
    ) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> classes = api.getMyClasses(jwtStore, state);
                List<ClassItem> items = classes.stream()
                        .map(this::mapClassItem)
                        .toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load teacher classes.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.attendance.loadClassesFailed") + " "
                                + (ex.getMessage() == null ? "Unknown error" : ex.getMessage()))
                );
            }
        }).start();
    }

    ClassItem mapClassItem(Map<String, Object> classMap) {
        long id = Long.parseLong(String.valueOf(classMap.get("id")));
        String classCode = String.valueOf(classMap.get("classCode"));
        String name = String.valueOf(classMap.get("name"));
        return new ClassItem(id, classCode + " — " + name);
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank() || "—".equals(status)) {
            return "—";
        }

        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> helper.getMessage("student.attendance.stats.present");
            case "ABSENT" -> helper.getMessage("student.attendance.stats.absent");
            case "EXCUSED" -> helper.getMessage("student.attendance.stats.excused");
            default -> status;
        };
    }
}