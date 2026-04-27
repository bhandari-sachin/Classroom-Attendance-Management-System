package config;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseConnectionTest {

    @Test
    void getConnection_shouldReturnConnection() throws Exception {
        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Connection result = DatabaseConnection.getConnection();

            assertNotNull(result);
            assertEquals(mockConnection, result);
            result.close();
        }
    }

    @Test
    void getConnection_shouldThrowSQLException() {
        SQLException exception = new SQLException("DB error");

        try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(exception);

            assertThrows(SQLException.class, DatabaseConnection::getConnection);
            mocked.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()));
        }
    }

    @Test
    void privateConstructor_shouldBeCovered() throws Exception {
        Constructor<DatabaseConnection> constructor =
                DatabaseConnection.class.getDeclaredConstructor();

        constructor.setAccessible(true);
        DatabaseConnection instance = constructor.newInstance();

        assertNotNull(instance);
    }
}