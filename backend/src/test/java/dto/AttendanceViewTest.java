package dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceViewTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        LocalDate date = LocalDate.of(2026, 4, 14);

        AttendanceView view = new AttendanceView(
                1001L,
                "John",
                "Doe",
                date,
                "PRESENT"
        );

        assertEquals(1001L, view.getStudentNumber());
        assertEquals("John", view.getFirstName());
        assertEquals("Doe", view.getLastName());
        assertEquals(date, view.getSessionDate());
        assertEquals("PRESENT", view.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        AttendanceView view = new AttendanceView();

        LocalDate date = LocalDate.of(2026, 4, 14);

        view.setStudentNumber(2002L);
        view.setFirstName("Alice");
        view.setLastName("Smith");
        view.setSessionDate(date);
        view.setStatus("ABSENT");

        assertEquals(2002L, view.getStudentNumber());
        assertEquals("Alice", view.getFirstName());
        assertEquals("Smith", view.getLastName());
        assertEquals(date, view.getSessionDate());
        assertEquals("ABSENT", view.getStatus());
    }
}