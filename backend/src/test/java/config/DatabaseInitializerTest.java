package config;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class DatabaseInitializerTest {

    @Test
    void init_shouldExecuteSqlStatements() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);

        try (MockedStatic<DatabaseConnection> mockedDb =
                     mockStatic(DatabaseConnection.class)) {

            mockedDb.when(DatabaseConnection::getConnection)
                    .thenReturn(mockConnection);

            DatabaseInitializer.init();

            // verify SQL commands executed (2 from test file)
            verify(mockStatement, atLeastOnce()).execute(anyString());
            verify(mockConnection).createStatement();
            verify(mockStatement).close();
            verify(mockConnection).close();
        }
    }

    @Test
    void init_shouldHandleSqlExceptionGracefully() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new java.sql.SQLException("fail"))
                .when(mockStatement).execute(anyString());

        try (MockedStatic<DatabaseConnection> mockedDb =
                     mockStatic(DatabaseConnection.class)) {

            mockedDb.when(DatabaseConnection::getConnection)
                    .thenReturn(mockConnection);

            // should NOT throw (exception is caught internally)
            DatabaseInitializer.init();
            assertNotNull(mockStatement); // just to use the variable and avoid unused warning
        }
    }

    @Test
    void init_shouldHandleMissingFileGracefully() {
        ClassLoader original = Thread.currentThread().getContextClassLoader();

        ClassLoader fakeLoader = new ClassLoader(original) {
            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                return null; // simulate missing file
            }
        };

        try {
            Thread.currentThread().setContextClassLoader(fakeLoader);

            DatabaseInitializer.init();

            assertNotNull(original);

        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void privateConstructor_shouldBeCovered() throws Exception {
        Constructor<DatabaseInitializer> constructor =
                DatabaseInitializer.class.getDeclaredConstructor();

        constructor.setAccessible(true);
        DatabaseInitializer instance = constructor.newInstance();

        assertNotNull(instance);
    }
}