
import frontend.UserRow;
import model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserRowTest {

    @Test
    void testConstructorAndGetters() {
        UserRow userRow = new UserRow(
                "Alice",
                "alice@school.com",
                //UserRole.STUDENT,   // Make sure this exists in your enum
                "Math 101"
        );

        assertEquals("Alice", userRow.getUser());
        //assertEquals("alice@school.com", userRow.emailProperty().get());
        //assertEquals(String.valueOf(UserRole.STUDENT), userRow.typeProperty().get());
        assertEquals("Math 101", userRow.enrolledProperty().get());
    }

    @Test
    void testPropertiesNotNull() {
        UserRow userRow = new UserRow(
                "John",
                "john@school.com",
                //UserRole.TEACHER,
                "Science"
        );

        assertNotNull(userRow.userProperty());
        //assertNotNull(userRow.emailProperty());
        assertNotNull(userRow.typeProperty());
        assertNotNull(userRow.enrolledProperty());
    }

    @Test
    void testPropertyUpdateReflectsInGetter() {
        UserRow userRow = new UserRow(
                "Sara",
                "sara@school.com",
                //UserRole.ADMIN,
                "None"
        );

        userRow.userProperty().set("Sarah");
        //userRow.emailProperty().set("sarah@school.com");
        userRow.enrolledProperty().set("History");

        assertEquals("Sarah", userRow.getUser());
        //assertEquals("sarah@school.com", userRow.emailProperty().get());
        assertEquals("History", userRow.enrolledProperty().get());
    }
}