package config;

import exception.ConfigurationException;

import java.util.Properties;
import java.io.InputStream;

public class ConfigLoader {
    private ConfigLoader () {
        // Prevent instantiation
    }
    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            props.load(input);

        } catch (Exception e) {
            throw new ConfigurationException("application.properties not found in resources");
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
