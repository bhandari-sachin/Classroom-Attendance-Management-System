package frontend.teacher;

import frontend.ui.StudentRow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeacherEmailPageTest {

    private static TeacherEmailPage page;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        page = new TeacherEmailPage();
    }

    @Test
    void mapClassItemMapsValuesCorrectly() {
        Map<String, Object> classData = Map.of(
                "id", 15,
                "classCode", "SE101",
                "name", "Software Engineering"
        );

        TeacherEmailPage.ClassItem item = page.mapClassItem(classData);

        assertEquals(15L, item.id);
        assertEquals("SE101 — Software Engineering", item.label);
        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void mapClassItemParsesStringIdCorrectly() {
        Map<String, Object> classData = Map.of(
                "id", "22",
                "classCode", "WD202",
                "name", "Web Development"
        );

        TeacherEmailPage.ClassItem item = page.mapClassItem(classData);

        assertEquals(22L, item.id);
        assertEquals("WD202 — Web Development", item.label);
    }

    @Test
    void mapStudentRowMapsValuesCorrectly() {
        Map<String, Object> studentData = Map.of(
                "id", 7,
                "firstName", "Farah",
                "lastName", "Smith",
                "email", "farah@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals(7L, row.getStudentId());
        assertEquals("Farah Smith", row.getStudentName());
        assertEquals("farah@example.com", row.getEmail());
        assertEquals("—", row.getStatus());
    }

    @Test
    void mapStudentRowParsesStringIdCorrectly() {
        Map<String, Object> studentData = Map.of(
                "id", "9",
                "firstName", "John",
                "lastName", "Doe",
                "email", "john@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals(9L, row.getStudentId());
        assertEquals("John Doe", row.getStudentName());
        assertEquals("john@example.com", row.getEmail());
        assertEquals("—", row.getStatus());
    }

    @Test
    void mapStudentRowTrimsFullName() {
        Map<String, Object> studentData = Map.of(
                "id", 10,
                "firstName", "Anna",
                "lastName", "",
                "email", "anna@example.com"
        );

        StudentRow row = page.mapStudentRow(studentData);

        assertEquals("Anna", row.getStudentName());
    }

    @Test
    void buildTitleReturnsStyledLabel() throws Exception {
        Label title = invokePrivate("buildTitle");

        assertNotNull(title);
        assertFalse(title.getText().isBlank());
        assertTrue(title.getStyleClass().contains("title"));
    }

    @Test
    void buildSubtitleReturnsStyledLabel() throws Exception {
        Label subtitle = invokePrivate("buildSubtitle");

        assertNotNull(subtitle);
        assertFalse(subtitle.getText().isBlank());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildClassBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> classBox = invokePrivate("buildClassBox");

        assertNotNull(classBox);
        assertFalse(classBox.getPromptText().isBlank());
        assertEquals(360.0, classBox.getMaxWidth());
    }

    @Test
    void buildRefreshButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildRefreshButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildTopRowContainsControlsAndAlignment() throws Exception {
        ComboBox<TeacherEmailPage.ClassItem> classBox = new ComboBox<>();
        Button refreshButton = new Button("Refresh");

        HBox topRow = invokePrivate(
                "buildTopRow",
                new Class<?>[]{ComboBox.class, Button.class},
                classBox,
                refreshButton
        );

        assertNotNull(topRow);
        assertEquals(Pos.CENTER_LEFT, topRow.getAlignment());
        assertEquals(2, topRow.getChildren().size());
        assertSame(classBox, topRow.getChildren().get(0));
        assertSame(refreshButton, topRow.getChildren().get(1));
    }

    @Test
    void buildStudentTableCreatesExpectedColumnsAndSettings() throws Exception {
        TableView<?> table = invokePrivate("buildStudentTable");

        assertNotNull(table);
        assertEquals(340.0, table.getPrefHeight());
        assertEquals(2, table.getColumns().size());

        TableColumn<?, ?> firstColumn = table.getColumns().get(0);
        TableColumn<?, ?> secondColumn = table.getColumns().get(1);

        assertFalse(firstColumn.getText().isBlank());
        assertFalse(secondColumn.getText().isBlank());
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherEmailPage.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(page);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherEmailPage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(page, args);
    }
}