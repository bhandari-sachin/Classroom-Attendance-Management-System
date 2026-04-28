package frontend.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRowTest {

    @Test
    void testConstructorAndGetters() {
        UserRow userRow = new UserRow(
                "Alice",
                "alice@school.com",
                //UserRole.STUDENT,   // Make sure this exists in your enum
                "Math 101"
        );

        assertEquals("Alice", userRow.getUser());
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
        userRow.enrolledProperty().set("History");

        assertEquals("Sarah", userRow.getUser());
        assertEquals("History", userRow.enrolledProperty().get());
    }
}