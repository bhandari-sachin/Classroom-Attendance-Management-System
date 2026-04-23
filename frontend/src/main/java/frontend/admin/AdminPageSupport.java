package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public final class AdminPageSupport {

    private AdminPageSupport() {
        // Utility class
    }

    public static String resolveAdminName(AuthState state, HelperClass helper) {
        return (state.name() == null || state.name().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.name();
    }

    public static VBox buildContentContainer() {
        VBox content = new VBox(14);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));
        return content;
    }

    public static Parent wrapWithSidebar(
            String adminName,
            HelperClass helper,
            Node content,
            String activeKey,
            AppRouter router,
            JwtStore jwtStore
    ) {
        return AdminAppLayout.wrapWithSidebar(
                adminName,
                helper.getMessage("admin.sidebar.title"),
                helper.getMessage("admin.dashboard.title"),
                helper.getMessage("admin.classes.title"),
                helper.getMessage("admin.users.title"),
                helper.getMessage("admin.reports.title"),
                content,
                activeKey,
                new AdminNavigator(router, jwtStore)
        );
    }

    private static final class AdminNavigator implements AdminAppLayout.Navigator {

        private final AppRouter router;
        private final JwtStore jwtStore;

        private AdminNavigator(AppRouter router, JwtStore jwtStore) {
            this.router = router;
            this.jwtStore = jwtStore;
        }

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
}