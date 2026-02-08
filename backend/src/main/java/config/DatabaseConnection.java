package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn==null) {
            try {
                conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/classroom_attendance?user=db_user&password=db_password");
            } catch (SQLException e) {
                System.out.println("Connection failed.");
                e.printStackTrace();
            }
            return conn;
        }
        else {
            return conn;
        }
    }
    public static void terminate() {
        try {
            getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

