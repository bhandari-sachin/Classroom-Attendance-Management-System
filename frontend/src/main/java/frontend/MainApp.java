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
import frontend.student.StudentAttendancePage;
import frontend.student.StudentDashboardApp;
import frontend.student.StudentEmailPage;
import frontend.student.StudentMarkAttendancePage;
import frontend.teacher.TeacherDashboardApp;
import frontend.teacher.TeacherEmailPage;
import frontend.teacher.TeacherReportsPage;
import frontend.teacher.TeacherTakeAttendancePage;
import frontend.ui.HelperClass;
import frontend.ui.LoginPage;
import frontend.ui.SignupPage;
import frontend.ui.UiPreferences;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Set;
import java.util.function.Supplier;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        HelperClass helper = new HelperClass();

        Scene scene = new Scene(new StackPane(), 1100, 700);

        var css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        UiPreferences.applyTheme(scene);

        JwtStore store = new JwtStore();
        String backendUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
        AuthService auth = new AuthService(backendUrl);

        AppRouter router = new AppRouter(scene);

        router.register("login", () -> new LoginPage(router, auth, store));
        router.register("signup", () -> new SignupPage(router, auth, store));

        router.register("admin-dashboard", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminDashboardApp().build(scene, router, store, store.load().orElseThrow())));
        router.register("admin-classes", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageClassesPage().build(scene, router, store, store.load().orElseThrow())));
        router.register("admin-users", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminManageUsersPage().build(scene, router, store, store.load().orElseThrow())));
        router.register("admin-reports", guard(router, store, Set.of(Role.ADMIN),
                () -> new AdminAttendanceReportsPage().build(scene, router, store, store.load().orElseThrow())));

        router.register("student-dashboard", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentDashboardApp().build(scene, router, store, store.load().orElseThrow())));
        router.register("student-mark", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentMarkAttendancePage().build(scene, router, store, store.load().orElseThrow())));
        router.register("student-attendance", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentAttendancePage().build(scene, router, store, store.load().orElseThrow())));
        router.register("student-email", guard(router, store, Set.of(Role.STUDENT),
                () -> new StudentEmailPage().build(scene, router, store, store.load().orElseThrow())));

        router.register("teacher-dashboard", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherDashboardApp().build(scene, router, store, store.load().orElseThrow())));
        router.register("teacher-take", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherTakeAttendancePage().build(scene, router, store, store.load().orElseThrow())));
        router.register("teacher-reports", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherReportsPage().build(scene, router, store, store.load().orElseThrow())));
        router.register("teacher-email", guard(router, store, Set.of(Role.TEACHER),
                () -> new TeacherEmailPage().build(scene, router, store, store.load().orElseThrow())));

        //Role previewRole = Role.STUDENT;
        //Role previewRole = Role.TEACHER;
         Role previewRole = Role.ADMIN;

        store.clear();
        store.save(new AuthState("demo-token", previewRole, "Demo User"));

        switch (previewRole) {
            case STUDENT -> router.go("student-dashboard");
            case TEACHER -> router.go("teacher-dashboard");
            case ADMIN -> router.go("admin-dashboard");
        }

        stage.setTitle(helper.getMessage("common.app.title"));
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }

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
                return new StackPane();
            }

            return page.get();
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}