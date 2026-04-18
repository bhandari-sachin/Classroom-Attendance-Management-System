package frontend.admin;

import frontend.ClassRow;
import frontend.UserRow;
import frontend.ui.HelperClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class AdminUI {

    private static final double ACTION_ICON_BUTTON_SIZE = 26.0;

    private AdminUI() {
        // Utility class
    }

    public static Pane makeStatCard(String label, String value, String icon, String accentClass) {
        VBox card = createCard("card", "stat-card");
        card.setMinHeight(86);
        card.setMaxWidth(Double.MAX_VALUE);

        VBox left = new VBox(4);
        left.getChildren().addAll(
                styledLabel(label, "stat-label"),
                styledLabel(value, "stat-number")
        );

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);
        top.getChildren().addAll(
                left,
                growRegion(),
                createBadge(icon, accentClass)
        );

        card.getChildren().add(top);
        return card;
    }

    public static Pane makeActionCard(String title, String desc, String icon, String styleClass) {
        VBox card = createCard("action-card", styleClass);
        card.setMinHeight(74);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        StackPane iconBox = new StackPane(new Label(icon));
        iconBox.getStyleClass().add("action-icon");

        VBox textBox = new VBox(2);
        textBox.getChildren().addAll(
                styledLabel(title, "action-title"),
                styledLabel(desc, "action-desc")
        );

        Label arrow = styledLabel("→", "action-arrow");

        HBox row = new HBox(10, iconBox, textBox, growRegion(), arrow);
        row.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().add(row);
        return card;
    }

    public static Pane makeClassCard(String className, String code, String teacherEmail, String schedule) {
        VBox card = createCard("card", "class-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);

        Label nameLabel = styledLabel(className, "class-name");
        Label codeLabel = styledLabel(code, "class-code");
        Label teacherText = styledLabel(teacherEmail, "class-meta");
        Label scheduleText = styledLabel(schedule, "class-meta");

        HBox teacherRow = createInfoRow("👤", teacherText);
        HBox timeRow = createInfoRow("🗓", scheduleText);

        card.getChildren().addAll(nameLabel, codeLabel, teacherRow, timeRow);
        return card;
    }

    public static Pane smallSummaryCard(String label, String value, String icon, String accentClass) {
        VBox card = createCard("card", "summary-card");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        VBox texts = new VBox(2);
        texts.getChildren().addAll(
                styledLabel(value, "stat-number"),
                styledLabel(label, "stat-label")
        );

        HBox row = new HBox(10, createBadge(icon, accentClass), texts);
        row.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().add(row);
        return card;
    }

    public static TableView<ClassRow> buildClassesTable() {
        HelperClass helper = new HelperClass();

        TableView<ClassRow> table = createBaseTable();

        table.getColumns().addAll(
                textColumn(helper.getMessage("common.table.column.name"), row -> row.getValue().classNameProperty()),
                textColumn(helper.getMessage("common.table.column.class"), row -> row.getValue().codeProperty()),
                textColumn(helper.getMessage("admin.users.filter.teacher"), row -> row.getValue().teacherProperty()),
                textColumn(helper.getMessage("common.table.column.session"), row -> row.getValue().scheduleProperty()),
                textColumn(
                        helper.getMessage("admin.users.filter.student") + "s",
                        row -> row.getValue().studentsProperty()
                )
        );

        return table;
    }

    public static TableView<UserRow> buildUsersTable() {
        HelperClass helper = new HelperClass();

        TableView<UserRow> table = createBaseTable();

        TableColumn<UserRow, Void> actionsColumn =
                new TableColumn<>(helper.getMessage("teacher.attendance.actions"));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox actionsBox = createUserActionsBox();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionsBox);
            }
        });

        table.getColumns().addAll(
                textColumn(helper.getMessage("admin.users.table.user"), row -> row.getValue().userProperty()),
                textColumn(helper.getMessage("admin.users.table.type"), row -> row.getValue().typeProperty()),
                textColumn(helper.getMessage("admin.users.table.enrolled"), row -> row.getValue().enrolledProperty()),
                actionsColumn
        );

        return table;
    }

    private static <T> TableView<T> createBaseTable() {
        TableView<T> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private static <T> TableColumn<T, String> textColumn(
            String title,
            javafx.util.Callback<TableColumn.CellDataFeatures<T, String>, javafx.beans.value.ObservableValue<String>> factory
    ) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(factory);
        return column;
    }

    private static VBox createCard(String... styleClasses) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll(styleClasses);
        return card;
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private static StackPane createBadge(String icon, String accentClass) {
        StackPane badge = new StackPane(new Label(icon));
        badge.getStyleClass().addAll("icon-badge", accentClass);
        return badge;
    }

    private static HBox createInfoRow(String icon, Label textLabel) {
        HBox row = new HBox(8, new Label(icon), textLabel);
        row.getStyleClass().add("info-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static HBox createUserActionsBox() {
        Button editButton = iconBtn("✎");
        editButton.getStyleClass().add("action-icon-btn");

        Button deleteButton = iconBtn("🗑");
        deleteButton.getStyleClass().add("danger-icon-btn");

        HBox box = new HBox(10, editButton, deleteButton);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Region growRegion() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    static Button iconBtn(String text) {
        Button button = new Button(text);
        button.setMinSize(ACTION_ICON_BUTTON_SIZE, ACTION_ICON_BUTTON_SIZE);
        button.setPrefSize(ACTION_ICON_BUTTON_SIZE, ACTION_ICON_BUTTON_SIZE);
        button.setMaxSize(ACTION_ICON_BUTTON_SIZE, ACTION_ICON_BUTTON_SIZE);
        button.setFocusTraversable(false);
        return button;
    }
}