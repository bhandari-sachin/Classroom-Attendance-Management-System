package frontend.app;

import frontend.admin.AdminAttendanceReportsPage;
import frontend.admin.AdminDashboardApp;
import frontend.admin.AdminManageClassesPage;
import frontend.admin.AdminManageUsersPage;
import frontend.api.ApiException;
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

    private static final String LOGIN_PAGE = "login";

    @Override
    public void start(Stage stage) {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf"), 12
        );
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fonts/NotoSansEthiopic-Regular.ttf"), 12
        );
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fonts/NotoNaskhArabic-Regular.ttf"), 12
        );
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fonts/NotoSansDevanagari-VariableFont_wdth,wght.ttf"), 12
        );
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

        router.register(LOGIN_PAGE, () -> new LoginPage(router, auth, store));
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

        AuthState state = store.load().orElse(null);

        if (state == null) {
            router.go(LOGIN_PAGE);
        } else {
            switch (state.role()) {
                case STUDENT -> router.go("student-dashboard");
                case TEACHER -> router.go("teacher-dashboard");
                case ADMIN -> router.go("admin-dashboard");
            }
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

            if (state == null || state.token() == null || state.token().isBlank()) {
                router.go(LOGIN_PAGE);
                return new StackPane();
            }

            if (!allowedRoles.contains(state.role())) {
                return new StackPane();
            }

            try {
                return page.get();
            } catch (ApiException e) {
                handleApiException(e, store, router);
                return new StackPane();
            }
        };
    }

    private void handleApiException(ApiException e, JwtStore store, AppRouter router) {
        String msg = e.getMessage();

        if (msg != null && msg.contains("Token has expired")) {

            store.clear();

            javafx.application.Platform.runLater(() ->
                router.go(LOGIN_PAGE)
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}