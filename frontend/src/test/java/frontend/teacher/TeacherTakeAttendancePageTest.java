package frontend.teacher;

import frontend.ui.StudentRow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeacherTakeAttendancePageTest {

    private static TeacherTakeAttendancePage page;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        page = new TeacherTakeAttendancePage();
    }

    @Test
    void classItemToStringReturnsLabel() {
        TeacherTakeAttendancePage.ClassItem item =
                new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering");

        assertEquals("SE101 — Software Engineering", item.toString());
    }

    @Test
    void mapClassItemMapsValuesCorrectly() {
        Map<String, Object> classMap = Map.of(
                "id", 10,
                "classCode", "SE101",
                "name", "Software Engineering"
        );

        TeacherTakeAttendancePage.ClassItem item = page.mapClassItem(classMap);

        assertEquals(10L, item.id);
        assertEquals("SE101 — Software Engineering", item.label);
    }

    @Test
    void mapClassItemParsesStringIdCorrectly() {
        Map<String, Object> classMap = Map.of(
                "id", "22",
                "classCode", "WD202",
                "name", "Web Development"
        );

        TeacherTakeAttendancePage.ClassItem item = page.mapClassItem(classMap);

        assertEquals(22L, item.id);
        assertEquals("WD202 — Web Development", item.label);
    }

    @Test
    void mapStudentRowMapsValuesCorrectly() throws Exception {
        Map<String, Object> student = Map.of(
                "id", 7,
                "firstName", "Farah",
                "lastName", "Smith",
                "email", "farah@example.com"
        );

        StudentRow row = page.mapStudentRow(student);

        assertEquals("Farah Smith", row.studentNameProperty().get());
        assertEquals("farah@example.com", row.emailProperty().get());
        assertEquals("—", row.statusProperty().get());

        Field studentIdField = StudentRow.class.getDeclaredField("studentId");
        studentIdField.setAccessible(true);
        javafx.beans.property.LongProperty studentIdProperty =
                (javafx.beans.property.LongProperty) studentIdField.get(row);

        assertEquals(7L, studentIdProperty.get());
    }

    @Test
    void mapStudentRowParsesStringIdCorrectly() throws Exception {
        Map<String, Object> student = Map.of(
                "id", "9",
                "firstName", "John",
                "lastName", "Doe",
                "email", "john@example.com"
        );

        StudentRow row = page.mapStudentRow(student);

        assertEquals("John Doe", row.studentNameProperty().get());
        assertEquals("john@example.com", row.emailProperty().get());
        assertEquals("—", row.statusProperty().get());

        Field studentIdField = StudentRow.class.getDeclaredField("studentId");
        studentIdField.setAccessible(true);
        javafx.beans.property.LongProperty studentIdProperty =
                (javafx.beans.property.LongProperty) studentIdField.get(row);

        assertEquals(9L, studentIdProperty.get());
    }

    @Test
    void localizeAttendanceStatusReturnsDashForNullBlankAndDash() {
        assertEquals("—", page.localizeAttendanceStatus(null));
        assertEquals("—", page.localizeAttendanceStatus(""));
        assertEquals("—", page.localizeAttendanceStatus("   "));
        assertEquals("—", page.localizeAttendanceStatus("—"));
    }

    @Test
    void localizeAttendanceStatusLocalizesKnownStatuses() {
        assertFalse(page.localizeAttendanceStatus("PRESENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("ABSENT").isBlank());
        assertFalse(page.localizeAttendanceStatus("EXCUSED").isBlank());
    }

    @Test
    void localizeAttendanceStatusHandlesWhitespaceAndCase() {
        String result = page.localizeAttendanceStatus("  present  ");
        assertFalse(result.isBlank());
        assertNotEquals("  present  ", result);
    }

    @Test
    void localizeAttendanceStatusReturnsOriginalForUnknownStatus() {
        assertEquals("LATE", page.localizeAttendanceStatus("LATE"));
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
    void buildSelectClassLabelReturnsStyledLabel() throws Exception {
        Label label = invokePrivate("buildSelectClassLabel");

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getStyleClass().contains("section-title"));
    }

    @Test
    void buildClassBoxReturnsConfiguredComboBox() throws Exception {
        ComboBox<?> classBox = invokePrivate("buildClassBox");

        assertNotNull(classBox);
        assertFalse(classBox.getPromptText().isBlank());
        assertEquals(320.0, classBox.getMaxWidth());
    }

    @Test
    void buildQrImageViewReturnsConfiguredImageView() throws Exception {
        ImageView imageView = invokePrivate("buildQrImageView");

        assertNotNull(imageView);
        assertEquals(180.0, imageView.getFitWidth());
        assertEquals(180.0, imageView.getFitHeight());
        assertTrue(imageView.isPreserveRatio());
    }

    @Test
    void buildManualCodeLabelReturnsDefaultStyledLabel() throws Exception {
        Label label = invokePrivate("buildManualCodeLabel");

        assertEquals("—", label.getText());
        assertTrue(label.getStyleClass().contains("small-subtitle"));
    }

    @Test
    void buildGenerateButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildGenerateButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertEquals(Double.MAX_VALUE, button.getMaxWidth());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildQrCardContainsExpectedChildren() throws Exception {
        ImageView imageView = new ImageView();
        Label manualCode = new Label("CODE123");
        Button generateButton = new Button("Generate");

        VBox card = invokePrivate(
                "buildQrCard",
                new Class<?>[]{ImageView.class, Label.class, Button.class},
                imageView,
                manualCode,
                generateButton
        );

        assertNotNull(card);
        assertEquals(4, card.getChildren().size());
        assertTrue(card.getStyleClass().contains("card"));
        assertEquals(16.0, card.getPadding().getTop());

        assertInstanceOf(Label.class, card.getChildren().get(0));
        assertInstanceOf(StackPane.class, card.getChildren().get(1));
        assertInstanceOf(VBox.class, card.getChildren().get(2));
        assertSame(generateButton, card.getChildren().get(3));

        StackPane qrArea = (StackPane) card.getChildren().get(1);
        assertEquals(Pos.CENTER, qrArea.getAlignment());
        assertEquals(180.0, qrArea.getPrefHeight());
        assertEquals(180.0, qrArea.getPrefWidth());
        assertTrue(qrArea.getStyleClass().contains("qr-area"));
    }

    @Test
    void buildStudentsTitleReturnsStyledEmptyLabel() throws Exception {
        Label label = invokePrivate("buildStudentsTitle");

        assertNotNull(label);
        assertEquals("", label.getText());
        assertTrue(label.getStyleClass().contains("section-title"));
    }

    @Test
    void buildMarkAllPresentButtonReturnsStyledButton() throws Exception {
        Button button = invokePrivate("buildMarkAllPresentButton");

        assertNotNull(button);
        assertFalse(button.getText().isBlank());
        assertTrue(button.getStyleClass().contains("pill"));
        assertTrue(button.getStyleClass().contains("pill-green"));
    }

    @Test
    void buildStudentsHeaderContainsExpectedNodes() throws Exception {
        Label studentsTitle = new Label("Students");
        Button markAllPresentButton = new Button("Mark all");

        HBox header = invokePrivate(
                "buildStudentsHeader",
                new Class<?>[]{Label.class, Button.class},
                studentsTitle,
                markAllPresentButton
        );

        assertNotNull(header);
        assertEquals(Pos.CENTER_LEFT, header.getAlignment());
        assertEquals(3, header.getChildren().size());
        assertSame(studentsTitle, header.getChildren().get(0));
        assertInstanceOf(Region.class, header.getChildren().get(1));
        assertSame(markAllPresentButton, header.getChildren().get(2));
    }

    @Test
    void createActionButtonReturnsConfiguredButton() throws Exception {
        Button button = invokePrivate(
                "createActionButton",
                new Class<?>[]{String.class},
                "✓"
        );

        assertEquals("✓", button.getText());
        assertEquals(30.0, button.getMinWidth());
        assertEquals(30.0, button.getMinHeight());
        assertEquals(30.0, button.getPrefWidth());
        assertEquals(30.0, button.getPrefHeight());
    }

    @Test
    void buildStudentsTableCreatesFourColumns() throws Exception {
        TableView<?> table = invokePrivate(
                "buildStudentsTable",
                new Class<?>[]{
                        frontend.api.TeacherApi.class,
                        frontend.auth.JwtStore.class,
                        frontend.auth.AuthState.class,
                        long[].class
                },
                null,
                null,
                null,
                new long[]{-1L}
        );

        assertNotNull(table);
        assertEquals(300.0, table.getPrefHeight());
        assertEquals(36.0, table.getFixedCellSize());
        assertTrue(table.getStyleClass().contains("students-table"));
        assertEquals(4, table.getColumns().size());
        assertFalse(table.getColumns().get(0).getText().isBlank());
        assertFalse(table.getColumns().get(1).getText().isBlank());
        assertFalse(table.getColumns().get(2).getText().isBlank());
        assertFalse(table.getColumns().get(3).getText().isBlank());
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(page);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(page, args);
    }
}