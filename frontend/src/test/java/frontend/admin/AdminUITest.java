package frontend.admin;

import frontend.ui.ClassRow;
import frontend.ui.UserRow;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminUITest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void shouldCreateStatCardCorrectly() {
        Pane pane = AdminUI.makeStatCard("Students", "120", "🎓", "accent-green");

        assertInstanceOf(VBox.class, pane);

        VBox card = (VBox) pane;
        assertTrue(card.getStyleClass().contains("card"));
        assertTrue(card.getStyleClass().contains("stat-card"));
        assertEquals(86, card.getMinHeight());

        assertEquals(1, card.getChildren().size());
        assertInstanceOf(HBox.class, card.getChildren().getFirst());
    }

    @Test
    void shouldCreateActionCardCorrectly() {
        Pane pane = AdminUI.makeActionCard("Manage Users", "Open users page", "👤", "qa-purple");

        assertInstanceOf(VBox.class, pane);

        VBox card = (VBox) pane;
        assertTrue(card.getStyleClass().contains("action-card"));
        assertTrue(card.getStyleClass().contains("qa-purple"));
        assertEquals(74, card.getMinHeight());
        assertEquals(1, card.getChildren().size());

        assertInstanceOf(HBox.class, card.getChildren().getFirst());
    }

    @Test
    void shouldCreateClassCardCorrectly() {
        Pane pane = AdminUI.makeClassCard(
                "Mathematics",
                "MTH101",
                "teacher@example.com",
                "Spring 2025"
        );

        assertInstanceOf(VBox.class, pane);

        VBox card = (VBox) pane;
        assertTrue(card.getStyleClass().contains("card"));
        assertTrue(card.getStyleClass().contains("class-card"));
        assertEquals(110, card.getMinHeight());

        assertEquals(4, card.getChildren().size());

        assertInstanceOf(Label.class, card.getChildren().get(0));
        assertEquals("Mathematics", ((Label) card.getChildren().get(0)).getText());

        assertInstanceOf(Label.class, card.getChildren().get(1));
        assertEquals("MTH101", ((Label) card.getChildren().get(1)).getText());
    }

    @Test
    void shouldCreateSmallSummaryCardCorrectly() {
        Pane pane = AdminUI.smallSummaryCard("Teachers", "12", "👥", "accent-purple");

        assertInstanceOf(VBox.class, pane);

        VBox card = (VBox) pane;
        assertTrue(card.getStyleClass().contains("card"));
        assertTrue(card.getStyleClass().contains("summary-card"));
        assertEquals(1, card.getChildren().size());

        assertInstanceOf(HBox.class, card.getChildren().getFirst());
    }

    @Test
    void shouldBuildClassesTableWithExpectedColumns() {
        TableView<ClassRow> table = AdminUI.buildClassesTable();

        assertNotNull(table);
        assertTrue(table.getStyleClass().contains("table"));
        assertEquals(5, table.getColumns().size());
    }

    @Test
    void shouldBuildUsersTableWithExpectedColumns() {
        TableView<UserRow> table = AdminUI.buildUsersTable();

        assertNotNull(table);
        assertTrue(table.getStyleClass().contains("table"));
        assertEquals(4, table.getColumns().size());
    }

    @Test
    void shouldCreateIconButtonWithFixedSize() {
        Button button = AdminUI.iconBtn("✎");

        assertEquals("✎", button.getText());
        assertEquals(26.0, button.getMinWidth());
        assertEquals(26.0, button.getMinHeight());
        assertEquals(26.0, button.getPrefWidth());
        assertEquals(26.0, button.getPrefHeight());
        assertEquals(26.0, button.getMaxWidth());
        assertEquals(26.0, button.getMaxHeight());
        assertFalse(button.isFocusTraversable());
    }

    @Test
    void usersTableShouldHaveActionsColumnAsLastColumn() {
        TableView<UserRow> table = AdminUI.buildUsersTable();

        TableColumn<UserRow, ?> lastColumn = table.getColumns().get(3);
        assertNotNull(lastColumn);
        assertNotNull(lastColumn.getText());
    }

    @Test
    void actionCardShouldContainArrowAndIconStructure() {
        Pane pane = AdminUI.makeActionCard("Manage Users", "Open users page", "👤", "qa-purple");

        VBox card = (VBox) pane;
        HBox row = (HBox) card.getChildren().getFirst();

        assertFalse(row.getChildren().isEmpty());

        boolean containsStackPane = row.getChildren().stream().anyMatch(StackPane.class::isInstance);
        boolean containsLabel = row.getChildren().stream().anyMatch(Label.class::isInstance);

        assertTrue(containsStackPane);
        assertTrue(containsLabel);
    }

    @Test
    void classCardShouldContainTeacherAndScheduleRows() {
        Pane pane = AdminUI.makeClassCard(
                "Mathematics",
                "MTH101",
                "teacher@example.com",
                "Spring 2025"
        );

        VBox card = (VBox) pane;

        Node teacherRow = card.getChildren().get(2);
        Node scheduleRow = card.getChildren().get(3);

        assertInstanceOf(HBox.class, teacherRow);
        assertInstanceOf(HBox.class, scheduleRow);
    }
    @Test
    void usersTableActionsCellShouldHandleEmptyAndNonEmptyRows() throws Exception {
        TableView<UserRow> table = AdminUI.buildUsersTable();

        @SuppressWarnings("unchecked")
        TableColumn<UserRow, Void> actionsColumn =
                (TableColumn<UserRow, Void>) table.getColumns().get(3);

        assertNotNull(actionsColumn.getCellFactory());

        var cell = actionsColumn.getCellFactory().call(actionsColumn);

        var updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
        updateItem.setAccessible(true);

        updateItem.invoke(cell, null, true);
        assertNull(cell.getGraphic());

        UserRow row = new UserRow("Oscar\noscar@example.com", "Student", "2 classes");
        table.getItems().setAll(row);

        cell.updateTableView(table);
        cell.updateIndex(0);

        updateItem.invoke(cell, null, false);
        assertNotNull(cell.getGraphic());
        assertInstanceOf(HBox.class, cell.getGraphic());
    }
    @Test
    void usersTableActionsCellShouldContainButtons() throws Exception {
        TableView<UserRow> table = AdminUI.buildUsersTable();

        @SuppressWarnings("unchecked")
        TableColumn<UserRow, Void> actionsColumn =
                (TableColumn<UserRow, Void>) table.getColumns().get(3);

        var cell = actionsColumn.getCellFactory().call(actionsColumn);

        var updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
        updateItem.setAccessible(true);

        UserRow row = new UserRow("Anna\nanna@example.com", "Teacher", "5 classes");
        table.getItems().setAll(row);

        cell.updateTableView(table);
        cell.updateIndex(0);

        updateItem.invoke(cell, null, false);

        assertInstanceOf(HBox.class, cell.getGraphic());
        HBox actions = (HBox) cell.getGraphic();
        assertFalse(actions.getChildren().isEmpty());
        assertTrue(actions.getChildren().stream().allMatch(Button.class::isInstance));
    }

}