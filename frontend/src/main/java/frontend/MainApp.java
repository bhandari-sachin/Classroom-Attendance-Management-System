package frontend;

import frontend.admin.AdminAttendanceReportsPage;
import frontend.admin.AdminDashboardApp;
import frontend.admin.AdminManageClassesPage;
import frontend.admin.AdminManageUsersPage;
import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.auth.RoleRedirect;
import frontend.i18n.FrontendI18n;
import frontend.student.StudentAttendancePage;
import frontend.student.StudentDashboardApp;
import frontend.student.StudentEmailPage;
import frontend.student.StudentMarkAttendancePage;
import frontend.teacher.TeacherDashboardApp;
import frontend.teacher.TeacherEmailPage;
import frontend.teacher.TeacherReportsPage;
import frontend.teacher.TeacherTakeAttendancePage;
import frontend.ui.LoginPage;
import frontend.ui.SignupPage;
import frontend.ui.UiPreferences;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Main JavaFX application entry point.
 *
 * <p>This class:
 * creates the scene,
 * configures routing,
 * applies saved UI preferences,
 * initializes authentication services,
 * and loads the first page.</p>
 */
public class MainApp extends Application {

    private static final double INITIAL_WIDTH = 1100;
    private static final double INITIAL_HEIGHT = 700;
    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 650;
    private static final String LOGIN_ROUTE = "login";

    @Override
    public void start(Stage stage) {
        Scene scene = createScene();
        applyStyles(scene);

        JwtStore store = new JwtStore();
        String backendUrl = getBackendUrl();
        AuthService authService = new AuthService(backendUrl);

        AppRouter router = new AppRouter(scene);

        registerPublicRoutes(router, authService, store);
        registerAdminRoutes(router, scene, store);
        registerStudentRoutes(router, scene, store);
        registerTeacherRoutes(router, scene, store);

        configureInitialRoute(router, store);

        stage.setTitle(t());
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();
    }

    /**
     * Creates the main application scene.
     */
    private Scene createScene() {
        Scene scene = new Scene(new StackPane(), INITIAL_WIDTH, INITIAL_HEIGHT);
        UiPreferences.applyTheme(scene);
        return scene;
    }

    /**
     * Applies the main application stylesheet if available.
     */
    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    /**
     * Returns the backend URL from environment, or localhost fallback.
     */
    private String getBackendUrl() {
        return System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
    }

    /**
     * Registers public routes that do not require authentication.
     */
    private void registerPublicRoutes(AppRouter router, AuthService authService, JwtStore store) {
        router.register(LOGIN_ROUTE, () -> new LoginPage(router, authService, store));
        router.register("signup", () -> new SignupPage(router, authService, store));
    }

    /**
     * Registers admin-only routes.
     */
    private void registerAdminRoutes(AppRouter router, Scene scene, JwtStore store) {
        router.register("admin-dashboard", guardedRoute(router, store, Set.of(Role.ADMIN),
                () -> new AdminDashboardApp().build(scene, router, store, requireAuth(store))));
        router.register("admin-classes", guardedRoute(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageClassesPage().build(scene, router, store, requireAuth(store))));
        router.register("admin-users", guardedRoute(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageUsersPage().build(scene, router, store, requireAuth(store))));
        router.register("admin-reports", guardedRoute(router, store, Set.of(Role.ADMIN),
                () -> new AdminAttendanceReportsPage().build(scene, router, store, requireAuth(store))));
    }

    /**
     * Registers student-only routes.
     */
    private void registerStudentRoutes(AppRouter router, Scene scene, JwtStore store) {
        router.register("student-dashboard", guardedRoute(router, store, Set.of(Role.STUDENT),
                () -> new StudentDashboardApp().build(scene, router, store, requireAuth(store))));
        router.register("student-mark", guardedRoute(router, store, Set.of(Role.STUDENT),
                () -> new StudentMarkAttendancePage().build(scene, router, store, requireAuth(store))));
        router.register("student-attendance", guardedRoute(router, store, Set.of(Role.STUDENT),
                () -> new StudentAttendancePage().build(scene, router, store, requireAuth(store))));
        router.register("student-email", guardedRoute(router, store, Set.of(Role.STUDENT),
                () -> new StudentEmailPage().build(scene, router, store, requireAuth(store))));
    }

    /**
     * Registers teacher-only routes.
     */
    private void registerTeacherRoutes(AppRouter router, Scene scene, JwtStore store) {
        router.register("teacher-dashboard", guardedRoute(router, store, Set.of(Role.TEACHER),
                () -> new TeacherDashboardApp().build(scene, router, store, requireAuth(store))));
        router.register("teacher-take", guardedRoute(router, store, Set.of(Role.TEACHER),
                () -> new TeacherTakeAttendancePage().build(scene, router, store, requireAuth(store))));
        router.register("teacher-reports", guardedRoute(router, store, Set.of(Role.TEACHER),
                () -> new TeacherReportsPage().build(scene, router, store, requireAuth(store))));
        router.register("teacher-email", guardedRoute(router, store, Set.of(Role.TEACHER),
                () -> new TeacherEmailPage().build(scene, router, store, requireAuth(store))));
    }

    /**
     * Decides which route to open first.
     *
     * <p>In normal mode:
     * - go to saved user's dashboard if logged in
     * - otherwise go to login
     * For demo/preview mode, uncomment setupDemoUser(...).</p>
     */
    private void configureInitialRoute(AppRouter router, JwtStore store) {
        AuthState state = store.load().orElse(null);
        if (state == null) {
            router.go(LOGIN_ROUTE);
            return;
        }

        router.go(RoleRedirect.routeFor(state.getRole()));
    }

    /**
     * Returns authenticated state or throws if missing.
     */
    private AuthState requireAuth(JwtStore store) {
        return store.load()
                .orElseThrow(() -> new IllegalStateException("User is not authenticated"));
    }

    /**
     * Creates a guarded route that checks authentication and role before loading a page.
     */
    private Supplier<Parent> guardedRoute(
            AppRouter router,
            JwtStore store,
            Set<Role> allowedRoles,
            Supplier<Parent> pageFactory
    ) {
        return () -> {
            AuthState state = store.load().orElse(null);

            if (state == null) {
                router.go(LOGIN_ROUTE);
                return new StackPane();
            }

            if (!allowedRoles.contains(state.getRole())) {
                router.go(RoleRedirect.routeFor(state.getRole()));
                return new StackPane();
            }

            return pageFactory.get();
        };
    }

    /**
     * Returns a translated value, or fallback if the key is missing.
     */
    private String t() {
        String value = FrontendI18n.t("common.app.title");
        return "common.app.title".equals(value) ? "Classroom Attendance Management System" : value;
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch(args);
    }
}