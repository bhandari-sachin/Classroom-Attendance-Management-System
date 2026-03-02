package dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceViewTest {

    private Object getField(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    @Test
    void constructor_ShouldInitializeFields() throws Exception {
        Long studentNumber = 12345L;
        String firstName = "Jane";
        String lastName = "Doe";
        LocalDate date = LocalDate.of(2026, 3, 1);
        String status = "PRESENT";

        AttendanceView view = new AttendanceView(studentNumber, firstName, lastName, date, status);

        assertEquals(studentNumber, getField(view, "studentNumber"));
        assertEquals(firstName, getField(view, "firstName"));
        assertEquals(lastName, getField(view, "lastName"));
        assertEquals(date, getField(view, "sessionDate"));
        assertEquals(status, getField(view, "status"));
    }

    @Test
    void getStatus_ShouldReturnStatus() {
        AttendanceView view = new AttendanceView(1L, "A", "B", LocalDate.now(), "ABSENT");

        assertEquals("ABSENT", view.getStatus());
    }

    @Test
    void constructor_ShouldAllowNullValues() throws Exception {
        AttendanceView view = new AttendanceView(null, null, null, null, null);

        assertNull(getField(view, "studentNumber"));
        assertNull(getField(view, "firstName"));
        assertNull(getField(view, "lastName"));
        assertNull(getField(view, "sessionDate"));
        assertNull(getField(view, "status"));
    }
}