package dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceViewTest {

    @Test
    void constructor_initializesAllFields() {

        Long id = 42L;
        String firstName = "Bob";
        String lastName = "Smith";
        LocalDate date = LocalDate.of(2024, 12, 31);
        String status = "ABSENT";

        AttendanceView view = new AttendanceView(id, firstName, lastName, date, status);

        assertEquals(id, view.getStudentNumber());
        assertEquals(firstName, view.getFirstName());
        assertEquals(lastName, view.getLastName());
        assertEquals(date, view.getSessionDate());
        assertEquals(status, view.getStatus());
    }

    @Test
    void setters_updateFieldsCorrectly() {

        AttendanceView view = new AttendanceView();

        view.setStudentNumber(1L);
        view.setFirstName("Alice");
        view.setLastName("Brown");
        view.setSessionDate(LocalDate.of(2025, 1, 2));
        view.setStatus("PRESENT");

        assertEquals(1L, view.getStudentNumber());
        assertEquals("Alice", view.getFirstName());
        assertEquals("Brown", view.getLastName());
        assertEquals(LocalDate.of(2025, 1, 2), view.getSessionDate());
        assertEquals("PRESENT", view.getStatus());
    }

    @Test
    void defaultConstructor_allowsEmptyObject() {

        AttendanceView view = new AttendanceView();

        assertNull(view.getStudentNumber());
        assertNull(view.getFirstName());
        assertNull(view.getLastName());
        assertNull(view.getSessionDate());
        assertNull(view.getStatus());
    }

    @Test
    void status_canBeUpdatedIndependently() {

        AttendanceView view = new AttendanceView();
        view.setStatus("PRESENT");

        assertEquals("PRESENT", view.getStatus());

        view.setStatus("ABSENT");
        assertEquals("ABSENT", view.getStatus());
    }
}