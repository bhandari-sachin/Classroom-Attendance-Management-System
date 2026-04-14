package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class AdminDashboardApp {

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String adminName = resolveAdminName(state);
        Parent dashboardContent = AdminPages.dashboardPage(scene, router, jwtStore, state);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                helper.getMessage("admin.sidebar.title"),
                helper.getMessage("admin.dashboard.title"),
                helper.getMessage("admin.classes.title"),
                helper.getMessage("admin.users.title"),
                helper.getMessage("admin.reports.title"),
                dashboardContent,
                "dashboard",
                new AdminAppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("admin-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("admin-classes");
                    }

                    @Override
                    public void goReports() {
                        router.go("admin-users");
                    }

                    @Override
                    public void goEmail() {
                        router.go("admin-reports");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }

    private String resolveAdminName(AuthState state) {
        return (state.getName() == null || state.getName().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.getName();
    }
}