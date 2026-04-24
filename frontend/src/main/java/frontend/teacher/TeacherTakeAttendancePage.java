package frontend.teacher;

import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import frontend.ui.StudentRow;
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

    private static final String UNKNOWN_ERROR = "Unknown error";
    private static final String STATUS_PRESENT = "PRESENT";
    private static final String STATUS_ABSENT = "ABSENT";
    private static final String STATUS_EXCUSED = "EXCUSED";
    private static final String STYLE_SECTION_TITLE = "section-title";
    private static final String STYLE_ACTION_BTN = "action-btn";
    private static final String STYLE_ACTION_BTN_OUTLINE = "action-btn-outline";
    private static final String DASH = "—";

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

    private static final class SessionState {
        private long currentSessionId = -1L;

        long getCurrentSessionId() {
            return currentSessionId;
        }

        void setCurrentSessionId(long currentSessionId) {
            this.currentSessionId = currentSessionId;
        }

        void reset() {
            this.currentSessionId = -1L;
        }

        boolean hasActiveSession() {
            return currentSessionId > 0;
        }
    }

    private record SessionControls(
            Button generateButton,
            Label manualCode,
            ImageView qrImageView,
            SessionState sessionState
    ) {
    }

    private record AttendanceUpdateRequest(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            StudentRow row,
            long sessionId,
            String status,
            Runnable onSuccess,
            Runnable onFinally
    ) {
    }

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();
    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        logIfSceneMissing(scene);

        String teacherName = TeacherPageSupport.resolveTeacherName(state, helper);
        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        SessionState sessionState = new SessionState();

        VBox page = TeacherPageSupport.buildAttendancePageContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();
        Label selectClassLabel = buildSelectClassLabel();

        ComboBox<ClassItem> classBox = buildClassBox();

        ImageView qrImageView = buildQrImageView();
        Label manualCode = buildManualCodeLabel();
        Button generateButton = buildGenerateButton();

        SessionControls sessionControls =
                new SessionControls(generateButton, manualCode, qrImageView, sessionState);

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
                sessionState
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
                sessionControls
        ));

        markAllPresentButton.setOnAction(e -> handleMarkAllPresent(
                api,
                jwtStore,
                state,
                markAllPresentButton,
                sessionState
        ));

        classBox.setOnAction(e -> handleClassSelection(
                api,
                jwtStore,
                state,
                classBox,
                sessionControls
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

    private void logIfSceneMissing(Scene scene) {
        if (scene == null) {
            LOGGER.fine("TeacherTakeAttendancePage.build called with a null scene.");
        }
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
        label.getStyleClass().add(STYLE_SECTION_TITLE);
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
        Label manualCode = new Label(DASH);
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
        qrTitle.getStyleClass().add(STYLE_SECTION_TITLE);

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
        studentsTitle.getStyleClass().add(STYLE_SECTION_TITLE);
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
            SessionState sessionState
    ) {
        TableView<StudentRow> table = new TableView<>(rows);
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
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
                sessionState
        );

        table.getColumns().setAll(colName, colEmail, colStatus, colActions);
        return table;
    }

    private TableColumn<StudentRow, Void> buildActionsColumn(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            TableView<StudentRow> table,
            SessionState sessionState
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
                configureStatusAction(presentBtn, STATUS_PRESENT);
                configureStatusAction(absentBtn, STATUS_ABSENT);
                configureStatusAction(excusedBtn, STATUS_EXCUSED);
            }

            private void configureStatusAction(Button button, String status) {
                button.setOnAction(e -> updateAttendance(status));
            }

            private void updateAttendance(String status) {
                StudentRow row = getTableView().getItems().get(getIndex());

                if (!sessionState.hasActiveSession()) {
                    showWarning(helper.getMessage("teacher.attendance.generateSessionFirst"));
                    return;
                }

                setButtonsDisabled(true);

                updateAttendanceStatusAsync(new AttendanceUpdateRequest(
                        api,
                        jwtStore,
                        state,
                        row,
                        sessionState.getCurrentSessionId(),
                        status,
                        () -> {
                            row.setStatus(status);
                            applyStatusStyles(status, presentBtn, absentBtn, excusedBtn);
                            table.refresh();
                        },
                        () -> setButtonsDisabled(false)
                ));
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
                applyStatusStyles(row.getStatus(), presentBtn, absentBtn, excusedBtn);
                setGraphic(box);
            }
        });

        return colActions;
    }

    private void updateAttendanceStatusAsync(AttendanceUpdateRequest request) {
        new Thread(() -> {
            try {
                request.api().markAttendance(
                        request.jwtStore(),
                        request.state(),
                        request.row().getStudentId(),
                        request.sessionId(),
                        request.status()
                );
                Platform.runLater(() -> {
                    request.onSuccess().run();
                    request.onFinally().run();
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Updating attendance status was interrupted.", ex);
                Platform.runLater(() -> {
                    request.onFinally().run();
                    showError(helper.getMessage("teacher.attendance.updateFailed") + " "
                            + safeErrorMessage(ex));
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to update attendance status.", ex);
                Platform.runLater(() -> {
                    request.onFinally().run();
                    showError(helper.getMessage("teacher.attendance.updateFailed") + " "
                            + safeErrorMessage(ex));
                });
            }
        }).start();
    }

    private void applyStatusStyles(
            String currentStatus,
            Button presentBtn,
            Button absentBtn,
            Button excusedBtn
    ) {
        resetActionButtonStyles(presentBtn, absentBtn, excusedBtn);

        switch (normalizeStatus(currentStatus)) {
            case STATUS_PRESENT -> setActiveStyle(presentBtn, "present-btn");
            case STATUS_ABSENT -> setActiveStyle(absentBtn, "absent-btn");
            case STATUS_EXCUSED -> setActiveStyle(excusedBtn, "excused-btn");
            default -> {
                // Keep all buttons in outline style for unknown or empty status.
            }
        }
    }

    private void resetActionButtonStyles(Button... buttons) {
        for (Button button : buttons) {
            button.getStyleClass().setAll(STYLE_ACTION_BTN, STYLE_ACTION_BTN_OUTLINE);
        }
    }

    private void setActiveStyle(Button button, String activeStyleClass) {
        button.getStyleClass().setAll(STYLE_ACTION_BTN, activeStyleClass);
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
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
            SessionControls controls
    ) {
        ClassItem selected = classBox.getValue();
        if (selected == null) {
            showWarning(helper.getMessage("teacher.attendance.selectClassFirst"));
            return;
        }

        prepareForSessionGeneration(controls);

        new Thread(() -> {
            try {
                Map<String, Object> response = api.createSession(jwtStore, state, selected.id);
                String code = api.extractCode(response);
                long sessionId = extractSessionId(response);

                Platform.runLater(() -> {
                    controls.sessionState().setCurrentSessionId(sessionId);
                    updateGeneratedSessionUi(controls, code);
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Generating attendance session was interrupted.", ex);
                Platform.runLater(() -> {
                    resetSessionDisplay(controls);
                    controls.generateButton().setDisable(false);
                    showError(helper.getMessage("teacher.attendance.generateFailed") + " "
                            + safeErrorMessage(ex));
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to generate attendance session.", ex);
                Platform.runLater(() -> {
                    resetSessionDisplay(controls);
                    controls.generateButton().setDisable(false);
                    showError(helper.getMessage("teacher.attendance.generateFailed") + " "
                            + safeErrorMessage(ex));
                });
            }
        }).start();
    }

    private void prepareForSessionGeneration(SessionControls controls) {
        controls.generateButton().setDisable(true);
        controls.manualCode().setText(helper.getMessage("teacher.attendance.generating"));
        controls.qrImageView().setImage(null);
        controls.sessionState().reset();
    }

    private long extractSessionId(Map<String, Object> response) {
        Object sessionIdValue = response.get("sessionId");
        if (sessionIdValue instanceof Number sessionIdNumber) {
            return sessionIdNumber.longValue();
        }
        return -1L;
    }

    private void updateGeneratedSessionUi(SessionControls controls, String code) {
        setManualCode(controls.manualCode(), code);
        renderQrCode(controls.qrImageView(), code);
        controls.generateButton().setDisable(false);
    }

    private void resetSessionDisplay(SessionControls controls) {
        controls.sessionState().reset();
        controls.manualCode().setText(DASH);
        controls.qrImageView().setImage(null);
    }

    private void setManualCode(Label manualCode, String code) {
        manualCode.setText(code == null || code.isBlank() ? DASH : code);
    }

    private void renderQrCode(ImageView qrImageView, String code) {
        if (code == null || code.isBlank()) {
            qrImageView.setImage(null);
            return;
        }

        try {
            BufferedImage bufferedImage =
                    QRCodeImageUtil.generateQRCodeImage(code, 180, 180);
            qrImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
        } catch (Exception qrException) {
            LOGGER.log(Level.SEVERE, "Failed to generate QR image.", qrException);
            qrImageView.setImage(null);
        }
    }

    private void handleMarkAllPresent(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            Button markAllPresentButton,
            SessionState sessionState
    ) {
        if (!sessionState.hasActiveSession()) {
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
                    api.markAttendance(jwtStore, state, row.getStudentId(), sessionState.getCurrentSessionId(), STATUS_PRESENT);
                }

                Platform.runLater(() -> {
                    for (StudentRow row : rows) {
                        row.setStatus(STATUS_PRESENT);
                    }
                    markAllPresentButton.setDisable(false);
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Marking all students present was interrupted.", ex);
                Platform.runLater(() -> {
                    markAllPresentButton.setDisable(false);
                    showError(helper.getMessage("teacher.attendance.markAllFailed") + " "
                            + safeErrorMessage(ex));
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to mark all students present.", ex);
                Platform.runLater(() -> {
                    markAllPresentButton.setDisable(false);
                    showError(helper.getMessage("teacher.attendance.markAllFailed") + " "
                            + safeErrorMessage(ex));
                });
            }
        }).start();
    }

    private void handleClassSelection(
            TeacherApi api,
            JwtStore jwtStore,
            AuthState state,
            ComboBox<ClassItem> classBox,
            SessionControls controls
    ) {
        ClassItem selected = classBox.getValue();
        if (selected == null) {
            return;
        }

        resetSessionDisplay(controls);
        rows.clear();
        rows.add(new StudentRow(-1L, helper.getMessage("teacher.attendance.loading.students"), "-", DASH));

        new Thread(() -> {
            try {
                List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selected.id);

                Platform.runLater(() -> {
                    rows.clear();
                    for (Map<String, Object> student : students) {
                        rows.add(mapStudentRow(student));
                    }
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading students for selected class was interrupted.", ex);
                Platform.runLater(() -> {
                    rows.clear();
                    showError(helper.getMessage("teacher.attendance.error.loadStudents") + " "
                            + safeErrorMessage(ex));
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load students for selected class.", ex);
                Platform.runLater(() -> {
                    rows.clear();
                    showError(helper.getMessage("teacher.attendance.error.loadStudents") + " "
                            + safeErrorMessage(ex));
                });
            }
        }).start();
    }

    StudentRow mapStudentRow(Map<String, Object> student) {
        long studentId = Long.parseLong(String.valueOf(student.get("id")));
        String firstName = String.valueOf(student.get("firstName"));
        String lastName = String.valueOf(student.get("lastName"));
        String email = String.valueOf(student.get("email"));

        StudentRow row = new StudentRow(studentId, firstName + " " + lastName, email, DASH);
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
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Loading teacher classes was interrupted.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.attendance.loadClassesFailed") + " "
                                + safeErrorMessage(ex))
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load teacher classes.", ex);
                Platform.runLater(() ->
                        showError(helper.getMessage("teacher.attendance.loadClassesFailed") + " "
                                + safeErrorMessage(ex))
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

    private String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return UNKNOWN_ERROR;
        }
        return throwable.getMessage();
    }

    String localizeAttendanceStatus(String status) {
        if (status == null || status.isBlank() || DASH.equals(status)) {
            return DASH;
        }

        return switch (normalizeStatus(status)) {
            case STATUS_PRESENT -> helper.getMessage("student.attendance.stats.present");
            case STATUS_ABSENT -> helper.getMessage("student.attendance.stats.absent");
            case STATUS_EXCUSED -> helper.getMessage("student.attendance.stats.excused");
            default -> status;
        };
    }
}