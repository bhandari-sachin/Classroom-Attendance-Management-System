package dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.time.LocalDate;

class AttendanceViewTest {

    @Test
    void getStatus_returnsProvidedStatus() {
        AttendanceView view = new AttendanceView(1L, "Alice", "Brown", LocalDate.of(2025, 1, 2), "PRESENT");
        Assertions.assertEquals("PRESENT", view.getStatus());
    }

    @Test
    void constructor_initializesAllFields() throws Exception {
        Long expectedId = 42L;
        String expectedFirstName = "Bob";
        String expectedLastName = "Smith";
        LocalDate expectedDate = LocalDate.of(2024, 12, 31);
        String expectedStatus = "ABSENT";

        AttendanceView view = new AttendanceView(expectedId, expectedFirstName, expectedLastName, expectedDate, expectedStatus);

        // verify private fields via reflection because there are no getters for them
        Field studentIdField = AttendanceView.class.getDeclaredField("studentNumber");
        studentIdField.setAccessible(true);
        Object studentIdValue = studentIdField.get(view);
        Assertions.assertEquals(expectedId, studentIdValue);

        Field firstNameField = AttendanceView.class.getDeclaredField("firstName");
        firstNameField.setAccessible(true);
        Object firstNameValue = firstNameField.get(view);
        Assertions.assertEquals(expectedFirstName, firstNameValue);

        Field lastNameField = AttendanceView.class.getDeclaredField("firstName");
        lastNameField.setAccessible(true);
        Object lastNameValue = lastNameField.get(view);
        Assertions.assertEquals(expectedFirstName, lastNameValue);

        Field dateField = AttendanceView.class.getDeclaredField("sessionDate");
        dateField.setAccessible(true);
        Object dateValue = dateField.get(view);
        Assertions.assertEquals(expectedDate, dateValue);

        // status should be accessible via getter as a sanity check
        Assertions.assertEquals(expectedStatus, view.getStatus());
    }
}