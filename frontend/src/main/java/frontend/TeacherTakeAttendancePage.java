package frontend;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TeacherTakeAttendancePage {

    // Use shared store so email + status persist
    private final ObservableList<StudentRow> rows = DataStore.getStudents();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

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

        ComboBox<String> classBox = new ComboBox<>();
        classBox.getItems().addAll("Class A", "Class B", "Class C");
        classBox.setPromptText("Choose a class");
        classBox.setMaxWidth(200);

        // ---- QR PLACEHOLDER ----
        VBox qrCard = new VBox(12);
        qrCard.getStyleClass().add("card");
        qrCard.setPadding(new Insets(16));

        Label qrTitle = new Label("Attendance QR Code");
        qrTitle.getStyleClass().add("section-title");

        Region qrArea = new Region();
        qrArea.setPrefHeight(180);
        qrArea.getStyleClass().add("qr-area");

        Label manualTitle = new Label("Manual Code");
        manualTitle.getStyleClass().add("small-title");

        Label manualCode = new Label("code-4948-838-01");
        manualCode.getStyleClass().add("small-subtitle");

        VBox manualBox = new VBox(4, manualTitle, manualCode);
        manualBox.setAlignment(Pos.CENTER);
        manualBox.getStyleClass().add("manual-box");

        qrCard.getChildren().addAll(qrTitle, qrArea, manualBox);

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

        btnExcused.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s == null) return;

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
            for (StudentRow s : rows) {
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