package frontend.ui;

import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportRowTest {

    @Test
    void constructorShouldInitializeAllProperties() {
        ReportRow row = new ReportRow("Oscar", "2026-04-22", "PRESENT");

        assertEquals("Oscar", row.studentProperty().get());
        assertEquals("2026-04-22", row.dateProperty().get());
        assertEquals("PRESENT", row.statusProperty().get());
    }

    @Test
    void propertiesShouldNotBeNull() {
        ReportRow row = new ReportRow("Oscar", "2026-04-22", "ABSENT");

        assertNotNull(row.studentProperty());
        assertNotNull(row.dateProperty());
        assertNotNull(row.statusProperty());
    }

    @Test
    void updatingPropertiesShouldChangeStoredValues() {
        ReportRow row = new ReportRow("Oscar", "2026-04-22", "PRESENT");

        row.studentProperty().set("Farah");
        row.dateProperty().set("2026-04-23");
        row.statusProperty().set("EXCUSED");

        assertEquals("Farah", row.studentProperty().get());
        assertEquals("2026-04-23", row.dateProperty().get());
        assertEquals("EXCUSED", row.statusProperty().get());
    }

    @Test
    void propertyMethodsShouldReturnSameReferenceEachTime() {
        ReportRow row = new ReportRow("Oscar", "2026-04-22", "PRESENT");

        StringProperty student1 = row.studentProperty();
        StringProperty student2 = row.studentProperty();

        StringProperty date1 = row.dateProperty();
        StringProperty date2 = row.dateProperty();

        StringProperty status1 = row.statusProperty();
        StringProperty status2 = row.statusProperty();

        assertSame(student1, student2);
        assertSame(date1, date2);
        assertSame(status1, status2);
    }
}