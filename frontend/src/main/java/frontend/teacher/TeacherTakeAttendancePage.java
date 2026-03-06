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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import org.example.util.QRCodeImageUtil;

import java.util.List;
import java.util.Map;

public class TeacherTakeAttendancePage {

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();

    private static class ClassItem {
        final long id;
        final String label;
        ClassItem(long id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String teacherName = (state.getName() == null || state.getName().isBlank()) ? "Name" : state.getName();
        TeacherApi api = new TeacherApi("http://localhost:8081");

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

            new Thread(() -> {
                try {
                    Map<String, Object> res = api.createSession(jwtStore, state, selected.id);
                    String code = api.extractCode(res);

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
                        generate.setDisable(false);
                        new Alert(Alert.AlertType.ERROR, "Generate failed: " + ex.getMessage(), ButtonType.OK).showAndWait();
                    });
                }
            }).start();
        });

        qrCard.getChildren().addAll(qrTitle, qrArea, manualBox, generate);

        // ---- Students table ----
        Label studentsTitle = new Label();
        studentsTitle.getStyleClass().add("section-title");
        studentsTitle.textProperty().bind(Bindings.size(rows).asString("Students (%d)"));

        TableView<StudentRow> table = new TableView<>(rows);
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(30);
        table.setPrefHeight(260);

        TableColumn<StudentRow, String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(d -> d.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        TableColumn<StudentRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(colName, colEmail, colStatus);

        page.getChildren().addAll(title, subtitle, selectClass, classBox, qrCard, studentsTitle, table);

        // ✅ Load classes on open
        new Thread(() -> {
            try {
                List<Map<String, Object>> list = api.getMyClasses(jwtStore, state);

                var items = list.stream().map(m -> {
                    long id = Long.parseLong(String.valueOf(m.get("id")));
                    String classCode = String.valueOf(m.get("classCode"));
                    String name2 = String.valueOf(m.get("name"));
                    return new ClassItem(id, classCode + " — " + name2);
                }).toList();

                Platform.runLater(() -> classBox.getItems().setAll(items));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Failed to load classes: " + ex.getMessage(), ButtonType.OK).showAndWait()
                );
            }
        }).start();

        // ✅ When a class is selected -> load students
        classBox.setOnAction(e -> {
            ClassItem selected = classBox.getValue();
            if (selected == null) return;

            rows.clear();
            rows.add(new StudentRow("Loading...", "-", "—"));

            new Thread(() -> {
                try {
                    List<Map<String, Object>> students = api.getStudentsForClass(jwtStore, state, selected.id);

                    Platform.runLater(() -> {
                        rows.clear();
                        for (var s : students) {
                            String fn = String.valueOf(s.get("firstName"));
                            String ln = String.valueOf(s.get("lastName"));
                            String email = String.valueOf(s.get("email"));

                            // ✅ FIX: your constructor requires (name, email, status)
                            StudentRow row = new StudentRow(fn + " " + ln, email, "—");
                            row.setExcuseReason(""); // optional
                            rows.add(row);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        rows.clear();
                        new Alert(Alert.AlertType.ERROR, "Failed to load students: " + ex.getMessage(), ButtonType.OK).showAndWait();
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
                    @Override public void logout() { jwtStore.clear(); router.go("login"); }
                }
        );
    }
}