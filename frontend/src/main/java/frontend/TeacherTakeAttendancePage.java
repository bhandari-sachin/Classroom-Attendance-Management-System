package frontend;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
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
import service.AttendanceService;
import service.ClassService;
import service.SessionService;
import config.SessionSQL;
import config.AttendanceSQL;
import util.QRCodeGenerator;

import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TeacherTakeAttendancePage {

    // Use ObservableList populated from backend
    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();

    private final ClassService classService = new ClassService(new ClassSQL());
    private final SessionService sessionService = new SessionService(new SessionSQL());
    private final AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        Long teacherId = state.getUserId();
        String teacherName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

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
        classBox.setMaxWidth(200);

        // Load classes from backend
        List<CourseClass> classes = classService.getClassesByTeacher(teacherId);
        ObservableList<CourseClass> classItems = FXCollections.observableArrayList();
        if (classes != null) classItems.addAll(classes);
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
        ComboBox<Session> sessionBox = new ComboBox<>();
        sessionBox.setPromptText("Choose a session");
        sessionBox.setMaxWidth(400);
        sessionBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getSessionDate() + " " + item.getStartTime() + " — " + (item.getTopic() == null ? "" : item.getTopic()));
            }
        });
        sessionBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getSessionDate() + " " + item.getStartTime() + " — " + (item.getTopic() == null ? "" : item.getTopic()));
            }
        });

        // When class selected, fetch students and sessions from backend
        classBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            rows.clear();
            sessionBox.getItems().clear();
            if (newV != null) {
                List<Student> students = classService.getStudentsInClass(newV.getId());
                for (Student s : students) {
                    rows.add(new StudentRow(s.getStudentId(), s.getName(), s.getEmail(), "Absent"));
                }

                List<Session> sessions = sessionService.getSessionsByClassId(newV.getId());
                if (sessions != null) sessionBox.getItems().addAll(sessions);
            }
        });

        // ---- QR CARD ----
        VBox qrCard = new VBox(12);
        qrCard.getStyleClass().add("card");
        qrCard.setPadding(new Insets(16));

        Label qrTitle = new Label("Attendance QR Code");
        qrTitle.getStyleClass().add("section-title");

        ImageView qrImageView = new ImageView();
        qrImageView.setFitHeight(180);
        qrImageView.setPreserveRatio(true);
        qrImageView.getStyleClass().add("qr-area");

        HBox qrImageHolder = new HBox(qrImageView);
        qrImageHolder.setAlignment(Pos.CENTER);
        qrImageHolder.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(qrImageHolder, Priority.NEVER);
        qrImageHolder.prefWidthProperty().bind(qrCard.widthProperty());
        HBox.setHgrow(qrImageHolder, Priority.ALWAYS);

        Label manualTitle = new Label("Manual Code");
        manualTitle.getStyleClass().add("small-title");

        Label manualCode = new Label("");
        manualCode.getStyleClass().add("small-subtitle");

        VBox manualBox = new VBox(4, manualTitle, manualCode);
        manualBox.setAlignment(Pos.CENTER);
        manualBox.getStyleClass().add("manual-box");

        Button startSessionBtn = new Button("Start Session");
        startSessionBtn.getStyleClass().addAll("pill", "pill-green");

        Button endSessionBtn = new Button("End Session");
        endSessionBtn.getStyleClass().addAll("pill");

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

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                baos.flush();
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                Image fxImage = new Image(is);
                qrImageView.setImage(fxImage);
                baos.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error starting session: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        endSessionBtn.setOnAction(e -> {
            Session sel = sessionBox.getValue();
            if (sel == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please select a session first.", ButtonType.OK);
                a.showAndWait();
                return;
            }
            try {
                sessionService.endSession(sel.getId());
                qrImageView.setImage(null);
                manualCode.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error ending session: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        qrCard.getChildren().addAll(qrTitle, sessionBox, qrImageHolder, manualBox, new HBox(8, startSessionBtn, endSessionBtn));

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

        // Visible only when selection exists
        btnPresent.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        btnAbsent.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        btnExcused.visibleProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());

        btnPresent.managedProperty().bind(btnPresent.visibleProperty());
        btnAbsent.managedProperty().bind(btnAbsent.visibleProperty());
        btnExcused.managedProperty().bind(btnExcused.visibleProperty());

        // Actions
        btnPresent.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            Session sess = sessionBox.getSelectionModel().getSelectedItem();
            if (s != null && sess != null) {
                if (s.getStudentId() != null) attendanceService.markPresent(s.getStudentId(), sess.getId());
                s.setStatus("Present");
                s.setExcuseReason("");
                table.refresh();
            } else {
                Alert a = new Alert(Alert.AlertType.WARNING, "Select a student and an active session first.", ButtonType.OK);
                a.showAndWait();
            }
        });

        btnAbsent.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            Session sess = sessionBox.getSelectionModel().getSelectedItem();
            if (s != null && sess != null) {
                if (s.getStudentId() != null) attendanceService.markAbsent(s.getStudentId(), sess.getId());
                s.setStatus("Absent");
                s.setExcuseReason("");
                table.refresh();
            } else {
                Alert a = new Alert(Alert.AlertType.WARNING, "Select a student and an active session first.", ButtonType.OK);
                a.showAndWait();
            }
        });

        // IMPORTANT: Excused opens the new page to write reason
        btnExcused.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            Session sess = sessionBox.getSelectionModel().getSelectedItem();
            if (s == null || sess == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Select a student and a session first.", ButtonType.OK);
                a.showAndWait();
                return;
            }

            scene.setRoot(
                    new TeacherExcuseReasonPage().build(
                            scene,
                            router,
                            jwtStore,
                            state,
                            s,
                            () -> router.go("teacher-take") // ✅ go back cleanly
                    )
            );
        });

        markAllPresent.setOnAction(e -> {
            Session sess = sessionBox.getSelectionModel().getSelectedItem();
            if (sess == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Select a session first.", ButtonType.OK);
                a.showAndWait();
                return;
            }
            for (StudentRow s : rows) {
                if (s.getStudentId() != null) attendanceService.markPresent(s.getStudentId(), sess.getId());
                s.setStatus("Present");
                s.setExcuseReason("");
            }
            table.refresh();
        });

        page.getChildren().addAll(
                title,
                subtitle,
                selectClass,
                classBox,
                qrCard,
                studentsHeader,
                table
        );

        return AppLayout.wrapWithSidebar(
                teacherName,
                "Teacher Panel",
                "Dashboard",
                "Take Attendance",
                "Reports",
                "Email",
                page,
                "second", // ✅ active = Take Attendance
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