package frontend.auth;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AppRouter {

    private final Scene scene;
    private final Map<String, Supplier<Parent>> routes = new HashMap<>();

    public AppRouter(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public void register(String name, Supplier<Parent> viewFactory) {
        routes.put(name, viewFactory);
    }

    public void go(String name) {
        Supplier<Parent> f = routes.get(name);
        if (f == null) throw new RuntimeException("Route not found: " + name);
        scene.setRoot(f.get());
    }
}