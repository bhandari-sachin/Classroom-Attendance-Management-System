package frontend.admin;

import frontend.ClassRow;
import frontend.UserRow;
import frontend.ui.HelperClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class AdminUI {

    public static Pane makeStatCard(String label, String value, String icon, String accentClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("card", "stat-card");

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);

        VBox left = new VBox(4);
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        Label v = new Label(value);
        v.getStyleClass().add("stat-number");

        left.getChildren().addAll(l, v);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane badge = new StackPane(new Label(icon));
        badge.getStyleClass().addAll("icon-badge", accentClass);

        top.getChildren().addAll(left, spacer, badge);
        card.getChildren().add(top);

        card.setMinHeight(86);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    public static Pane makeActionCard(String title, String desc, String icon, String styleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("action-card", styleClass);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane(new Label(icon));
        iconBox.getStyleClass().add("action-icon");

        VBox text = new VBox(2);
        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");

        text.getChildren().addAll(t, d);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("action-arrow");

        row.getChildren().addAll(iconBox, text, spacer, arrow);
        card.getChildren().add(row);

        card.setMinHeight(74);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    public static Pane makeClassCard(String className, String code, String teacherEmail, String schedule) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("card", "class-card");

        Label name = new Label(className);
        name.getStyleClass().add("class-name");

        Label codeLbl = new Label(code);
        codeLbl.getStyleClass().add("class-code");

        Label teacherText = new Label(teacherEmail);
        teacherText.getStyleClass().add("class-meta");

        Label timeText = new Label(schedule);
        timeText.getStyleClass().add("class-meta");

        HBox teacherRow = new HBox(8, new Label("👤"), teacherText);
        teacherRow.getStyleClass().add("info-row");
        teacherRow.setAlignment(Pos.CENTER_LEFT);

        HBox timeRow = new HBox(8, new Label("🗓"), timeText);
        timeRow.getStyleClass().add("info-row");
        timeRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(name, codeLbl, teacherRow, timeRow);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);

        return card;
    }

    public static Pane smallSummaryCard(String label, String value, String icon, String accentClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("card", "summary-card");

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane(new Label(icon));
        badge.getStyleClass().addAll("icon-badge", accentClass);

        VBox texts = new VBox(2);

        Label v = new Label(value);
        v.getStyleClass().add("stat-number");

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        texts.getChildren().addAll(v, l);
        row.getChildren().addAll(badge, texts);

        card.getChildren().add(row);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    public static TableView<ClassRow> buildClassesTable() {
        HelperClass helper = new HelperClass();

        TableView<ClassRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ClassRow, String> cName = new TableColumn<>(helper.getMessage("common.table.column.name"));
        cName.setCellValueFactory(d -> d.getValue().classNameProperty());

        TableColumn<ClassRow, String> cCode = new TableColumn<>(helper.getMessage("common.table.column.class"));
        cCode.setCellValueFactory(d -> d.getValue().codeProperty());

        TableColumn<ClassRow, String> cTeacher = new TableColumn<>(helper.getMessage("admin.users.filter.teacher"));
        cTeacher.setCellValueFactory(d -> d.getValue().teacherProperty());

        TableColumn<ClassRow, String> cSchedule = new TableColumn<>(helper.getMessage("common.table.column.session"));
        cSchedule.setCellValueFactory(d -> d.getValue().scheduleProperty());

        TableColumn<ClassRow, String> cStudents = new TableColumn<>(helper.getMessage("admin.users.filter.student") + "s");
        cStudents.setCellValueFactory(d -> d.getValue().studentsProperty());

        table.getColumns().addAll(cName, cCode, cTeacher, cSchedule, cStudents);
        return table;
    }

    public static TableView<UserRow> buildUsersTable() {
        HelperClass helper = new HelperClass();

        TableView<UserRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserRow, String> cUser = new TableColumn<>(helper.getMessage("admin.users.table.user"));
        cUser.setCellValueFactory(d -> d.getValue().userProperty());

        TableColumn<UserRow, String> cType = new TableColumn<>(helper.getMessage("admin.users.table.type"));
        cType.setCellValueFactory(d -> d.getValue().typeProperty());

        TableColumn<UserRow, String> cEnrolled = new TableColumn<>(helper.getMessage("admin.users.table.enrolled"));
        cEnrolled.setCellValueFactory(d -> d.getValue().enrolledProperty());

        TableColumn<UserRow, Void> cActions = new TableColumn<>(helper.getMessage("teacher.attendance.actions"));
        cActions.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(10);
            private final Button edit = iconBtn("✎");
            private final Button del = iconBtn("🗑");

            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.getStyleClass().add("action-icon-btn");
                del.getStyleClass().add("danger-icon-btn");
                box.getChildren().addAll(edit, del);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(cUser, cType, cEnrolled, cActions);
        table.getItems().addAll(
                new UserRow("User\nuser@example.com", "Admin", "-"),
                new UserRow("teacher\nteacher@example.com", "Teacher", "4")
        );

        return table;
    }

    private static Button iconBtn(String txt) {
        Button b = new Button(txt);
        b.setMinSize(26, 26);
        b.setPrefSize(26, 26);
        b.setMaxSize(26, 26);
        b.setFocusTraversable(false);
        return b;
    }
}