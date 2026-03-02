import config.AttendanceSQL;
import config.ClassSQL;
import config.DatabaseInitializer;
import http.*;
import repository.UserRepository;
import security.JwtService;

import com.sun.net.httpserver.HttpServer;
import service.AttendanceService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BackendMain {

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.init(); // ensure tables exist

        int port = 8081;

        UserRepository users = new UserRepository();
        JwtService jwtService = new JwtService(System.getenv().getOrDefault("JWT_SECRET", "SECRET"));
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL());
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/auth/login", new LoginHandler(users, jwtService));
        server.createContext("/api/auth/signup", new SignupHandler(users));
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler(jwtService));
        server.createContext("/api/admin/users", new AdminUsersHandler(users, jwtService));
        server.createContext("/api/admin/attendance/stats",
                new AdminStatsHandler(jwtService, attendanceService));
        server.createContext("/api/admin/classes", new AdminClassesHandler(jwtService, new ClassSQL()));
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("Backend running on http://localhost:" + port);
    }
}