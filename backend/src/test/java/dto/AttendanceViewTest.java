package dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class AttendanceViewTest {

    @Test
    void getStatus_returnsProvidedStatus() {
        AttendanceView view = new AttendanceView(1L, "Alice", LocalDate.of(2025, 1, 2), "PRESENT");
        Assertions.assertEquals("PRESENT", view.getStatus());
    }

    @Test
    void constructor_initializesAllFields() throws Exception {
        Long expectedId = 42L;
        String expectedName = "Bob Smith";
        LocalDate expectedDate = LocalDate.of(2024, 12, 31);
        String expectedStatus = "ABSENT";

        AttendanceView view = new AttendanceView(expectedId, expectedName, expectedDate, expectedStatus);

        // verify private fields via reflection because there are no getters for them
        Field studentIdField = AttendanceView.class.getDeclaredField("studentId");
        studentIdField.setAccessible(true);
        Object studentIdValue = studentIdField.get(view);
        Assertions.assertEquals(expectedId, studentIdValue);

        Field nameField = AttendanceView.class.getDeclaredField("name");
        nameField.setAccessible(true);
        Object nameValue = nameField.get(view);
        Assertions.assertEquals(expectedName, nameValue);

        Field dateField = AttendanceView.class.getDeclaredField("sessionDate");
        dateField.setAccessible(true);
        Object dateValue = dateField.get(view);
        Assertions.assertEquals(expectedDate, dateValue);

        // status should be accessible via getter as a sanity check
        Assertions.assertEquals(expectedStatus, view.getStatus());
    }
}

