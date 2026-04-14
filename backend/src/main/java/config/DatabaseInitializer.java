package config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {
    }

    public static void init() {
        try {
            InputStream inputStream = DatabaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("classroom_attendance_schema.sql");

            if (inputStream == null) {
                throw new IllegalStateException("file not found in resources");
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

            LOGGER.info("Database schema loaded successfully");

        } catch (IOException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
        }
    }
}
