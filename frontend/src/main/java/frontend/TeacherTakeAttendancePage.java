package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class TeacherTakeAttendancePage {

    private ContextMenu hamburgerMenu;

    // for selection actions
    private TableView<StudentRow> table;
    private Button presentBtn, absentBtn, excusedBtn;

    public Parent createView(Runnable onBackToDashboard) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");

        /* ================= TOP BAR ================= */
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(12, Color.web("#D9D9D9"));

        Label name = new Label("Name");
        name.getStyleClass().add("topbar-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label menu = new Label("≡");
        menu.getStyleClass().add("icon-button");

        hamburgerMenu = buildHamburgerMenu(onBackToDashboard);

        menu.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (hamburgerMenu.isShowing()) hamburgerMenu.hide();
                else hamburgerMenu.show(menu, Side.BOTTOM, 0, 6);
            }
        });

        root.setOnMousePressed(e -> {
            if (hamburgerMenu != null && hamburgerMenu.isShowing()) hamburgerMenu.hide();
        });

        topBar.getChildren().addAll(avatar, name, spacer, menu);
        root.setTop(topBar);

        /* ================= CONTENT ================= */
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        root.setCenter(content);

        Label title = new Label("Take Attendance");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select class and generate QR code");
        subtitle.getStyleClass().add("subtitle");

        Label selectLbl = new Label("Select class");
        selectLbl.getStyleClass().add("field-label");

        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.getItems().addAll("Choose a class", "OOP1", "Databases", "Web Dev");
        classCombo.setValue("Choose a class");
        classCombo.getStyleClass().add("filter-combo");

        /* ================= QR CARD ================= */
        VBox qrOuter = new VBox(10);
        qrOuter.getStyleClass().add("qr-outer");

        Label qrTitle = new Label("Attendance QR Code");
        qrTitle.getStyleClass().add("section-title");

        StackPane qrBox = new StackPane();
        qrBox.getStyleClass().add("qr-box");
        qrBox.setMinHeight(220);

        Label qrIcon = new Label("〰");
        qrIcon.setFont(Font.font(26));
        qrBox.getChildren().add(qrIcon);

        VBox manual = new VBox(4);
        manual.getStyleClass().add("manual-code");
        manual.setAlignment(Pos.CENTER);

        Label m1 = new Label("Manual Code");
        m1.getStyleClass().add("manual-title");

        Label m2 = new Label("code-4948-838-01");
        m2.getStyleClass().add("manual-value");

        manual.getChildren().addAll(m1, m2);

        qrOuter.getChildren().addAll(qrTitle, qrBox, manual);

        /* ================= STUDENTS HEADER ================= */
        Label studentsTitle = new Label("Students (3)");
        studentsTitle.getStyleClass().add("section-title");

        Button markAll = new Button("Mark All Present");
        markAll.getStyleClass().add("mark-all-btn");

        presentBtn = new Button("Present");
        absentBtn = new Button("Absent");
        excusedBtn = new Button("Excused");

        presentBtn.getStyleClass().add("status-btn-present");
        absentBtn.getStyleClass().add("status-btn-absent");
        excusedBtn.getStyleClass().add("status-btn-excused");

        // hidden until row selected
        presentBtn.setVisible(false);
        absentBtn.setVisible(false);
        excusedBtn.setVisible(false);

        HBox selectActions = new HBox(8, presentBtn, absentBtn, excusedBtn);
        selectActions.setAlignment(Pos.CENTER_RIGHT);

        Region sspacer = new Region();
        HBox.setHgrow(sspacer, Priority.ALWAYS);

        HBox studentsHeader = new HBox(12, studentsTitle, sspacer, selectActions, markAll);
        studentsHeader.setAlignment(Pos.CENTER_LEFT);

        /* ================= TABLE ================= */
        table = new TableView<>();
        table.getStyleClass().add("students-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Student");
        nameCol.setCellValueFactory(data -> data.getValue().studentNameProperty());

        TableColumn<StudentRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        table.getColumns().addAll(nameCol, statusCol);

        ObservableList<StudentRow> items = FXCollections.observableArrayList(
                new StudentRow("User", "Present"),
                new StudentRow("User", "Absent"),
                new StudentRow("User", "Excused")
        );

        table.setItems(items);

        // when row selected -> show buttons
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean selected = newSel != null;
            presentBtn.setVisible(selected);
            absentBtn.setVisible(selected);
            excusedBtn.setVisible(selected);
        });

        // button actions -> change selected status
        presentBtn.setOnAction(e -> setSelectedStatus("Present"));
        absentBtn.setOnAction(e -> setSelectedStatus("Absent"));
        excusedBtn.setOnAction(e -> setSelectedStatus("Excused"));

        // mark all present
        markAll.setOnAction(e -> {
            for (StudentRow s : table.getItems()) s.setStatus("Present");
            table.refresh();
        });

        content.getChildren().addAll(
                title,
                subtitle,
                selectLbl,
                classCombo,
                qrOuter,
                studentsHeader,
                table
        );

        return root;
    }

    private void setSelectedStatus(String status) {
        StudentRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.setStatus(status);
        table.refresh();
    }

    private ContextMenu buildHamburgerMenu(Runnable onBackToDashboard) {

        MenuItem dashboard = new MenuItem("Dashboard", new Label("▦"));
        dashboard.setOnAction(e -> {
            hamburgerMenu.hide();
            onBackToDashboard.run();
        });

        // this page
        MenuItem takeAttendance = new MenuItem("Take Attendance", new Label("📷"));
        takeAttendance.setOnAction(e -> hamburgerMenu.hide());

        MenuItem reports = new MenuItem("Reports", new Label("📋"));
        reports.setOnAction(e -> {
            hamburgerMenu.hide();
            TeacherReportsPage page = new TeacherReportsPage();
            Parent view = page.createView(onBackToDashboard);
            hamburgerMenu.getOwnerNode().getScene().setRoot(view);
        });

        MenuItem signout = new MenuItem("Sign out", new Label("⎋"));
        signout.setOnAction(e -> {
            hamburgerMenu.hide();
            System.out.println("TODO: Sign out");
        });

        ContextMenu menu = new ContextMenu(
                dashboard,
                takeAttendance,
                reports,
                new SeparatorMenuItem(),
                signout
        );

        menu.getStyleClass().add("hamburger-menu");
        return menu;
    }
}
