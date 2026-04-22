package frontend.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentRowTest {

    @Test
    void constructor_setsInitialValues() {
        StudentRow row = new StudentRow(1L, "Alice One", "alice@test.com", "PRESENT");

        assertEquals(1L, row.getStudentId());
        assertEquals("Alice One", row.getStudentName());
        assertEquals("alice@test.com", row.getEmail());
        assertEquals("PRESENT", row.getStatus());
        assertEquals("", row.getExcuseReason()); // default empty string
    }

    @Test
    void setStatus_updatesStatusProperty() {
        StudentRow row = new StudentRow(2L, "Bob Two", "bob@test.com", "ABSENT");

        row.setStatus("LATE");

        assertEquals("LATE", row.getStatus());
        assertEquals("LATE", row.statusProperty().get());
    }

    @Test
    void setExcuseReason_updatesExcuseReason() {
        StudentRow row = new StudentRow(3L, "Charlie Three", "charlie@test.com", "ABSENT");

        row.setExcuseReason("Medical appointment");

        assertEquals("Medical appointment", row.getExcuseReason());
    }

    @Test
    void properties_returnCorrectJavaFXProperties() {
        StudentRow row = new StudentRow(4L, "Dana Four", "dana@test.com", "PRESENT");

        assertEquals("Dana Four", row.studentNameProperty().get());
        assertEquals("dana@test.com", row.emailProperty().get());
        assertEquals("PRESENT", row.statusProperty().get());
    }
}
