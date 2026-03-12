package frontend.teacher;

import frontend.AppLayout;
import frontend.StudentRow;
import frontend.api.TeacherApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.util.QRCodeImageUtil;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class TeacherTakeAttendancePage {

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();

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

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();
        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        TeacherApi api = new TeacherApi(backendUrl);

        final long[] currentSessionId = { -1L };

        VBox page = new VBox(16);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Take Attendance");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select class and generate QR code");
        subtitle.getStyleClass().add("subtitle");

        Label selectClass = new Label("Select class");
        selectClass.getStyleClass().add("section-title");

        ComboBox<ClassItem> classBox = new ComboBox<>();
        classBox.setPromptText("Choose a class");
        classBox.setMaxWidth(320);

        // ---- QR / code card ----
        VBox qrCard = new VBox(12);
        qrCard.getStyleClass().add("card");
        qrCard.setPadding(new Insets(16));

        Label qrTitle = new Label("Attendance QR Code");
        qrTitle.getStyleClass().add("section-title");

        ImageView qrImageView = new ImageView();
        qrImageView.setFitWidth(180);
        qrImageView.setFitHeight(180);
        qrImageView.setPreserveRatio(true);

        StackPane qrArea = new StackPane(qrImageView);
        qrArea.setAlignment(Pos.CENTER);
        qrArea.setPrefHeight(180);
        qrArea.setPrefWidth(180);
        qrArea.getStyleClass().add("qr-area");

        Label manualTitle = new Label("Manual Code");
        manualTitle.getStyleClass().add("small-title");

        Label manualCode = new Label("—");
        manualCode.getStyleClass().add("small-subtitle");

        VBox manualBox = new VBox(4, manualTitle, manualCode);
        manualBox.setAlignment(Pos.CENTER);
        manualBox.getStyleClass().add("manual-box");

        Button generate = new Button("Generate Code");
        generate.getStyleClass().addAll("pill", "pill-green");
        generate.setMaxWidth(Double.MAX_VALUE);

        generate.setOnAction(e -> {
            ClassItem selected = classBox.getValue();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a class first.", ButtonType.OK).showAndWait();
                return;
            }

            generate.setDisable(true);
            manualCode.setText("Generating...");
            qrImageView.setImage(null);
            currentSessionId[0] = -1L;

            new Thread(() -> {
                try {
                    Map<String, Object> res = api.createSession(jwtStore, state, selected.id);
                    String code = api.extractCode(res);

                    Number sessionIdNum = (Number) res.get("sessionId");
                    if (sessionIdNum != null) {
                        currentSessionId[0] = sessionIdNum.longValue();
                    }

                    Platform.runLater(() -> {
                        manualCode.setText(code == null || code.isBlank() ? "—" : code);

                        if (code != null && !code.isBlank()) {
                            try {
                                BufferedImage bufferedImage =
                                        QRCodeImageUtil.generateQRCodeImage(code, 180, 180);
                                qrImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                            } catch (Exception qrEx) {
                                qrEx.printStackTrace();
                            }
                        }

                        generate.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        manualCode.setText("—");
                        qrImageView.setImage(null);
                        generate.setDisable(false);
                        new Alert(Alert.AlertType.ERROR,
                                "Generate failed: " + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                    });
                }
            }).start();
        });

        qrCard.getChildren().addAll(qrTitle, qrArea, manualBox, generate);

        // ---- Students header + mark all ----
        Label studentsTitle = new Label();
        studentsTitle.getStyleClass().add("section-title");
        studentsTitle.textProperty().bind(Bindings.size(rows).asString("Students (%d)"));

        Button markAllPresentBtn = new Button("Mark All Present");
        markAllPresentBtn.getStyleClass().addAll("pill", "pill-green");

        markAllPresentBtn.setOnAction(e -> {
            if (currentSessionId[0] <= 0) {
                new Alert(Alert.AlertType.WARNING, "Please generate a session first.", ButtonType.OK).showAndWait();
                return;
            }

            if (rows.isEmpty()) {
                return;
            }

            markAllPresentBtn.setDisable(true);

            new Thread(() -> {
                try {
                    for (StudentRow row : rows) {
                        api.markAttendance(jwtStore, state, row.getStudentId(), currentSessionId[0], "PRESENT");
                    }

                    Platform.runLater(() -> {
                        for (StudentRow row : rows) {
                            row.setStatus("PRESENT");
                        }
                        markAllPresentBtn.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        markAllPresentBtn.setDisable(false);
                        new Alert(Alert.AlertType.ERROR,
                                "Failed to mark all present: " + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                    });
                }
            }).start();
        });

        Region studentsSpacer = new Region();
        HBox.setHgrow(studentsSpacer, Priority.ALWAYS);

        HBox studentsHeader = new HBox(10, studentsTitle, studentsSpacer, markAllPresentBtn);
        studentsHeader.setAlignment(Pos.CENTER_LEFT);

        // ---- Students table ----
        TableView<StudentRow> table = new TableView<>(rows);
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(36);
        table.setPrefHeight(300);

        TableColumn<StudentRow, String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(d -> d.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        TableColumn<StudentRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        TableColumn<StudentRow, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {

            private final Button presentBtn = new Button("✓");

            private final Button absentBtn = new Button("✕");
            private final Button excusedBtn = new Button("◔");
            private final HBox box = new HBox(8, presentBtn, absentBtn, excusedBtn);

            {
                box.setAlignment(Pos.CENTER);

                presentBtn.setMinSize(30, 30);
                presentBtn.setPrefSize(30, 30);
                absentBtn.setMinSize(30, 30);
                absentBtn.setPrefSize(30, 30);
                excusedBtn.setMinSize(30, 30);
                excusedBtn.setPrefSize(30, 30);

                presentBtn.setOnAction(e -> updateAttendance("PRESENT"));
                absentBtn.setOnAction(e -> updateAttendance("ABSENT"));
                excusedBtn.setOnAction(e -> updateAttendance("EXCUSED"));
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

            private void updateAttendance(String status) {
                StudentRow row = getTableView().getItems().get(getIndex());

                if (currentSessionId[0] <= 0) {
                    new Alert(Alert.AlertType.WARNING, "Please generate a session first.", ButtonType.OK).showAndWait();
                    return;
                }

                presentBtn.setDisable(true);
                absentBtn.setDisable(true);
                excusedBtn.setDisable(true);

                new Thread(() -> {
                    try {
                        api.markAttendance(jwtStore, state, row.getStudentId(), currentSessionId[0], status);

                        Platform.runLater(() -> {
                            row.setStatus(status);
                            applyStatusStyles(status);
                            presentBtn.setDisable(false);
                            absentBtn.setDisable(false);
                            excusedBtn.setDisable(false);
                            table.refresh();
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            presentBtn.setDisable(false);
                            absentBtn.setDisable(false);
                            excusedBtn.setDisable(false);
                            new Alert(Alert.AlertType.ERROR,
                                    "Failed to update attendance: " + ex.getMessage(),
                                    ButtonType.OK).showAndWait();
                        });
                    }
                }).start();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                StudentRow row = getTableView().getItems().get(getIndex());
                applyStatusStyles(row.statusProperty().get());
                setGraphic(box);
            }
        });

        table.getColumns().setAll(colName, colEmail, colStatus, colActions);

        page.getChildren().addAll(title, subtitle, selectClass, classBox, qrCard, studentsHeader, table);

        // ---- Load classes on open ----
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
                        new Alert(Alert.AlertType.ERROR,
                                "Failed to load classes: " + ex.getMessage(),
                                ButtonType.OK).showAndWait()
                );
            }
        }).start();

        // ---- When class selected, load students ----
        classBox.setOnAction(e -> {
            ClassItem selected = classBox.getValue();
            if (selected == null) return;

            currentSessionId[0] = -1L;
            manualCode.setText("—");
            qrImageView.setImage(null);

            rows.clear();
            rows.add(new StudentRow(-1L, "Loading...", "-", "—"));

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

                            StudentRow row = new StudentRow(studentId, fn + " " + ln, email, "—");
                            row.setExcuseReason("");
                            rows.add(row);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        rows.clear();
                        new Alert(Alert.AlertType.ERROR,
                                "Failed to load students: " + ex.getMessage(),
                                ButtonType.OK).showAndWait();
                    });
                }
            }).start();
        });

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Teacher Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
                page,
                "second",
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
}