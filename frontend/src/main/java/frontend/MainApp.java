package frontend;

import frontend.auth.*;
import frontend.ui.LoginPage;
import frontend.ui.SignupPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Set;
import java.util.function.Supplier;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(new StackPane(), 1100, 700);

        var css = getClass().getResource("/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        JwtStore store = new JwtStore();
        AuthService auth = new AuthService("http://localhost:8080"); // change
        AppRouter router = new AppRouter(scene);

        // ===== Auth routes =====
        router.register("login", () -> new LoginPage(router, auth, store));
        router.register("signup", () -> new SignupPage(router, auth, store));

        // ===== Admin routes =====
        router.register("admin-dashboard", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminDashboardApp().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("admin-classes", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageClassesPage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("admin-users", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageUsersPage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("admin-reports", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminAttendanceReportsPage().build(scene, router, store, store.load().orElseThrow())
        ));

        // ===== Student routes =====
        router.register("student-dashboard", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentDashboardApp().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("student-mark", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentMarkAttendancePage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("student-attendance", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentAttendancePage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("student-email", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentEmailPage().build(scene, router, store, store.load().orElseThrow())
        ));

        // ===== Teacher routes =====
        router.register("teacher-dashboard", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherDashboardApp().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("teacher-take", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherTakeAttendancePage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("teacher-reports", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherReportsPage().build(scene, router, store, store.load().orElseThrow())
        ));

        router.register("teacher-email", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherEmailPage().build(scene, router, store, store.load().orElseThrow())
        ));

        // Auto redirect based on stored JWT + role
        store.load().ifPresentOrElse(
                s -> router.go(RoleRedirect.routeFor(s.getRole())),
                () -> router.go("login")
        );

        stage.setTitle("Frontend");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }

    /**
     * Simple protected-route wrapper:
     * - if no JWT -> login
     * - if role mismatch -> redirect to correct dashboard
     */
    private Supplier<javafx.scene.Parent> guard(
            AppRouter router,
            JwtStore store,
            Set<Role> allowedRoles,
            Supplier<javafx.scene.Parent> page
    ) {
        return () -> {
            AuthState state = store.load().orElse(null);

            if (state == null) {
                router.go("login");
                return new StackPane();
            }

            if (!allowedRoles.contains(state.getRole())) {
                router.go(RoleRedirect.routeFor(state.getRole()));
                return new StackPane();
            }

            return page.get();
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}