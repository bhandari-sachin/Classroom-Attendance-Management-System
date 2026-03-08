import com.sun.net.httpserver.HttpServer;
import config.AttendanceSQL;
import config.DatabaseInitializer;
import config.SessionSQL;
import http.*;
import repository.UserRepository;
import security.JwtService;
import service.AttendanceService;
import service.SessionService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BackendMain {

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.init();

        int port = 8081;

        UserRepository users = new UserRepository();
        JwtService jwtService = new JwtService(
                System.getenv().getOrDefault("JWT_SECRET", "SECRET")
        );

        AttendanceSQL attendanceSQL = new AttendanceSQL();
        AttendanceService attendanceService = new AttendanceService(attendanceSQL);

        ClassSQL classSQL = new ClassSQL();
        SessionSQL sessionSQL = new SessionSQL();
        SessionService sessionService = new SessionService(sessionSQL);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ===== AUTH =====
        server.createContext("/api/auth/login", new LoginHandler(users, jwtService));
        server.createContext("/api/auth/signup", new SignupHandler(users));

        // ===== STUDENT =====
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler(jwtService, attendanceService));
        server.createContext("/api/student/attendance/summary", new StudentAttendanceSummaryHandler(jwtService, attendanceService));
        server.createContext("/api/student/attendance/records", new StudentAttendanceRecordsHandler(jwtService, attendanceService));
        server.createContext("/api/student/teachers", new StudentTeachersHandler(jwtService, users));

        // ===== ADMIN =====
        server.createContext("/api/admin/users", new AdminUsersHandler(users, jwtService));
        server.createContext("/api/admin/attendance/stats", new AdminStatsHandler(jwtService, attendanceService));
        server.createContext("/api/admin/classes", new AdminClassesHandler(jwtService, classSQL));
        server.createContext("/api/admin/attendance/report",
                new AdminAttendanceReportsHandler(jwtService, attendanceSQL));

        // ===== TEACHER =====
        server.createContext("/api/teacher/classes", new TeacherClassesHandler(jwtService, classSQL));
        server.createContext("/api/teacher/students", new TeacherStudentsHandler(jwtService, classSQL));
        server.createContext("/api/teacher/sessions", new TeacherSessionsHandler(jwtService, classSQL, sessionSQL));
        server.createContext("/api/teacher/reports/session", new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL));
        server.createContext("/api/teacher/dashboard/stats", new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL));
        server.createContext("/api/teacher/attendance/mark",
                new TeacherMarkAttendanceHandler(jwtService, attendanceService));

        // ===== QR / ATTENDANCE CODE =====
        server.createContext("/api/teacher/session/start", new StartSessionHandler(jwtService, sessionService));

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("Backend running on http://localhost:" + port);
    }
}