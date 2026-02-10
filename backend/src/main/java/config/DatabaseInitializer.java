package config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void init() {
        try {
            InputStream inputStream = DatabaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("classroom_attendance_schema.sql");

            if (inputStream == null) {
                throw new RuntimeException("file not found in resources");
            }

            String sql;
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(inputStream))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                for (String command : sql.split(";")) {
                    if (!command.trim().isEmpty()) {
                        stmt.execute(command);
                    }
                }
            }

            System.out.println("Database schema loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
