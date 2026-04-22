package frontend.admin;

import frontend.ui.ClassRow;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminManageClassesPageTest {

    private final AdminManageClassesPage page = new AdminManageClassesPage();

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
        student1.setLastName("Wikman");
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
        student1.setLastName("Wikman");
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

        page.applyStudentFilter(filteredStudents, "oscar wikman");
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
        student.setLastName("Wikman");
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
        student.setLastName("Wikman");
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
}