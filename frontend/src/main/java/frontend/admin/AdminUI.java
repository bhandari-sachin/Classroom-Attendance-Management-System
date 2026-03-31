package frontend.admin;

import frontend.ClassRow;
import frontend.UserRow;
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
import util.I18n;
import util.RtlUtil;

public class AdminUI {

    public static Pane makeStatCard(String label, String value, String icon, String accentClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("card", "stat-card");
        RtlUtil.apply(card);

        HBox top = new HBox();
        top.setAlignment(Pos.TOP_LEFT);
        RtlUtil.apply(top);

        VBox left = new VBox(4);
        RtlUtil.apply(left);

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
        RtlUtil.apply(card);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(row);

        StackPane iconBox = new StackPane(new Label(icon));
        iconBox.getStyleClass().add("action-icon");

        VBox text = new VBox(2);
        RtlUtil.apply(text);

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
        RtlUtil.apply(card);

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
        RtlUtil.apply(teacherRow);

        HBox timeRow = new HBox(8, new Label("🗓"), timeText);
        timeRow.getStyleClass().add("info-row");
        timeRow.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(timeRow);

        card.getChildren().addAll(name, codeLbl, teacherRow, timeRow);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);

        return card;
    }

    public static Pane smallSummaryCard(String label, String value, String icon, String accentClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("card", "summary-card");
        RtlUtil.apply(card);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        RtlUtil.apply(row);

        StackPane badge = new StackPane(new Label(icon));
        badge.getStyleClass().addAll("icon-badge", accentClass);

        VBox texts = new VBox(2);
        RtlUtil.apply(texts);

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
        TableView<ClassRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        RtlUtil.apply(table);

        TableColumn<ClassRow, String> cName = new TableColumn<>(I18n.t("common.table.column.class"));
        cName.setCellValueFactory(d -> d.getValue().classNameProperty());

        TableColumn<ClassRow, String> cCode = new TableColumn<>(I18n.t("admin.classes.dialog.add.classCode"));
        cCode.setCellValueFactory(d -> d.getValue().codeProperty());

        TableColumn<ClassRow, String> cTeacher = new TableColumn<>(I18n.t("common.table.column.email"));
        cTeacher.setCellValueFactory(d -> d.getValue().teacherProperty());

        TableColumn<ClassRow, String> cSchedule = new TableColumn<>(I18n.t("admin.dashboard.recentClasses.noSchedule"));
        cSchedule.setCellValueFactory(d -> d.getValue().scheduleProperty());

        TableColumn<ClassRow, String> cStudents = new TableColumn<>(I18n.t("admin.dashboard.stats.students"));
        cStudents.setCellValueFactory(d -> d.getValue().studentsProperty());

        table.getColumns().addAll(cName, cCode, cTeacher, cSchedule, cStudents);
        return table;
    }

    public static TableView<UserRow> buildUsersTable() {
        TableView<UserRow> table = new TableView<>();
        table.getStyleClass().add("table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        RtlUtil.apply(table);

        TableColumn<UserRow, String> cUser = new TableColumn<>(I18n.t("admin.users.table.user"));
        cUser.setCellValueFactory(d -> d.getValue().userProperty());

        TableColumn<UserRow, String> cType = new TableColumn<>(I18n.t("admin.users.table.type"));
        cType.setCellValueFactory(d -> d.getValue().typeProperty());

        TableColumn<UserRow, String> cEnrolled = new TableColumn<>(I18n.t("admin.users.table.enrolled"));
        cEnrolled.setCellValueFactory(d -> d.getValue().enrolledProperty());

        TableColumn<UserRow, Void> cActions = new TableColumn<>(I18n.t("common.button.edit"));
        cActions.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(10);
            private final Button edit = iconBtn("✎");
            private final Button del = iconBtn("🗑");

            {
                box.setAlignment(Pos.CENTER_LEFT);
                RtlUtil.apply(box);
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