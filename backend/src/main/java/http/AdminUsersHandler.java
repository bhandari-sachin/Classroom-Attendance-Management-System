package http;

import com.sun.net.httpserver.HttpExchange;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.JwtService;

import java.io.IOException;
import java.util.*;

public class AdminUsersHandler extends BaseHandler {

    private final UserRepository users;

    public AdminUsersHandler(UserRepository users, JwtService jwtService) {
        super(jwtService, "GET");
        this.users = users;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        requireAdmin(ex, ctx);

        int students = users.countByRole(UserRole.STUDENT);
        int teachers = users.countByRole(UserRole.TEACHER);
        int admins   = users.countByRole(UserRole.ADMIN);

        List<User> all = users.findAll();

        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : all) {
            String name = (u.getFirstName() + " " + u.getLastName()).trim();

            list.add(Map.of(
                    "name", name,
                    "email", u.getEmail(),
                    "role", u.getUserType().name(),
                    "enrolled", "-"
            ));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("students", students);
        payload.put("teachers", teachers);
        payload.put("admins", admins);
        payload.put("users", list);

        HttpUtil.json(ex, 200, payload);
    }
}