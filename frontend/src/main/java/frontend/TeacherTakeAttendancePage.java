package frontend;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.CourseClass;
import model.Student;
import model.Session;
import service.ClassService;
import service.SessionService;
import config.ClassSQL;
import config.SessionSQL;
import util.QRCodeGenerator;

import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.zxing.WriterException;

public class TeacherTakeAttendancePage {

    // Use ObservableList populated from backend
    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();

    private final ClassService classService = new ClassService(new ClassSQL());
    private final SessionService sessionService = new SessionService(new SessionSQL());

    public Parent build(Scene scene, String teacherName) {

        VBox page = new VBox(16);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");

        Label title = new Label("Take Attendance");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select class and generate QR code");
        subtitle.getStyleClass().add("subtitle");

        // ---- CLASS SELECT ----
        Label selectClass = new Label("Select class");
        selectClass.getStyleClass().add("section-title");

        ComboBox<CourseClass> classBox = new ComboBox<>();
        classBox.setPromptText("Choose a class");
        classBox.setMaxWidth(400);

        // Load classes from backend
        List<CourseClass> classes = classService.getAllClasses();
        ObservableList<CourseClass> classItems = FXCollections.observableArrayList(classes);
        classBox.setItems(classItems);

        // show friendly label in combobox
        classBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(CourseClass item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        classBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CourseClass item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        // ---- SESSION SELECT ----
        Label selectSessionLbl = new Label("Select session");
        selectSessionLbl.getStyleClass().add("section-title");

        ComboBox<Session> sessionBox = new ComboBox<>();
        sessionBox.setPromptText("Choose a session");
        sessionBox.setMaxWidth(400);
        sessionBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSessionDate() + " " + item.getStartTime() + " — " + (item.getTopic() == null ? "" : item.getTopic()));
                }
            }
        });
        sessionBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSessionDate() + " " + item.getStartTime() + " — " + (item.getTopic() == null ? "" : item.getTopic()));
                }
            }
        });

        // When class selected, fetch students and sessions from backend
        classBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            rows.clear();
            sessionBox.getItems().clear();
            if (newV != null) {
                List<Student> students = classService.getStudentsInClass(newV.getId());
                for (Student s : students) {
                    rows.add(new StudentRow(s.getName(), s.getEmail(), "Absent"));
                }

                List<Session> sessions = sessionService.getSessionsByClassId(newV.getId());
                if (sessions != null) sessionBox.getItems().addAll(sessions);
            }
        });

        // ---- QR PLACEHOLDER ----
        VBox qrCard = new VBox(12);
        qrCard.getStyleClass().add("card");
        qrCard.setPadding(new Insets(16));

        Label qrTitle = new Label("Attendance QR Code");
        qrTitle.getStyleClass().add("section-title");

        ImageView qrImageView = new ImageView();
        qrImageView.setFitWidth(180);
        qrImageView.setFitHeight(180);
        qrImageView.setPreserveRatio(true);
        qrImageView.getStyleClass().add("qr-area");

        Label manualTitle = new Label("Manual Code");
        manualTitle.getStyleClass().add("small-title");

        Label manualCode = new Label("");
        manualCode.getStyleClass().add("small-subtitle");

        VBox manualBox = new VBox(4, manualTitle, manualCode);
        manualBox.setAlignment(Pos.CENTER);
        manualBox.getStyleClass().add("manual-box");

        Button startSessionBtn = new Button("Start Session");
        startSessionBtn.getStyleClass().addAll("pill", "pill-green");

        // start session -> generate code, persist via SessionService, show QR
        startSessionBtn.setOnAction(e -> {
            Session sel = sessionBox.getValue();
            if (sel == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please select a session first.", ButtonType.OK);
                a.showAndWait();
                return;
            }

            try {
                String code = sessionService.startSession(sel.getId());
                manualCode.setText(code);

                BufferedImage img = QRCodeGenerator.generateQRCodeImage(code, 300, 300);

                // convert BufferedImage to JavaFX Image without Swing dependency
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                baos.flush();
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                Image fxImage = new Image(is);
                qrImageView.setImage(fxImage);
                baos.close();

            } catch (WriterException ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Failed to generate QR image: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error starting session: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        HBox qrTop = new HBox(12, qrImageView, manualBox);
        qrTop.setAlignment(Pos.CENTER);

        qrCard.getChildren().addAll(qrTitle, qrTop, startSessionBtn);

        // ================= STUDENTS SECTION =================
        Label studentsTitle = new Label();
        studentsTitle.getStyleClass().add("section-title");
        studentsTitle.textProperty().bind(Bindings.size(rows).asString("Students (%d)"));

        Button btnPresent = new Button("Present");
        btnPresent.getStyleClass().addAll("pill", "pill-green");

        Button btnAbsent = new Button("Absent");
        btnAbsent.getStyleClass().addAll("pill", "pill-red");

        Button btnExcused = new Button("Excused");
        btnExcused.getStyleClass().addAll("pill", "pill-orange");

        Button markAllPresent = new Button("Mark All Present");
        markAllPresent.getStyleClass().addAll("pill", "pill-green");

        HBox buttons = new HBox(10, btnPresent, btnAbsent, btnExcused, markAllPresent);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox studentsHeader = new HBox(12, studentsTitle, spacer, buttons);
        studentsHeader.setAlignment(Pos.CENTER_LEFT);

        TableView<StudentRow> table = new TableView<>(rows);
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(30);
        table.setPrefHeight(260);

        TableColumn<StudentRow, String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(data -> data.getValue().studentNameProperty());

        TableColumn<StudentRow, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<StudentRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        table.getColumns().addAll(colName, colEmail, colStatus);

        // Disable buttons until row selected
        btnPresent.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnAbsent.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnExcused.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // If you want them hidden until selection, keep this:
        btnPresent.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        btnAbsent.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        btnExcused.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());

        btnPresent.managedProperty().bind(btnPresent.visibleProperty());
        btnAbsent.managedProperty().bind(btnAbsent.visibleProperty());
        btnExcused.managedProperty().bind(btnExcused.visibleProperty());

        // Actions
        btnPresent.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                s.setStatus("Present");
                s.setExcuseReason("");
                table.refresh();
            }
        });

        btnAbsent.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                s.setStatus("Absent");
                s.setExcuseReason("");
                table.refresh();
            }
        });

        // IMPORTANT: Excused opens the new page to write reason
        btnExcused.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s == null) return;

            scene.setRoot(
                    new TeacherExcuseReasonPage().build(
                            scene,
                            teacherName,
                            s,
                            () -> scene.setRoot(build(scene, teacherName))
                    )
            );
        });

        markAllPresent.setOnAction(e -> {
            for (StudentRow s : rows) {
                s.setStatus("Present");
                s.setExcuseReason("");
            }
            table.refresh();
        });

        VBox leftCol = new VBox(12, selectClass, classBox, selectSessionLbl, sessionBox, qrCard);
        leftCol.setMaxWidth(420);

        VBox rightCol = new VBox(12, studentsHeader, table);
        rightCol.setFillWidth(true);

        HBox main = new HBox(18, leftCol, rightCol);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        page.getChildren().addAll(
                title,
                subtitle,
                main
        );

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Student Panel", "Dashboard", "Mark Attendance", "My Attendance", "Contact", page,
                "takeAttendance",
                new AppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new TeacherDashboardApp().build(scene, teacherName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(build(scene, teacherName)); }
                    @Override public void goReports() { scene.setRoot(new TeacherReportsPage().build(scene, teacherName)); }
                    @Override public void goEmail() { scene.setRoot(new TeacherEmailPage().build(scene, teacherName)); }
                    @Override public void logout() { System.out.println("TODO: Logout"); }
                }
        );
    }
}
