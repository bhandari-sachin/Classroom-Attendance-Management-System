package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.*;

public class AdminUsersHandler implements HttpHandler {

    private final UserRepository users;
    private final JwtService jwtService;
    private final ObjectMapper om = new ObjectMapper();

    public AdminUsersHandler(UserRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "ADMIN");

            int students = users.countByRole(UserRole.STUDENT);
            int teachers = users.countByRole(UserRole.TEACHER);
            int admins   = users.countByRole(UserRole.ADMIN);

            List<User> all = users.findAll();

            // Shape exactly as frontend expects:
            // {"students":..,"teachers":..,"admins":..,"users":[{"name","email","role","enrolled"}]}
            List<Map<String, Object>> list = new ArrayList<>();
            for (User u : all) {
                String name = (u.getFirstName() + " " + u.getLastName()).trim();
                list.add(Map.of(
                        "name", name,
                        "email", u.getEmail(),
                        "role", u.getUserType().name(),
                        "enrolled", "-" // add real enrolled count later if you have enrollments query
                ));
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("students", students);
            payload.put("teachers", teachers);
            payload.put("admins", admins);
            payload.put("users", list);

            HttpUtil.json(ex, 200, payload);

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, Map.of("error", sec.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}