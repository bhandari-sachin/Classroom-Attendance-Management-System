import config.DatabaseInitializer;
import http.LoginHandler;
import http.MarkAttendanceHandler;
import http.SignupHandler;
import repository.UserRepository;
import security.JwtService;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BackendMain {

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.init(); // ensure tables exist

        int port = 8080;

        UserRepository users = new UserRepository();
        JwtService jwtService = new JwtService(System.getenv().getOrDefault("JWT_SECRET", "SECRET"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/auth/login", new LoginHandler(users, jwtService));
        server.createContext("/api/auth/signup", new SignupHandler(users));
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler(jwtService));

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("Backend running on http://localhost:" + port);
    }
}