package main;

import config.DatabaseInitializer;
import http.*;
import repository.UserRepository;
import security.JwtService;
import service.AttendanceService;
import service.ClassService;
import service.SessionService;
import service.UserService;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import config.UserSQL;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BackendMain {

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.init(); // ensure tables exist

        int port = 8081;

        UserRepository users = new UserRepository();
        JwtService jwtService = new JwtService(System.getenv().getOrDefault("JWT_SECRET", "SECRET"));

        // services
        ClassService classService = new ClassService(new ClassSQL());
        SessionService sessionService = new SessionService(new SessionSQL());
        AttendanceService attendanceService = new AttendanceService(new AttendanceSQL(), new SessionSQL());
        UserService userService = new UserService(new UserSQL());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/auth/login", new LoginHandler(users, jwtService));
        server.createContext("/api/auth/signup", new SignupHandler(users));
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler(jwtService, attendanceService));

        server.createContext("/api/classes", new ClassesHandler(classService, sessionService));
        server.createContext("/api/sessions", new SessionsHandler(sessionService));
        server.createContext("/api/attendance/submit-code", new AttendanceCodeHandler(attendanceService));
        server.createContext("/api/users", new UsersHandler(userService));
        server.createContext("/api/stats", new StatsHandler(attendanceService));

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("Backend running on http://localhost:" + port);
    }
}