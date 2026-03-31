package frontend.auth;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AppRouter {

    private final Scene scene;
    private final Map<String, Supplier<Parent>> routes = new HashMap<>();
    private String currentRoute;

    public AppRouter(Scene scene) {
        this.scene = scene;
    }

    public void register(String route, Supplier<Parent> pageFactory) {
        routes.put(route, pageFactory);
    }

    public void go(String route) {
        Supplier<Parent> pageFactory = routes.get(route);

        if (pageFactory == null) {
            throw new IllegalArgumentException("Route not found: " + route);
        }

        currentRoute = route;
        scene.setRoot(pageFactory.get());
    }

    public void refresh() {
        if (currentRoute != null) {
            go(currentRoute);
        }
    }

    public String getCurrentRoute() {
        return currentRoute;
    }
}