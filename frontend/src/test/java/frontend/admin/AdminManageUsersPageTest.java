package frontend.admin;

import frontend.ui.UserRow;
import frontend.dto.AdminUserDto;
import frontend.dto.AdminUsersResponseDto;
import frontend.ui.HelperClass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminManageUsersPageTest {
    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    private final AdminManageUsersPage page = new AdminManageUsersPage();

    @Test
    void shouldShowAllRowsWhenSearchIsBlankAndTypeIsAll() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Anna\nanna@example.com", "Teacher", "Class B");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        TextField searchField = new TextField("");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("All Types");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(2, filteredRows.size());
        assertTrue(filteredRows.contains(row1));
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldFilterRowsBySearchText() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Anna\nanna@example.com", "Teacher", "Class B");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        TextField searchField = new TextField("oscar");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("All Types");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row1));
    }

    @Test
    void shouldFilterRowsByTypeStudent() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Anna\nanna@example.com", "Teacher", "Class B");
        UserRow row3 = new UserRow("Mark\nmark@example.com", "Admin", "N/A");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2, row3),
                row -> true
        );

        TextField searchField = new TextField("");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("Student");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row1));
    }

    @Test
    void shouldFilterRowsByTypeTeacher() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Anna\nanna@example.com", "Teacher", "Class B");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        TextField searchField = new TextField("");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("Teacher");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldFilterRowsByTypeAdmin() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Admin User\nadmin@example.com", "Admin", "N/A");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        TextField searchField = new TextField("");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("Admin");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldCombineSearchAndTypeFilter() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");
        UserRow row2 = new UserRow("Oscar Teacher\nteacher@example.com", "Teacher", "Class B");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, row2),
                row -> true
        );

        TextField searchField = new TextField("oscar");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("Teacher");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row2));
    }

    @Test
    void shouldExcludeNullRowsInFilter() {
        UserRow row1 = new UserRow("Oscar\noscar@example.com", "Student", "Class A");

        FilteredList<UserRow> filteredRows = new FilteredList<>(
                FXCollections.observableArrayList(row1, null),
                row -> true
        );

        TextField searchField = new TextField("oscar");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.setValue("All Types");

        page.applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                "Student",
                "Teacher",
                "Admin"
        );

        assertEquals(1, filteredRows.size());
        assertTrue(filteredRows.contains(row1));
    }

    @Test
    void shouldMapUserRowsCorrectly() {
        AdminUserDto user1 = new AdminUserDto();
        user1.setName("Oscar");
        user1.setEmail("oscar@example.com");
        user1.setRole("STUDENT");
        user1.setEnrolled("2 classes");

        AdminUserDto user2 = new AdminUserDto();
        user2.setName("Anna");
        user2.setEmail("anna@example.com");
        user2.setRole("TEACHER");
        user2.setEnrolled("5 classes");

        AdminUsersResponseDto data = new AdminUsersResponseDto();
        data.setUsers(List.of(user1, user2));

        ObservableList<UserRow> result = page.mapUserRows(data);

        assertEquals(2, result.size());
        assertEquals("Oscar\noscar@example.com", result.get(0).getUser());
        assertEquals("2 classes", result.get(0).getEnrolled());

        assertEquals("Anna\nanna@example.com", result.get(1).getUser());
        assertEquals("5 classes", result.get(1).getEnrolled());
    }

    @Test
    void shouldReturnEmptyListWhenUsersAreNull() {
        AdminUsersResponseDto data = new AdminUsersResponseDto();
        data.setUsers(null);

        ObservableList<UserRow> result = page.mapUserRows(data);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMapNullNameAndEmailToEmptyStrings() {
        AdminUserDto user = new AdminUserDto();
        user.setName(null);
        user.setEmail(null);
        user.setRole("ADMIN");
        user.setEnrolled(null);

        AdminUsersResponseDto data = new AdminUsersResponseDto();
        data.setUsers(List.of(user));

        ObservableList<UserRow> result = page.mapUserRows(data);

        assertEquals(1, result.size());
        assertEquals("\n", result.getFirst().getUser());
    }

    @ParameterizedTest
    @CsvSource({
            "STUDENT, admin.users.filter.student",
            "TEACHER, admin.users.filter.teacher",
            "ADMIN, admin.users.filter.admin"
    })
    void shouldLocalizeRolesCorrectly(String role, String key) {
        HelperClass helper = new HelperClass();

        String expected = helper.getMessage(key);
        String actual = page.localizeRole(role);

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnOriginalRoleWhenUnknown() {
        assertEquals("MANAGER", page.localizeRole("MANAGER"));
    }

    @Test
    void shouldReturnEmptyStringWhenRoleIsNull() {
        assertEquals("", page.localizeRole(null));
    }

    @Test
    void shouldReturnOriginalEnrolledWhenPresent() {
        assertEquals("3 classes", page.localizeEnrolled("3 classes"));
    }

    @Test
    void shouldReturnNoDataWhenEnrolledIsNull() {
        String result = page.localizeEnrolled(null);
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void shouldReturnNoDataWhenEnrolledIsBlank() {
        String result = page.localizeEnrolled("   ");
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void safeShouldReturnLowercaseOrEmptyString() {
        assertEquals("student", AdminManageUsersPage.safe("STUDENT"));
        assertEquals("", AdminManageUsersPage.safe(null));
    }

    @Test
    void nullToEmptyShouldReturnOriginalOrEmptyString() {
        assertEquals("hello", AdminManageUsersPage.nullToEmpty("hello"));
        assertEquals("", AdminManageUsersPage.nullToEmpty(null));
    }

    @Test
    void buildTitleShouldReturnStyledLabel() throws Exception {
        Label title = invokePrivate("buildTitle", Label.class);

        assertNotNull(title);
        assertNotNull(title.getText());
        assertTrue(title.getStyleClass().contains("title"));
    }

    @Test
    void buildSubtitleShouldReturnStyledLabel() throws Exception {
        Label subtitle = invokePrivate("buildSubtitle", Label.class);

        assertNotNull(subtitle);
        assertNotNull(subtitle.getText());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildSummaryRowShouldHaveCorrectStyleAndSpacing() throws Exception {
        HBox summary = invokePrivate("buildSummaryRow", HBox.class);

        assertNotNull(summary);
        assertEquals(12.0, summary.getSpacing());
        assertTrue(summary.getStyleClass().contains("summary-row"));
    }
    @Test
    void buildSearchFieldShouldHavePromptAndStyle() throws Exception {
        TextField search = invokePrivate("buildSearchField", TextField.class);

        assertNotNull(search);
        assertNotNull(search.getPromptText());
        assertTrue(search.getStyleClass().contains("search-field"));
    }

    @Test
    void buildTypeFilterShouldContainAllOptions() throws Exception {
        ComboBox<String> combo = invokePrivate("buildTypeFilter", ComboBox.class);

        assertNotNull(combo);
        assertEquals(4, combo.getItems().size());
        assertNotNull(combo.getValue());
        assertTrue(combo.getStyleClass().contains("filter-combo"));
    }

    @Test
    void buildFiltersRowShouldContainSearchAndCombo() throws Exception {
        TextField search = new TextField();
        ComboBox<String> combo = new ComboBox<>();

        HBox filters = invokePrivate(
                "buildFiltersRow",
                HBox.class,
                new Class<?>[]{TextField.class, ComboBox.class},
                search,
                combo
        );

        assertEquals(2, filters.getChildren().size());
        assertTrue(filters.getChildren().contains(search));
        assertTrue(filters.getChildren().contains(combo));
    }
    @Test
    void buildLoadErrorLabelShouldBeHiddenByDefault() throws Exception {
        Label label = invokePrivate("buildLoadErrorLabel", Label.class);

        assertNotNull(label);
        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
        assertTrue(label.getStyleClass().contains("subtitle"));
    }
    @Test
    void showLabelShouldDisplayMessage() throws Exception {
        Label label = new Label();

        invokePrivateVoid("showLabel", new Class[]{Label.class, String.class}, label, "Error");

        assertEquals("Error", label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    @Test
    void hideLabelShouldHideLabel() throws Exception {
        Label label = new Label();
        label.setVisible(true);
        label.setManaged(true);

        invokePrivateVoid("hideLabel", new Class[]{Label.class}, label);

        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
    }
    @Test
    void updateSummaryCardsShouldCreateThreeCards() throws Exception {
        HBox summaryRow = new HBox();

        AdminUsersResponseDto dto = new AdminUsersResponseDto();
        dto.setStudents(10);
        dto.setTeachers(5);
        dto.setAdmins(2);

        invokePrivateVoid(
                "updateSummaryCards",
                new Class[]{HBox.class, AdminUsersResponseDto.class},
                summaryRow,
                dto
        );

        assertEquals(3, summaryRow.getChildren().size());
    }
    private <T> T invokePrivate(String methodName, Class<T> returnType) throws Exception {
        Method m = page.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        return returnType.cast(m.invoke(page));
    }

    private <T> T invokePrivate(
            String methodName,
            Class<T> returnType,
            Class<?>[] paramTypes,
            Object... args
    ) throws Exception {
        Method m = page.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return returnType.cast(m.invoke(page, args));
    }

    private void invokePrivateVoid(
            String methodName,
            Class<?>[] paramTypes,
            Object... args
    ) throws Exception {
        Method m = page.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        m.invoke(page, args);
    }
}