package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import model.UserRole;
import service.UserService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    private final UserService userService;
    private final ObjectMapper om = new ObjectMapper();

    public UsersHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            URI uri = ex.getRequestURI();
            String path = uri.getPath();
            String[] parts = path.split("/");

            if ("GET".equalsIgnoreCase(method)) {
                // /api/users
                if (parts.length == 3) {
                    String query = uri.getQuery();
                    UserRole role = null;
                    if (query != null && query.startsWith("role=")) {
                        String rv = query.substring("role=".length()).toUpperCase();
                        try { role = UserRole.valueOf(rv); } catch (IllegalArgumentException ignored) {}
                    }
                    List<User> users = role == null ? userService.getAllUsers() : userService.filterByRole(role, null);
                    HttpUtil.json(ex, 200, users);
                    return;
                }

                // /api/users/{id}/enrolled-count
                if (parts.length >= 5) {
                    Long userId = Long.parseLong(parts[3]);
                    String sub = parts[4];
                    if ("enrolled-count".equals(sub)) {
                        int count = userService.getEnrolledClasses(userId);
                        HttpUtil.json(ex, 200, Map.of("count", count));
                        return;
                    }
                }
            }

            HttpUtil.json(ex, 404, Map.of("error", "Not found"));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", e.getMessage()));
        }
    }
}

