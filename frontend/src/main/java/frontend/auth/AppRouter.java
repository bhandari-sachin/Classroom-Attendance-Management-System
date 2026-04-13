package frontend.auth;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simple navigation router for JavaFX applications.
 *
 * <p>Manages view registration and switching between views
 * by replacing the root node of the scene.</p>
 */
public class AppRouter {

    private final Scene scene;
    private final Map<String, Supplier<Parent>> routes;

    public AppRouter(Scene scene) {
        this.scene = Objects.requireNonNull(scene, "Scene must not be null");
        this.routes = new HashMap<>();
    }

    /**
     * Returns the managed JavaFX scene.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Registers a new route with a view factory.
     *
     * @param name route name (e.g., "login", "dashboard")
     * @param viewFactory supplier that creates the view when navigated to
     */
    public void register(String name, Supplier<Parent> viewFactory) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Route name must not be null or blank");
        }
        if (viewFactory == null) {
            throw new IllegalArgumentException("View factory must not be null");
        }

        routes.put(name, viewFactory);
    }

    /**
     * Navigates to a registered route by replacing the scene root.
     *
     * @param name route name
     */
    public void go(String name) {
        Supplier<Parent> factory = routes.get(name);

        if (factory == null) {
            throw new IllegalArgumentException("Route not found: " + name
                    + ". Available routes: " + routes.keySet());
        }

        Parent root = factory.get();

        if (root == null) {
            throw new IllegalStateException("View factory returned null for route: " + name);
        }

        scene.setRoot(root);
    }

}