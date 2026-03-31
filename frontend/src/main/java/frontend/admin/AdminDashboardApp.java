package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.scene.Parent;
import javafx.scene.Scene;
import util.I18n;

public class AdminDashboardApp {

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {

        String adminName = (state.getName() == null || state.getName().isBlank())
                ? I18n.t("student.name.placeholder")
                : state.getName();

        Parent scroll = AdminPages.dashboardPage(scene, router, jwtStore, state);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                I18n.t("admin.dashboard.title"),
                I18n.t("student.nav.dashboard"),
                I18n.t("admin.classes.title"),
                I18n.t("admin.users.title"),
                I18n.t("admin.reports.title"),
                scroll,
                "dashboard",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { router.go("admin-dashboard"); }
                    @Override public void goTakeAttendance() { router.go("admin-classes"); }
                    @Override public void goReports() { router.go("admin-users"); }
                    @Override public void goEmail() { router.go("admin-reports"); }
                    @Override public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                },
                router
        );
    }
}