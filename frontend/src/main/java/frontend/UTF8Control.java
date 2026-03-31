package frontend.ui;

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

public class UTF8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IllegalAccessException, InstantiationException, IOException {

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        InputStream stream = null;

        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream == null) {
            return null;
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
}