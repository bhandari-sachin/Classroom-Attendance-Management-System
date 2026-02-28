

import backend.http.LoginHandler;
import backend.http.MarkAttendanceHandler;
import backend.http.SignupHandler;
import backend.security.JwtService;
import backend.user.UserRepository;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class BackendMain {

    public static void main(String[] args) throws Exception {
        int port = 8080;

        // real deps
        UserRepository users = new UserRepository();
        JwtService jwtService = new JwtService("RANDOM_SECRET");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/auth/login", new LoginHandler(users, jwtService));
        server.createContext("/api/auth/signup", new SignupHandler(users));
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler(jwtService));

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("Backend running on http://localhost:" + port);
    }
}