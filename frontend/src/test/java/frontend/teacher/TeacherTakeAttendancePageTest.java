package frontend.teacher;

import frontend.auth.AppRouter;
import frontend.ui.StudentRow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.api.TeacherApi;

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



    @Test
    void buildStudentsTableActionCellShouldHandleEmptyAndNonEmptyRows() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                TableView<StudentRow> table = invokePrivate(
                        "buildStudentsTable",
                        new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, long[].class},
                        null,
                        null,
                        null,
                        new long[]{-1L}
                );

                @SuppressWarnings("unchecked")
                TableColumn<StudentRow, Void> actionColumn =
                        (TableColumn<StudentRow, Void>) table.getColumns().get(3);

                TableCell<StudentRow, Void> cell = actionColumn.getCellFactory().call(actionColumn);

                TableRow<StudentRow> row = new TableRow<>();
                cell.updateTableView(table);
                cell.updateTableColumn(actionColumn);
                row.updateTableView(table);
                cell.updateTableRow(row);

                Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
                updateItem.setAccessible(true);

                updateItem.invoke(cell, null, true);
                assertNull(cell.getGraphic());

                StudentRow studentRow = new StudentRow(1L, "Farah Smith", "farah@example.com", "—");
                table.getItems().setAll(studentRow);

                row.updateIndex(0);
                cell.updateIndex(0);

                updateItem.invoke(cell, null, false);
                assertNotNull(cell.getGraphic());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Test
    void buildStudentsTableActionCellShouldApplyStatusStyles() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                TableView<StudentRow> table = invokePrivate(
                        "buildStudentsTable",
                        new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, long[].class},
                        null,
                        null,
                        null,
                        new long[]{-1L}
                );

                @SuppressWarnings("unchecked")
                TableColumn<StudentRow, Void> actionColumn =
                        (TableColumn<StudentRow, Void>) table.getColumns().get(3);

                TableCell<StudentRow, Void> cell = actionColumn.getCellFactory().call(actionColumn);

                TableRow<StudentRow> row = new TableRow<>();
                StudentRow studentRow = new StudentRow(1L, "Farah Smith", "farah@example.com", "PRESENT");
                table.getItems().setAll(studentRow);

                cell.updateTableView(table);
                cell.updateTableColumn(actionColumn);
                row.updateTableView(table);
                row.updateIndex(0);
                cell.updateTableRow(row);
                cell.updateIndex(0);

                Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
                updateItem.setAccessible(true);
                updateItem.invoke(cell, null, false);

                Method applyStatusStyles = cell.getClass().getDeclaredMethod("applyStatusStyles", String.class);
                applyStatusStyles.setAccessible(true);

                assertDoesNotThrow(() -> applyStatusStyles.invoke(cell, "PRESENT"));
                assertDoesNotThrow(() -> applyStatusStyles.invoke(cell, "ABSENT"));
                assertDoesNotThrow(() -> applyStatusStyles.invoke(cell, "EXCUSED"));
                assertDoesNotThrow(() -> applyStatusStyles.invoke(cell, "LATE"));

                assertNotNull(cell.getGraphic());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Test
    void buildStudentsTableActionCellShouldToggleButtonsDisabledState() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                TableView<StudentRow> table = invokePrivate(
                        "buildStudentsTable",
                        new Class<?>[]{TeacherApi.class, JwtStore.class, AuthState.class, long[].class},
                        null,
                        null,
                        null,
                        new long[]{-1L}
                );

                @SuppressWarnings("unchecked")
                TableColumn<StudentRow, Void> actionColumn =
                        (TableColumn<StudentRow, Void>) table.getColumns().get(3);

                TableCell<StudentRow, Void> cell = actionColumn.getCellFactory().call(actionColumn);

                TableRow<StudentRow> row = new TableRow<>();
                StudentRow studentRow = new StudentRow(1L, "Farah Smith", "farah@example.com", "PRESENT");
                table.getItems().setAll(studentRow);

                cell.updateTableView(table);
                cell.updateTableColumn(actionColumn);
                row.updateTableView(table);
                row.updateIndex(0);
                cell.updateTableRow(row);
                cell.updateIndex(0);

                Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
                updateItem.setAccessible(true);
                updateItem.invoke(cell, null, false);

                Method setButtonsDisabled = cell.getClass().getDeclaredMethod("setButtonsDisabled", boolean.class);
                setButtonsDisabled.setAccessible(true);

                assertDoesNotThrow(() -> setButtonsDisabled.invoke(cell, true));
                assertDoesNotThrow(() -> setButtonsDisabled.invoke(cell, false));

                assertNotNull(cell.getGraphic());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS), "FX task timed out");

        if (error.get() != null) {
            if (error.get() instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void buildShouldReturnPage() throws Exception {
        runOnFxThreadAndWait(() -> {
            TeacherTakeAttendancePage page = new TeacherTakeAttendancePage();
            Scene scene = new Scene(new StackPane());
            AppRouter router = new AppRouter(scene);
            JwtStore jwtStore = new JwtStore();
            AuthState state = new AuthState("dummy-token", frontend.auth.Role.TEACHER, "Teacher");

            Parent root = page.build(scene, router, jwtStore, state);

            assertNotNull(root);
        });

    }
    @Test
    void buildShouldReturnRootNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            Scene scene = new Scene(new StackPane());
            AppRouter router = new AppRouter(scene);
            JwtStore jwtStore = new JwtStore();
            AuthState state = new AuthState("dummy-token", frontend.auth.Role.TEACHER, "Teacher");

            Parent root = page.build(scene, router, jwtStore, state);

            assertNotNull(root);
        });
    }
    @Test
    void handleGenerateSessionShouldResetUiBeforeBackgroundWork() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
                TeacherTakeAttendancePage.ClassItem selected =
                        new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering");
                classBox.setValue(selected);

                Button generateButton = new Button("Generate");
                Label manualCode = new Label("OLD");
                ImageView qrImageView = new ImageView();
                qrImageView.setImage(new javafx.scene.image.WritableImage(10, 10));
                long[] currentSessionId = {99L};

                Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(
                        "handleGenerateSession",
                        TeacherApi.class,
                        JwtStore.class,
                        AuthState.class,
                        ComboBox.class,
                        Button.class,
                        Label.class,
                        ImageView.class,
                        long[].class
                );
                method.setAccessible(true);

                method.invoke(
                        page,
                        null,
                        null,
                        null,
                        classBox,
                        generateButton,
                        manualCode,
                        qrImageView,
                        currentSessionId
                );

                assertTrue(generateButton.isDisable());
                assertFalse(manualCode.getText().isBlank());
                assertNull(qrImageView.getImage());
                assertEquals(-1L, currentSessionId[0]);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Test
    void handleClassSelectionShouldReturnWhenNoClassSelected() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
                Label manualCode = new Label("OLD");
                ImageView qrImageView = new ImageView();
                long[] currentSessionId = {99L};

                Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(
                        "handleClassSelection",
                        TeacherApi.class,
                        JwtStore.class,
                        AuthState.class,
                        ComboBox.class,
                        Label.class,
                        ImageView.class,
                        long[].class
                );
                method.setAccessible(true);

                method.invoke(
                        page,
                        null,
                        null,
                        null,
                        classBox,
                        manualCode,
                        qrImageView,
                        currentSessionId
                );

                assertEquals("OLD", manualCode.getText());
                assertEquals(99L, currentSessionId[0]);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Test
    void handleClassSelectionShouldResetUiBeforeLoadingStudents() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                ComboBox<TeacherTakeAttendancePage.ClassItem> classBox = new ComboBox<>();
                TeacherTakeAttendancePage.ClassItem item =
                        new TeacherTakeAttendancePage.ClassItem(1L, "SE101 — Software Engineering");
                classBox.setValue(item);

                Label manualCode = new Label("ABC123");
                ImageView qrImageView = new ImageView();
                qrImageView.setImage(new javafx.scene.image.WritableImage(10, 10));
                long[] currentSessionId = {55L};

                Method method = TeacherTakeAttendancePage.class.getDeclaredMethod(
                        "handleClassSelection",
                        TeacherApi.class,
                        JwtStore.class,
                        AuthState.class,
                        ComboBox.class,
                        Label.class,
                        ImageView.class,
                        long[].class
                );
                method.setAccessible(true);

                method.invoke(
                        page,
                        null,
                        null,
                        null,
                        classBox,
                        manualCode,
                        qrImageView,
                        currentSessionId
                );

                assertEquals(-1L, currentSessionId[0]);
                assertEquals("—", manualCode.getText());
                assertNull(qrImageView.getImage());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}