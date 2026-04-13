package frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Custom ResourceBundle control that reads .properties files using UTF-8.
 *
 * <p>By default, Java uses ISO-8859-1 for properties files.
 * This class ensures proper UTF-8 support (Arabic, Amharic, etc.).</p>
 */
public class UTF8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IOException {

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        InputStream stream = getInputStream(resourceName, loader, reload);
        if (stream == null) {
            return null;
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }

    /**
     * Loads the resource stream, handling reload mode correctly.
     */
    private InputStream getInputStream(String resourceName, ClassLoader loader, boolean reload) throws IOException {
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url == null) return null;

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } else {
            return loader.getResourceAsStream(resourceName);
        }
    }
}