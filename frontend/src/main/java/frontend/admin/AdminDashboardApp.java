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
        String adminName = AdminPageSupport.resolveAdminName(state, helper);
        Parent dashboardContent = AdminPages.dashboardPage(scene, router, jwtStore, state);

        return AdminPageSupport.wrapWithSidebar(
                adminName,
                helper,
                dashboardContent,
                "dashboard",
                router,
                jwtStore
        );
    }
}