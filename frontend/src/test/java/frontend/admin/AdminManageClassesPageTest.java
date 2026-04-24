package frontend.admin;

import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import frontend.ui.ClassRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AdminManageClassesPageTest {

    private final AdminManageClassesPage page = new AdminManageClassesPage();

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void shouldFilterAllRowsWhenSearchQueryIsBlank() {
        ClassRow row1 = new ClassRow("Mathematics", "MTH101", "teacher1@example.com", "Spring 2025", "20");
        ClassRow row2 = new ClassRow("Physics", "PHY202", "teacher2@example.com", "Autumn 2025", "15");

        FilteredList<ClassRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        page.applySearchFilter(filteredRows, "");

        assertEquals(2, filteredRows.size());
        assertTrue(filteredRows.contains(row1));
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldFilterRowsByClassName() {
        ClassRow math = new ClassRow("Mathematics", "MTH101", "teacher1@example.com", "Spring 2025", "20");
        ClassRow physics = new ClassRow("Physics", "PHY202", "teacher2@example.com", "Autumn 2025", "15");

        FilteredList<ClassRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(math, physics),
                row -> true
        );

        page.applySearchFilter(filteredRows, "math");

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(math));
    }

    @Test
    void shouldFilterRowsByCodeTeacherOrSchedule() {
        ClassRow row1 = new ClassRow("Mathematics", "MTH101", "teacher1@example.com", "Spring 2025", "20");
        ClassRow row2 = new ClassRow("Physics", "PHY202", "teacher2@example.com", "Autumn 2025", "15");

        FilteredList<ClassRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        page.applySearchFilter(filteredRows, "phy202");
        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row2));

        page.applySearchFilter(filteredRows, "teacher1");
        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row1));

        page.applySearchFilter(filteredRows, "autumn");
        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldReturnNoRowsWhenNoClassMatchesSearch() {
        ClassRow row1 = new ClassRow("Mathematics", "MTH101", "teacher1@example.com", "Spring 2025", "20");
        ClassRow row2 = new ClassRow("Physics", "PHY202", "teacher2@example.com", "Autumn 2025", "15");

        FilteredList<ClassRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        page.applySearchFilter(filteredRows, "biology");

        assertEquals(0, filteredRows.size());
    }

    @Test
    void shouldIgnoreNullRowsInClassFilter() {
        ClassRow row = new ClassRow("Mathematics", "MTH101", "teacher@example.com", "Spring 2025", "20");

        FilteredList<ClassRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row, null),
                item -> true
        );

        page.applySearchFilter(filteredRows, "math");

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row));
    }

    @Test
    void shouldFilterAllStudentsWhenSearchQueryIsBlank() {
        AdminStudentDto student1 = new AdminStudentDto();
        student1.setFirstName("Oscar");
        student1.setLastName("al");
        student1.setEmail("oscar@example.com");
        student1.setStudentCode("ST001");

        AdminStudentDto student2 = new AdminStudentDto();
        student2.setFirstName("Anna");
        student2.setLastName("Smith");
        student2.setEmail("anna@example.com");
        student2.setStudentCode("ST002");

        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(
                FXCollections.observableArrayList(student1, student2),
                student -> true
        );

        page.applyStudentFilter(filteredStudents, " ");

        assertEquals(2, filteredStudents.size());
    }

    @Test
    void shouldFilterStudentsByFullNameEmailOrStudentCode() {
        AdminStudentDto student1 = new AdminStudentDto();
        student1.setFirstName("Oscar");
        student1.setLastName("al");
        student1.setEmail("oscar@example.com");
        student1.setStudentCode("ST001");

        AdminStudentDto student2 = new AdminStudentDto();
        student2.setFirstName("Anna");
        student2.setLastName("Smith");
        student2.setEmail("anna@example.com");
        student2.setStudentCode("ST002");

        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(
                FXCollections.observableArrayList(student1, student2),
                student -> true
        );

        page.applyStudentFilter(filteredStudents, "oscar al");
        assertEquals(1, filteredStudents.size());
        assertTrue(filteredStudents.contains(student1));

        page.applyStudentFilter(filteredStudents, "anna@example.com");
        assertEquals(1, filteredStudents.size());
        assertTrue(filteredStudents.contains(student2));

        page.applyStudentFilter(filteredStudents, "st001");
        assertEquals(1, filteredStudents.size());
        assertTrue(filteredStudents.contains(student1));
    }

    @Test
    void shouldReturnNoStudentsWhenNoStudentMatchesSearch() {
        AdminStudentDto student = new AdminStudentDto();
        student.setFirstName("Oscar");
        student.setLastName("al");
        student.setEmail("oscar@example.com");
        student.setStudentCode("ST001");

        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(
                FXCollections.observableArrayList(student),
                s -> true
        );

        page.applyStudentFilter(filteredStudents, "biology");

        assertEquals(0, filteredStudents.size());
    }

    @Test
    void shouldIgnoreNullStudentsInStudentFilter() {
        AdminStudentDto student = new AdminStudentDto();
        student.setFirstName("Oscar");
        student.setLastName("al");
        student.setEmail("oscar@example.com");
        student.setStudentCode("ST001");

        FilteredList<AdminStudentDto> filteredStudents = new FilteredList<>(
                FXCollections.observableArrayList(student, null),
                s -> true
        );

        page.applyStudentFilter(filteredStudents, "oscar");

        assertEquals(1, filteredStudents.size());
        assertTrue(filteredStudents.contains(student));
    }

    @Test
    void shouldMapAdminClassDtoToClassRow() {
        AdminClassDto dto = new AdminClassDto();
        dto.setName("Mathematics");
        dto.setClassCode("MTH101");
        dto.setTeacherEmail("teacher@example.com");
        dto.setSemester("Spring");
        dto.setAcademicYear("2025/2026");
        dto.setStudents(25);

        ClassRow result = page.mapClassRow(dto);

        assertEquals("Mathematics", result.getClassName());
        assertEquals("MTH101", result.getCode());
        assertEquals("teacher@example.com", result.getTeacher());
        assertEquals("Spring 2025/2026", result.getSchedule());
        assertEquals("25", result.getStudents());
    }

    @Test
    void shouldMapAdminClassDtoWithNullValuesToEmptyStrings() {
        AdminClassDto dto = new AdminClassDto();
        dto.setName(null);
        dto.setClassCode(null);
        dto.setTeacherEmail(null);
        dto.setSemester(null);
        dto.setAcademicYear(null);
        dto.setStudents(0);

        ClassRow result = page.mapClassRow(dto);

        assertEquals("", result.getClassName());
        assertEquals("", result.getCode());
        assertEquals("", result.getTeacher());
        assertEquals("", result.getSchedule());
        assertEquals("0", result.getStudents());
    }

    @Test
    void shouldParseIntegerCorrectly() {
        assertEquals(123, page.parseInteger("123"));
    }

    @Test
    void shouldReturnNullWhenParsingInvalidInteger() {
        assertNull(page.parseInteger("abc"));
    }

    @Test
    void shouldReturnNullWhenParsingBlankOrNullInteger() {
        assertNull(page.parseInteger(""));
        assertNull(page.parseInteger("   "));
        assertNull(page.parseInteger(null));
    }

    @Test
    void safeShouldReturnLowercaseStringOrEmptyWhenNull() {
        assertEquals("hello", AdminManageClassesPage.safe("HELLO"));
        assertEquals("", AdminManageClassesPage.safe(null));
    }

    @Test
    void nullToEmptyShouldReturnEmptyWhenNullOtherwiseOriginalValue() {
        assertEquals("", AdminManageClassesPage.nullToEmpty(null));
        assertEquals("text", AdminManageClassesPage.nullToEmpty("text"));
    }

    @Test
    void joinNonEmptyShouldJoinValuesCorrectly() {
        assertEquals("Spring 2025/2026", AdminManageClassesPage.joinNonEmpty("Spring", "2025/2026"));
        assertEquals("Spring", AdminManageClassesPage.joinNonEmpty("Spring", ""));
        assertEquals("2025/2026", AdminManageClassesPage.joinNonEmpty("", "2025/2026"));
        assertEquals("", AdminManageClassesPage.joinNonEmpty("", ""));
        assertEquals("", AdminManageClassesPage.joinNonEmpty(null, null));
    }

    @Test
    void shouldBuildTitleRowWithExpectedStructure() throws Exception {
        HBox titleRow = invokePrivate(page, "buildTitleRow", HBox.class);

        assertNotNull(titleRow);
        assertEquals(4, titleRow.getChildren().size());

        assertInstanceOf(VBox.class, titleRow.getChildren().getFirst());
        VBox titleColumn = (VBox) titleRow.getChildren().getFirst();
        assertEquals(2, titleColumn.getChildren().size());

        assertInstanceOf(Label.class, titleColumn.getChildren().get(0));
        assertInstanceOf(Label.class, titleColumn.getChildren().get(1));

        Button enrollButton = invokePrivate(page, "getEnrollButton", Button.class, HBox.class, titleRow);
        Button addButton = invokePrivate(page, "getAddButton", Button.class, HBox.class, titleRow);

        assertNotNull(enrollButton);
        assertNotNull(addButton);
        assertTrue(enrollButton.getStyleClass().contains("secondary-btn"));
        assertTrue(addButton.getStyleClass().contains("primary-btn"));
        assertTrue(addButton.getText().startsWith("+"));
    }

    @Test
    void shouldBuildSearchFieldWithPromptAndStyleClass() throws Exception {
        TextField searchField = invokePrivate(page, "buildSearchField", TextField.class);

        assertNotNull(searchField);
        assertNotNull(searchField.getPromptText());
        assertFalse(searchField.getPromptText().isBlank());
        assertTrue(searchField.getStyleClass().contains("search-field"));
    }

    @Test
    void shouldBuildLoadErrorLabelHiddenByDefault() throws Exception {
        Label loadError = invokePrivate(page, "buildLoadErrorLabel", Label.class);

        assertNotNull(loadError);
        assertFalse(loadError.isVisible());
        assertFalse(loadError.isManaged());
        assertTrue(loadError.getStyleClass().contains("subtitle"));
    }

    @Test
    void shouldBuildSectionTitleWithStyleClass() throws Exception {
        Label sectionTitle = invokePrivate(page, "buildSectionTitle", Label.class);

        assertNotNull(sectionTitle);
        assertNotNull(sectionTitle.getText());
        assertFalse(sectionTitle.getText().isBlank());
        assertTrue(sectionTitle.getStyleClass().contains("section-title"));
    }

    @Test
    void shouldBuildAddClassFormWithExpectedSpacingAndPadding() throws Exception {
        GridPane form = invokePrivate(page, "buildAddClassForm", GridPane.class);

        assertNotNull(form);
        assertEquals(10.0, form.getHgap());
        assertEquals(10.0, form.getVgap());
        assertEquals(10.0, form.getPadding().getTop());
        assertEquals(10.0, form.getPadding().getRight());
        assertEquals(10.0, form.getPadding().getBottom());
        assertEquals(10.0, form.getPadding().getLeft());
    }

    @Test
    void shouldBuildAddFormFieldWithPromptText() throws Exception {
        TextField field = invokePrivate(page, "buildAddFormField", TextField.class, String.class, "e.g. Mathematics");

        assertNotNull(field);
        assertEquals("e.g. Mathematics", field.getPromptText());
    }

    @Test
    void shouldDisableCreateButtonWhenRequiredFieldsAreBlank() throws Exception {
        Button createButton = new Button();
        TextField classCodeField = new TextField(" ");
        TextField nameField = new TextField("Mathematics");
        TextField teacherEmailField = new TextField("teacher@example.com");

        invokePrivateVoid(
                page,
                "updateCreateButtonState",
                new Class<?>[]{Button.class, TextField.class, TextField.class, TextField.class},
                createButton, classCodeField, nameField, teacherEmailField
        );

        assertTrue(createButton.isDisable());
    }

    @Test
    void shouldEnableCreateButtonWhenRequiredFieldsAreFilled() throws Exception {
        Button createButton = new Button();
        TextField classCodeField = new TextField("MTH101");
        TextField nameField = new TextField("Mathematics");
        TextField teacherEmailField = new TextField("teacher@example.com");

        invokePrivateVoid(
                page,
                "updateCreateButtonState",
                new Class<?>[]{Button.class, TextField.class, TextField.class, TextField.class},
                createButton, classCodeField, nameField, teacherEmailField
        );

        assertFalse(createButton.isDisable());
    }

    @Test
    void shouldBuildStudentListViewWithExpectedConfiguration() throws Exception {
        ListView<AdminStudentDto> listView = invokePrivate(page, "buildStudentListView", ListView.class);

        assertNotNull(listView);
        assertEquals(SelectionMode.MULTIPLE, listView.getSelectionModel().getSelectionMode());
        assertEquals(320.0, listView.getPrefHeight());
        assertNotNull(listView.getCellFactory());
    }

    @Test
    void shouldRenderStudentCellText() throws Exception {
        ListView<AdminStudentDto> listView = invokePrivate(page, "buildStudentListView", ListView.class);
        ListCell<AdminStudentDto> cell = listView.getCellFactory().call(listView);

        AdminStudentDto student = new AdminStudentDto();
        student.setFirstName("Oscar");
        student.setLastName("al");
        student.setEmail("oscar@example.com");
        student.setStudentCode("ST001");

        invokeUpdateItem(cell, student, false);

        assertEquals("Oscar al  |  oscar@example.com  |  ST001", cell.getText());
    }

    @Test
    void shouldRenderStudentCellWithNullFieldsAsEmptyStrings() throws Exception {
        ListView<AdminStudentDto> listView = invokePrivate(page, "buildStudentListView", ListView.class);
        ListCell<AdminStudentDto> cell = listView.getCellFactory().call(listView);

        AdminStudentDto student = new AdminStudentDto();
        student.setFirstName(null);
        student.setLastName(null);
        student.setEmail(null);
        student.setStudentCode(null);

        invokeUpdateItem(cell, student, false);

        assertEquals("  |    |  ", cell.getText());
    }

    @Test
    void shouldClearStudentCellTextWhenItemIsNullOrEmpty() throws Exception {
        ListView<AdminStudentDto> listView = invokePrivate(page, "buildStudentListView", ListView.class);
        ListCell<AdminStudentDto> cell = listView.getCellFactory().call(listView);

        invokeUpdateItem(cell, null, true);
        assertNull(cell.getText());

        AdminStudentDto student = new AdminStudentDto();
        student.setFirstName("Oscar");
        invokeUpdateItem(cell, student, false);
        assertNotNull(cell.getText());

        invokeUpdateItem(cell, null, true);
        assertNull(cell.getText());
    }

    @Test
    void shouldBuildSelectedCountLabelWithSubtitleStyle() throws Exception {
        Label label = invokePrivate(page, "buildSelectedCountLabel", Label.class);

        assertNotNull(label);
        assertNotNull(label.getText());
        assertFalse(label.getText().isBlank());
        assertTrue(label.getStyleClass().contains("subtitle"));
    }

    private static void invokeUpdateItem(ListCell<AdminStudentDto> cell, AdminStudentDto item, boolean empty) throws Exception {
        Method method = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
        method.setAccessible(true);
        method.invoke(cell, item, empty);
    }

    private static void invokePrivateVoid(
            Object target,
            String methodName,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(target, args);
    }

    private static <T> T invokePrivate(Object target, String methodName, Class<T> returnType) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        Object result = method.invoke(target);
        return returnType.cast(result);
    }

    private static <T> T invokePrivate(
            Object target,
            String methodName,
            Class<T> returnType,
            Class<?> parameterType,
            Object arg
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterType);
        method.setAccessible(true);
        Object result = method.invoke(target, arg);
        return returnType.cast(result);
    }

    @Test
    void showLabelShouldMakeLabelVisibleAndManaged() throws Exception {
        Label label = new Label();

        invokePrivateVoid(
                page,
                "showLabel",
                new Class<?>[]{Label.class, String.class},
                label,
                "Error message"
        );

        assertEquals("Error message", label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    @Test
    void hideLabelShouldHideAndUnmanageLabel() throws Exception {
        Label label = new Label("Error");
        label.setVisible(true);
        label.setManaged(true);

        invokePrivateVoid(
                page,
                "hideLabel",
                new Class<?>[]{Label.class},
                label
        );

        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
    }
}