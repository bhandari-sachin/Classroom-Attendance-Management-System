package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class AdminDashboardApp {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? "Name"
                : state.getName();

        Parent scroll = AdminPages.dashboardPage(scene, router, jwtStore, state);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                FrontendI18n.t("admin.panel"),
                FrontendI18n.t("nav.dashboard"),
                FrontendI18n.t("admin.classes"),
                FrontendI18n.t("admin.reports"),
                FrontendI18n.t("admin.users"),
                scroll,
                "dashboard",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-reports"); }
                    @Override public void goEmail() { router.go("admin-users"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}