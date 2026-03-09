package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv()
            .getOrDefault("DB_URL",
                    "jdbc:mysql://localhost:3306/classroom_attendance?serverTimezone=UTC");

    private static final String USER = System.getenv()
            .getOrDefault("DB_USERNAME", "db_user");

    private static final String PASSWORD = System.getenv()
            .getOrDefault("DB_PASSWORD", "db_password");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}